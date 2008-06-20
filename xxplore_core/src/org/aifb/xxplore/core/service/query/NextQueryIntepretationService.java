package org.aifb.xxplore.core.service.query;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import org.aifb.xxplore.core.service.IServiceListener;
import org.aifb.xxplore.shared.util.Pair;
import org.aifb.xxplore.shared.util.UniqueIdGenerator;
import org.apache.log4j.Logger;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.WeightedPseudograph;
import org.jgrapht.traverse.ClosestFirstIterator;
import org.xmedia.oms.model.api.INamedConcept;
import org.xmedia.oms.model.api.IObjectProperty;
import org.xmedia.oms.model.api.IOntology;
import org.xmedia.oms.model.api.IProperty;
import org.xmedia.oms.model.api.IResource;
import org.xmedia.oms.model.impl.Property;
import org.xmedia.oms.persistence.PersistenceUtil;
import org.xmedia.oms.persistence.SessionFactory;
import org.xmedia.oms.persistence.StatelessSession;
import org.xmedia.oms.persistence.dao.IConceptDao;
import org.xmedia.oms.query.ConceptMemberPredicate;
import org.xmedia.oms.query.OWLPredicate;
import org.xmedia.oms.query.PropertyMemberPredicate;
import org.xmedia.oms.query.Variable;

public class NextQueryIntepretationService implements IQueryInterpretationService {

	private static Logger s_log = Logger.getLogger(NextQueryIntepretationService.class);
	
	private int TOTAL_NUMBER_OF_VERTEX = -1; 
	
	private int TOTAL_NUMBER_OF_EDGE = -1;

	private WeightedPseudograph<KbVertex,KbEdge> resourceGraph;
	
	private Set<KbVertex> verticesOfGraphIndex;
	
	private Set<KbVertex> verticesOfGraphWithCursors;
	
	private Collection<Collection<OWLPredicate>> queries;
	
	private Map<String,Collection<KbElement>> resources;
	
	private Collection<Collection<KbEdge>> subgraphs;
	
	private PriorityQueue<QueueEntry> expansionQueue;
	
	private PriorityQueue<Subgraph> subgraphQueue;
	
	private Set<KbVertex> connectingElements;
	
	private Set<KbElement> matchingREdges;
	
	private Map<String,Collection<KbElement>> keywordEdgeMap;
	
	private double threshold = 50;
	
	private int width = 0;
	
	public void callService(IServiceListener listener, Object... params) {
		// TODO Auto-generated method stub
	}

	public void disposeService() {
		// TODO Auto-generated method stub
	}

	public void init(Object... params) {
		// TODO Auto-generated method stub
	}

	public Collection<Collection<OWLPredicate>> computeQueries(Map<String,Collection<KbElement>> elements, int width, int depth) {
		
		if (elements == null) {
			return null;
		}

		if(width > 0) {
			this.width = width;
		}
		
		computeTotalNumber();
		expansionQueue = new PriorityQueue<QueueEntry>();
		subgraphQueue = new PriorityQueue<Subgraph>();
		connectingElements = new LinkedHashSet<KbVertex>();
		
	    resources = computeResources(elements);
	    if((resources == null) || (resources.size() == 0)) {
			return null;
		}
		resourceGraph = computeGraphSchemaIndex();
		computeGraph(resources,resourceGraph);
		subgraphs = computeSubgraphs(resources);
		if((subgraphs == null) || (subgraphs.size() == 0)) {
			return null;
		}
		queries = computeQueries(subgraphs);
		if((queries == null) || (queries.size() == 0)) {
			return null;
		}

		return queries;
	}

