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
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import org.aifb.xxplore.shared.exception.Emergency;
import org.apache.log4j.Logger;
import org.ateam.xxplore.core.service.IServiceListener;
import org.ateam.xxplore.core.service.mapping.Mapping;
import org.ateam.xxplore.core.service.mapping.MappingIndexService;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.Pseudograph;
import org.jgrapht.graph.WeightedPseudograph;
import org.jgrapht.traverse.ClosestFirstIterator;
import org.xmedia.oms.model.api.IDataProperty;
import org.xmedia.oms.model.api.IEntity;
import org.xmedia.oms.model.api.INamedConcept;
import org.xmedia.oms.model.api.IResource;
import org.xmedia.oms.query.OWLPredicate;

public class QueryInterpretationService implements IQueryInterpretationService {

	private static Logger s_log = Logger.getLogger(QueryInterpretationService.class);

	private WeightedPseudograph<SummaryGraphElement,SummaryGraphEdge> resourceGraph;

	private Collection<String> m_datasources = new ArrayList<String>();

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

	public Collection<Collection<OWLPredicate>> computeQueries(Map<String,Collection<SummaryGraphElement>> elements, MappingIndexService index, int distance) {

		if (elements == null) return null;

		Collection<Pseudograph<SummaryGraphElement, SummaryGraphEdge>> sumGraphs = retrieveSummaryGraphs(elements);
		getAugmentedSummaryGraphs(sumGraphs, elements);
		resourceGraph = getIntegratedSummaryGraph(sumGraphs, index);

		getTopKSubgraphs(resourceGraph, elements, distance);

//		connectingElements = new LinkedHashSet<SummaryGraphElement>();

//		subgraphs = computeSubgraphs(resources);
//		if((subgraphs == null) || (subgraphs.size() == 0)) {
//		return null;
//		}
//		queries = computeQueries(subgraphs);
//		if((queries == null) || (queries.size() == 0)) {
//		return null;
//		}

		return null;
	}

