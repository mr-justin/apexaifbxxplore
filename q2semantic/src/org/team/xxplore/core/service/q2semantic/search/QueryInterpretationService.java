package org.team.xxplore.core.service.q2semantic.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import org.jgrapht.graph.Pseudograph;
import org.team.xxplore.core.service.impl.NamedConcept;
import org.team.xxplore.core.service.impl.Property;
import org.team.xxplore.core.service.mapping.Mapping;
import org.team.xxplore.core.service.mapping.MappingIndexSearcher;
import org.team.xxplore.core.service.q2semantic.Cursor;
import org.team.xxplore.core.service.q2semantic.SummaryGraphEdge;
import org.team.xxplore.core.service.q2semantic.SummaryGraphElement;
import org.team.xxplore.core.service.q2semantic.SummaryGraphUtil;

/**
 * Implement the topk algorithm.
 * @author jqchen
 *
 */
public class QueryInterpretationService {

	private Parameters param;
	public static final String ConceptMark = "c", PredicateMark = "p";

	public MappingIndexSearcher mis;
	public Graph4TopKFactory factory;
	
	public QueryInterpretationService() {
		param = Parameters.getParameters();
		mis = new MappingIndexSearcher(param.mappingIndexRoot);
		factory = new Graph4TopKFactory(mis);
	}
	
	/**
	 * TopK will modified some data structures of SummaryPart. This method is used to remove the modification.
	 */
	private void refreshFactory() {
		for(SummaryPart sg : factory.summaryGraph_HM.values()) { 
			for(SummaryGraphElement ele : sg.summaryGraph.vertexSet()) {
				ele.setCursors(null);
				ele.m_exploredCursorCombinations = null;
				ele.m_newCursorCombinations = null;			
			}
		}
	}
	
	/**
	 * Get the subgraph from keywords.
	 * @param elements - keyword search result
	 * @param distance - the max length of topk graph path
	 * @param k - top k.
	 * @return
	 */
	public LinkedList<Subgraph> computeQueries(
			Map<String, Collection<SummaryGraphElement>> elements,
			int distance, int k) {

		if (elements == null) {
			return null;
		}
		
		this.refreshFactory();
		Graph4TopK iGraph = factory.createGraphAdapter(elements);

		Collection<Subgraph> subgraphs = getTopKSubgraphs(iGraph,elements, distance, k);
		
		int count = 0;
		
		for (Subgraph g : subgraphs) {
			System.out.println("========= Top" + (++count) + "==========");
			System.out.println(g.toString());
		}


		if ((subgraphs == null) || (subgraphs.size() == 0)) {
			return null;
		}
		LinkedList<Subgraph> results = new LinkedList<Subgraph>();
		for (int i = 0; i < k && i < subgraphs.size(); i++) {
			results.add(((List<Subgraph>) subgraphs).get(i));
		}
		
		return results;
	}


	public Set<String> getSuggestion(List<String> concept, String ds, MappingIndexSearcher index) throws Exception {
		HashMap<String, String> mappedConcept = new HashMap<String, String>();
		Collection<Mapping> mapping;
		for (String con : concept) {
			mapping = index.searchMappings(con, ds,
					MappingIndexSearcher.SEARCH_SOURCE);

			for (Mapping map : mapping) {
				mappedConcept.put(SummaryGraphUtil.removeGtOrLs(map.getTarget()), map.getTargetDsURI());
			}
		}
		Pseudograph<SummaryGraphElement, SummaryGraphEdge> graph;
		Set<String> res = new HashSet<String>();
		HashSet<String> uri_set = new HashSet<String>();
		
		for (String uri : mappedConcept.keySet()) {
			String datasource = mappedConcept.get(uri);
			graph = this.factory.getObj(datasource);
			if(graph == null) continue;
			for (SummaryGraphEdge edge : graph.edgeSet()) {
				SummaryGraphElement source = edge.getSource(), target = edge
						.getTarget();
				if (source.getResource() instanceof NamedConcept && ((NamedConcept) source.getResource()).getUri().equals(uri)
						&& target.getType() == SummaryGraphElement.RELATION) {
					res.add(((NamedConcept) source.getResource()).getUri()
							+ "\t" + datasource + "\t"
							+ source.getEF() + "\t"
							+ ConceptMark);
					String tmp = SummaryGraphUtil.removeNum(((Property) target.getResource()).getUri());
					if(!uri_set.contains(tmp + "\t" + datasource)) {
						uri_set.add(tmp + "\t" + datasource);
						res.add(tmp + "\t"
								+ datasource + "\t"
								+ target.getEF() + "\t"
								+ PredicateMark);
					}
				} else if (target.getResource() instanceof NamedConcept
						&& ((NamedConcept) target.getResource()).getUri()
								.equals(uri)
						&& source.getType() == SummaryGraphElement.RELATION) {
					res.add(((NamedConcept) target.getResource()).getUri()
							+ "\t" + datasource + "\t"
							+ target.getEF() + "\t"
							+ ConceptMark);
					
					String tmp = SummaryGraphUtil.removeNum(((Property) source.getResource()).getUri());
					if(!uri_set.contains(tmp + "\t" + datasource)) {
						uri_set.add(tmp + "\t" + datasource);
						res.add(tmp + "\t"
								+ datasource + "\t"
								+ source.getEF() + "\t"
								+ PredicateMark);
					}
				}
			}
		}
		return res;
	}