	public Map<String,Collection<KbElement>> computeResources(Map<String,Collection<KbElement>> ress){
		keywordEdgeMap = new LinkedHashMap<String,Collection<KbElement>>();
		matchingREdges = new LinkedHashSet<KbElement>();
		Set<String> keywords = new LinkedHashSet<String>();
		for(String keyword : ress.keySet()){
			boolean allEdges = false;
			Collection<KbElement> collection = ress.get(keyword);
			for(Iterator<KbElement> ite = collection.iterator(); ite.hasNext(); ){
				KbElement element = ite.next();
				if((element instanceof KbVertex) && (element.getType() == KbElement.CVERTEX)){
					double weight = computeWeight((INamedConcept)((KbVertex)element).getResource());
					if(weight == Double.POSITIVE_INFINITY) {
						ite.remove();
					}
				}
				else if((element instanceof KbEdge) && (element.getType() == KbElement.REDGE)){
					double weight = computeWeight(((KbEdge)element).getProperty());
					if(weight == Double.POSITIVE_INFINITY) {
						ite.remove();
					} else {
						double weight1 = computeWeight((INamedConcept)((KbEdge)element).getVertex1().getResource());
						double weight2 = computeWeight((INamedConcept)((KbEdge)element).getVertex2().getResource());
						if((weight1 == Double.POSITIVE_INFINITY) || (weight2 == Double.POSITIVE_INFINITY)) {
							ite.remove();
						}
					}
				}
			}
			if((collection == null) || (collection.size() == 0)){
				keywords.add(keyword);
			}	
			for(KbElement element : collection){
				if((element instanceof KbEdge) && (element.getType() == KbElement.REDGE)) {
					allEdges = true;
					matchingREdges.add(element);
				}	
				else {
					allEdges = false;
					break;
				}	
			}
			if(allEdges){
				keywordEdgeMap.put(keyword,ress.get(keyword));
				keywords.add(keyword);
			}
		}
		for(String keyword : keywords){
			ress.remove(keyword);
		}
		s_log.debug("matchingREdges: " + matchingREdges);
		s_log.debug("keywordEdgeMap: " + keywordEdgeMap);
		return ress;
	}
	
	public WeightedPseudograph<KbVertex,KbEdge> computeGraphSchemaIndex(){
//		if(resourceGraph != null) return resourceGraph;
		
		WeightedPseudograph<KbVertex,KbEdge> graph = new WeightedPseudograph<KbVertex,KbEdge>(KbEdge.class);
		
		//construct graphSchemaIndex by Concept
		IConceptDao conceptDao = (IConceptDao) PersistenceUtil.getDaoManager().getAvailableDao(IConceptDao.class);
		List concepts = conceptDao.findAll();
		int numConcept = concepts.size();
//		System.out.println("number of Concept: " + numConcept);
		
		for(Object concept : concepts){
			if(concept instanceof IResource){
				IResource resource = (IResource)concept;
				addGraphElements(resource, graph);
			}	
		}
		
		verticesOfGraphIndex = graph.vertexSet();
		return graph;
	}
	
	public void computeGraph(Map<String,Collection<KbElement>> resources, WeightedPseudograph<KbVertex,KbEdge> graph){
		for(String keyword : resources.keySet()){
			Collection<KbElement> ress = resources.get(keyword);
			for(KbElement element : ress){
				if(element.getType() == KbElement.VVERTEX){
					KbVertex vertex = (KbVertex)element;
					addGraphElement(vertex, graph);
				}
				else if(element.getType() == KbElement.CVERTEX){
					KbVertex vertex = (KbVertex)element;
					for(KbVertex ver : verticesOfGraphIndex){
						if(ver.equals(vertex)) {
							ver.setScore(vertex.getScore());
						}
					}
					addGraphElement(vertex, graph);
				}
				else if(element.getType() == KbElement.AEDGE){
					KbEdge edge = (KbEdge)element;
					addGraphElement(edge, graph);
				}
			}
		}
		
		for(KbEdge edge : graph.edgeSet()){
			if(!matchingREdges.contains(edge)){
				if(edge.getProperty().equals(Property.SUBCLASS_OF)){
					graph.setEdgeWeight(edge, 3);
				} 
				else {	
					double weight = edge.getVertex1().getCost()+edge.getVertex2().getCost()+edge.getCost();
					graph.setEdgeWeight(edge, weight);
				}
			}	
			else {
				double weight = edge.getVertex1().getCost()+edge.getVertex2().getCost();
				graph.setEdgeWeight(edge, 3);
			}	
		}
		
		verticesOfGraphWithCursors = graph.vertexSet();
	}
	
	
	
