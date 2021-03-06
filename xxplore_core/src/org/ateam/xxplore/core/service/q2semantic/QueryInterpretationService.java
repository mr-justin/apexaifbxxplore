package org.ateam.xxplore.core.service.q2semantic;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.aifb.xxplore.shared.exception.Emergency;
import org.aifb.xxplore.shared.util.Pair;
import org.aifb.xxplore.shared.util.UniqueIdGenerator;
import org.apache.log4j.Logger;
import org.ateam.xxplore.core.service.IServiceListener;
import org.ateam.xxplore.core.service.mapping.Mapping;
import org.ateam.xxplore.core.service.mapping.MappingIndexService;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.Pseudograph;
import org.jgrapht.graph.WeightedPseudograph;
import org.xmedia.oms.model.api.IDataProperty;
import org.xmedia.oms.model.api.IEntity;
import org.xmedia.oms.model.api.INamedConcept;
import org.xmedia.oms.model.api.IProperty;
import org.xmedia.oms.model.api.IResource;
import org.xmedia.oms.model.impl.DataProperty;
import org.xmedia.oms.model.impl.Literal;
import org.xmedia.oms.model.impl.NamedConcept;
import org.xmedia.oms.model.impl.ObjectProperty;
import org.xmedia.oms.model.impl.Property;
import org.xmedia.oms.query.ConceptMemberPredicate;
import org.xmedia.oms.query.OWLPredicate;
import org.xmedia.oms.query.PropertyMemberPredicate;
import org.xmedia.oms.query.Variable;

import com.ice.tar.tar;
import com.sun.org.apache.xml.internal.serializer.ElemDesc;

public class QueryInterpretationService implements IQueryInterpretationService {

	private static Logger s_log = Logger.getLogger(QueryInterpretationService.class);
	public static double DEFAULT_SCORE = 2.0;
	public static double EDGE_SCORE = 0.5;
	public MappingIndexService mis;
	
	public GraphAdapterFactory factory;
	
	public QueryInterpretationService(Set<String> keys) {
		mis = new MappingIndexService();
		mis.init4Search(SearchQ2SemanticService.mappingIndexRoot);
		Set<String> keys_set = new HashSet<String>();
		for(String key : keys) {
			keys_set.add(key);
		}
		factory = new GraphAdapterFactory(keys_set,mis);
	}
	
	public void refreshFactory() {
		for(SummaryPart sg : factory.summaryGraph_HM.values()) { 
			for(SummaryGraphElement ele : sg.summaryGraph.vertexSet()) {
				ele.cursors = null;
				ele.m_exploredCursorCombinations = null;
				ele.m_newCursorCombinations = null;
				
			}
		}
	}
	
	public QueryInterpretationService() {
		
	}

	private Pseudograph<SummaryGraphElement, SummaryGraphEdge> resourceGraph;

	// store datasources and coverage
	public Map<String, Integer> m_datasources = new HashMap<String, Integer>();

	private Set<SummaryGraphElement> m_startingElements = new HashSet<SummaryGraphElement>();

	private Map<String, Pseudograph<SummaryGraphElement, SummaryGraphEdge>> m_DsGraphMap = 
		new HashMap<String, Pseudograph<SummaryGraphElement, SummaryGraphEdge>>();

	public void callService(IServiceListener listener, Object... params) {
		// TODO Auto-generated method stub
	}

	public void disposeService() {
		// TODO Auto-generated method stub
	}

	public void init(Object... params) {
		// TODO Auto-generated method stub
	}
	
	public void outputGraphInfo(Pseudograph<SummaryGraphElement, SummaryGraphEdge> graph) {
		System.out.println("graph ========================");
		for(SummaryGraphEdge edge : graph.edgeSet()) {
			System.out.println( edge.getEdgeLabel()  + "\t");
			System.out.println(SummaryGraphUtil.getResourceUri(edge.getSource()) + " [" + edge.getSource().getEF() + "]" + " [" + edge.getSource().getMatchingScore() + "]");
			System.out.println(SummaryGraphUtil.getResourceUri(edge.getTarget()) + " [" + edge.getTarget().getEF() + "]" + " [" + edge.getTarget().getMatchingScore() + "]");
			System.out.println();
		}
	}
	