	/**
	 * TopK algorithm implement
	 * @param iGraph
	 * @param elements
	 * @param distance
	 * @param k
	 * @return
	 */
	private Collection<Subgraph> getTopKSubgraphs(
			Graph4TopK iGraph,
			Map<String, Collection<SummaryGraphElement>> elements,
			int distance, int k) {

		double max = Double.MAX_VALUE;
		ExpansionQueue expansionQueue = new ExpansionQueue(elements);
		List<Subgraph> subgraphList = new LinkedList<Subgraph>();
		
		Set<String> keywords = elements.keySet();
		HashMap<SummaryGraphElement, HashSet<SummaryGraphElement>> map = new HashMap<SummaryGraphElement, HashSet<SummaryGraphElement>>();
		for (String keyword : keywords) {
			for (SummaryGraphElement ele : elements.get(keyword)) {
				Set<SummaryGraphElement> ele_set = 
					iGraph.getElementFromUri(ele.getDatasource(), SummaryGraphUtil.getResourceUri(ele));
				for(SummaryGraphElement element : ele_set) {
					HashSet<SummaryGraphElement> set = map.get(keyword);
					
					if (set == null) {
						set = new HashSet<SummaryGraphElement>();
						map.put(element, set);
					}
					set.add(element);
					
					System.out.println("Begin Expansion from:");
					System.out.println(element);
					System.out.println();
					
					Cursor cursor = new Cursor(element, element, null, null,
							keyword, element.getTotalCost());
					expansionQueue.addCursor(cursor, keyword);
				}
			}
		}

		while (!expansionQueue.isEmpty()) {
			Cursor c = expansionQueue.pollRoundRobinMinCostCursor();
			
			if (c!=null && c.getLength() < distance) {
				SummaryGraphElement e = c.getElement();
				SummaryGraphElement matchingElement = c.getMatchingElement();
				String keyword = c.getKeyword();
				if (e.getCursors() == null)
					e.initCursorQueues(keywords);
				e.addCursor(c, keyword);

				// add new subgraphs
				if (e.isConnectingElement()) {
					Set<Set<Cursor>> combinations = e.getNewCursorCombinations();
					if (combinations != null && combinations.size() != 0) {
						e.addExploredCursorCombinations(combinations);
						e.clearNewCursorCombinations();
						
						Collection<Subgraph> sglist = computeSubgraphs(e, combinations);		

						for(Subgraph sg : sglist) {
							if(!subgraphList.contains(sg)) {
								subgraphList.add(sg);
							}	
							else {
								int index = subgraphList.indexOf(sg);
								Subgraph sgp = subgraphList.get(index);
								if(sgp.getCost() > sg.getCost()){
									sgp.setCost(sg.getCost());
								}
							}
						}
						
						// check for top-k
						if (subgraphList.size() >= k) {
							Collections.sort(subgraphList);
							
							for(int i=k; i<subgraphList.size(); i++)
								subgraphList.remove(i);
							
							max = subgraphList.get(k-1).getCost();
						}
					}
				}
				else {
					Collection<Cursor> neighbors;
					neighbors = getNonVisitedNeighbors(iGraph, e, c);
					for (Cursor n : neighbors) {						
						if (!map.get(matchingElement).contains(n.getElement())) {
							if(n.getCost() < max) {
								expansionQueue.addCursor(n, keyword);
							}
							map.get(matchingElement).add(n.getElement());
						}
					}
				}

			}
		}
		
		return subgraphList;
	}

