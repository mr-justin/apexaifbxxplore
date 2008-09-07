//package org.ateam.xxplore.core.service.search;
//
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.IOException;
//import java.io.ObjectInputStream;
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.Collections;
//import java.util.LinkedHashMap;
//import java.util.LinkedHashSet;
//import java.util.LinkedList;
//import java.util.List;
//import java.util.Map;
//import java.util.PriorityQueue;
//import java.util.Queue;
//import java.util.Set;
//
//import org.aifb.xxplore.shared.util.Pair;
//import org.aifb.xxplore.shared.util.UniqueIdGenerator;
//import org.apache.log4j.Logger;
//import org.ateam.xxplore.core.ExploreEnvironment;
//import org.ateam.xxplore.core.service.IServiceListener;
//import org.jgrapht.Graph;
//import org.jgrapht.Graphs;
//import org.jgrapht.graph.Pseudograph;
//import org.jgrapht.graph.WeightedPseudograph;
//import org.jgrapht.traverse.ClosestFirstIterator;
//import org.xmedia.oms.model.api.INamedConcept;
//import org.xmedia.oms.model.api.IObjectProperty;
//import org.xmedia.oms.model.api.IProperty;
//import org.xmedia.oms.model.api.IResource;
//import org.xmedia.oms.model.impl.Property;
//import org.xmedia.oms.query.ConceptMemberPredicate;
//import org.xmedia.oms.query.OWLPredicate;
//import org.xmedia.oms.query.PropertyMemberPredicate;
//import org.xmedia.oms.query.Variable;
//
//public class QueryInterpretationService implements IQueryInterpretationService {
//
//	private static Logger s_log = Logger.getLogger(QueryIntepretationService.class);
//	
//	private int TOTAL_NUMBER_OF_VERTEX = -1; 
//	
//	private int TOTAL_NUMBER_OF_EDGE = -1;
//	
//	private int K_TOP = 15;
//
//	private Pseudograph<SummaryGraphElement,SummaryGraphEdge> resourceGraph;
//	
//	private Set<SummaryGraphElement> verticesOfGraphIndex;
//	
//	private Set<SummaryGraphElement> verticesOfGraphWithCursors;
//	
//	private Collection<Collection<OWLPredicate>> queries;
//	
//	private Map<String,Collection<ISummaryGraphElement>> resources;
//	
//	private Collection<Collection<SummaryGraphEdge>> subgraphs;
//	
//	private PriorityQueue<QueueEntry> expansionQueue;
//	
//	private PriorityQueue<Subgraph> subgraphQueue;
//	
//	private Set<SummaryGraphElement> connectingElements;
//	
//	private Set<ISummaryGraphElement> matchingREdges;
//	
//	private Map<String,Collection<ISummaryGraphElement>> keywordREdgeMap;
//	
//	private double threshold = 50;
//	
//	private int m_distance = 0;
//	
//	public void callService(IServiceListener listener, Object... params) {
//		// TODO Auto-generated method stub
//	}
//
//	public void disposeService() {
//		// TODO Auto-generated method stub
//	}
//
//	public void init(Object... params) {
//		// TODO Auto-generated method stub
//	}
//
//	public Collection<Collection<OWLPredicate>> computeQueries(Map<String,Collection<ISummaryGraphElement>> elements, String datasourceUri, int distance) {
//		
//		if (elements == null) return null;
//		
//		if(m_distance > 0) {
//			m_distance = distance;
//		}
//		
//		expansionQueue = new PriorityQueue<QueueEntry>();
//		subgraphQueue = new PriorityQueue<Subgraph>();
//		connectingElements = new LinkedHashSet<SummaryGraphElement>();
//		
//	    resources = computeResources(elements);
//	    if((resources == null) || (resources.size() == 0)) {
//			return null;
//		}
//		resourceGraph = computeGraphSchemaIndex(datasourceUri);
//		computeGraph(resources,resourceGraph);
//		subgraphs = computeSubgraphs(resources);
//		if((subgraphs == null) || (subgraphs.size() == 0)) {
//			return null;
//		}
//		queries = computeQueries(subgraphs);
//		if((queries == null) || (queries.size() == 0)) {
//			return null;
//		}
//
//		return queries;
//	}
//
//	public Map<String,Collection<ISummaryGraphElement>> computeResources(Map<String,Collection<ISummaryGraphElement>> ress){
//		keywordREdgeMap = new LinkedHashMap<String,Collection<ISummaryGraphElement>>();
//		matchingREdges = new LinkedHashSet<ISummaryGraphElement>();
//		for(String keyword : ress.keySet()){
//			boolean allEdges = false;
//			Collection<ISummaryGraphElement> collection = ress.get(keyword);
//			for(ISummaryGraphElement element : collection){
//				if(element instanceof SummaryGraphEdge && element.getType() == SummaryGraphElement.RELATION) {
//					allEdges = true;
//					matchingREdges.add(element);
//				}	
//				else {
//					allEdges = false;
//					break;
//				}	
//			}
//			if(allEdges){
//				keywordREdgeMap.put(keyword,ress.get(keyword));
//			}
//		}
//		return ress;
//	}
//	
//	public Pseudograph<SummaryGraphElement,SummaryGraphEdge> computeGraphSchemaIndex(String datasourceUri){
//		
//		Pseudograph<SummaryGraphElement,SummaryGraphEdge> resourceGraph = null;
//		File graphIndex = new File(ExploreEnvironment.GRAPH_INDEX_DIR, datasourceUri + ".graph");
//		ObjectInputStream in;
//		
//		try {
//			in = new ObjectInputStream(new FileInputStream(graphIndex));
//			resourceGraph = (Pseudograph<SummaryGraphElement, SummaryGraphEdge>)in.readObject(); 
//			in.close();
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (ClassNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//		verticesOfGraphIndex = resourceGraph.vertexSet();
//		return resourceGraph;
//	}
//	
//	public void computeGraph(Map<String,Collection<ISummaryGraphElement>> resources, WeightedPseudograph<SummaryGraphResource,SummaryGraphProperty> graph){
//		for(String keyword : resources.keySet()){
//			Collection<ISummaryGraphElement> ress = resources.get(keyword);
//			for(ISummaryGraphElement element : ress){
//				if(element.getType() == SummaryGraphElement.VALUE){
//					SummaryGraphElement vertex = (SummaryGraphElement)element;
//					addGraphElement(vertex, graph);
//				}
//				else if(element.getType() == ISummaryGraphElement.CONCEPT){
//					SummaryGraphElement vertex = (SummaryGraphElement)element;
//					for(SummaryGraphElement ver : verticesOfGraphIndex){
//						if(ver.equals(vertex)) {
//							ver.setScore(vertex.getScore());
//						}
//					}
//					addGraphElement(vertex, graph);
//				}
//				else if(element.getType() == SummaryGraphElement.ATTRIBUTE){
//					SummaryGraphEdge edge = (SummaryGraphEdge)element;
//					addGraphElement(edge, graph);
//				}
//			}
//		}
//		
////		for(SummaryGraphEdge edge : graph.edgeSet()){
////			if(!matchingREdges.contains(edge)){
////				if(edge.getEdgeLabel().equals(Property.SUBCLASS_OF)){
////					graph.setEdgeWeight(edge, 3);
////				} 
////				else {	
////					double weight = edge.getVertex1().getCost()+edge.getVertex2().getCost()+edge.getCost();
////					graph.setEdgeWeight(edge, weight);
////				}
////			}	
////			else {
////				double weight = edge.getVertex1().getCost()+edge.getVertex2().getCost();
////				graph.setEdgeWeight(edge, 3);
////			}	
////		}
//		
//		verticesOfGraphWithCursors = graph.vertexSet();
//	}
//	
//	
//	
//	public Collection<Collection<SummaryGraphEdge>> computeSubgraphs(Map<String,Collection<ISummaryGraphElement>> ress){
//		
//		for(SummaryGraphElement vertex : verticesOfGraphWithCursors){
//			vertex.createCursors(ress.keySet());
//		}
//
//		for(String keyword : ress.keySet()){
//			for(ISummaryGraphElement element : ress.get(keyword)){
//				if(element instanceof SummaryGraphElement){
//					SummaryGraphElement vertex = (SummaryGraphElement)element; 
//					QueueEntry entry = new QueueEntry(keyword, getVertexInGraphWithCursors(vertex));
//					expansionQueue.add(entry);
//				}
//			}
//		}
//		boolean isFinished = false;
//		while(!isFinished){
//			QueueEntry expansion = expansionQueue.poll();
//			boolean isExhausted = expansion.cursorExpand();
//			expansionQueue.add(expansion);
//			if(isExhausted == true) {
//				expansionQueue.remove(expansion);
//			}
//			if(expansionQueue.size() == 0) {
//				isFinished = true;
//			} else if(!(subgraphQueue.size() < K_TOP)) {
//				double lowestCost = 0;
//				for(QueueEntry entry : expansionQueue){
//					lowestCost =+ entry.getCost(); 
//				}
//				double highestCost = subgraphQueue.peek().getCost();
//				if(lowestCost > highestCost) {
//					isFinished = true;
//				}
//			} 
//		}
//		
//		List<Collection<SummaryGraphEdge>> subgraphs = new LinkedList<Collection<SummaryGraphEdge>>();
//		int size = subgraphQueue.size();
//		System.out.println("computeSubgraphs - subgraphs: " + size);
//		for(int i = 0; i < size; i++){
//			Subgraph subgraph = subgraphQueue.poll();
//			Set<SummaryGraphEdge> edges = subgraph.getPaths();
//			if(!subgraphs.contains(edges)){
//				subgraphs.add(0,edges);
//			}
//		}
//	
//		return subgraphs;
//	}
//	
//	public SummaryGraphResource getVertexInGraphWithCursors(SummaryGraphResource vertex){
//		for(SummaryGraphResource vertexInGraph : verticesOfGraphWithCursors){
//			if(vertex.equals(vertexInGraph)) {
//				return vertexInGraph;
//			}
//		}
//		return null;
//	}
//	
//	private Collection<Collection<OWLPredicate>> computeQueries(Collection<Collection<SummaryGraphProperty>> subgraphs) {
//		Collection<Collection<OWLPredicate>> queries = new LinkedHashSet<Collection<OWLPredicate>>();
//		for(Collection<SummaryGraphProperty> subgraph : subgraphs){
//			Collection<OWLPredicate> query = new LinkedHashSet<OWLPredicate>();
//			queries.add(query);
//			Map<Pair, Variable> labelVar = new LinkedHashMap<Pair, Variable>();
//			UniqueIdGenerator.getInstance().resetVarIds();
//			for(SummaryGraphProperty edge : subgraph){
//				if((edge.getType() == ISummaryGraphElement.ATTRIBUTE) && (edge.getVertex2().getType() != ISummaryGraphElement.DUMMY_VALUE)){
//					IProperty p = edge.getProperty();
//					IResource v1 = edge.getVertex1().getResource();
//					IResource v2 = edge.getVertex2().getResource();
//					IResource t;
//					String conceptLabel = v1.getLabel();
//					String literalLabel = v2.getLabel();
//					Pair label = new Pair(conceptLabel,literalLabel);
//					Variable var = labelVar.get(label); 
//					if (var == null) {
//						t = getNewVariable();
//						labelVar.put(label, (Variable)t);
//						query.add(new ConceptMemberPredicate(v1, t));
//					} else {
//						t = var;
//					}
//				
//					query.add(new PropertyMemberPredicate(p, t,v2));
//				}
//			}
//			for(SummaryGraphProperty edge : subgraph){
//				if((edge.getType() == ISummaryGraphElement.RELATION) && (!edge.getProperty().equals(Property.SUBCLASS_OF))){
//					IProperty p = edge.getProperty();
//					IResource v1 = edge.getVertex1().getResource();
//					IResource v2 = edge.getVertex2().getResource();
//					
//					Set<IResource> t1 = new LinkedHashSet<IResource>();
//					String label1 = v1.getLabel();
//					for(Pair pair : labelVar.keySet()){
//						if(pair.getHead().equals(label1)) {
//							t1.add(labelVar.get(pair));
//						}
//					}
//					if(t1.size() == 0) {
//						Variable var = getNewVariable();
//						t1.add(var);
//						labelVar.put(new Pair(label1,null), var);
//						query.add(new ConceptMemberPredicate(v1, var));
//					}
//					
//					
//					Set<IResource> t2 = new LinkedHashSet<IResource>();
//					String label2 = v2.getLabel();
//					for(Pair pair : labelVar.keySet()){
//						if(pair.getHead().equals(label2)) {
//							t2.add(labelVar.get(pair));
//						}
//					}
//					if(t2.size() == 0) {
//						Variable var = getNewVariable();
//						t2.add(var);
//						labelVar.put(new Pair(label2,null), var);
//						query.add(new ConceptMemberPredicate(v2, var));
//					}
//					
//					for(IResource resource1 : t1){
//						for(IResource resource2 : t2) {
//							query.add(new PropertyMemberPredicate(p, resource1,resource2));
//						}
//					}
//					
//				}
//			}
//			for(SummaryGraphProperty edge : subgraph){
//				if((edge.getType() == ISummaryGraphElement.ATTRIBUTE) && (edge.getVertex2().getType() == ISummaryGraphElement.DUMMY_VALUE)){
//					IProperty p = edge.getProperty();
//					IResource v1 = edge.getVertex1().getResource();
//					IResource v2 = edge.getVertex2().getResource();
//					
//					Set<IResource> t1 = new LinkedHashSet<IResource>();
//					String label1 = v1.getLabel();
//					for(Pair pair : labelVar.keySet()){
//						if(pair.getHead().equals(label1)) {
//							t1.add(labelVar.get(pair));
//						}
//					}
//					if(t1.size() == 0) {
//						Variable var = getNewVariable();
//						t1.add(var);
//						labelVar.put(new Pair(label1,null), var);
//						query.add(new ConceptMemberPredicate(v1, var));
//					}
//					
//					for(IResource resource1 : t1){
//						query.add(new PropertyMemberPredicate(p, resource1,getNewVariable()));
//					}
//				}
//			}
//		}
//		
//		return queries;
//	}
//	
//	public void addGraphElements(IResource resource, WeightedPseudograph<SummaryGraphResource,SummaryGraphProperty> graph){
//		boolean addVertex = false;
//		if(resource instanceof INamedConcept){
//			INamedConcept concept = (INamedConcept)resource;
//			SummaryGraphResource vertex = new SummaryGraphResource(resource, ISummaryGraphElement.CONCEPT);
//			addVertex = graph.addVertex(vertex);
//			if(addVertex) {
//				s_log.debug("Vertex " + vertex + " is added to the graph!");
//			} else {
//				s_log.debug("Vertex " + vertex + " is already in the graph!");
//			}
//				
////			Set<IConcept> subconcepts = concept.getSubconcepts();
////			if(subconcepts != null && subconcepts.size() != 0){
////				for(IConcept con : subconcepts){
////					KbVertex subvertex = new KbVertex(con, KbElement.CVERTEX,computeWeight((INamedConcept)con)); 
////					addVertex = graph.addVertex(subvertex);
////					if(addVertex) s_log.debug("Vertex " + subvertex + " is added to the graph!");
////					else s_log.debug("Vertex " + subvertex + " is already in the graph!");
////					
////					addGraphElements(subvertex, vertex, Property.SUBCLASS_OF, graph);
////				}
////			}
//			
//			Set<Pair> proAndRanges = concept.getPropertiesAndRangesFrom();
//			if((proAndRanges != null) && (proAndRanges.size() != 0)){
//				for(Pair pair : proAndRanges){
//					IProperty property = (IProperty)pair.getHead();
//					if(property instanceof IObjectProperty){
//						INamedConcept range = (INamedConcept)pair.getTail();
//						SummaryGraphResource rangevertex = new SummaryGraphResource(range, ISummaryGraphElement.CONCEPT); 
//						addVertex = graph.addVertex(rangevertex);
//						if(addVertex) {
//							s_log.debug("Vertex " + rangevertex + " is added to the graph!");
//						} else {
//							s_log.debug("Vertex " + rangevertex + " is already in the graph!");
//						}
//						
//						addGraphElements(vertex, rangevertex, property, graph);
//					}
//				}
//			}
//		} 
//	}
//	
//	public void addGraphElements(SummaryGraphResource vertex1, SummaryGraphResource vertex2, IProperty property, WeightedPseudograph<SummaryGraphResource,SummaryGraphProperty> graph){
//		boolean addEdge = false; 
//		SummaryGraphProperty edge = null;
//		if(vertex2.getType() == ISummaryGraphElement.CONCEPT){
//			if(property.equals(Property.SUBCLASS_OF)) {
//				edge = new SummaryGraphProperty(vertex1, vertex2, property, ISummaryGraphElement.RELATION,0);
//			} else {
//				edge = new SummaryGraphProperty(vertex1, vertex2, property, ISummaryGraphElement.RELATION);
//			}
//		}
//		else {
//			edge = new SummaryGraphProperty(vertex1, vertex2, property, ISummaryGraphElement.ATTRIBUTE,1);
//		} 
//			
//		if(!(graph.containsEdge(edge))){
//			addEdge = graph.addEdge(vertex1, vertex2, edge);
//			if(addEdge) {
//				s_log.debug("Edge " + edge + " is added to the graph!");
//			}
//		} else {
//			s_log.debug("Edge " + edge + " is already in the graph!");
//		}
//	}
//	
//	private void addGraphElement(SummaryGraphResource vertex, WeightedPseudograph<SummaryGraphResource,SummaryGraphProperty> graph){
//		boolean addVertex = false;
//		addVertex = graph.addVertex(vertex);
//		if(addVertex) {
//			s_log.debug("Vertex " + vertex + " is added to the graph!");
//		} else {
//			s_log.debug("Vertex " + vertex + " is already in the graph!");
//		}
//		
//	}
//	
//	public void addGraphElement(SummaryGraphProperty edge, WeightedPseudograph<SummaryGraphResource,SummaryGraphProperty> graph){
//		boolean addEdge = false; 
//		if(!(graph.containsEdge(edge))){
//			SummaryGraphResource vertex1 = edge.getVertex1();
//			if(!(graph.containsVertex(vertex1))) {
//				graph.addVertex(vertex1);
//			}
//			SummaryGraphResource vertex2 = edge.getVertex2();
//			if(!(graph.containsVertex(vertex2))) {
//				graph.addVertex(vertex2);
//			}
//			addEdge = graph.addEdge(vertex1, vertex2, edge);
//			if(addEdge) {
//				s_log.debug("Edge " + edge + " is added to the graph!");
//			}
//		} else {
//			s_log.debug("Edge " + edge + " is already in the graph!");
//		}
//	}
//	
//	private Variable getNewVariable(){
//		String var = String.valueOf(UniqueIdGenerator.getInstance().getNewVarId());
//		return new Variable("x" + var);
//	}
//	
//	public Set<Variable> getRankedVariables(Collection<OWLPredicate> query){
//		Set<Variable> vars = new LinkedHashSet<Variable>();
//		if((query == null) || (query.size() == 0)) {
//			return null;
//		} 
//
//		//TODO perform the ranking
//		for (OWLPredicate p : query) {
//			IResource var1 = null;
//			IResource var2 = null;
//
//			if (p instanceof PropertyMemberPredicate) {
//				var1 = ((PropertyMemberPredicate)p).getFirstTerm();
//				var2 = ((PropertyMemberPredicate)p).getSecondTerm();
//			}
//			else if (p instanceof ConceptMemberPredicate) {
//				var1 = ((ConceptMemberPredicate)p).getConcept();
//				var2 = ((ConceptMemberPredicate)p).getTerm();
//			}
//			if(var1 instanceof Variable) {
//				vars.add((Variable)var1);
//			}
//			if(var2 instanceof Variable) {
//				vars.add((Variable)var2);
//			}
//		}
//
//		return vars; 
//	}
//
//	public boolean isSubConcept(Object subconcept, Object superconcept){
//		if ((subconcept instanceof INamedConcept) && (superconcept instanceof INamedConcept)){
//			INamedConcept sup = (INamedConcept)superconcept;
//			INamedConcept sub = (INamedConcept)subconcept;
//			if(sup.getSubconcepts() !=  null){
//				if (sup.getSubconcepts().contains(sub)) {
//					return true;
//				} else {
//					for (Object obj : sup.getSubconcepts()) {
//						if(isSubConcept(sub,obj)) {
//							return true;
//						}
//					}
//				}
//			} else {
//				return false;
//			}
//		}
//		return false;
//	} 
//
//	
//	public int mul(int a[],int n){
//	    return n>0?((a[n-1]+1)*mul(a,--n)):1;
//	}
//	
//	class QueueEntry implements Comparable {
//		
//		private ClosestFirstIterator<SummaryGraphResource, SummaryGraphProperty> iter;
//		
//		private String keyword;
//		
//		private SummaryGraphResource matchingVertex;
//		
//		private float score;
//		
//		private double cost = 0;
//		
//		public QueueEntry(String keyword, SummaryGraphResource startVertex){
//			this.matchingVertex = startVertex;
//			this.score = startVertex.getScore();
//			this.keyword = keyword; 
//			
//			if(m_distance > 0){
////				iter =  new ClosestFirstIterator<KbVertex, KbEdge>(resourceGraph, matchingVertex, width);
//				iter =  new ClosestFirstIterator<SummaryGraphResource, SummaryGraphProperty>(resourceGraph, matchingVertex);
//				iter.next();
//			}	
//			else{
//				iter =  new ClosestFirstIterator<SummaryGraphResource, SummaryGraphProperty>(resourceGraph, matchingVertex);
//				iter.next();
//			}	
//		}
//		
//		public boolean cursorExpand(){
//			if(!iter.hasNext()) {
//				return true;
//			} else {
//				 SummaryGraphResource endVertex = iter.next();
////				 Map<String,Queue<Cursor>> cursors = getVertexInGraphWithCursors(endVertex).getCursors();
//				 Map<String,Queue<Cursor>> cursors = null;
//				 List<SummaryGraphProperty> path = createEdgeList(resourceGraph, iter, endVertex);
//
//				 double pathCost = iter.getShortestPathLength(endVertex);
//				 cost =  pathCost/score;
////				 System.out.println("(" + matchingVertex + ")" + " to " + "(" + endVertex + ")");
////				 System.out.println("cost: " + cost);
//				 if(cost > threshold) {
//					return true;
//				 }
//				 cursors.get(keyword).add(new Cursor(matchingVertex, cost, path));
//				 
//				 boolean isConnectingVertex = false;
//				 double subgraphCost = 0;
//		         List<List<Cursor>> allCursors = new ArrayList<List<Cursor>>();
//		         for(Queue<Cursor> queue : cursors.values()){
//		           	if(queue.size() != 0){
//		           		isConnectingVertex = true;
//		           		Cursor cursor = queue.peek();
//		           		double cursorCost = cursor.getCost();
//		          		if(cursorCost > threshold) {
//							break;
//						}
//		           		subgraphCost += cursorCost;
//		          		
//		          		List<Cursor> css = new ArrayList<Cursor>();
//		          		css.addAll(queue);
//		           		allCursors.add(css);
//		           	}
//		           	else {
//		           		isConnectingVertex = false;
//		           		break;
//		           	}	
//		         }
//		         if((isConnectingVertex == true) && (subgraphCost <= threshold)){
//		          	connectingElements.add(endVertex);
//		          	Cursor[][] targetCursors = computeTargetCursors(allCursors);
//		          	out:for(int i = 0; i < targetCursors.length; i++){
//						Cursor[] curs = targetCursors[i];
//						double cost = 0;
//						Set<SummaryGraphProperty> edges = new LinkedHashSet<SummaryGraphProperty>();
//						for(Cursor cursor : curs){
//							cost += cursor.getCost();
//							if(cost > threshold) {
//								continue out;
//							}
//							edges.addAll(cursor.getPath());
//						}
//						
//						boolean isSubgraph = false;
//						if((keywordREdgeMap != null) && (keywordREdgeMap.size() != 0)){
//							for(Collection<ISummaryGraphElement> collection : keywordREdgeMap.values()){
//								isSubgraph = !Collections.disjoint(edges, collection);
//								if(!isSubgraph) {
//									break;
//								}
//							}
//						} else {
//							isSubgraph = true;
//						}
//						if(isSubgraph){
//							Subgraph subgraph = new Subgraph(endVertex, edges, cost);
//							if(!subgraphQueue.contains(subgraph)) {
//								if(subgraphQueue.size() < K_TOP) {
//									subgraphQueue.add(subgraph);
//								} else {
//									double highestCost = subgraphQueue.peek().getCost();
//									if(subgraph.getCost() < highestCost) {
//										subgraphQueue.poll();
//										subgraphQueue.add(subgraph);
//									}
//								}	
//							}
//							else {
//								for(Subgraph sub : subgraphQueue){
//									if(sub.equals(subgraph)){
//										if(sub.getCost() > subgraph.getCost())
//											sub.setCost(subgraph.getCost());
//									}
//								}
//							}
//						}
//		          	}
//		         }
//			}
//			return false;
//		}
//		
//		public List<SummaryGraphProperty> createEdgeList(Graph<SummaryGraphResource, SummaryGraphProperty> graph, ClosestFirstIterator<SummaryGraphResource, SummaryGraphProperty> iter, SummaryGraphResource endVertex){
//		
//			List<SummaryGraphProperty> edgeList = new ArrayList<SummaryGraphProperty>();
//
//	        while (true) {
//	            SummaryGraphProperty edge = iter.getSpanningTreeEdge(endVertex);
//
//	            if (edge == null) {
//					break;
//				}
//	            edgeList.add(edge);
//	            endVertex = Graphs.getOppositeVertex(graph, edge, endVertex);
//	        }
//	        Collections.reverse(edgeList);
//	        return edgeList;
//		}
//		
//		private Cursor[][] computeTargetCursors(List<List<Cursor>> cursors) {
//			int size = cursors.size();
//			int[] guard = new int[size];
//			int i = 0;
//			for(List<Cursor> list : cursors){
//				guard[i++] = list.size()-1;
//			}
//			int entrySize = mul(guard,size);
//			Cursor[][] entries = new Cursor[entrySize][size];
//			
//			int[] index = new int[size];
//			for(int p : index) {
//				p = 0;
//			} 
//			guard[size-1]++;
//			i = 0;
//			do {
//				for(int m = 0; m < size; m++){
//					entries[i][m] = cursors.get(m).get(index[m]);
//				}
//				i++;
//				index[0]++;
//				for(int j = 0; j < size; j++){
//					if(index[j] > guard[j]){
//						index[j] = 0;
//						index[j+1]++; 
//					}
//				}
//			}
//			while(index[size-1] < guard[size-1]);
//			
//			return entries;
//		}
//
//		public double getCost() {
//			return cost; 
//		}
//		
//		public int compareTo(Object o) {
//			QueueEntry other = (QueueEntry)o;
//			if(cost > other.cost) {
//				return 1;
//			}
//			if(cost < other.cost) {
//				return -1;
//			}
//			return 0;
//		}
//		
//	}
//	

//	class Subgraph implements Comparable {
//		
//		private SummaryGraphResource connectingVertex;
//		
//		private Set<SummaryGraphProperty> paths;
//		
//		double cost;
//		
//		public Subgraph(SummaryGraphResource connectingVertex, Set<SummaryGraphProperty> paths, double cost){
//			this.connectingVertex = connectingVertex;
//			this.cost = cost;
//			this.paths = paths;
//		}
//		
//		public Set<SummaryGraphProperty> getPaths(){
//			return paths;
//		}
//		
//		public SummaryGraphResource getConnectingVertex(){
//			return connectingVertex;
//		}
//		
//		public void setCost(double cost){
//			this.cost = cost;
//		}
//		
//		public double getCost(){
//			return cost;
//		}
//		
//		public int compareTo(Object o){
//			Subgraph other = (Subgraph)o;
//			if(this.cost > other.cost) {
//				return -1;
//			}
//			if(this.cost < other.cost) {
//				return 1;
//			}
//			return 0;
//		}
//		
//		@Override
//		public boolean equals(Object o){
//			if(this == o) {
//				return true;
//			}
//			if(!(o instanceof Subgraph)) {
//				return false;
//			}
//			
//			Subgraph other = (Subgraph)o;
//			if(!(paths.equals(other.getPaths()))) {
//				return false;
//			}
//			return true;
//		}
//		
//		@Override
//		public int hashCode(){
//			return 13*paths.hashCode();
//		}
//		
//		@Override
//		public String toString(){
//			return "cost: " + cost 
//				+ "\n" + "Connecting vertex: " + connectingVertex
//				+ "\n" + "Paths: " + paths
// 				+ "\n";
//		}
//	}
//}
