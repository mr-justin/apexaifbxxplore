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

public class QueryInterpretationService implements IQueryInterpretationService {

	private static Logger s_log = Logger.getLogger(QueryInterpretationService.class);
	private static double DEFAULT_SCORE = 0.0;
	private static double EDGE_SCORE = 0.5;

	private Pseudograph<SummaryGraphElement, SummaryGraphEdge> resourceGraph;

	// store datasources and coverage
	private Map<String, Integer> m_datasources = new HashMap<String, Integer>();

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
			Map<String, Collection<SummaryGraphElement>> elements,
			MappingIndexService index, int distance, int k) {

		if (elements == null) {
			return null;
		}
		
		Collection<Pseudograph<SummaryGraphElement, SummaryGraphEdge>> sumGraphs = retrieveSummaryGraphs(elements);

		getAugmentedSummaryGraphs(sumGraphs, elements);		

		resourceGraph = getIntegratedSummaryGraph(sumGraphs, index);
		
		for(SummaryGraphElement ele : resourceGraph.vertexSet()) {
			if(ele.getMatchingScore() != 0) {
				ele.setTotalScore(1.0 / ele.getMatchingScore());
			}
			else if(ele.getEF() != 0) {
				ele.setTotalScore(ele.getEF());
			}
			else {
				ele.setTotalScore(this.DEFAULT_SCORE);
			}
			ele.setTotalScore(ele.getTotalScore() + this.EDGE_SCORE);
		}

		Collection<Subgraph> subgraphs = getTopKSubgraphs(resourceGraph,elements, distance, k);
		
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

	private Collection<Pseudograph<SummaryGraphElement, SummaryGraphEdge>> retrieveSummaryGraphs(
			Map<String, Collection<SummaryGraphElement>> elements) {
		if (elements == null || elements.size() == 0) {
			return null;
		}
		Collection<Pseudograph<SummaryGraphElement, SummaryGraphEdge>> result = 
			new ArrayList<Pseudograph<SummaryGraphElement, SummaryGraphEdge>>();
		Collection<Collection<SummaryGraphElement>> gElements = elements.values();
		// retrieve data source URI
		Set<String> dsURISet = new HashSet<String>();
		
		for (Collection<SummaryGraphElement> c : gElements) {
			for (SummaryGraphElement e : c) {
				String dsURI = e.getDatasource();
				dsURISet.add(dsURI);
			}
		}
		for(String dsURI : dsURISet) {
			// store and update ds coverage
			updateDsCoverage(dsURI);
			// ============================by kaifengxu
			if (m_DsGraphMap.containsKey(dsURI))
				continue;
			String dsDFileName = SearchQ2SemanticService.summaryObjSet.get(dsURI);

			File graphIndex = new File(dsDFileName);
			ObjectInputStream in;
			Pseudograph<SummaryGraphElement, SummaryGraphEdge> graph = null;
			try {
				in = new ObjectInputStream(new FileInputStream(graphIndex));
				graph = (Pseudograph<SummaryGraphElement, SummaryGraphEdge>) in.readObject();
				
				for (SummaryGraphElement elem : graph.vertexSet()) {
					elem.setDatasource(dsURI);
				}
				result.add(graph);
				in.close();
			} catch (FileNotFoundException ex) {
				ex.printStackTrace();
			} catch (IOException ex) {
				ex.printStackTrace();
			} catch (ClassNotFoundException ex) {
				ex.printStackTrace();
			}
			if (graph != null){
				m_DsGraphMap.put(dsURI, graph);
			}
		}

		return result;
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

	private void getAugmentedSummaryGraphs(
			Collection<Pseudograph<SummaryGraphElement, SummaryGraphEdge>> graphs,
			Map<String, Collection<SummaryGraphElement>> keywords) {
		if (graphs == null || graphs.size() == 0) {
			return;
		}
		Set<String> keys = keywords.keySet();
		for (String key : keys) {
			m_startingElements.addAll(keywords.get(key));
		}
		
		// == chenjunquan ==
		HashMap<String, SummaryGraphElement> vertexMap = new HashMap<String, SummaryGraphElement>();
		for(Pseudograph<SummaryGraphElement, SummaryGraphEdge> g : graphs) {
			for(SummaryGraphElement ele : g.vertexSet()) {
				vertexMap.put(SummaryGraphUtil.getResourceUri(ele) + " " + ele.getDatasource(), ele);
			}
		}
		// int count = 0;
		for (Pseudograph<SummaryGraphElement, SummaryGraphEdge> graph : graphs) {
			int count = 0;
			String graph_ds = null;
			if(graph.vertexSet().size() != 0) {
				Iterator<SummaryGraphElement> iter = graph.vertexSet().iterator();
				graph_ds = iter.next().getDatasource();
			}
			else continue;
			for (SummaryGraphElement e : m_startingElements) {
				if (e instanceof SummaryGraphValueElement) {
					// =============by kaifengxu
					String ds = e.getDatasource();
					if(!graph_ds.equals(ds)) continue;
					
					Map<IDataProperty, Collection<INamedConcept>> neighbors = 
						((SummaryGraphValueElement) e).getNeighbors();
					Set<IDataProperty> props = neighbors.keySet();
					Iterator<IDataProperty> propIter = props.iterator();
					
					while (propIter.hasNext()) {
						IDataProperty prop = propIter.next();
						SummaryGraphElement pvertex = new SummaryGraphElement(
								new DataProperty(prop.getUri()+"("+(count++)+")"), SummaryGraphElement.ATTRIBUTE);
						Collection<INamedConcept> cons = neighbors.get(prop);
						Iterator<INamedConcept> conIter = cons.iterator();
						while (conIter.hasNext()) {
							INamedConcept con = conIter.next();
							SummaryGraphElement cvertex = new SummaryGraphElement(
									con, SummaryGraphElement.CONCEPT);
							
							if(vertexMap.get(SummaryGraphUtil.getResourceUri(cvertex) + " " + cvertex.getDatasource()) != null) {
								cvertex = vertexMap.get(SummaryGraphUtil.getResourceUri(cvertex)+ " " + cvertex.getDatasource());
							}
							if(vertexMap.get(SummaryGraphUtil.getResourceUri(pvertex) + " " + pvertex.getDatasource()) != null) {
								pvertex = vertexMap.get(SummaryGraphUtil.getResourceUri(pvertex)+ " " + cvertex.getDatasource());
							}
							if(vertexMap.get(SummaryGraphUtil.getResourceUri(e) + " " + e.getDatasource()) != null) {
								e = vertexMap.get(SummaryGraphUtil.getResourceUri(e) + " " + e.getDatasource());
							}
							
							
							SummaryGraphEdge domain = new SummaryGraphEdge(
									cvertex, pvertex,
									SummaryGraphEdge.DOMAIN_EDGE);
							SummaryGraphEdge range = new SummaryGraphEdge(
									pvertex, (SummaryGraphElement) e,
									SummaryGraphEdge.RANGE_EDGE);
							// =============by kaifengxu
							// System.out.println(domain.getSource().getResource().getClass());
							cvertex.setDatasource(ds);
							pvertex.setDatasource(ds);
							graph.addVertex(cvertex);
							graph.addVertex(pvertex);
							graph.addVertex(e);
							graph.addEdge(domain.getSource(), domain.getTarget(), domain);
							graph.addEdge(range.getSource(), range.getTarget(), range);
						}
					}
				}
				System.out.println("add: "+SummaryGraphUtil.getResourceUri(e)+"\t"+e.getType());
				updateScore(graph, e, m_startingElements);
			}
		}
	}

	private Pseudograph<SummaryGraphElement, SummaryGraphEdge> getIntegratedSummaryGraph(
			Collection<Pseudograph<SummaryGraphElement, SummaryGraphEdge>> graphs,
			MappingIndexService index) {
		
		Pseudograph<SummaryGraphElement, SummaryGraphEdge> iGraph = new Pseudograph<SummaryGraphElement, SummaryGraphEdge>(
				SummaryGraphEdge.class);
		
		HashMap<String,HashSet<SummaryGraphElement>> hm = new HashMap<String, HashSet<SummaryGraphElement>>();
		
		for(Pseudograph<SummaryGraphElement, SummaryGraphEdge> graph : graphs) {
			for(SummaryGraphElement ele : graph.vertexSet()) {
				String uri = SummaryGraphUtil.getResourceUri(ele);
				uri = SummaryGraphUtil.removeNum(uri);

				HashSet<SummaryGraphElement> set = hm.get(uri);
				if(set == null) set = new  HashSet<SummaryGraphElement>();
				set.add(ele);
				hm.put(uri, set);

				iGraph.addVertex(ele);
			}
			
			for(SummaryGraphEdge edge : graph.edgeSet()) {
				iGraph.addEdge(edge.getSource(), edge.getTarget(), edge);
			}
		}
		
		if (m_datasources == null || m_datasources.size() == 0)
			return null;
		Collection<Mapping> mappings = new ArrayList<Mapping>();
		for (String ds : m_datasources.keySet()) {
			mappings.addAll(index.searchMappingsForDS(ds,
					MappingIndexService.SEARCH_TARGET_AND_SOURCE_DS));
		}
		HashSet<String> conceptMappings = new HashSet<String>();
		for (Mapping m : mappings) {
			String source_uri = SummaryGraphUtil.removeGtOrLs(m.getSource());
			String target_uri = SummaryGraphUtil.removeGtOrLs(m.getTarget());
			conceptMappings.add(m.getSourceDsURI()+source_uri+m.getTargetDsURI()+target_uri);
		}
		
		for (Mapping m : mappings) {
			String source_uri = SummaryGraphUtil.removeGtOrLs(m.getSource());
			String target_uri = SummaryGraphUtil.removeGtOrLs(m.getTarget());
//			if(source_uri.equals(target_uri)) continue;

			HashSet<SummaryGraphElement> source = hm.get(source_uri);
			HashSet<SummaryGraphElement> target = hm.get(target_uri);

			
			if(source != null && target != null) {
				for(SummaryGraphElement s: source)
					label1:
					for(SummaryGraphElement t : target)
					{
						label2:
						if(s.getType()==SummaryGraphElement.RELATION && t.getType()==SummaryGraphElement.RELATION)
						{
							boolean flag = false;
							label3:
							for(SummaryGraphElement sElem: getElem(SummaryGraphEdge.DOMAIN_EDGE, iGraph, s))
								for(SummaryGraphElement tElem: getElem(SummaryGraphEdge.DOMAIN_EDGE, iGraph, t))
									if(conceptMappings.contains(sElem.getDatasource()+SummaryGraphUtil.getResourceUri(sElem)+tElem.getDatasource()+SummaryGraphUtil.getResourceUri(tElem)))
										{flag = true; break label3;}
							if(flag)
							for(SummaryGraphElement sElem: getElem(SummaryGraphEdge.RANGE_EDGE, iGraph, s))
								for(SummaryGraphElement tElem: getElem(SummaryGraphEdge.RANGE_EDGE, iGraph, t))
									if(conceptMappings.contains(sElem.getDatasource()+SummaryGraphUtil.getResourceUri(sElem)+tElem.getDatasource()+SummaryGraphUtil.getResourceUri(tElem)))
										break label2;
							//System.out.println("gua le!!!"+SummaryGraphUtil.getResourceUri(s)+"\t"+SummaryGraphUtil.getResourceUri(t));
							continue label1;
						}
						
						if(s.getType()==SummaryGraphElement.RELATION && t.getType()==SummaryGraphElement.RELATION)
							System.out.println("nb le!!!"+SummaryGraphUtil.getResourceUri(s)+"\t"+SummaryGraphUtil.getResourceUri(t));
						SummaryGraphEdge edge = new SummaryGraphEdge(s, t, SummaryGraphEdge.MAPPING_EDGE);
						iGraph.addEdge(s, t, edge);
					}

			}
			
//			if(source != null && target != null) {
//				SummaryGraphEdge edge = new SummaryGraphEdge(source, target, SummaryGraphEdge.MAPPING_EDGE);
//				iGraph.addEdge(source, target, edge);
//			}
		}
		return iGraph;
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

	public Set<String> getSuggestion(List<String> concept, String ds,
			MappingIndexService index) throws Exception {
		HashMap<String, String> mappedConcept = new HashMap<String, String>();
		Collection<Mapping> mapping;
		for (String con : concept) {
			mapping = index.searchMappings(con, ds,
					MappingIndexService.SEARCH_SOURCE);
			for (Mapping map : mapping)
				mappedConcept.put(map.getTarget(), map.getTargetDsURI());
			mapping = index.searchMappings(con, ds,
					MappingIndexService.SEARCH_TARGET);
			for (Mapping map : mapping)
				mappedConcept.put(map.getSource(), map.getSourceDsURI());
		}
		SummaryGraphIndexServiceForBTFromNT sss = new SummaryGraphIndexServiceForBTFromNT();
		Pseudograph<SummaryGraphElement, SummaryGraphEdge> graph;
		Set<String> res = new HashSet<String>();
		for (String uri : mappedConcept.keySet()) {
			String datasource = mappedConcept.get(uri);
			String objFile = SearchQ2SemanticService.schemaObjSet
					.get(datasource);// SesameDao.root +
										// datasource+"-schema.obj";
			graph = sss.readGraphIndexFromFile(objFile);
			for (SummaryGraphEdge edge : graph.edgeSet()) {
				SummaryGraphElement source = edge.getSource(), target = edge
						.getTarget();
				if (source.getResource() instanceof NamedConcept
						&& ((NamedConcept) source.getResource()).getUri()
								.equals(uri)
						&& target.getType() == SummaryGraphElement.RELATION) {
					res.add(((NamedConcept) source.getResource()).getUri()
							+ "\t" + source.getDatasource() + "\t"
							+ source.getTotalScore() + "\t"
							+ SearchQ2SemanticService.ConceptMark);
					res.add(((Property) target.getResource()).getUri() + "\t"
							+ target.getDatasource() + "\t"
							+ target.getTotalScore() + "\t"
							+ SearchQ2SemanticService.PredicateMark);
				} else if (target.getResource() instanceof NamedConcept
						&& ((NamedConcept) target.getResource()).getUri()
								.equals(uri)
						&& source.getType() == SummaryGraphElement.RELATION) {
					res.add(((NamedConcept) target.getResource()).getUri()
							+ "\t" + target.getDatasource() + "\t"
							+ target.getTotalScore() + "\t"
							+ SearchQ2SemanticService.ConceptMark);
					res.add(((Property) source.getResource()).getUri() + "\t"
							+ source.getDatasource() + "\t"
							+ source.getTotalScore() + "\t"
							+ SearchQ2SemanticService.PredicateMark);
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
			Pseudograph<SummaryGraphElement, SummaryGraphEdge> iGraph,
			Map<String, Collection<SummaryGraphElement>> elements,
			int distance, int k) {

		double max = Double.MAX_VALUE;
		ExpansionQueue expansionQueue = new ExpansionQueue(elements);

		List<Subgraph> subgraphList = new LinkedList<Subgraph>();
		
//		List<Subgraph> output_list = new LinkedList<Subgraph>();

		Set<String> keywords = elements.keySet();
		HashMap<String, HashSet<SummaryGraphElement>> map = new HashMap<String, HashSet<SummaryGraphElement>>();
		for (String keyword : keywords) {
			for (SummaryGraphElement element : elements.get(keyword)) {
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
					neighbors = getNonVisitedNeighbors(iGraph, e, c);
					// System.out.println(neighbors.size());
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
			Pseudograph<SummaryGraphElement, SummaryGraphEdge> iGraph,
			SummaryGraphElement e, Cursor c) {// System.out.println(c.getLength());
		Collection<Cursor> neighbors = new ArrayList<Cursor>();
		// == chenjunquan ==
		//if(e.getDatasource().equals("freebase")) System.out.println("freebase element!");
		Set<SummaryGraphEdge> edges = null;
		try {
			edges = iGraph.edgesOf(e);
		}
		catch (Exception eff) {
			return new ArrayList<Cursor>();
		}
		Cursor nextCursor = null;
		for (SummaryGraphEdge edge : edges) {
			// incoming
			if (edge.getTarget().equals(e)) {
//				System.out.println(SummaryGraphUtil.getResourceUri(edge.getSource())+"\t"+edge.getSource().getTotalScore());
				SummaryGraphElement source = edge.getSource();
				//if (!c.hasVisited(edge)) {
					if (edge.getEdgeLabel().equals(SummaryGraphEdge.MAPPING_EDGE)) {
						nextCursor = new Cursor(source, c.getMatchingElement(),
								edge, c, c.getKeyword(),
								// need to multiply with coverage
								source.getTotalScore());
						// if(source.getTotalScore()==0&&source.getType()==0)System.out.println(((NamedConcept)source.getResource()).getUri());
					}
					else {
						nextCursor = new Cursor(source, c.getMatchingElement(),
								edge, c, c.getKeyword(),
								// need to multiply with coverage
								source.getTotalScore() + c.getCost());
						// if(source.getTotalScore()==0&&source.getType()==0)System.out.println(((NamedConcept)source.getResource()).getUri());
					}
					neighbors.add(nextCursor);
				//}
				// else System.out.println("aaa");
			}
			// outgoing
			else if (edge.getSource().equals(e)) {
//				System.out.println(SummaryGraphUtil.getResourceUri(edge.getTarget())+"\t"+edge.getTarget().getTotalScore());
				SummaryGraphElement target = edge.getTarget();
				//if (!c.hasVisited(edge)) {

					if (edge.getEdgeLabel().equals(SummaryGraphEdge.MAPPING_EDGE)) {
						nextCursor = new Cursor(target, c.getMatchingElement(),
								edge, c, c.getKeyword(),
								// need to multiply with coverage
								target.getTotalScore());
					}
					else {
						nextCursor = new Cursor(target, c.getMatchingElement(),
								edge, c, c.getKeyword(),
								// need to multiply with coverage
								target.getTotalScore() + c.getCost());
					}

					neighbors.add(nextCursor);
				//}
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