	/**
	 * After the connect vertex is found. Get the subgraphs.
	 * @param connectingElement
	 * @param cursors
	 * @return
	 */
	private Collection<Subgraph> computeSubgraphs(
			SummaryGraphElement connectingElement, Set<Set<Cursor>> cursors) {
		Collection<Subgraph> subgraphs = new HashSet<Subgraph>();
		for (Set<Cursor> cursorsOfSubgraph : cursors) {
			Set<List<SummaryGraphEdge>> paths = new HashSet<List<SummaryGraphEdge>>();
			double cost = 0;
			for (Cursor cursor : cursorsOfSubgraph) {
				paths.add(cursor.getPath());
				cost += cursor.getCost();
			}
			Subgraph g = new Subgraph(SummaryGraphEdge.class);
			g.setCost(cost);
			Set<SummaryGraphEdge> allPaths = new LinkedHashSet<SummaryGraphEdge>();
			for(List<SummaryGraphEdge> list : paths){
				allPaths.addAll(list);
			}
			g.setPaths(allPaths);
			g.setConnectingElement(connectingElement);
			subgraphs.add(g);
		}
		return subgraphs;
	}

	/**
	 * Get the non visited neighbor elements of the Graph4TopK.
	 * @param iGraph
	 * @param e
	 * @param c
	 * @return
	 */
	private Collection<Cursor> getNonVisitedNeighbors(
			Graph4TopK iGraph,
			SummaryGraphElement e, Cursor c) {// System.out.println(c.getLength());
		Collection<Cursor> neighbors = new ArrayList<Cursor>();
		Collection<SummaryGraphEdge> ele_coll = null;
		ele_coll = iGraph.neighborEdges(e);
		
		Cursor nextCursor = null;
		for (SummaryGraphEdge edge : ele_coll) {
			if(edge.getTarget().equals(e)) {
				SummaryGraphElement source = edge.getSource();
				if (edge.getEdgeLabel().equals(SummaryGraphEdge.MAPPING_EDGE)) {
						nextCursor = new Cursor(source, c.getMatchingElement(),
								edge, c, c.getKeyword(),
								source.getTotalCost() + c.getCost());
						neighbors.add(nextCursor);
				}
				else {
					nextCursor = new Cursor(source, c.getMatchingElement(),
							edge, c, c.getKeyword(),
							source.getTotalCost() + c.getCost());
					neighbors.add(nextCursor);
				}
			}
			else if (edge.getSource().equals(e)) {
				SummaryGraphElement target = edge.getTarget();
				if (edge.getEdgeLabel().equals(SummaryGraphEdge.MAPPING_EDGE)) {
						nextCursor = new Cursor(target, c.getMatchingElement(),
								edge, c, c.getKeyword(),
								target.getTotalCost() + c.getCost());
						neighbors.add(nextCursor);
				}
				else {
					nextCursor = new Cursor(target, c.getMatchingElement(),
							edge, c, c.getKeyword(),
							target.getTotalCost() + c.getCost());
					neighbors.add(nextCursor);
				}
			}
		}

		return neighbors;
	}

	/**
	 * used by topk algorithm, keep all the path of the subgraph.
	 * @author jqchen
	 *
	 */
	private class ExpansionQueue {
		Map<String, PriorityQueue<Cursor>> m_queue;
		Set<String> m_keywords;
		ArrayList<PriorityQueue<Cursor>> m_queues;
		int roundRobin = 0;

		private ExpansionQueue(
				Map<String, Collection<SummaryGraphElement>> elements) {
			m_queue = new HashMap<String, PriorityQueue<Cursor>>();
			m_keywords = elements.keySet();
			m_queues = new ArrayList<PriorityQueue<Cursor>>();
			for (String k : elements.keySet()) {
				PriorityQueue<Cursor> q = new PriorityQueue<Cursor>();
				m_queue.put(k, q);
				m_queues.add(q);
			}
		}

		/**
		 * add a cursor to the queues.
		 * @param c
		 * @param keyword
		 */
		private void addCursor(Cursor c, String keyword) {
			PriorityQueue<Cursor> q = m_queue.get(keyword);
			q.add(c);
		}

		/**
		 * check whether the queue is empty.
		 * @return
		 */
		private boolean isEmpty() {
			if (m_queue.isEmpty())
				return true;
			for (PriorityQueue<Cursor> q : m_queues) {
				if (!q.isEmpty())
					return false;
			}
			return true;
		}
		
		/**
		 * poll cursor from all the queues, use RoundRobin policy.
		 * @return
		 */
		private Cursor pollRoundRobinMinCostCursor()
		  {
		   if (m_queues == null || m_queues.size() == 0)
		    return null;
		   
		   int rr = roundRobin;
		   
		   while(m_queues.get(roundRobin).isEmpty() && roundRobin != rr-1) {
			   roundRobin = (roundRobin + 1)%m_queues.size();
		   }
		   if(roundRobin == rr-1) {
			   return null;
		   }
		   
		   Cursor cur = m_queues.get(roundRobin).poll();
		   roundRobin = (roundRobin + 1)%m_queues.size();
		   return cur;
		  }
	}
}
