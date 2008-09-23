package org.ateam.xxplore.core.service.search;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
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
import org.xmedia.oms.model.impl.NamedConcept;
import org.xmedia.oms.model.impl.Property;
import org.xmedia.oms.query.ConceptMemberPredicate;
import org.xmedia.oms.query.OWLPredicate;
import org.xmedia.oms.query.PropertyMemberPredicate;
import org.xmedia.oms.query.Variable;

public class QueryInterpretationService implements IQueryInterpretationService {

	private static Logger s_log = Logger.getLogger(QueryInterpretationService.class);

	private WeightedPseudograph<SummaryGraphElement,SummaryGraphEdge> resourceGraph;

	//store datasources and coverage
	private Map<String, Integer> m_datasources = new HashMap<String, Integer>();

	private Set<SummaryGraphElement> m_startingElements = new HashSet<SummaryGraphElement>();

	private Map<String, Pseudograph<SummaryGraphElement, SummaryGraphEdge>> m_DsGraphMap = new HashMap<String, Pseudograph<SummaryGraphElement,SummaryGraphEdge>>(); 

	public void callService(IServiceListener listener, Object... params) {
		// TODO Auto-generated method stub
	}

	public void disposeService() {
		// TODO Auto-generated method stub
	}

	public void init(Object... params) {
		// TODO Auto-generated method stub
	}

	public static void main(String[] args){

		QueryInterpretationService inter = new QueryInterpretationService();
		SesameDao.root = "D:\\semplore\\";
		LinkedList<Subgraph> res = inter.computeQueries(new KeywordIndexServiceForBT("D:\\semplore\\wordnet-keywordIndex",false).searchKb("word net", 0),null,10,10);
//		System.out.println(res==null);
//		for(Map<String,Collection<OWLPredicate>> map: res)
//		{
//		System.out.println("=====================");
//		for(String name: map.keySet())
//		{
//		System.out.println("<<<"+name);
//		Collection<OWLPredicate> preds = map.get(name);
//		for(OWLPredicate pred: preds)
//		System.out.println(pred);
//		}
//		}

	}

	public LinkedList<Subgraph> computeQueries(Map<String,Collection<SummaryGraphElement>> elements, 
			MappingIndexService index, int distance, int k) {

		if (elements == null) return null;

//		for(Collection<SummaryGraphElement> elemCol: elements.values())
//		{
//		for(SummaryGraphElement elem: elemCol)
//		if(elem instanceof SummaryGraphValueElement)
//		System.out.println(((SummaryGraphValueElement)elem).getNeighbors()==null);
//		}
//		Collection<Map<String,Collection<OWLPredicate>>> results = new ArrayList<Map<String,Collection<OWLPredicate>>>();


		Collection<Pseudograph<SummaryGraphElement, SummaryGraphEdge>> sumGraphs = retrieveSummaryGraphs(elements); 
		if(sumGraphs == null)
			return null;
//		for(Pseudograph<SummaryGraphElement, SummaryGraphEdge> graph: sumGraphs)
//		{
//		System.out.println("===================");
//		for(SummaryGraphElement elem: graph.vertexSet())
//		System.out.println("aa");
//		}
		getAugmentedSummaryGraphs(sumGraphs, elements);
//		System.out.println("============");
//		System.out.println(sumGraphs.size());
//		==============by kaifengxu
//		resourceGraph = (WeightedPseudograph<SummaryGraphElement, SummaryGraphEdge>) ((ArrayList)sumGraphs).get(0);
		resourceGraph = getIntegratedSummaryGraph(sumGraphs, index);
		if(resourceGraph == null)
			return null;

		Collection<Subgraph> subgraphs = getTopKSubgraphs(resourceGraph, elements, distance, k);

		if((subgraphs == null) || (subgraphs.size() == 0)) 
			return null;
		return (LinkedList<Subgraph>)subgraphs;
//		for (Subgraph g : subgraphs){
//		Map<String,Collection<OWLPredicate>> query = new HashMap<String, Collection<OWLPredicate>>();
//		Collection<QueryGraph> qGraphs = getQuerygraphs(g);{
//		if(qGraphs == null || qGraphs.size() == 0){
//		for(QueryGraph qg : qGraphs){
//		query.put(qg.getDatasource(), computeQuery(qg));
//		}
//		}
//		}
//		results.add(query);
//		}

//		return results;
	}