	public Collection<Collection<KbEdge>> computeSubgraphs(Map<String,Collection<KbElement>> ress){
		
		for(KbVertex vertex : verticesOfGraphWithCursors){
			vertex.createCursors(ress.keySet());
		}

		for(String keyword : ress.keySet()){
			for(KbElement element : ress.get(keyword)){
				if(element instanceof KbVertex){
					KbVertex vertex = (KbVertex)element; 
					QueueEntry entry = new QueueEntry(keyword, getVertexInGraphWithCursors(vertex));
					expansionQueue.add(entry);
				}
			}
		}
		boolean isFinished = false;
		while(!isFinished){
			QueueEntry expansion = expansionQueue.poll();
			boolean isExhausted = expansion.cursorExpand();
			expansionQueue.add(expansion);
			if(isExhausted == true) {
				expansionQueue.remove(expansion);
			}
			if(expansionQueue.size() == 0) {
				isFinished = true;
			}
		}
		
		Collection<Collection<KbEdge>> subgraphs = new LinkedHashSet<Collection<KbEdge>>();
		int size = subgraphQueue.size();
//		System.out.println("computeSubgraphs - subgraphs: " + size);
		for(int i = 0, j = 0; i < size; i++){
			Subgraph subgraph = subgraphQueue.poll();
//			System.out.println(i + ": " + subgraph + "\n");
			Set<KbEdge> edges = subgraph.getPaths();
			if(!subgraphs.contains(edges)){
				subgraphs.add(edges);
				j++;
				if(j >= 15) {
					return subgraphs;
				}
			}
		}
		
		return subgraphs;
	}
	
	public KbVertex getVertexInGraphWithCursors(KbVertex vertex){
		for(KbVertex vertexInGraph : verticesOfGraphWithCursors){
			if(vertex.equals(vertexInGraph)) {
				return vertexInGraph;
			}
		}
		return null;
	}
	