	private Collection<Pseudograph<SummaryGraphElement, SummaryGraphEdge>>retrieveSummaryGraphs(Map<String, Collection<SummaryGraphElement>> elements){
		if (elements == null || elements.size() == 0) return null;
		Collection<Pseudograph<SummaryGraphElement, SummaryGraphEdge>> result = new ArrayList<Pseudograph<SummaryGraphElement,SummaryGraphEdge>>();
		Collection<Collection<SummaryGraphElement>> gElements = elements.values();
		//retrieve data source URI 
		for (Collection<SummaryGraphElement> c : gElements){
			for (SummaryGraphElement e : c){
				String dsURI = e.getDatasource();
				//store the datasource URI; this will be used later to collect mappings
				m_datasources.add(dsURI);

				String dsDFileName = IndexingDatawebService.getSummaryGraphFilePath(dsURI);
				File graphIndex = new File(dsDFileName);
				ObjectInputStream in;
				Pseudograph<SummaryGraphElement, SummaryGraphEdge> graph = null;
				try {
					in = new ObjectInputStream(new FileInputStream(graphIndex));
					graph = (Pseudograph<SummaryGraphElement, SummaryGraphEdge>)in.readObject();
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


	private void getAugmentedSummaryGraphs(Collection<Pseudograph<SummaryGraphElement, SummaryGraphEdge>> graphs, Map<String, Collection<SummaryGraphElement>> keywords){
		if(graphs == null || graphs.size() == 0) return;
		Set<String> keys = keywords.keySet();
		for (String key : keys){
			m_startingElements.addAll(keywords.get(key));
		}			
		for (Pseudograph<SummaryGraphElement, SummaryGraphEdge> graph : graphs){
			for (SummaryGraphElement e : m_startingElements){
				if (e instanceof SummaryGraphValueElement){
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
							Emergency.checkPrecondition(graph.containsVertex(cvertex), "Classvertex must be contained in summary graph:" + cvertex.toString());
							SummaryGraphEdge domain = new SummaryGraphEdge(cvertex, pvertex, SummaryGraphEdge.DOMAIN_EDGE);
							SummaryGraphEdge range = new SummaryGraphEdge(pvertex, (SummaryGraphElement)e, SummaryGraphEdge.RANGE_EDGE);
							graph.addEdge(domain.getSource(), domain.getTarget(), domain);
							graph.addEdge(range.getSource(), range.getTarget(), range);
						}
					}
				}
				if (e instanceof SummaryGraphAttributeElement){
					Collection<INamedConcept> cons = ((SummaryGraphAttributeElement)e).getNeighborConcepts();	
					Iterator<INamedConcept> conIter = cons.iterator();
					while (conIter.hasNext()){
						INamedConcept con = conIter.next();
						SummaryGraphElement cvertex = new SummaryGraphElement(con,SummaryGraphElement.CONCEPT);
						Emergency.checkPrecondition(graph.containsVertex(cvertex), "Classvertex must be contained in summary graph:" + cvertex.toString());
						SummaryGraphEdge domain = new SummaryGraphEdge(cvertex, (SummaryGraphAttributeElement)e, SummaryGraphEdge.DOMAIN_EDGE);
						graph.addEdge(cvertex, (SummaryGraphAttributeElement)e, domain);
					}
				}
				updateScore(graph, e, m_startingElements);
			}
		}
	}

	private WeightedPseudograph<SummaryGraphElement, SummaryGraphEdge> getIntegratedSummaryGraph(Collection<Pseudograph<SummaryGraphElement, SummaryGraphEdge>> graphs, MappingIndexService index){
		if(m_datasources == null || m_datasources.size() == 0) return null;
		Collection<Mapping> mappings = new ArrayList<Mapping>();
		for(String ds : m_datasources){
			mappings.addAll(index.searchMappingsForDS(ds, MappingIndexService.SEARCH_SOURCE_DS_ONLY));
		}

		if (mappings.size() == 0) return null;
		WeightedPseudograph<SummaryGraphElement, SummaryGraphEdge> iGraph = new WeightedPseudograph<SummaryGraphElement, SummaryGraphEdge>(SummaryGraphEdge.class);
		Collection<Pseudograph<SummaryGraphElement, SummaryGraphEdge>> addedGraphs = new ArrayList<Pseudograph<SummaryGraphElement, SummaryGraphEdge>>(); 

		for (Mapping m : mappings){
			Pseudograph<SummaryGraphElement, SummaryGraphEdge> sourceGraph = m_DsGraphMap.get(m.getSourceDsURI());
			Pseudograph<SummaryGraphElement, SummaryGraphEdge> targetGraph = m_DsGraphMap.get(m.getTargetDsURI());
			if(!addedGraphs.contains(sourceGraph)){
				for(SummaryGraphElement v : sourceGraph.vertexSet()){
					iGraph.addVertex(v);	
				}
				for(SummaryGraphEdge e : sourceGraph.edgeSet()){
					iGraph.addEdge(e.getSource(), e.getTarget(), e);
				}
			}
			addedGraphs.add(sourceGraph);
			if(!addedGraphs.contains(targetGraph)){
				for(SummaryGraphElement v : targetGraph.vertexSet()){
					iGraph.addVertex(v);	
				}
				for(SummaryGraphEdge e : targetGraph.edgeSet()){
					iGraph.addEdge(e.getSource(), e.getTarget(), e);
				}
			}
			addedGraphs.add(targetGraph);

			SummaryGraphElement source = getVertex(sourceGraph, m.getSource());
			SummaryGraphElement target = getVertex(sourceGraph, m.getTarget());
			SummaryGraphEdge iEdge = new SummaryGraphEdge(source, target, SummaryGraphEdge.MAPPING_EDGE);
			iGraph.addEdge(source, target, iEdge);
			iGraph.setEdgeWeight(iEdge, m.getConfidence());
		}
		return iGraph;
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
			Map<String,Collection<SummaryGraphElement>> elements, int distance){
		
		ExpansionQueue expansionQueue = new ExpansionQueue(new HashMap<String, PriorityQueue<Cursor>>());
		PriorityQueue<Subgraph> subgraphQueue  = new PriorityQueue<Subgraph>();

		Set<String> keywords = elements.keySet();
		for(String keyword : keywords){
			for(SummaryGraphElement element : elements.get(keyword)){
				Cursor cursor = new Cursor(element, element, null, keyword, element.getTotalScore());
				expansionQueue.addCursor(cursor, keyword);
			}
		}
		
		while (!expansionQueue.isEmpty()){
			Cursor c = expansionQueue.pollMinCostCursor();
			
			if(c.getLength() < distance){
				SummaryGraphElement e = c.getElement();
				String keyword = c.getKeyword();
				if(e.getCursors() == null) e.initCursorQueues(keywords);
				e.addCursor(c, keyword);
				if(e.isConnectingElement()){
					Cursor[][] combinations = e.getNewCursorCombinations();
					e.addExploredCursorCombinations(combinations); 					
					subgraphQueue.addAll(computeSubgraphs(combinations));
					
				}
				
				//add cursors to queue 
				Set<SummaryGraphElement> neighbors = getNonVisitedNeighbors(iGraph, e, c);
				for (SummaryGraphElement n : neighbors){
					Cursor nextCursor = new Cursor(n, c.getMatchingElement(), c, keyword, 
							//need to multiply with coverage
							n.getTotalScore() + c.getCost());
					expansionQueue.addCursor(nextCursor, keyword);
				}
				
			}
		}
		
		return null;
	}
	
	private Collection<Subgraph> computeSubgraphs(Cursor[][] cursors){
		for(int i = 0; i < cursors.length; i++){
//			Cursor[] curs = cursors[i];
//			edges.addAll(cursor.getPath());
//			
//			boolean isSubgraph = false;
//			if((keywordREdgeMap != null) && (keywordREdgeMap.size() != 0)){
//				for(Collection<ISummaryGraphElement> collection : keywordREdgeMap.values()){
//					isSubgraph = !Collections.disjoint(edges, collection);
//					if(!isSubgraph) {
//						break;
//					}
//				}
//			} else {
//				isSubgraph = true;
//			}
//			if(isSubgraph){
//				Subgraph subgraph = new Subgraph(endVertex, edges, cost);
//				if(!subgraphQueue.contains(subgraph)) {
//					if(subgraphQueue.size() < K_TOP) {
//						subgraphQueue.add(subgraph);
//					} else {
//						double highestCost = subgraphQueue.peek().getCost();
//						if(subgraph.getCost() < highestCost) {
//							subgraphQueue.poll();
//							subgraphQueue.add(subgraph);
//						}
//					}	
//				}
//				else {
//					for(Subgraph sub : subgraphQueue){
//						if(sub.equals(subgraph)){
//							if(sub.getCost() > subgraph.getCost())
//								sub.setCost(subgraph.getCost());
//						}
//					}
//				}
//			}
		}
		return null;
	}


	private Set<SummaryGraphElement> getNonVisitedNeighbors(WeightedPseudograph<SummaryGraphElement, SummaryGraphEdge> iGraph, SummaryGraphElement e, Cursor c)
	{
		Set<SummaryGraphElement> neighbors = new HashSet<SummaryGraphElement>();
		Set<SummaryGraphEdge> edges = iGraph.incomingEdgesOf(e);
		for (SummaryGraphEdge edge : edges){
			
			SummaryGraphElement source = edge.getSource();
			if(!c.hasVisited(source)) {
				if(edge.getEdgeLabel() == SummaryGraphEdge.MAPPING_EDGE){
					// update score: (EF/EDF*matchingscore) + mappingScore
					source.setTotalScore(source.getTotalScore() + iGraph.getEdgeWeight(edge));
				}
				neighbors.add(source);
			}
		}
		edges = iGraph.outgoingEdgesOf(e);
		for (SummaryGraphEdge edge : edges){
			SummaryGraphElement target = edge.getTarget();
			if(!c.hasVisited(target)) {
				if(edge.getEdgeLabel() == SummaryGraphEdge.MAPPING_EDGE){
					// update score: (EF/EDF*matchingscore) + mappingScore
					target.setTotalScore(target.getTotalScore() + iGraph.getEdgeWeight(edge));
				}
				neighbors.add(target);
			}
		}
		return neighbors;
	}



//	private Collection<Collection<OWLPredicate>> computeQueries(Collection<Collection<SummaryGraphProperty>> subgraphs) {
//	Collection<Collection<OWLPredicate>> queries = new LinkedHashSet<Collection<OWLPredicate>>();
//	for(Collection<SummaryGraphProperty> subgraph : subgraphs){
//	Collection<OWLPredicate> query = new LinkedHashSet<OWLPredicate>();
//	queries.add(query);
//	Map<Pair, Variable> labelVar = new LinkedHashMap<Pair, Variable>();
//	UniqueIdGenerator.getInstance().resetVarIds();
//	for(SummaryGraphProperty edge : subgraph){
//	if((edge.getType() == ISummaryGraphElement.ATTRIBUTE) && (edge.getVertex2().getType() != ISummaryGraphElement.DUMMY_VALUE)){
//	IProperty p = edge.getProperty();
//	IResource v1 = edge.getVertex1().getResource();
//	IResource v2 = edge.getVertex2().getResource();
//	IResource t;
//	String conceptLabel = v1.getLabel();
//	String literalLabel = v2.getLabel();
//	Pair label = new Pair(conceptLabel,literalLabel);
//	Variable var = labelVar.get(label); 
//	if (var == null) {
//	t = getNewVariable();
//	labelVar.put(label, (Variable)t);
//	query.add(new ConceptMemberPredicate(v1, t));
//	} else {
//	t = var;
//	}

//	query.add(new PropertyMemberPredicate(p, t,v2));
//	}
//	}
//	for(SummaryGraphProperty edge : subgraph){
//	if((edge.getType() == ISummaryGraphElement.RELATION) && (!edge.getProperty().equals(Property.SUBCLASS_OF))){
//	IProperty p = edge.getProperty();
//	IResource v1 = edge.getVertex1().getResource();
//	IResource v2 = edge.getVertex2().getResource();

//	Set<IResource> t1 = new LinkedHashSet<IResource>();
//	String label1 = v1.getLabel();
//	for(Pair pair : labelVar.keySet()){
//	if(pair.getHead().equals(label1)) {
//	t1.add(labelVar.get(pair));
//	}
//	}
//	if(t1.size() == 0) {
//	Variable var = getNewVariable();
//	t1.add(var);
//	labelVar.put(new Pair(label1,null), var);
//	query.add(new ConceptMemberPredicate(v1, var));
//	}


//	Set<IResource> t2 = new LinkedHashSet<IResource>();
//	String label2 = v2.getLabel();
//	for(Pair pair : labelVar.keySet()){
//	if(pair.getHead().equals(label2)) {
//	t2.add(labelVar.get(pair));
//	}
//	}
//	if(t2.size() == 0) {
//	Variable var = getNewVariable();
//	t2.add(var);
//	labelVar.put(new Pair(label2,null), var);
//	query.add(new ConceptMemberPredicate(v2, var));
//	}

//	for(IResource resource1 : t1){
//	for(IResource resource2 : t2) {
//	query.add(new PropertyMemberPredicate(p, resource1,resource2));
//	}
//	}

//	}
//	}
//	for(SummaryGraphProperty edge : subgraph){
//	if((edge.getType() == ISummaryGraphElement.ATTRIBUTE) && (edge.getVertex2().getType() == ISummaryGraphElement.DUMMY_VALUE)){
//	IProperty p = edge.getProperty();
//	IResource v1 = edge.getVertex1().getResource();
//	IResource v2 = edge.getVertex2().getResource();

//	Set<IResource> t1 = new LinkedHashSet<IResource>();
//	String label1 = v1.getLabel();
//	for(Pair pair : labelVar.keySet()){
//	if(pair.getHead().equals(label1)) {
//	t1.add(labelVar.get(pair));
//	}
//	}
//	if(t1.size() == 0) {
//	Variable var = getNewVariable();
//	t1.add(var);
//	labelVar.put(new Pair(label1,null), var);
//	query.add(new ConceptMemberPredicate(v1, var));
//	}

//	for(IResource resource1 : t1){
//	query.add(new PropertyMemberPredicate(p, resource1,getNewVariable()));
//	}
//	}
//	}
//	}

//	return queries;
//	}

//	private Variable getNewVariable(){
//	String var = String.valueOf(UniqueIdGenerator.getInstance().getNewVarId());
//	return new Variable("x" + var);
//	}

//	public Set<Variable> getRankedVariables(Collection<OWLPredicate> query){
//	Set<Variable> vars = new LinkedHashSet<Variable>();
//	if((query == null) || (query.size() == 0)) {
//	return null;
//	} 

//	//TODO perform the ranking
//	for (OWLPredicate p : query) {
//	IResource var1 = null;
//	IResource var2 = null;

//	if (p instanceof PropertyMemberPredicate) {
//	var1 = ((PropertyMemberPredicate)p).getFirstTerm();
//	var2 = ((PropertyMemberPredicate)p).getSecondTerm();
//	}
//	else if (p instanceof ConceptMemberPredicate) {
//	var1 = ((ConceptMemberPredicate)p).getConcept();
//	var2 = ((ConceptMemberPredicate)p).getTerm();
//	}
//	if(var1 instanceof Variable) {
//	vars.add((Variable)var1);
//	}
//	if(var2 instanceof Variable) {
//	vars.add((Variable)var2);
//	}
//	}

//	return vars; 
//	}

	public class Subgraph implements Comparable {

		private SummaryGraphElement connectingVertex;

		private Set<SummaryGraphElement> paths;

		double cost;

		public Subgraph(SummaryGraphElement connectingVertex, Set<SummaryGraphElement> paths, double cost){
			this.connectingVertex = connectingVertex;
			this.cost = cost;
			this.paths = paths;
		}

		public Set<SummaryGraphElement> getPaths(){
			return paths;
		}

		public SummaryGraphElement getConnectingVertex(){
			return connectingVertex;
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
	class ExpansionQueue{
		Map<String, PriorityQueue<Cursor>> m_queue;
		Set<String> m_keywords;
		Collection<PriorityQueue<Cursor>> m_queues;
		
		private ExpansionQueue(Map<String, PriorityQueue<Cursor>> queue){
			m_queue = queue;
			m_keywords = queue.keySet();
			m_queues = new ArrayList<PriorityQueue<Cursor>>();
			for (String k : m_keywords){
				m_queues.add(queue.get(k));
			}
		}
		
		private void addCursor(Cursor c, String keyword){
			PriorityQueue<Cursor> q = m_queue.get(keyword);
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
				double cCost = q.peek().getCost();
				if(cCost < minCost || minCost == 0) {
					minCost = cCost;
					minCostQ = q;
				}
			}
			return minCostQ.poll();
		}
	}
}