	private Collection<Pseudograph<SummaryGraphElement, SummaryGraphEdge>>retrieveSummaryGraphs(Map<String, Collection<SummaryGraphElement>> elements){
		if (elements == null || elements.size() == 0) return null;
		Collection<Pseudograph<SummaryGraphElement, SummaryGraphEdge>> result = new ArrayList<Pseudograph<SummaryGraphElement,SummaryGraphEdge>>();
		Collection<Collection<SummaryGraphElement>> gElements = elements.values();
		//retrieve data source URI 
		for (Collection<SummaryGraphElement> c : gElements){
			for (SummaryGraphElement e : c){
				String dsURI = e.getDatasource();
				//store and update ds coverage
				updateDsCoverage(dsURI);
//				============================by kaifengxu
				if(m_DsGraphMap.containsKey(dsURI))
					continue;
				String dsDFileName = SesameDao.root+dsURI+"-summary.obj";
//				String dsDFileName = IndexingDatawebService.getSummaryGraphFilePath(dsURI);

				File graphIndex = new File(dsDFileName);
				ObjectInputStream in;
				Pseudograph<SummaryGraphElement, SummaryGraphEdge> graph = null;
				try {
					in = new ObjectInputStream(new FileInputStream(graphIndex));
					graph = (Pseudograph<SummaryGraphElement, SummaryGraphEdge>)in.readObject();
//					================by kaifengxu
					for(SummaryGraphElement elem: graph.vertexSet())
						elem.setDatasource(dsURI);
					result.add(graph); 
					in.close();
				} catch (FileNotFoundException ex) {
					ex.printStackTrace();
				} catch (IOException ex) {
					ex.printStackTrace();
				} catch (ClassNotFoundException ex) {
					ex.printStackTrace();
				}
				if(graph != null) m_DsGraphMap.put(dsURI, graph);
			}
		}		
		return result;
	}

	private void updateDsCoverage(String ds){
		Integer cover = m_datasources.get(ds);
		if (cover != null){
			cover = new Integer(cover.intValue() + 1);
		}
		else m_datasources.put(ds, new Integer(1));
	}

	private int getDsCoverage(SummaryGraphElement e){
		String ds = e.getDatasource();
//		System.out.println(ds);
//		Emergency.checkPrecondition(ds != null, "No datasource stored for element:" + e);
		return m_datasources.get(ds).intValue();
	}