	public LinkedList<Subgraph> computeQueries(
			Set<String> ds_used,
			Map<String, Collection<SummaryGraphElement>> elements,
			int distance, int k) {

		if (elements == null) {
			return null;
		}
		
		this.refreshFactory();
		GraphAdapter iGraph = factory.createGraphAdapter(elements);

		Collection<Subgraph> subgraphs = getTopKSubgraphs(ds_used,iGraph,elements, distance, k);
		
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

	private LinkedList<Subgraph> resultRefinement(
			LinkedList<Subgraph> subgraphs, int k) {
		// TODO Auto-generated method stub
		LinkedList<Subgraph> res = new LinkedList<Subgraph>();
		for (int i = 0; i < subgraphs.size(); i++) {
			if (i < k) {
				Subgraph graph = subgraphs.get(i);
				// remove concept
				HashSet<SummaryGraphElement> removeSet = new HashSet<SummaryGraphElement>();
				for (SummaryGraphElement node : graph.vertexSet())
					if (node.getType() == SummaryGraphElement.CONCEPT)
						removeSet.add(node);
				graph.removeAllVertices(removeSet);
				// add top concept
				SummaryGraphElement top = new SummaryGraphElement(
						NamedConcept.TOP, SummaryGraphElement.CONCEPT);
				graph.addVertex(top);
				// change the edge
				for (SummaryGraphEdge edge : graph.edgeSet()) {
					if (edge.getSource().getType() == SummaryGraphElement.CONCEPT)
						edge.setSource(top);
					if (edge.getTarget().getType() == SummaryGraphElement.CONCEPT)
						edge.setTarget(top);
				}
				// add missing domain or range
				boolean hasDomain, hasRange;
				HashMap<SummaryGraphElement, String> addMap = new HashMap<SummaryGraphElement, String>();
				for (SummaryGraphElement node : graph.vertexSet()) {
					hasDomain = false;
					hasRange = false;
					if (node.getType() == SummaryGraphElement.ATTRIBUTE
							|| node.getType() == SummaryGraphElement.RELATION) {
						System.out.println(((Property) node.getResource()).getUri());
						// Set<SummaryGraphEdge> edges = graph.edgesOf(node);
						// System.out.println(edges.size());
						for (SummaryGraphEdge edge : graph.edgeSet()) {
							if (edge.getTarget().equals(node)
									&& edge.getEdgeLabel().equals(
											SummaryGraphEdge.DOMAIN_EDGE))
								hasDomain = true;
							else if (edge.getSource().equals(node)
									&& edge.getEdgeLabel().equals(
											SummaryGraphEdge.RANGE_EDGE))
								hasRange = true;
						}
						System.out.println(hasDomain + "\t" + hasRange);
						if (!hasDomain)
							addMap.put(node, SummaryGraphEdge.DOMAIN_EDGE);// graph.addEdge(top,
																			// node,
																			// new
																			// SummaryGraphEdge(top,
																			// node,
																			// SummaryGraphEdge.DOMAIN_EDGE));
						if (!hasRange)
							addMap.put(node, SummaryGraphEdge.RANGE_EDGE);// graph.addEdge(node,
																			// top,
																			// new
																			// SummaryGraphEdge(node,
																			// top,
																			// SummaryGraphEdge.RANGE_EDGE));
					}
				}
				System.out.println("=====");
				for (SummaryGraphElement node : addMap.keySet()) {
					// System.out.println(i);
					// System.out.println(graph.edgeSet().size());
					if (addMap.get(node).equals(SummaryGraphEdge.DOMAIN_EDGE))
						graph.addEdge(top, node, new SummaryGraphEdge(top,
								node, SummaryGraphEdge.DOMAIN_EDGE));
					else if (addMap.get(node).equals(
							SummaryGraphEdge.RANGE_EDGE))
						graph.addEdge(node, top, new SummaryGraphEdge(node,
								top, SummaryGraphEdge.RANGE_EDGE));
					// System.out.println(graph.edgeSet().size());
				}
				res.add(graph);
			}
		}

		for (Subgraph g : res) {
			System.out.println("========= Top Refine ==========");
			for (SummaryGraphEdge edge : g.edgeSet()) {
				if (edge.getEdgeLabel().equals(SummaryGraphEdge.DOMAIN_EDGE))
					System.out.println(((NamedConcept) edge.getSource()
							.getResource()).getUri()
							+ " c->p "
							+ ((Property) edge.getTarget().getResource())
									.getUri());
				else if (edge.getEdgeLabel()
						.equals(SummaryGraphEdge.RANGE_EDGE)
						&& edge.getTarget().getType() == SummaryGraphElement.CONCEPT)
					System.out.println(((Property) edge.getSource()
							.getResource()).getUri()
							+ " r->c "
							+ ((NamedConcept) edge.getTarget().getResource())
									.getUri());
				else if (edge.getEdgeLabel()
						.equals(SummaryGraphEdge.RANGE_EDGE)
						&& edge.getTarget().getType() == SummaryGraphElement.VALUE)
					System.out.println(((Property) edge.getSource()
							.getResource()).getUri()
							+ " a->v "
							+ ((Literal) edge.getTarget().getResource())
									.getLabel());
			}
		}
		return res;
	}

	private void updateDsCoverage(String ds) {
		Integer cover = m_datasources.get(ds);
		if (cover != null) {
			cover = new Integer(cover.intValue() + 1);
		} else {
			m_datasources.put(ds, new Integer(1));
		}
	}

	private int getDsCoverage(SummaryGraphElement e) {
		String ds = e.getDatasource();
		return m_datasources.get(ds).intValue();
	}
	
	public List<SummaryGraphElement> getElem(String label, Pseudograph<SummaryGraphElement, SummaryGraphEdge> iGraph, SummaryGraphElement elem)
	{
		ArrayList<SummaryGraphElement> list = null;
		Set<SummaryGraphEdge> Edges = iGraph.edgesOf(elem);
		if(Edges != null)
		{
			list = new ArrayList<SummaryGraphElement>();
			for(SummaryGraphEdge edge : Edges)
			{
				if(label.equals(SummaryGraphEdge.DOMAIN_EDGE) && edge.getEdgeLabel().equals(label))
					list.add(edge.getSource());
				else if(label.equals(SummaryGraphEdge.RANGE_EDGE) && edge.getEdgeLabel().equals(label))
					list.add(edge.getTarget());
			}
		}
		return list;
	}

	public Set<String> getSuggestion(List<String> concept, String ds, MappingIndexService index) throws Exception {
		HashMap<String, String> mappedConcept = new HashMap<String, String>();
		Collection<Mapping> mapping;
		for (String con : concept) {
			mapping = index.searchMappings(con, ds,
					MappingIndexService.SEARCH_SOURCE);
//			mapping = this.factory.getMappings();

			for (Mapping map : mapping)
			{
				mappedConcept.put(SummaryGraphUtil.removeGtOrLs(map.getTarget()), map.getTargetDsURI());
//				System.out.println("t "+map.getTarget()+" "+map.getTargetDsURI());
			}
//			mapping = index.searchMappings(con, ds,
//					MappingIndexService.SEARCH_TARGET);
//
//			for (Mapping map : mapping)
//			{
//				mappedConcept.put(SummaryGraphUtil.removeGtOrLs(map.getSource()), map.getSourceDsURI());
////				System.out.println("s "+map.getSource()+" "+map.getSourceDsURI());
//			}
		}
//		SummaryGraphIndexServiceForBTFromNT sss = new SummaryGraphIndexServiceForBTFromNT();
		Pseudograph<SummaryGraphElement, SummaryGraphEdge> graph;
		Set<String> res = new HashSet<String>();
		HashSet<String> uri_set = new HashSet<String>();
		
		for (String uri : mappedConcept.keySet()) {
			String datasource = mappedConcept.get(uri);
//			String objFile = SearchQ2SemanticService.schemaObjSet
//					.get(datasource);// SesameDao.root +
//										// datasource+"-schema.obj";
//			graph = sss.readGraphIndexFromFile(objFile);
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
							+ SearchQ2SemanticService.ConceptMark);
					String tmp = SummaryGraphUtil.removeNum(((Property) target.getResource()).getUri());
					if(!uri_set.contains(tmp + "\t" + datasource)) {
						uri_set.add(tmp + "\t" + datasource);
						res.add(tmp + "\t"
								+ datasource + "\t"
								+ target.getEF() + "\t"
								+ SearchQ2SemanticService.PredicateMark);
					}
				} else if (target.getResource() instanceof NamedConcept
						&& ((NamedConcept) target.getResource()).getUri()
								.equals(uri)
						&& source.getType() == SummaryGraphElement.RELATION) {
					res.add(((NamedConcept) target.getResource()).getUri()
							+ "\t" + datasource + "\t"
							+ target.getEF() + "\t"
							+ SearchQ2SemanticService.ConceptMark);
					
					String tmp = SummaryGraphUtil.removeNum(((Property) source.getResource()).getUri());
					if(!uri_set.contains(tmp + "\t" + datasource)) {
						uri_set.add(tmp + "\t" + datasource);
						res.add(tmp + "\t"
								+ datasource + "\t"
								+ source.getEF() + "\t"
								+ SearchQ2SemanticService.PredicateMark);
					}
				}
			}
		}
		return res;
	}

	// TODO this is not so efficient when graph is huge...
	private void updateScore(
			Pseudograph<SummaryGraphElement, SummaryGraphEdge> graph,
			SummaryGraphElement e, Collection<SummaryGraphElement> keywords) {
		double score = -1;
		for (SummaryGraphElement k : keywords) {
			if (k.equals(e))
				score = k.getMatchingScore();
		}

		// == chenjunquan ==
		e.setMatchingScore(score);
		e.applyCoverage(getDsCoverage(e));
	}

	// TODO this is not so efficient when graph is huge...
	private SummaryGraphElement getVertex(
			Pseudograph<SummaryGraphElement, SummaryGraphEdge> graph, String URI) {
		Set<SummaryGraphElement> vertices = graph.vertexSet();
		if (vertices != null && vertices.size() > 0) {
			for (SummaryGraphElement v : vertices) {
				IResource res = v.getResource();
				if (res instanceof IEntity) {
					// == chenjunquan == ((IEntity)res).getUri() == URI
					if (URI.indexOf(((IEntity) res).getUri()) != -1)
						return v;
				}
			}
		}
		return null;
	}

	private Collection<Subgraph> getTopKSubgraphs(
			Set<String> ds_used,
			GraphAdapter iGraph,
			Map<String, Collection<SummaryGraphElement>> elements,
			int distance, int k) {

		double max = Double.MAX_VALUE;
		ExpansionQueue expansionQueue = new ExpansionQueue(elements);
		List<Subgraph> subgraphList = new LinkedList<Subgraph>();
		
//		List<Subgraph> output_list = new LinkedList<Subgraph>();

		Set<String> keywords = elements.keySet();
		HashMap<String, HashSet<SummaryGraphElement>> map = new HashMap<String, HashSet<SummaryGraphElement>>();
		for (String keyword : keywords) {
			for (SummaryGraphElement ele : elements.get(keyword)) {
				Set<SummaryGraphElement> ele_set = 
					iGraph.getElementFromString(ele.getDatasource(), SummaryGraphUtil.getResourceUri(ele));
				for(SummaryGraphElement element : ele_set) {
					HashSet<SummaryGraphElement> set = map.get(keyword);
					
					// == chenjunquan ==
					if (set == null) {
						set = new HashSet<SummaryGraphElement>();
						map.put(keyword, set);
					}
					set.add(element);
					// System.out.println(element.getTotalScore()+"\t"+keyword);
					Cursor cursor = new Cursor(element, element, null, null,
							keyword, element.getTotalScore());
					expansionQueue.addCursor(cursor, keyword);
				}
			}
		}

		// System.out.println(expansionQueue.isEmpty());
		while (!expansionQueue.isEmpty()) {
//			by kaifengxu
			Cursor c = expansionQueue.pollRoundRobinMinCostCursor();
			
			if (c!=null && c.getLength() < distance) {
				SummaryGraphElement e = c.getElement();
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
						subgraphList.addAll(sglist);
						
//						output_list.addAll(sglist);
						
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
					neighbors = getNonVisitedNeighbors(ds_used,iGraph, e, c);
					// System.out.println(neighbors.size());
					
//					for(Cursor n : neighbors) {
//						
//						if(SummaryGraphUtil.getResourceUri(n.getElement()).indexOf("produced_by") != -1) {
//							System.out.println(SummaryGraphUtil.getResourceUri(n.getElement()));
//						}
//					}
					for (Cursor n : neighbors) {
						// if(n.getCost()==0)System.out.println("aaa");
						
						if (!map.get(keyword).contains(n.getElement())) {
							if(n.getCost() < max) {
								expansionQueue.addCursor(n, keyword);
							}
							map.get(keyword).add(n.getElement());
						}
					}
				}

			}
		}
		
		return subgraphList;
	}

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
			g.setPaths(paths);
			g.setConnectingElement(connectingElement);
			subgraphs.add(g);
		}
		return subgraphs;
	}

	private Collection<QueryGraph> getQuerygraphs(Subgraph graph) {
		if (graph == null)
			return null;
		Collection<QueryGraph> graphs = new ArrayList<QueryGraph>();

		ConnectivityInspector<SummaryGraphElement, SummaryGraphEdge> connectedInsp = new ConnectivityInspector<SummaryGraphElement, SummaryGraphEdge>(
				graph);
		Emergency.checkPrecondition(connectedInsp.isGraphConnected(),
				"Computed Subgraphs must be weakly connected!!!");

		// split: remove all mapping edges
		for (SummaryGraphEdge e : graph.edgeSet()) {
			if (e.getEdgeLabel() == SummaryGraphEdge.MAPPING_EDGE)
				graph.removeEdge(e);
		}

		// build graphs from maximal connected component
		// each connected component correspond to one datasource
		ConnectivityInspector<SummaryGraphElement, SummaryGraphEdge> compInsp = new ConnectivityInspector<SummaryGraphElement, SummaryGraphEdge>(
				graph);
		List<Set<SummaryGraphElement>> components = compInsp.connectedSets();
		for (Set<SummaryGraphElement> comp : components) {
			QueryGraph qg = new QueryGraph();
			Collection<QueryGraphEdge> qgEdges = new ArrayList<QueryGraphEdge>();
			for (SummaryGraphElement e : comp) {
				// relation and attribute elements should have exactly one
				// incoming and one outgoing edge (since mapping edge has been
				// removed)
				if (e.getType() == SummaryGraphElement.RELATION
						|| e.getType() == SummaryGraphElement.ATTRIBUTE) {
					Set<SummaryGraphEdge> edges = graph.edgesOf(e);
					Emergency
							.checkPrecondition(
									edges.size() == 2,
									"relation and attribute elements should have exactly one incoming and one outgoing edge!");
					SummaryGraphEdge inEdge = null;
					SummaryGraphEdge outEdge = null;
					while (edges.iterator().hasNext()) {
						SummaryGraphEdge edge = edges.iterator().next();
						if (edge.getTarget().equals(e)) {
							inEdge = edge;
						} else if (edge.getSource().equals(e)) {
							outEdge = edge;
						}
					}
					qgEdges.add(new QueryGraphEdge(inEdge.getSource()
							.getResource(), outEdge.getTarget().getResource(),
							(IProperty) e.getResource(), e.getType()));
				}
			}

			qg.setEdges(qgEdges);
			// each connected component correspond to one datasource, i.e. every
			// elements of one component belongs to same ds
			qg.setDatasource(comp.iterator().next().getDatasource());
			graphs.add(qg);
		}
		return graphs;
	}

	private Collection<Cursor> getNonVisitedNeighbors(
			Set<String> ds_used,
			GraphAdapter iGraph,
			SummaryGraphElement e, Cursor c) {// System.out.println(c.getLength());
		Collection<Cursor> neighbors = new ArrayList<Cursor>();
		Collection<SummaryGraphEdge> ele_coll = null;
		ele_coll = iGraph.neighborVertex(e);
		
		Cursor nextCursor = null;
		for (SummaryGraphEdge edge : ele_coll) {
			if(edge.getTarget().equals(e)) {
				SummaryGraphElement source = edge.getSource();
				if (edge.getEdgeLabel().equals(SummaryGraphEdge.MAPPING_EDGE)) {
					if(ds_used.contains(source.getDatasource())) {
						nextCursor = new Cursor(source, c.getMatchingElement(),
								edge, c, c.getKeyword(),
								source.getTotalScore() + c.getCost());
						neighbors.add(nextCursor);
					}
				}
				else {
					nextCursor = new Cursor(source, c.getMatchingElement(),
							edge, c, c.getKeyword(),
							source.getTotalScore() + c.getCost());
					neighbors.add(nextCursor);
				}
			}
			else if (edge.getSource().equals(e)) {
				SummaryGraphElement target = edge.getTarget();
				if (edge.getEdgeLabel().equals(SummaryGraphEdge.MAPPING_EDGE)) {
					if(ds_used.contains(target.getDatasource())) {
						nextCursor = new Cursor(target, c.getMatchingElement(),
								edge, c, c.getKeyword(),
								target.getTotalScore() + c.getCost());
						neighbors.add(nextCursor);
					}
				}
				else {
					nextCursor = new Cursor(target, c.getMatchingElement(),
							edge, c, c.getKeyword(),
							target.getTotalScore() + c.getCost());
					neighbors.add(nextCursor);
				}
			}
		}

		return neighbors;
	}

	private Collection<OWLPredicate> computeQuery(QueryGraph subgraph) {
		Collection<OWLPredicate> query = new LinkedHashSet<OWLPredicate>();
		UniqueIdGenerator.getInstance().resetVarIds();
		for (QueryGraphEdge edge : subgraph.getEdges()) {
			Map<Pair, Variable> labelVar = new LinkedHashMap<Pair, Variable>();
			IResource target = edge.getTarget();

			if (edge.getType() == SummaryGraphElement.ATTRIBUTE
					&& !(target.getLabel() == SummaryGraphElement.DUMMY_VALUE_LABEL)) {
				IProperty p = edge.getProperty();
				IResource source = edge.getSource();
				IResource t;
				String conceptLabel = source.getLabel();
				String literalLabel = target.getLabel();
				Pair label = new Pair(conceptLabel, literalLabel);
				Variable var = labelVar.get(label);
				if (var == null) {
					t = getNewVariable();
					labelVar.put(label, (Variable) t);
					query.add(new ConceptMemberPredicate(source, t));
				} else {
					t = var;
				}

				query.add(new PropertyMemberPredicate(p, t, target));
			}

			else if (edge.getType() == SummaryGraphElement.ATTRIBUTE
					&& target.getLabel() == SummaryGraphElement.DUMMY_VALUE_LABEL) {
				IProperty p = edge.getProperty();
				IResource v1 = edge.getSource();

				Set<IResource> t1 = new LinkedHashSet<IResource>();
				String label1 = v1.getLabel();
				for (Pair pair : labelVar.keySet()) {
					if (pair.getHead().equals(label1)) {
						t1.add(labelVar.get(pair));
					}
				}
				if (t1.size() == 0) {
					Variable var = getNewVariable();
					t1.add(var);
					labelVar.put(new Pair(label1, null), var);
					query.add(new ConceptMemberPredicate(v1, var));
				}

				for (IResource resource1 : t1) {
					query.add(new PropertyMemberPredicate(p, resource1,
							getNewVariable()));
				}
			}

			else if (edge.getType() == SummaryGraphElement.RELATION
					&& !(edge.getProperty().equals(Property.SUBCLASS_OF))) {
				IProperty p = edge.getProperty();
				IResource v1 = edge.getSource();

				Set<IResource> t1 = new LinkedHashSet<IResource>();
				String label1 = v1.getLabel();
				for (Pair pair : labelVar.keySet()) {
					if (pair.getHead().equals(label1)) {
						t1.add(labelVar.get(pair));
					}
				}
				if (t1.size() == 0) {
					Variable var = getNewVariable();
					t1.add(var);
					labelVar.put(new Pair(label1, null), var);
					query.add(new ConceptMemberPredicate(v1, var));
				}

				Set<IResource> t2 = new LinkedHashSet<IResource>();
				String label2 = target.getLabel();
				for (Pair pair : labelVar.keySet()) {
					if (pair.getHead().equals(label2)) {
						t2.add(labelVar.get(pair));
					}
				}
				if (t2.size() == 0) {
					Variable var = getNewVariable();
					t2.add(var);
					labelVar.put(new Pair(label2, null), var);
					query.add(new ConceptMemberPredicate(target, var));
				}

				for (IResource resource1 : t1) {
					for (IResource resource2 : t2) {
						query.add(new PropertyMemberPredicate(p, resource1,
								resource2));
					}
				}

			}

		}
		return query;
	}

	private Variable getNewVariable() {
		String var = String.valueOf(UniqueIdGenerator.getInstance()
				.getNewVarId());
		return new Variable("x" + var);
	}

	private class QueryGraph {
		Collection<QueryGraphEdge> edges;

		String datasource;

		public QueryGraph() {
		};

		public QueryGraph(Collection<QueryGraphEdge> edges) {
			this.edges = edges;
		}

		public void setEdges(Collection<QueryGraphEdge> edges) {
			this.edges = edges;
		}

		public void setDatasource(String ds) {
			datasource = ds;
		}

		public String getDatasource() {
			return datasource;
		}

		public Collection<QueryGraphEdge> getEdges() {
			return edges;
		}
	}

	private class QueryGraphEdge {
		IResource source;
		IResource target;
		IProperty edge;
		int type;

		public QueryGraphEdge(IResource source, IResource target,
				IProperty edge, int type) {
			this.source = source;
			this.target = target;
			this.edge = edge;
			this.type = type;
		}

		public int getType() {
			return type;
		}

		public IResource getSource() {
			return source;
		}

		public void setSource(IResource source) {
			this.source = source;
		}

		public IResource getTarget() {
			return target;
		}

		public IProperty getProperty() {
			return edge;
		}

		public void setTarget(IResource target) {
			this.target = target;
		}

		public IProperty getEdge() {
			return edge;
		}

		public void setEdge(IProperty edge) {
			this.edge = edge;
		}
	}

	public class Subgraph extends
			Pseudograph<SummaryGraphElement, SummaryGraphEdge>
			implements Comparable {

		private SummaryGraphElement connectingVertex;

		private Set<List<SummaryGraphEdge>> paths;

		private boolean pathIsSet = false;

		double cost;

		public Subgraph(Class<? extends SummaryGraphEdge> edgeclass) {
			super(edgeclass);
		}

		public Set<List<SummaryGraphEdge>> getPaths() {
			return paths;
		}

		public void setPaths(Set<List<SummaryGraphEdge>> paths) {
			Emergency
					.checkPrecondition(!pathIsSet,
							"Set path can be invoked only once to initialized the graph!!!");
			if (paths == null || paths.size() == 0)
				return;
			this.paths = paths;
			for (List<SummaryGraphEdge> path : paths) {
				if (path.size() == 0)
					continue;
				for (SummaryGraphEdge e : path) {
					addVertex(e.getSource());
					addVertex(e.getTarget());
					addEdge(e.getSource(), e.getTarget(), e);
				}
			}
		}

		public SummaryGraphElement getConnectingVertex() {
			return connectingVertex;
		}

		public void setConnectingElement(SummaryGraphElement connectingE) {
			connectingVertex = connectingE;
		}

		public void setCost(double cost) {
			this.cost = cost;
		}

		public double getCost() {
			return cost;
		}

		public int compareTo(Object o) {
			Subgraph other = (Subgraph) o;
			if (this.cost > other.cost) {
				return 1;
			}
			if (this.cost < other.cost) {
				return -1;
			}
			return 0;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (!(o instanceof Subgraph)) {
				return false;
			}

			Subgraph other = (Subgraph) o;
			if (!(paths.equals(other.getPaths()))) {
				return false;
			}
			return true;
		}

		@Override
		public int hashCode() {
			return 13 * paths.hashCode();
		}

		@Override
		public String toString(){
			String ret = "cost: " + cost 
			+ "\n" + "Connecting vertex: " + connectingVertex
			+ "\n" + "Paths: \n";
			for(List<SummaryGraphEdge>path :paths) {
				ret += "************\n";
				for(SummaryGraphEdge edge : path) {
					ret += edge.toString() + "\n";
				}
				ret +="************\n";
			}
			return ret;
		}	
	}

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

		private void addCursor(Cursor c, String keyword) {
			PriorityQueue<Cursor> q = m_queue.get(keyword);
			q.add(c);
		}

		private boolean isEmpty() {
			if (m_queue.isEmpty())
				return true;
			for (PriorityQueue<Cursor> q : m_queues) {
				if (!q.isEmpty())
					return false;
			}
			return true;
		}
		
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
		 


		private Cursor pollMinCostCursor() {
			if (m_queues == null || m_queues.size() == 0)
				return null;
			PriorityQueue<Cursor> minCostQ = null;
			double minCost = -1;
			for (PriorityQueue<Cursor> q : m_queues) {
				// System.out.print(q.peek().getCost()+"\t");
				// ============by kaifengxu
				if (q.peek() == null)
					continue;
				double cCost = q.peek().getCost();
				if (cCost < minCost || minCost == -1) {
					minCost = cCost;
					// System.out.println(cCost);
					minCostQ = q;
				}
			}
			// System.out.println();
			return minCostQ.poll();
		}

		/**
		 * Simple return the cost of the min cost cursor in the queues.
		 * 
		 * @return
		 */
		private double getMinCostOfCandidadates() {
			double minCost = -1;
			if (m_queues == null || m_queues.size() == 0)
				return minCost;
			for (PriorityQueue<Cursor> q : m_queues) {

				double cCost = q.peek().getCost();
				if (cCost < minCost || minCost == 0) {
					minCost = cCost;
				}
			}
			return minCost;
		}

		/**
		 * Approximation. Returns the sum of the cost of the first cursor in
		 * each queue
		 * 
		 * @return
		 */
		private double getApproximateMinCostOfCandidates() {
			double minCost = -1;
			if (m_queues == null || m_queues.size() == 0)
				return minCost;
			for (PriorityQueue<Cursor> q : m_queues) {
				if(q.size()>0)
				minCost += q.peek().getCost();
			}
			return minCost;
		}
	}
}
