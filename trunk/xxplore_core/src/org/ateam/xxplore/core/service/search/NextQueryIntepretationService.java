//package org.ateam.xxplore.core.service.search;
//
//
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.IOException;
//import java.io.ObjectInputStream;
//import java.util.Collection;
//import java.util.Iterator;
//import java.util.LinkedHashMap;
//import java.util.LinkedHashSet;
//import java.util.LinkedList;
//import java.util.List;
//import java.util.Map;
//import java.util.PriorityQueue;
//import java.util.Set;
//
//import org.aifb.xxplore.shared.util.Pair;
//import org.aifb.xxplore.shared.util.UniqueIdGenerator;
//import org.apache.log4j.Logger;
//import org.ateam.xxplore.core.ExploreEnvironment;
//import org.ateam.xxplore.core.service.IServiceListener;
//import org.ateam.xxplore.core.service.search.QueryInterpretationService.QueueEntry;
//import org.ateam.xxplore.core.service.search.QueryInterpretationService.Subgraph;
//import org.jgrapht.graph.WeightedPseudograph;
//import org.xmedia.oms.model.api.INamedConcept;
//import org.xmedia.oms.model.api.IObjectProperty;
//import org.xmedia.oms.model.api.IOntology;
//import org.xmedia.oms.model.api.IProperty;
//import org.xmedia.oms.model.api.IResource;
//import org.xmedia.oms.model.impl.Property;
//import org.xmedia.oms.persistence.SessionFactory;
//import org.xmedia.oms.persistence.StatelessSession;
//import org.xmedia.oms.query.ConceptMemberPredicate;
//import org.xmedia.oms.query.OWLPredicate;
//import org.xmedia.oms.query.PropertyMemberPredicate;
//import org.xmedia.oms.query.Variable;
//
//public class NextQueryIntepretationService implements IQueryInterpretationService {
//
//	private static Logger s_log = Logger.getLogger(NextQueryIntepretationService.class);
//	
//	private int TOTAL_NUMBER_OF_VERTEX = -1; 
//	
//	private int TOTAL_NUMBER_OF_EDGE = -1;
//	
//	private int K_TOP = 15;
//
//	private WeightedPseudograph<SummaryGraphResource,SummaryGraphProperty> resourceGraph;
//	
//	private Set<SummaryGraphResource> verticesOfGraphIndex;
//	
//	private Set<SummaryGraphResource> verticesOfGraphWithCursors;
//	
//	private Collection<Collection<OWLPredicate>> queries;
//	
//	private Map<String,Collection<ISummaryGraphElement>> resources;
//	
//	private Collection<Collection<SummaryGraphProperty>> subgraphs;
//	
//	private PriorityQueue<QueueEntry> expansionQueue;
//	
//	private PriorityQueue<Subgraph> subgraphQueue;
//	
//	private Set<SummaryGraphResource> connectingElements;
//	
//	private Set<ISummaryGraphElement> matchingREdges;
//	
//	private Map<String,Collection<ISummaryGraphElement>> keywordREdgeMap;
//	
//	private double threshold = 50;
//	
//	private int width = 0;
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
//	public Collection<Collection<OWLPredicate>> computeQueries(Map<String,Collection<ISummaryGraphElement>> elements, String datasourceUri, int width, int depth) {
//		
//		if (elements == null) {
//			return null;
//		}
//
//		if(width > 0) {
//			this.width = width;
//		}
//		
//		computeTotalNumber();
//		expansionQueue = new PriorityQueue<QueueEntry>();
//		subgraphQueue = new PriorityQueue<Subgraph>();
//		connectingElements = new LinkedHashSet<SummaryGraphResource>();
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
//		Map<String,IProperty> keywordAttributeMap = new LinkedHashMap<String, IProperty>();
//		Set<SummaryGraphProperty> existingAEdges = new LinkedHashSet<SummaryGraphProperty>();
//		Set<String> keywords = new LinkedHashSet<String>();
//		for(String keyword : ress.keySet()){
//			boolean allEdges = false;
//			Collection<ISummaryGraphElement> collection = ress.get(keyword);
//			for(Iterator<ISummaryGraphElement> ite = collection.iterator(); ite.hasNext(); ){
//				ISummaryGraphElement element = ite.next();
//				if(element instanceof SummaryGraphResource && element.getType() == ISummaryGraphElement.CONCEPT){
//					double weight = computeWeight((INamedConcept)((SummaryGraphResource)element).getResource());
//					if(weight == Double.POSITIVE_INFINITY)
//						ite.remove();
//				}
//				else if(element instanceof SummaryGraphProperty && element.getType() == ISummaryGraphElement.RELATION){
//					double weight = computeWeight((IProperty)((SummaryGraphProperty)element).getProperty());
//					if(weight == Double.POSITIVE_INFINITY)
//						ite.remove();
//					else {
//						double weight1 = computeWeight((INamedConcept)((SummaryGraphProperty)element).getVertex1().getResource());
//						double weight2 = computeWeight((INamedConcept)((SummaryGraphProperty)element).getVertex2().getResource());
//						if(weight1 == Double.POSITIVE_INFINITY || weight2 == Double.POSITIVE_INFINITY)
//							ite.remove();
//					}
//				}
//				else if(element instanceof SummaryGraphProperty && element.getType() == ISummaryGraphElement.ATTRIBUTE){
//					SummaryGraphProperty edge = (SummaryGraphProperty)element;
//					IProperty dataprop = edge.getProperty();
//					SummaryGraphResource vertex = edge.getVertex2();
//					if(vertex.getType() == ISummaryGraphElement.VALUE){
//						existingAEdges.add(edge);
//					}
//					else if(vertex.getType() == ISummaryGraphElement.DUMMY_VALUE){
//						keywordAttributeMap.put(keyword, dataprop);
//					} 
//				}
//			}
//			if(collection == null || collection.size() == 0){
//				keywords.add(keyword);
//			}	
//			for(ISummaryGraphElement element : collection){
//				if(element instanceof SummaryGraphProperty && element.getType() == ISummaryGraphElement.RELATION) {
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
//				keywords.add(keyword);
//			}
//		}
//		for(String keyword : keywords){
//			ress.remove(keyword);
//		}
//		for(String attribute : keywordAttributeMap.keySet()){
//			IProperty keyattr = keywordAttributeMap.get(attribute); 
//			for(SummaryGraphProperty edge : existingAEdges){
//				IProperty attr = edge.getProperty();
//				if(keyattr.equals(attr)){
//					ress.get(attribute).add(edge.getVertex2());
//				}
//			}
//		}
//		
//		return ress;
//	}
//	
//	public WeightedPseudograph<SummaryGraphResource,SummaryGraphProperty> computeGraphSchemaIndex(String datasourceUri){
//		
//		WeightedPseudograph<SummaryGraphResource,SummaryGraphProperty> resourceGraph = null;
//		File graphIndex = new File(ExploreEnvironment.GRAPH_INDEX_DIR, datasourceUri + ".graph");
//		ObjectInputStream in;
//		
//		try {
//			in = new ObjectInputStream(new FileInputStream(graphIndex));
//			resourceGraph = (WeightedPseudograph<SummaryGraphResource,SummaryGraphProperty>)in.readObject(); 
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
//				if(element.getType() == ISummaryGraphElement.VALUE){
//					SummaryGraphResource vertex = (SummaryGraphResource)element;
//					addGraphElement(vertex, graph);
//				}
//				else if(element.getType() == ISummaryGraphElement.CONCEPT){
//					SummaryGraphResource vertex = (SummaryGraphResource)element;
//					for(SummaryGraphResource ver : verticesOfGraphIndex){
//						if(ver.equals(vertex)) {
//							ver.setScore(vertex.getScore());
//						}
//					}
//					addGraphElement(vertex, graph);
//				}
//				else if(element.getType() == ISummaryGraphElement.ATTRIBUTE){
//					SummaryGraphProperty edge = (SummaryGraphProperty)element;
//					addGraphElement(edge, graph);
//				}
//			}
//		}
//		
//		for(SummaryGraphProperty edge : graph.edgeSet()){
//			if(!matchingREdges.contains(edge)){
//				if(edge.getProperty().equals(Property.SUBCLASS_OF)){
//					graph.setEdgeWeight(edge, 3);
//				} 
//				else {	
//					double weight = edge.getVertex1().getCost()+edge.getVertex2().getCost()+edge.getCost();
//					graph.setEdgeWeight(edge, weight);
//				}
//			}	
//			else {
//				double weight = edge.getVertex1().getCost()+edge.getVertex2().getCost();
//				graph.setEdgeWeight(edge, 3);
//			}	
//		}
//		
//		verticesOfGraphWithCursors = graph.vertexSet();
//	}
//	
//	
//	
//	public Collection<Collection<SummaryGraphProperty>> computeSubgraphs(Map<String,Collection<ISummaryGraphElement>> ress){
//		
//		for(SummaryGraphResource vertex : verticesOfGraphWithCursors){
//			vertex.createCursors(ress.keySet());
//		}
//
//		for(String keyword : ress.keySet()){
//			for(ISummaryGraphElement element : ress.get(keyword)){
//				if(element instanceof SummaryGraphResource){
//					SummaryGraphResource vertex = (SummaryGraphResource)element; 
//					QueueEntry entry = null;//new QueueEntry(keyword, getVertexInGraphWithCursors(vertex));
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
//		List<Collection<SummaryGraphProperty>> subgraphs = new LinkedList<Collection<SummaryGraphProperty>>();
//		int size = subgraphQueue.size();
//		System.out.println("computeSubgraphs - subgraphs: " + size);
//		for(int i = 0; i < size; i++){
//			Subgraph subgraph = subgraphQueue.poll();
//			Set<SummaryGraphProperty> edges = subgraph.getPaths();
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
//			SummaryGraphResource vertex = new SummaryGraphResource(resource, ISummaryGraphElement.CONCEPT,computeWeight(concept));
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
//						SummaryGraphResource rangevertex = new SummaryGraphResource(range, ISummaryGraphElement.CONCEPT,computeWeight(range)); 
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
//				edge = new SummaryGraphProperty(vertex1, vertex2, property, ISummaryGraphElement.RELATION,computeWeight(property));
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
//	private void computeTotalNumber(){
//		if((TOTAL_NUMBER_OF_VERTEX != -1) && (TOTAL_NUMBER_OF_EDGE != -1)) {
//			return;
//		}
//		
//		StatelessSession session = (StatelessSession)SessionFactory.getInstance().getCurrentSession();
//		IOntology onto = session.getOntology();
//		
//		int numConcept = onto.getNumberOfConcept();
////		System.out.println("number of Concept: " + numConcept);
//		
//		int numIndividual = onto.getNumberOfIndividual();
////		System.out.println("number of Individual: " + numIndividual);
//		
//		int numoProperty = onto.getNumberOfObjectProperty();
////		System.out.println("number of ObjectProperty: " + numoProperty);
//		
//		int numoPropertyMember = onto.getNumberOfObjectPropertyMember();
////		System.out.println("number of ObjectPropertyMember: " + numoPropertyMember);
//		
//		TOTAL_NUMBER_OF_VERTEX = numConcept + numIndividual;
//		
//		TOTAL_NUMBER_OF_EDGE = numoProperty + numoPropertyMember;
//	}
//	
//	private double computeEdgeWeight(double num, double totalNum){
//		if(num == 0) {
//			return Double.POSITIVE_INFINITY;
//		}
//		return 2-Math.log(1+num/totalNum)/Math.log(2);
//	}
//	
//	private double computeVertexWeight(double num, double totalNum){
//		if(num == 0) {
//			return Double.POSITIVE_INFINITY;
//		}
//		return 2-Math.log(1+num/totalNum)/Math.log(2); 
//	}
//	
//	private double computeWeight(INamedConcept concept){
//		int numIndividual = concept.getNumberOfIndividuals();
//		return computeVertexWeight(numIndividual,TOTAL_NUMBER_OF_VERTEX);
//	}
//	
//	private double computeWeight(IProperty property){
//		int numProMem = property.getNumberOfPropertyMember();
//		return computeEdgeWeight(numProMem,TOTAL_NUMBER_OF_EDGE);
//	} 
//	
//	public int mul(int a[],int n){
//	    return n>0?((a[n-1]+1)*mul(a,--n)):1;
//	}
//
//
//}