	private Collection<Collection<OWLPredicate>> computeQueries(Collection<Collection<KbEdge>> subgraphs) {
		Collection<Collection<OWLPredicate>> queries = new LinkedHashSet<Collection<OWLPredicate>>();
		for(Collection<KbEdge> subgraph : subgraphs){
			Collection<OWLPredicate> query = new LinkedHashSet<OWLPredicate>();
			queries.add(query);
			Map<Pair, Variable> labelVar = new LinkedHashMap<Pair, Variable>();
			UniqueIdGenerator.getInstance().resetVarIds();
			for(KbEdge edge : subgraph){
				if((edge.getType() == KbElement.AEDGE) && (edge.getVertex2().getType() != KbElement.DUMMY)){
					IProperty p = edge.getProperty();
					IResource v1 = edge.getVertex1().getResource();
					IResource v2 = edge.getVertex2().getResource();
					IResource t;
					String conceptLabel = v1.getLabel();
					String literalLabel = v2.getLabel();
					Pair label = new Pair(conceptLabel,literalLabel);
					Variable var = labelVar.get(label); 
					if (var == null) {
						t = getNewVariable();
						labelVar.put(label, (Variable)t);
						query.add(new ConceptMemberPredicate(v1, t));
					} else {
						t = var;
					}
				
					query.add(new PropertyMemberPredicate(p, t,v2));
				}
			}
			for(KbEdge edge : subgraph){
				if((edge.getType() == KbElement.REDGE) && (!edge.getProperty().equals(Property.SUBCLASS_OF))){
					IProperty p = edge.getProperty();
					IResource v1 = edge.getVertex1().getResource();
					IResource v2 = edge.getVertex2().getResource();
					
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
					String label2 = v2.getLabel();
					for(Pair pair : labelVar.keySet()){
						if(pair.getHead().equals(label2)) {
							t2.add(labelVar.get(pair));
						}
					}
					if(t2.size() == 0) {
						Variable var = getNewVariable();
						t2.add(var);
						labelVar.put(new Pair(label2,null), var);
						query.add(new ConceptMemberPredicate(v2, var));
					}
					
					for(IResource resource1 : t1){
						for(IResource resource2 : t2) {
							query.add(new PropertyMemberPredicate(p, resource1,resource2));
						}
					}
					
				}
			}
			for(KbEdge edge : subgraph){
				if((edge.getType() == KbElement.AEDGE) && (edge.getVertex2().getType() == KbElement.DUMMY)){
					IProperty p = edge.getProperty();
					IResource v1 = edge.getVertex1().getResource();
					IResource v2 = edge.getVertex2().getResource();
					
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
			}
		}
		
		return queries;
	}
	
	public void addGraphElements(IResource resource, WeightedPseudograph<KbVertex,KbEdge> graph){
		boolean addVertex = false;
		if(resource instanceof INamedConcept){
			INamedConcept concept = (INamedConcept)resource;
			KbVertex vertex = new KbVertex(resource, KbElement.CVERTEX,computeWeight(concept));
			addVertex = graph.addVertex(vertex);
			if(addVertex) {
				s_log.debug("Vertex " + vertex + " is added to the graph!");
			} else {
				s_log.debug("Vertex " + vertex + " is already in the graph!");
			}
				
//			Set<IConcept> subconcepts = concept.getSubconcepts();
//			if(subconcepts != null && subconcepts.size() != 0){
//				for(IConcept con : subconcepts){
//					KbVertex subvertex = new KbVertex(con, KbElement.CVERTEX,computeWeight((INamedConcept)con)); 
//					addVertex = graph.addVertex(subvertex);
//					if(addVertex) s_log.debug("Vertex " + subvertex + " is added to the graph!");
//					else s_log.debug("Vertex " + subvertex + " is already in the graph!");
//					
//					addGraphElements(subvertex, vertex, Property.SUBCLASS_OF, graph);
//				}
//			}
			
			Set<Pair> proAndRanges = concept.getPropertiesAndRangesFrom();
			if((proAndRanges != null) && (proAndRanges.size() != 0)){
				for(Pair pair : proAndRanges){
					IProperty property = (IProperty)pair.getHead();
					if(property instanceof IObjectProperty){
						INamedConcept range = (INamedConcept)pair.getTail();
						KbVertex rangevertex = new KbVertex(range, KbElement.CVERTEX,computeWeight(range)); 
						addVertex = graph.addVertex(rangevertex);
						if(addVertex) {
							s_log.debug("Vertex " + rangevertex + " is added to the graph!");
						} else {
							s_log.debug("Vertex " + rangevertex + " is already in the graph!");
						}
						
						addGraphElements(vertex, rangevertex, property, graph);
					}
				}
			}
		} 
	}
	
	public void addGraphElements(KbVertex vertex1, KbVertex vertex2, IProperty property, WeightedPseudograph<KbVertex,KbEdge> graph){
		boolean addEdge = false; 
		KbEdge edge = null;
		if(vertex2.getType() == KbElement.CVERTEX){
			if(property.equals(Property.SUBCLASS_OF)) {
				edge = new KbEdge(vertex1, vertex2, property, KbElement.REDGE,0);
			} else {
				edge = new KbEdge(vertex1, vertex2, property, KbElement.REDGE,computeWeight(property));
			}
		}
		else {
			edge = new KbEdge(vertex1, vertex2, property, KbElement.AEDGE,1);
		} 
			
		if(!(graph.containsEdge(edge))){
			addEdge = graph.addEdge(vertex1, vertex2, edge);
			if(addEdge) {
				s_log.debug("Edge " + edge + " is added to the graph!");
			}
		} else {
			s_log.debug("Edge " + edge + " is already in the graph!");
		}
	}
	
	private  void addGraphElement(KbVertex vertex, WeightedPseudograph<KbVertex,KbEdge> graph){
		boolean addVertex = false;
		addVertex = graph.addVertex(vertex);
		if(addVertex) {
			s_log.debug("Vertex " + vertex + " is added to the graph!");
		} else {
			s_log.debug("Vertex " + vertex + " is already in the graph!");
		}
		
	}
	
	public void addGraphElement(KbEdge edge, WeightedPseudograph<KbVertex,KbEdge> graph){
		boolean addEdge = false; 
		if(!(graph.containsEdge(edge))){
			KbVertex vertex1 = edge.getVertex1();
			if(!(graph.containsVertex(vertex1))) {
				graph.addVertex(vertex1);
			}
			KbVertex vertex2 = edge.getVertex2();
			if(!(graph.containsVertex(vertex2))) {
				graph.addVertex(vertex2);
			}
			addEdge = graph.addEdge(vertex1, vertex2, edge);
			if(addEdge) {
				s_log.debug("Edge " + edge + " is added to the graph!");
			}
		} else {
			s_log.debug("Edge " + edge + " is already in the graph!");
		}
	}
	
	private Variable getNewVariable(){
		String var = String.valueOf(UniqueIdGenerator.getInstance().getNewVarId());
		return new Variable("x" + var);
	}
	
	public Set<Variable> getRankedVariables(Collection<OWLPredicate> query){
		Set<Variable> vars = new LinkedHashSet<Variable>();
		if((query == null) || (query.size() == 0)) {
			return null;
		} 

		//TODO perform the ranking
		for (OWLPredicate p : query) {
			IResource var1 = null;
			IResource var2 = null;

			if (p instanceof PropertyMemberPredicate) {
				var1 = ((PropertyMemberPredicate)p).getFirstTerm();
				var2 = ((PropertyMemberPredicate)p).getSecondTerm();
			}
			else if (p instanceof ConceptMemberPredicate) {
				var1 = ((ConceptMemberPredicate)p).getConcept();
				var2 = ((ConceptMemberPredicate)p).getTerm();
			}
			if(var1 instanceof Variable) {
				vars.add((Variable)var1);
			}
			if(var2 instanceof Variable) {
				vars.add((Variable)var2);
			}
		}

		return vars; 
	}

	public boolean isSubConcept(Object subconcept, Object superconcept){
		if ((subconcept instanceof INamedConcept) && (superconcept instanceof INamedConcept)){
			INamedConcept sup = (INamedConcept)superconcept;
			INamedConcept sub = (INamedConcept)subconcept;
			if(sup.getSubconcepts() !=  null){
				if (sup.getSubconcepts().contains(sub)) {
					return true;
				} else {
					for (Object obj : sup.getSubconcepts()) {
						if(isSubConcept(sub,obj)) {
							return true;
						}
					}
				}
			} else {
				return false;
			}
		}
		return false;
	} 
	
	private void computeTotalNumber(){
		if((TOTAL_NUMBER_OF_VERTEX != -1) && (TOTAL_NUMBER_OF_EDGE != -1)) {
			return;
		}
		
		StatelessSession session = (StatelessSession)SessionFactory.getInstance().getCurrentSession();
		IOntology onto = session.getOntology();
		
		int numConcept = onto.getNumberOfConcept();
//		System.out.println("number of Concept: " + numConcept);
		
		int numIndividual = onto.getNumberOfIndividual();
//		System.out.println("number of Individual: " + numIndividual);
		
		int numoProperty = onto.getNumberOfObjectProperty();
//		System.out.println("number of ObjectProperty: " + numoProperty);
		
		int numoPropertyMember = onto.getNumberOfObjectPropertyMember();
//		System.out.println("number of ObjectPropertyMember: " + numoPropertyMember);
		
		TOTAL_NUMBER_OF_VERTEX = numConcept + numIndividual;
		
		TOTAL_NUMBER_OF_EDGE = numoProperty + numoPropertyMember;
	}
	
	private double computeEdgeWeight(double num, double totalNum){
		if(num == 0) {
			return Double.POSITIVE_INFINITY;
		}
		return 2-Math.log(1+num/totalNum)/Math.log(2);
	}
	
	private double computeVertexWeight(double num, double totalNum){
		if(num == 0) {
			return Double.POSITIVE_INFINITY;
		}
		return 2-Math.log(1+num/totalNum)/Math.log(2); 
	}
	
	private double computeWeight(INamedConcept concept){
		int numIndividual = concept.getNumberOfIndividuals();
		return computeVertexWeight(numIndividual,TOTAL_NUMBER_OF_VERTEX);
	}
	
	private double computeWeight(IProperty property){
		int numProMem = property.getNumberOfPropertyMember();
		return computeEdgeWeight(numProMem,TOTAL_NUMBER_OF_EDGE);
	} 
	
	public int mul(int a[],int n){
	    return n>0?((a[n-1]+1)*mul(a,--n)):1;
	}
	
	class QueueEntry implements Comparable {
		
		private ClosestFirstIterator<KbVertex, KbEdge> iter;
		
		private String keyword;
		
		private KbVertex matchingVertex;
		
		private float score;
		
		private double cost = 0;
		
		public QueueEntry(String keyword, KbVertex startVertex){
			this.matchingVertex = startVertex;
			this.score = startVertex.getScore();
			this.keyword = keyword; 
			
			if(width > 0){
//				iter =  new ClosestFirstIterator<KbVertex, KbEdge>(resourceGraph, matchingVertex, width);
				iter =  new ClosestFirstIterator<KbVertex, KbEdge>(resourceGraph, matchingVertex);
				iter.next();
			}	
			else{
				iter =  new ClosestFirstIterator<KbVertex, KbEdge>(resourceGraph, matchingVertex);
				iter.next();
			}	
		}
		
		public boolean cursorExpand(){
			if(!iter.hasNext()) {
				return true;
			} else {
				 KbVertex endVertex = iter.next();
				 Map<String,Queue<Cursor>> cursors = getVertexInGraphWithCursors(endVertex).getCursors();
				 List<KbEdge> path = createEdgeList(resourceGraph, iter, endVertex);
				 double pathLength = iter.getShortestPathLength(endVertex);
				 cost =  pathLength/score;
//				 System.out.println("(" + matchingVertex + ")" + " to " + "(" + endVertex + ")");
//				 System.out.println("cost: " + cost);
				 if(cost > threshold) {
					return true;
				 }
				 cursors.get(keyword).add(new Cursor(matchingVertex, cost, path));
				 
				 boolean isConnectingVertex = false;
				 double subgraphCost = 0;
		         List<List<Cursor>> allCursors = new ArrayList<List<Cursor>>();
		         for(Queue<Cursor> queue : cursors.values()){
		           	if(queue.size() != 0){
		           		isConnectingVertex = true;
		           		Cursor cursor = queue.peek();
		           		double cursorCost = cursor.getCost();
		          		if(cursorCost > threshold) {
							break;
						}
		           		subgraphCost += cursorCost;
		          		
		          		List<Cursor> css = new ArrayList<Cursor>();
		          		css.addAll(queue);
		           		allCursors.add(css);
		           	}
		           	else {
		           		isConnectingVertex = false;
		           		break;
		           	}	
		         }
		         if((isConnectingVertex == true) && (subgraphCost <= threshold)){
		          	connectingElements.add(endVertex);
		          	Cursor[][] targetCursors = computeTargetCursors(allCursors);
		          	out:for(int i = 0; i < targetCursors.length; i++){
						Cursor[] curs = targetCursors[i];
						double cost = 0;
						Set<KbEdge> edges = new LinkedHashSet<KbEdge>();
						for(Cursor cursor : curs){
							cost += cursor.getCost();
							if(cost > threshold) {
								continue out;
							}
							edges.addAll(cursor.getPath());
						}
						
						boolean isSubgraph = false;
						if((keywordEdgeMap != null) && (keywordEdgeMap.size() != 0)){
							for(Collection<KbElement> collection : keywordEdgeMap.values()){
								isSubgraph = !Collections.disjoint(edges, collection);
								if(!isSubgraph) {
									break;
								}
							}
						} else {
							isSubgraph = true;
						}
						if(isSubgraph){
							Subgraph subgraph = new Subgraph(endVertex, edges, cost);
							if(!subgraphQueue.contains(subgraph)) {
								subgraphQueue.add(subgraph);
							}
						}
					}
		         }
				 
//				 boolean isConnectingVertex = false;
//				 double subgraphCost = 0;
//		         Set<Cursor> cursorSet = new HashSet<Cursor>();
//		         for(Queue<Cursor> queue : cursors.values()){
//		           	if(queue.size() != 0){
//		           		isConnectingVertex = true;
//		           		Cursor cursor = queue.peek();
//		           		double cursorCost = cursor.getCost();
//		          		if(cursorCost > threshold) break;
//		           		subgraphCost += cursorCost;
//		           		cursorSet.add(cursor);
//		           	}
//		           	else {
//		           		isConnectingVertex = false;
//		           		break;
//		           	}	
//		         }
//		         if(isConnectingVertex == true && subgraphCost <= threshold){
//		          	connectingElements.add(endVertex);
//		           	CSubgraph cs = new CSubgraph(endVertex, cursorSet, subgraphCost);
//		           	if(!cursorsOfSubgraph.contains(cs)) cursorsOfSubgraph.add(cs);
//		         }
		         
			}
			return false;
		}
		
		public List<KbEdge> createEdgeList(Graph<KbVertex, KbEdge> graph, ClosestFirstIterator<KbVertex, KbEdge> iter, KbVertex endVertex){
		
			List<KbEdge> edgeList = new ArrayList<KbEdge>();

	        while (true) {
	            KbEdge edge = iter.getSpanningTreeEdge(endVertex);

	            if (edge == null) {
					break;
				}
	            edgeList.add(edge);
	            endVertex = Graphs.getOppositeVertex(graph, edge, endVertex);
	        }
	        Collections.reverse(edgeList);
	        return edgeList;
		}
		
		private Cursor[][] computeTargetCursors(List<List<Cursor>> cursors) {
			int size = cursors.size();
			int[] guard = new int[size];
			int i = 0;
			for(List<Cursor> list : cursors){
				guard[i++] = list.size()-1;
			}
			int entrySize = mul(guard,size);
			Cursor[][] entries = new Cursor[entrySize][size];
			
			int[] index = new int[size];
			for(int p : index) {
				p = 0;
			} 
			guard[size-1]++;
			i = 0;
			do {
				for(int m = 0; m < size; m++){
					entries[i][m] = cursors.get(m).get(index[m]);
				}
				i++;
				index[0]++;
				for(int j = 0; j < size; j++){
					if(index[j] > guard[j]){
						index[j] = 0;
						index[j+1]++; 
					}
				}
			}
			while(index[size-1] < guard[size-1]);
			
			return entries;
		}

		public int compareTo(Object o) {
			QueueEntry other = (QueueEntry)o;
			if(cost > other.cost) {
				return 1;
			}
			if(cost < other.cost) {
				return -1;
			}
			return 0;
		}
		
	}
	
	class Cursor implements Comparable {
		
		private KbVertex matchingVertex;
		
		private double cost;
		
		List<KbEdge> edges;
		
		public Cursor(){}
		
		public Cursor(KbVertex matchingVertex, double cost, List<KbEdge> edges){
			this.matchingVertex = matchingVertex;
			this.cost = cost;
			this.edges = edges;
		} 

		public double getCost(){
			return cost;
		}
		
		public List<KbEdge> getPath(){
			return edges;
		}
		
		public KbVertex getMatchingVertex(){
			return matchingVertex;
		}
		
		public int compareTo(Object o) {
			Cursor other = (Cursor)o;
			if(cost > other.cost) {
				return 1;
			}
			if(cost < other.cost) {
				return -1;
			}
			return 0;
		}
		
		@Override
		public boolean equals(Object o){
			if(this == o) {
				return true;
			}
			if(!(o instanceof Cursor)) {
				return false;
			}
			Cursor other = (Cursor)o;
			if(cost != other.getCost()) {
				return false;
			}
			if(!(matchingVertex.equals(other.getMatchingVertex()))) {
				return false;
			}
			if(!(edges.equals(other.getPath()))) {
				return false;
			}
			return true;
		}
		
		@Override
		public int hashCode(){
			int code = 0;
			code += 7*matchingVertex.hashCode() + 13*edges.hashCode();
			return code;
		}
		
		@Override
		public String toString(){
			return "cost: " + cost 
				+ "\n" + "matchingVertex: " + matchingVertex
				+ "\n" + "Path: " + edges
 				+ "\n";
		}
		
	}
	
	class Subgraph implements Comparable {
		
//		private KbVertex[] targetVertices;
		
		private KbVertex connectingVertex;
		
		private Set<KbEdge> paths;
		
		double cost;
		
//		public Subgraph(KbVertex[] targetVertices, KbVertex connectingVertex, Set<KbEdge> paths, double cost){
//			this.targetVertices = targetVertices;
//			this.connectingVertex = connectingVertex;
//			this.cost = cost;
//			this.paths = paths;
//		}
		
		public Subgraph(KbVertex connectingVertex, Set<KbEdge> paths, double cost){
			this.connectingVertex = connectingVertex;
			this.cost = cost;
			this.paths = paths;
		}
		
		public Set<KbEdge> getPaths(){
			return paths;
		}
		
//		public KbVertex[] getTargetVertices(){
//			return targetVertices;
//		}
		
		public KbVertex getConnectingVertex(){
			return connectingVertex;
		}
		
		public double getCost(){
			return cost;
		}
		
		public int compareTo(Object o){
			Subgraph other = (Subgraph)o;
			if(this.cost > other.cost) {
				return 1;
			}
			if(this.cost < other.cost) {
				return -1;
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
			if(cost != other.getCost()) {
				return false;
			}
			if(!(connectingVertex.equals(other.getConnectingVertex()))) {
				return false;
			}
//			if(!(Arrays.equals(targetVertices, other.getTargetVertices()))) return false;
			if(!(paths.equals(other.getPaths()))) {
				return false;
			}
			return true;
		}
		
		@Override
		public int hashCode(){
			int code = 0;
//			for(KbVertex vertex : targetVertices){
//				code += 3*vertex.hashCode();
//			}
			code += 7*connectingVertex.hashCode() + 13*paths.hashCode();
			return code;
		}
		
//		public String printTargetVertices(){
//			String str = "";
//			for(KbVertex vertex : targetVertices){
//				str += vertex.toString() + ", ";
//			}
//			return str;
//		}
		
		@Override
		public String toString(){
			return "cost: " + cost 
//			    + "\n" + "Target vertices: " + printTargetVertices()
				+ "\n" + "Connecting vertex: " + connectingVertex
				+ "\n" + "Paths: " + paths
 				+ "\n";
		}
	}

}