	private void getAugmentedSummaryGraphs(Collection<Pseudograph<SummaryGraphElement, SummaryGraphEdge>> graphs, Map<String, Collection<SummaryGraphElement>> keywords){
		if(graphs == null || graphs.size() == 0) return;
		Set<String> keys = keywords.keySet();
		for (String key : keys){
			m_startingElements.addAll(keywords.get(key));
		}			
		for (Pseudograph<SummaryGraphElement, SummaryGraphEdge> graph : graphs){
			for (SummaryGraphElement e : m_startingElements){
				if (e instanceof SummaryGraphValueElement){
//					=============by kaifengxu
					String ds = e.getDatasource();
					Map<IDataProperty, Collection<INamedConcept>> neighbors = ((SummaryGraphValueElement)e).getNeighbors();	
					Set<IDataProperty> props = neighbors.keySet();
					Iterator<IDataProperty> propIter = props.iterator();
					while (propIter.hasNext()){
						IDataProperty prop = propIter.next();
						SummaryGraphElement pvertex = new SummaryGraphElement(prop, SummaryGraphElement.ATTRIBUTE);
						Collection<INamedConcept> cons = neighbors.get(prop);	
						Iterator<INamedConcept> conIter = cons.iterator();
						while (conIter.hasNext()){
							INamedConcept con = conIter.next();
							SummaryGraphElement cvertex = new SummaryGraphElement(con,SummaryGraphElement.CONCEPT);
//							Emergency.checkPrecondition(graph.containsVertex(cvertex), "Classvertex must be contained in summary graph:" + cvertex.toString());
							SummaryGraphEdge domain = new SummaryGraphEdge(cvertex, pvertex, SummaryGraphEdge.DOMAIN_EDGE);
							SummaryGraphEdge range = new SummaryGraphEdge(pvertex, (SummaryGraphElement)e, SummaryGraphEdge.RANGE_EDGE);
//							=============by kaifengxu
//							System.out.println(domain.getSource().getResource().getClass());
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
				if (e instanceof SummaryGraphAttributeElement){
					Collection<INamedConcept> cons = ((SummaryGraphAttributeElement)e).getNeighborConcepts();	
					Iterator<INamedConcept> conIter = cons.iterator();
//					=============by kaifengxu
					String ds = e.getDatasource();
					while (conIter.hasNext()){
						INamedConcept con = conIter.next();
						SummaryGraphElement cvertex = new SummaryGraphElement(con,SummaryGraphElement.CONCEPT);
//						Emergency.checkPrecondition(graph.containsVertex(cvertex), "Classvertex must be contained in summary graph:" + cvertex.toString());
						SummaryGraphEdge domain = new SummaryGraphEdge(cvertex, (SummaryGraphAttributeElement)e, SummaryGraphEdge.DOMAIN_EDGE);
//						=============by kaifengxu
						cvertex.setDatasource(ds);
						graph.addVertex(cvertex);
						graph.addVertex(e);
						graph.addEdge(cvertex, (SummaryGraphAttributeElement)e, domain);
					}
				}
//				System.out.println(e.getDatasource());
				updateScore(graph, e, m_startingElements);
			}
		}
	}

	private WeightedPseudograph<SummaryGraphElement, SummaryGraphEdge> getIntegratedSummaryGraph(Collection<Pseudograph<SummaryGraphElement, SummaryGraphEdge>> graphs, MappingIndexService index){
//		if(m_datasources == null || m_datasources.size() == 0) return null;
//		Collection<Mapping> mappings = new ArrayList<Mapping>();
//		for(String ds : m_datasources.keySet()){
//		mappings.addAll(index.searchMappingsForDS(ds, MappingIndexService.SEARCH_SOURCE_DS_ONLY));
//		}

//		if (mappings.size() == 0) return null;
		WeightedPseudograph<SummaryGraphElement, SummaryGraphEdge> iGraph = new WeightedPseudograph<SummaryGraphElement, SummaryGraphEdge>(SummaryGraphEdge.class);
//		Collection<Pseudograph<SummaryGraphElement, SummaryGraphEdge>> addedGraphs = new ArrayList<Pseudograph<SummaryGraphElement, SummaryGraphEdge>>(); 

//		for (Mapping m : mappings){
//		Pseudograph<SummaryGraphElement, SummaryGraphEdge> sourceGraph = m_DsGraphMap.get(m.getSourceDsURI());
//		Pseudograph<SummaryGraphElement, SummaryGraphEdge> targetGraph = m_DsGraphMap.get(m.getTargetDsURI());
//		if(!addedGraphs.contains(sourceGraph)){
//		for(SummaryGraphElement v : sourceGraph.vertexSet()){
//		iGraph.addVertex(v);	
//		}
//		for(SummaryGraphEdge e : sourceGraph.edgeSet()){
//		iGraph.addEdge(e.getSource(), e.getTarget(), e);
//		}
//		}
//		addedGraphs.add(sourceGraph);
//		if(!addedGraphs.contains(targetGraph)){
//		for(SummaryGraphElement v : targetGraph.vertexSet()){
//		iGraph.addVertex(v);	
//		}
//		for(SummaryGraphEdge e : targetGraph.edgeSet()){
//		iGraph.addEdge(e.getSource(), e.getTarget(), e);
//		}
//		}
//		addedGraphs.add(targetGraph);

//		SummaryGraphElement source = getVertex(sourceGraph, m.getSource());
//		SummaryGraphElement target = getVertex(sourceGraph, m.getTarget());
//		SummaryGraphEdge iEdge = new SummaryGraphEdge(source, target, SummaryGraphEdge.MAPPING_EDGE);
//		iGraph.addEdge(source, target, iEdge);
//		iGraph.setEdgeWeight(iEdge, m.getConfidence());
//		}
//		==================by kaifengxu
		for(Pseudograph<SummaryGraphElement, SummaryGraphEdge> graph: graphs)
		{
			for(SummaryGraphElement v : graph.vertexSet()){
				if(v.getResource().getClass().toString().contains("org.xmedia.oms.model.impl.Literal"))
					if(v.getResource().toString().contains("catch with a net"))System.out.println(v.getResource().toString());;
				iGraph.addVertex(v);	
			}
			for(SummaryGraphEdge e : graph.edgeSet()){
				iGraph.addEdge(e.getSource(), e.getTarget(), e);
			}
		}
		return iGraph;
	}

	public Set<String> getSuggestion(List<String> concept, String ds, MappingIndexService index) throws Exception
	{
		HashMap<String, String> mappedConcept = new HashMap<String, String>();
		Collection<Mapping> mapping;
		for(String con: concept)
		{
			mapping = index.searchMappings(con, ds, MappingIndexService.SEARCH_SOURCE);
			for(Mapping map: mapping)
				mappedConcept.put(map.getTarget(), map.getTargetDsURI());
			mapping = index.searchMappings(con, ds, MappingIndexService.SEARCH_TARGET);
			for(Mapping map: mapping)
				mappedConcept.put(map.getSource(), map.getSourceDsURI());
		}
		SummaryGraphIndexServiceForBT sss = new SummaryGraphIndexServiceForBT();
		Pseudograph<SummaryGraphElement, SummaryGraphEdge> graph;
		Set<String> res = new HashSet<String>();
		for(String uri: mappedConcept.keySet())
		{
			String datasource = mappedConcept.get(uri);
			String objFile = SesameDao.root + datasource+"-schema.obj";
			graph = sss.readGraphIndexFromFile(objFile);
			for(SummaryGraphEdge edge: graph.edgeSet())
			{
				SummaryGraphElement source = edge.getSource(), target = edge.getTarget();
				if(source.getResource() instanceof NamedConcept && ((NamedConcept)source.getResource()).getUri().equals(uri)
						&& target.getType()==SummaryGraphElement.RELATION)
				{
					res.add(((NamedConcept)source.getResource()).getUri()+"\t"+source.getDatasource()+"\t"+source.getTotalScore()+"\tc");
					res.add(((Property)target.getResource()).getUri()+"\t"+target.getDatasource()+"\t"+target.getTotalScore()+"\tp");
				}
				else if(target.getResource() instanceof NamedConcept && ((NamedConcept)target.getResource()).getUri().equals(uri)
						&& source.getType()==SummaryGraphElement.RELATION)
				{
					res.add(((NamedConcept)target.getResource()).getUri()+"\t"+target.getDatasource()+"\t"+target.getTotalScore()+"\tc");
					res.add(((Property)source.getResource()).getUri()+"\t"+source.getDatasource()+"\t"+source.getTotalScore()+"\tp");
				}
			}
		}
		return res;
	}

	//TODO this is not so efficient when graph is huge...
	private void updateScore(Pseudograph<SummaryGraphElement, SummaryGraphEdge> graph, SummaryGraphElement e, Collection<SummaryGraphElement> keywords){

		Set<SummaryGraphElement> vertices = graph.vertexSet();
		if (vertices != null && vertices.size() > 0){
			for (SummaryGraphElement v : vertices){
				if(v.equals(e)){
					//get score as stored in keyword elements 
					double score = -1;
					for (SummaryGraphElement k : keywords){
						if (k.equals(e));
						score = k.getMatchingScore();
					}
					// score = 1 / (EF/IDF*matchingscore)
					v.setTotalScore(1/(v.getEF() * score));
//					System.out.println(v.getResource());
					v.applyCoverage(getDsCoverage(v));
				}
			}
		}
	}

	//TODO this is not so efficient when graph is huge...
	private SummaryGraphElement getVertex(Pseudograph<SummaryGraphElement, SummaryGraphEdge> graph, String URI){
		Set<SummaryGraphElement> vertices = graph.vertexSet();
		if (vertices != null && vertices.size() > 0){
			for (SummaryGraphElement v : vertices){
				IResource res = v.getResource();
				if(res instanceof IEntity){
					if(((IEntity)res).getUri() == URI) return v;
				}
			}
		}
		return null;
	}

	private Collection<Subgraph> getTopKSubgraphs(WeightedPseudograph<SummaryGraphElement, SummaryGraphEdge> iGraph, 
			Map<String,Collection<SummaryGraphElement>> elements, int distance, int k){

		ExpansionQueue expansionQueue = new ExpansionQueue(elements);

		List<Subgraph> subgraphList  = new LinkedList<Subgraph>();

		Set<String> keywords = elements.keySet();
		for(String keyword : keywords){
			for(SummaryGraphElement element : elements.get(keyword)){
				Cursor cursor = new Cursor(element, element, null, null, keyword, element.getTotalScore());
				expansionQueue.addCursor(cursor, keyword);
			}
		}

//		System.out.println(expansionQueue.isEmpty());
		while (!expansionQueue.isEmpty()){
			Cursor c = expansionQueue.pollMinCostCursor();

			if(c.getLength() < distance){
				SummaryGraphElement e = c.getElement();
//				System.out.println(e.getResource().getClass());
				String keyword = c.getKeyword();
				if(e.getCursors() == null) e.initCursorQueues(keywords);
				e.addCursor(c, keyword);

				// add new subgraphs 
				if(e.isConnectingElement()){
					Set<Set<Cursor>> combinations = e.getNewCursorCombinations();
					if(combinations != null && combinations.size() != 0){
						e.addExploredCursorCombinations(combinations); 
						e.clearNewCursorCombinations();
						subgraphList.addAll(computeSubgraphs(e,combinations));

						//check for top-k
						if(subgraphList.size() >= k){
							Collections.sort(subgraphList);
							if (subgraphList.get(k).getCost() < expansionQueue.getApproximateMinCostOfCandidates())
								return subgraphList;
						}
					}
				}

				//add cursors to queue 
				Set<Cursor> neighbors = getNonVisitedNeighbors(iGraph, e, c);
				for (Cursor n : neighbors){
					expansionQueue.addCursor(n, keyword);
				}

			}
		}

		return subgraphList;
	}

	private Collection<Subgraph> computeSubgraphs(SummaryGraphElement connectingElement, Set<Set<Cursor>> cursors){
		Collection<Subgraph> subgraphs = new HashSet<Subgraph>();
		for(Set<Cursor> cursorsOfSubgraph : cursors){
			Set<List<SummaryGraphEdge>> paths = new HashSet<List<SummaryGraphEdge>>();
			double cost = 0; 
			for(Cursor cursor : cursorsOfSubgraph){
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

	private Collection<QueryGraph> getQuerygraphs(Subgraph graph){
		if(graph == null) return null;
		Collection<QueryGraph> graphs = new ArrayList<QueryGraph>();

		ConnectivityInspector<SummaryGraphElement, SummaryGraphEdge> connectedInsp = new ConnectivityInspector<SummaryGraphElement, SummaryGraphEdge>(graph);
		Emergency.checkPrecondition(connectedInsp.isGraphConnected(), "Computed Subgraphs must be weakly connected!!!");

		//split: remove all mapping edges
		for (SummaryGraphEdge e: graph.edgeSet()){
			if (e.getEdgeLabel() == SummaryGraphEdge.MAPPING_EDGE)
				graph.removeEdge(e);
		}		

		//build graphs from maximal connected component
		//each connected component correspond to one datasource 
		ConnectivityInspector<SummaryGraphElement, SummaryGraphEdge> compInsp = new ConnectivityInspector<SummaryGraphElement, SummaryGraphEdge>(graph);
		List<Set<SummaryGraphElement>> components = compInsp.connectedSets();
		for(Set<SummaryGraphElement> comp : components){
			QueryGraph qg = new QueryGraph();
			Collection<QueryGraphEdge> qgEdges = new ArrayList<QueryGraphEdge>();
			for(SummaryGraphElement e : comp){
				//relation and attribute elements should have exactly one incoming and one outgoing edge (since mapping edge has been removed)
				if(e.getType() == SummaryGraphElement.RELATION || e.getType() == SummaryGraphElement.ATTRIBUTE){
					Set<SummaryGraphEdge> edges = graph.edgesOf(e);
					Emergency.checkPrecondition(edges.size() == 2,"relation and attribute elements should have exactly one incoming and one outgoing edge!");
					SummaryGraphEdge inEdge = null;
					SummaryGraphEdge outEdge = null;
					while (edges.iterator().hasNext()){
						SummaryGraphEdge edge = edges.iterator().next();
						if(edge.getTarget().equals(e)){
							inEdge = edge;
						}
						else if(edge.getSource().equals(e)){
							outEdge = edge;
						}
					}
					qgEdges.add(new QueryGraphEdge(inEdge.getSource().getResource(), 
							outEdge.getTarget().getResource(), (IProperty)e.getResource(), e.getType()));
				}
			}

			qg.setEdges(qgEdges);
			//each connected component correspond to one datasource, i.e. every elements of one component belongs to same ds
			qg.setDatasource(comp.iterator().next().getDatasource());
			graphs.add(qg);
		}
		return graphs;
	}

	private Set<Cursor> getNonVisitedNeighbors(WeightedPseudograph<SummaryGraphElement, SummaryGraphEdge> iGraph, SummaryGraphElement e, Cursor c)
	{
		Set<Cursor> neighbors = new HashSet<Cursor>();
		Set<SummaryGraphEdge> edges = iGraph.edgesOf(e);
		Cursor nextCursor = null;
		for (SummaryGraphEdge edge : edges){
			//incoming 
			if(edge.getTarget().equals(e)){
				SummaryGraphElement source = edge.getSource();
				if(!c.hasVisited(source)) {
					if(edge.getEdgeLabel() == SummaryGraphEdge.MAPPING_EDGE){
						// update score: (EF/EDF*matchingscore) + mappingScore
						source.setTotalScore(source.getTotalScore() + (iGraph.getEdgeWeight(edge)));
					}
					nextCursor = new Cursor(source, c.getMatchingElement(), edge, c, c.getKeyword(), 
							//need to multiply with coverage
							source.getTotalScore() + c.getCost());

					neighbors.add(nextCursor);
				}
			}
			//outgoing
			else{	
				SummaryGraphElement target = edge.getTarget();
				if(!c.hasVisited(target)) {
					if(edge.getEdgeLabel() == SummaryGraphEdge.MAPPING_EDGE){
						// update score: (EF/EDF*matchingscore) +  mappingScore
						target.setTotalScore(target.getTotalScore() + iGraph.getEdgeWeight(edge));
					}
					nextCursor = new Cursor(target, c.getMatchingElement(), edge, c, c.getKeyword(), 
							//need to multiply with coverage
							target.getTotalScore() + c.getCost());

					neighbors.add(nextCursor);
				}
			}

		}

		return neighbors;
	}



	private Collection<OWLPredicate> computeQuery(QueryGraph subgraph) {
		Collection<OWLPredicate>  query  = new LinkedHashSet<OWLPredicate>();
		UniqueIdGenerator.getInstance().resetVarIds();
		for(QueryGraphEdge edge : subgraph.getEdges()){
			Map<Pair, Variable> labelVar = new LinkedHashMap<Pair, Variable>();
			IResource target = edge.getTarget();

			if(edge.getType() == SummaryGraphElement.ATTRIBUTE && 
					!(target.getLabel() == SummaryGraphElement.DUMMY_VALUE_LABEL)){
				IProperty p = edge.getProperty();
				IResource source  = edge.getSource();
				IResource t;
				String conceptLabel = source.getLabel();
				String literalLabel = target.getLabel();
				Pair label = new Pair(conceptLabel,literalLabel);
				Variable var = labelVar.get(label); 
				if (var == null) {
					t = getNewVariable();
					labelVar.put(label, (Variable)t);
					query.add(new ConceptMemberPredicate(source, t));
				} else {
					t = var;
				}

				query.add(new PropertyMemberPredicate(p, t,target));
			}

			else if(edge.getType() == SummaryGraphElement.ATTRIBUTE && 
					target.getLabel() == SummaryGraphElement.DUMMY_VALUE_LABEL){
				IProperty p = edge.getProperty();
				IResource v1 = edge.getSource();

				Set<IResource> t1 = new LinkedHashSet<IResource>();
				String label1 = v1.getLabel();
				for(Pair pair : labelVar.keySet()){
					if(pair.getHead().equals(label1)) {
						t1.add(labelVar.get(pair));
					}
				}
				if(t1.size() == 0) {
					Variable var = getNewVariable();
					t1.add(var);
					labelVar.put(new Pair(label1,null), var);
					query.add(new ConceptMemberPredicate(v1, var));
				}

				for(IResource resource1 : t1){
					query.add(new PropertyMemberPredicate(p, resource1,getNewVariable()));
				}
			}

			else if(edge.getType() == SummaryGraphElement.RELATION && 
					!(edge.getProperty().equals(Property.SUBCLASS_OF))){
				IProperty p = edge.getProperty();
				IResource v1 = edge.getSource();

				Set<IResource> t1 = new LinkedHashSet<IResource>();
				String label1 = v1.getLabel();
				for(Pair pair : labelVar.keySet()){
					if(pair.getHead().equals(label1)) {
						t1.add(labelVar.get(pair));
					}
				}
				if(t1.size() == 0) {
					Variable var = getNewVariable();
					t1.add(var);
					labelVar.put(new Pair(label1,null), var);
					query.add(new ConceptMemberPredicate(v1, var));
				}


				Set<IResource> t2 = new LinkedHashSet<IResource>();
				String label2 = target.getLabel();
				for(Pair pair : labelVar.keySet()){
					if(pair.getHead().equals(label2)) {
						t2.add(labelVar.get(pair));
					}
				}
				if(t2.size() == 0) {
					Variable var = getNewVariable();
					t2.add(var);
					labelVar.put(new Pair(label2,null), var);
					query.add(new ConceptMemberPredicate(target, var));
				}

				for(IResource resource1 : t1){
					for(IResource resource2 : t2) {
						query.add(new PropertyMemberPredicate(p, resource1,resource2));
					}
				}

			}

		}
		return query;
	}

	private Variable getNewVariable(){
		String var = String.valueOf(UniqueIdGenerator.getInstance().getNewVarId());
		return new Variable("x" + var);
	}


	private class QueryGraph{
		Collection<QueryGraphEdge> edges;

		String datasource; 

		public QueryGraph(){};

		public QueryGraph (Collection<QueryGraphEdge> edges){
			this.edges = edges;
		}

		public void setEdges(Collection<QueryGraphEdge> edges){
			this.edges = edges;
		}

		public void setDatasource(String ds){
			datasource = ds;
		}

		public String getDatasource(){
			return datasource;
		}

		public Collection<QueryGraphEdge> getEdges(){
			return edges;
		}
	}


	private class QueryGraphEdge{
		IResource source;
		IResource target;
		IProperty edge;
		int type; 

		public QueryGraphEdge(IResource source, IResource target, IProperty edge, int type){
			this.source = source;
			this.target = target;
			this.edge = edge;
			this.type = type;
		}

		public int getType(){
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
		public IProperty getProperty(){
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


	private class Subgraph extends WeightedPseudograph<SummaryGraphElement, SummaryGraphEdge> implements Comparable {

		private SummaryGraphElement connectingVertex;

		private Set<List<SummaryGraphEdge>> paths;

		private boolean pathIsSet = false;

		double cost;

		public Subgraph(Class<? extends SummaryGraphEdge> edgeclass){
			super(edgeclass);
		}

		public Set<List<SummaryGraphEdge>> getPaths(){
			return paths;
		}

		public void setPaths(Set<List<SummaryGraphEdge>> paths){
			Emergency.checkPrecondition(!pathIsSet, "Set path can be invoked only once to initialized the graph!!!");
			if (paths == null || paths.size() == 0) return;
			for (List<SummaryGraphEdge> path : paths){
				if(path.size() == 0) continue;
				for(SummaryGraphEdge e : path){
					addVertex(e.getSource());
					addVertex(e.getTarget());
					addEdge(e.getSource(), e.getTarget(), e);
				}
			}
		}

		public SummaryGraphElement getConnectingVertex(){
			return connectingVertex;
		}

		public void setConnectingElement(SummaryGraphElement connectingE){
			connectingVertex = connectingE;
		}


		public void setCost(double cost){
			this.cost = cost;
		}

		public double getCost(){
			return cost;
		}

		public int compareTo(Object o){
			Subgraph other = (Subgraph)o;
			if(this.cost > other.cost) {
				return -1;
			}
			if(this.cost < other.cost) {
				return 1;
			}
			return 0;
		}

		@Override
		public boolean equals(Object o){
			if(this == o) {
				return true;
			}
			if(!(o instanceof Subgraph)) {
				return false;
			}

			Subgraph other = (Subgraph)o;
			if(!(paths.equals(other.getPaths()))) {
				return false;
			}
			return true;
		}

		@Override
		public int hashCode(){
			return 13*paths.hashCode();
		}

		@Override
		public String toString(){
			return "cost: " + cost 
			+ "\n" + "Connecting vertex: " + connectingVertex
			+ "\n" + "Paths: " + paths
			+ "\n";
		}		
	}	

	private class ExpansionQueue{
		Map<String, PriorityQueue<Cursor>> m_queue;
		Set<String> m_keywords;
		Collection<PriorityQueue<Cursor>> m_queues;

		private ExpansionQueue(Map<String,Collection<SummaryGraphElement>> elements){
			m_queue = new HashMap<String, PriorityQueue<Cursor>>();
			m_keywords = elements.keySet();
			m_queues = new ArrayList<PriorityQueue<Cursor>>();
			for (String k : elements.keySet()){
				PriorityQueue<Cursor> q = new PriorityQueue<Cursor>();
				m_queue.put(k, q);
				m_queues.add(q);
			}
		}

		private void addCursor(Cursor c, String keyword){
			PriorityQueue<Cursor> q = m_queue.get(keyword);
			if(q==null)
			{
				q = new PriorityQueue<Cursor>();
				m_queue.put(keyword, q);
			}
			q.add(c);
		}

		private boolean isEmpty(){
			if (m_queue.isEmpty()) return true;
			for (PriorityQueue<Cursor> q : m_queues){
				if(!q.isEmpty()) return false;
			}
			return true;
		}

		private Cursor pollMinCostCursor(){
			if(m_queues == null || m_queues.size() == 0) return null;
			PriorityQueue<Cursor> minCostQ = null;
			double minCost = 0; 
			for (PriorityQueue<Cursor> q : m_queues){
//				============by kaifengxu
				if(q.peek()==null) continue;
				double cCost = q.peek().getCost();
				if(cCost < minCost || minCost == 0) {
					minCost = cCost;
					minCostQ = q;
				}
			}
			return minCostQ.poll();
		}

		/**
		 * Simple return the cost of the min cost cursor in the queues. 
		 * @return
		 */
		private double getMinCostOfCandidadates(){
			double minCost = -1; 
			if(m_queues == null || m_queues.size() == 0) return minCost;
			for (PriorityQueue<Cursor> q : m_queues){

				double cCost = q.peek().getCost();
				if(cCost < minCost || minCost == 0) {
					minCost = cCost;
				}
			}
			return minCost;
		}


		/**
		 * Approximation. Returns the sum of the cost of the first cursor in each queue
		 * @return
		 */
		private double getApproximateMinCostOfCandidates(){
			double minCost = -1; 
			if(m_queues == null || m_queues.size() == 0) return minCost;
			for (PriorityQueue<Cursor> q : m_queues){
				minCost = + q.peek().getCost();
			}
			return minCost;
		}
	}
}
