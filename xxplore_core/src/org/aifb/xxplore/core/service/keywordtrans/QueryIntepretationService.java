package org.aifb.xxplore.core.service.keywordtrans;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.aifb.xxplore.core.ExploreEnvironment;
import org.aifb.xxplore.core.model.definition.EntityDefinition;
import org.aifb.xxplore.core.model.definition.IModelDefinition;
import org.aifb.xxplore.core.model.definition.ModelDefinition;
import org.aifb.xxplore.core.model.definition.RelationDefinition;
import org.aifb.xxplore.core.service.IService;
import org.aifb.xxplore.core.service.IServiceListener;
import org.aifb.xxplore.shared.exception.Emergency;
import org.aifb.xxplore.shared.util.Pair;
import org.aifb.xxplore.shared.util.UniqueIdGenerator;
import org.apache.log4j.Logger;
import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.xmedia.oms.model.api.IConcept;
import org.xmedia.oms.model.api.IIndividual;
import org.xmedia.oms.model.api.ILiteral;
import org.xmedia.oms.model.api.INamedConcept;
import org.xmedia.oms.model.api.INamedIndividual;
import org.xmedia.oms.model.api.IProperty;
import org.xmedia.oms.model.api.IPropertyMember;
import org.xmedia.oms.model.api.IResource;
import org.xmedia.oms.model.impl.NamedConcept;
import org.xmedia.oms.model.impl.Property;
import org.xmedia.oms.query.ConceptMemberPredicate;
import org.xmedia.oms.query.OWLPredicate;
import org.xmedia.oms.query.PropertyMemberPredicate;
import org.xmedia.oms.query.Variable;

public class QueryIntepretationService implements IQueryInterpretationService {

	private static Logger s_log = Logger.getLogger(QueryIntepretationService.class);

	//a graph computed by traversing neighbors of resources matching the query terms 
	//this graph may contains many connected components, i.e. contains many subgraphs
	//A simple directed graph is a directed graph in which neither multiple edges between any two vertices 
	//nor loops are permitted.
	private SimpleDirectedGraph<IResource, KbEdge> m_resourceGraph;

	//the largest subgraph of the computed resource graph
	private SimpleDirectedGraph<IResource, KbEdge> m_largestResourceGraph;

	private Collection<Collection<OWLPredicate>> m_queries;
	
	//Allows obtaining various connectivity aspects of a graph. The inspected graph is specified at construction time and cannot be modified. 
	//Currently,the inspector supports connected components for an undirected graph and weakly connected components for a directed graph.
	//To find strongly connected components, use StrongConnectivityInspector instead.
	private ConnectivityInspector<IResource, KbEdge> m_conInspector;
	
	//provided resources for building resource graph
	private Collection<IResource> m_resources;
	
	//individuals or literals that already have been traversed during E-P-E traversal, where E stands for I(individual) or J (literal) 
	private List<IResource> m_processed;
	
	//properties that already have been traversed during E-P-E traversal, where E stands for I(individual) or J (literal) 
	private Set<IProperty> m_traversedProperties;
	
	//provided resources existing in largestCC
	private Collection<IResource> m_matchingResources;

	private int m_width;

	private int m_defaultwidth = 1;

	public QueryIntepretationService(){
		init();
	}

	public void callService(IServiceListener listener, Object... params) {}

	public void disposeService() {
		// TODO Auto-generated method stub
	}

	public void init(Object... params) {}	


	//TODO check of query belong to the list that have been previously computed 
	public Set<Variable> getRankedVariables(Collection<OWLPredicate> query){
		Set<Variable> vars = new HashSet<Variable>();
		if(query == null || query.size() == 0) return null; 

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
			if(var1 instanceof Variable) 
				vars.add((Variable)var1);
			if(var2 instanceof Variable) 
				vars.add((Variable)var2);
		}

		return vars; 
	}

	/**
	 * Return a ranked collection of queries. 
	 * @param resources
	 * @param width
	 * @param depth
	 * @return
	 */
	public Collection<Collection<OWLPredicate>> computeQueries(Collection<IResource> resources, int width, int depth){
		if (resources == null) return null;

		m_resources = resources;
		m_traversedProperties = new HashSet<IProperty>();
		m_processed = new ArrayList<IResource>();
		m_resourceGraph =  new SimpleDirectedGraph<IResource, KbEdge>(KbEdge.class); 
		m_largestResourceGraph = null;
		m_queries = new ArrayList<Collection<OWLPredicate>>();
		m_conInspector = new ConnectivityInspector<IResource, KbEdge>(m_resourceGraph);

		Collection<IResource> ress = new ArrayList<IResource>();
		ress.addAll(m_resources);
		s_log.debug("BUILDING RESOURCE GRAPHS from provided these resources: " + m_resources + " size:" + m_resources.size());
		computeResourceGraphs(ress, width, depth);
		s_log.debug("------RESOURCEGRAPH:"  + m_resourceGraph);
		SimpleDirectedGraph<IResource, KbEdge> largestCC = getLargestConnectedComponent(m_resourceGraph);
		s_log.debug("------LARGEST SUBGRAPH OF RESOURCEGRAPH:"  + largestCC);
		//compute the resource that are covered by the largest CC 
		m_matchingResources = computeMatchingResources(largestCC, m_resources);
		s_log.debug("------RESOURCES: " + m_resources + " size:" + m_resources.size());
		s_log.debug("------MATCHING RESOURCES:" + m_matchingResources);
		m_queries = computeQueries(largestCC);
		s_log.debug("------QUERIES:" + m_queries);
		m_queries = rankQueries(m_queries);
		s_log.debug("------RANKED QUERIES:"  + m_queries);

		return m_queries;
	}

	private void computeResourceGraphs(Collection<IResource> ress, int width, int depth){
		if (width > 0) m_width = width; else m_width = m_defaultwidth;
		Collection<IResource> tmp = new ArrayList<IResource>();
		tmp.addAll(ress);
		for(IResource res : tmp){
			if (res instanceof IIndividual) {
				traverseRelatedResources((INamedIndividual) res, m_width, true);
				ress.remove(res);
			}
			else if (res instanceof ILiteral) {
				traverseRelatedResources((ILiteral) res, m_width, true);
				ress.remove(res);
			}
		}

		for (IResource res : ress){
			if(res instanceof IProperty){
				if (m_traversedProperties.contains((IProperty)res))
					continue;
			}
			else if(m_resourceGraph.containsVertex(res))
				continue;

			if (res instanceof IConcept) traverseRelatedResources((INamedConcept) res, m_width);
			else if (res instanceof IProperty) traverseRelatedResources((IProperty) res, m_width);

		}
	}


	private void traverseRelatedResources(INamedConcept concept, int width) {
		s_log.debug("TRAVERSE FROM CONCEPT: " + concept);

		// traverse C-I: the objective is to connect this concept to other concepts (elements) of the resource graph 
		// use heuristics to travel only some concept individuals
		// heuritic 1: do traversal only if concept is not already discovered, i.e. is already an element of the graph   
		if(m_resourceGraph.containsVertex(concept)) return; 
		int counter = 0;

		Set<IIndividual> inds = concept.getMemberIndividuals();
		if(inds == null || !(inds.size() > 0)) return; 
		//heuritic 2: do traversal until maxInds has been reached or concept has been connected with one another mathing concept
		for (IIndividual ind : inds){
			if(ind instanceof INamedIndividual && !isConceptConnected(concept)){
				KbEdge edge = new KbEdge(concept,ind,Property.IS_INSTANCE_OF);
				if(m_resourceGraph.containsEdge(edge)) return; 
				if(!m_resourceGraph.containsVertex(ind)){
					if(counter < ExploreEnvironment.MAX_NO_OF_TRAVERSALS){
						traverseRelatedResources((INamedIndividual)ind, width, true);
						counter ++;
					}
					else return;
				}
				//already connected with another individual 
				//TODO: maybe this connectedness is not sufficient to provide connections to one other matching concept... 
				else return;	
			}
		}
	}

	/**
	 * Check if the concept is already connected with any other concepts matching the query terms.
	 * @param concept
	 * @return
	 */
	private boolean isConceptConnected(INamedConcept concept){
		//Returns a list of Set s, where each set contains all vertices that are in the same maximally connected component.
		List<Set<IResource>> connectedSets = m_conInspector.connectedSets();
		List<INamedConcept> matchingCons = new ArrayList<INamedConcept>();
		for(IResource res : m_resources){
			if (res instanceof INamedConcept && !res.equals(concept)) 
				matchingCons.add((INamedConcept)res);
		}

		if(connectedSets == null || connectedSets.size() == 0) return false; 
		else{
			boolean matchCon;
			boolean matchAnotherCon; 
			// return true if the concept as well as one of the matching concept are vertices of
			// one connected component of the graph 
			for(Set<IResource> cc : connectedSets){
				matchCon = false;
				matchAnotherCon = false; 
				for(IResource res : cc){
					if(res instanceof INamedConcept){
						if(!matchAnotherCon) {
							for(INamedConcept con : matchingCons){
								if (con.equals(res)) matchAnotherCon = true;
							}
						}
						if(concept.equals(res)) matchCon = true;
					}
				}
				if(matchCon && matchAnotherCon) return true;
			}
		}
		return false; 
	}


	private void traverseRelatedResources(INamedIndividual ind, int width, boolean add) {
		s_log.debug("TRAVERSE FROM INDIVIDUAL: " + ind);
		//note this is the case when this is method is called for the first time, i.e. not at recursion
		if (add){
			m_resourceGraph.addVertex(ind);
			s_log.debug("Vertex " + ind.getLabel() + " has been added to resource graph " + m_resourceGraph);
		}

		//traverse I-C
		Set<IConcept> cons = ind.getTypes();
		if(cons != null && cons.size() > 0){
			for(IConcept concept : cons){
				addGraphElements(ind, concept, Property.IS_INSTANCE_OF, DESCENDANT, m_resourceGraph);
			}
		}		
		else addGraphElements(ind, NamedConcept.TOP, Property.IS_INSTANCE_OF, DESCENDANT, m_resourceGraph);				

		if(m_processed.contains(ind) || width == 0) return;
		//traverse I-P-E,where E stands for I(individual) or J (literal) 	
		m_processed.add(ind);
		Set<IPropertyMember> propmembers = ind.getPropertyFromValues();
		if (propmembers != null && propmembers.size() != 0) {
			for (IPropertyMember propmember : propmembers){
				m_traversedProperties.add(propmember.getProperty());
				IResource target = propmember.getTarget();
				if (target.getLabel().length() < ExploreEnvironment.MAX_LABEL_LENGTH){
					addGraphElements(ind, target, propmember.getProperty(), DESCENDANT, m_resourceGraph);
					if (target instanceof INamedIndividual) 
						traverseRelatedResources((INamedIndividual)target, width - 1, false);
					//is literal
					else traverseRelatedResources((ILiteral)target, width - 1, false);
				}
			}
		}
	}

	private void traverseRelatedResources(ILiteral lit, int width, boolean add) {
		s_log.debug("TRAVERSE FROM LITERAL: " + lit);

		//note this is the case when this is method is called for the first time, i.e. not at recursion
		if (add){
			m_resourceGraph.addVertex(lit);
			s_log.debug("Vertex " + lit.getLabel() + " has been added to resource graph " + m_resourceGraph);
		}

		if(m_processed.contains(lit) || width == 0) return;
		m_processed.add(lit);
		//traverse I-P-J
		Set<IPropertyMember> propmembers = lit.getPropertyToValues();
		if (propmembers != null && propmembers.size() > 0) {
			//find related individuals of individuals that are related to this literal via a property (source)
			for (IPropertyMember propmember : propmembers){
				m_traversedProperties.add(propmember.getProperty());
				IResource source = propmember.getSource();
				addGraphElements(lit, source, propmember.getProperty(), ANCESTOR, m_resourceGraph);

				if (source instanceof INamedIndividual) traverseRelatedResources((INamedIndividual)source, width - 1, false);
				//is literal
				else traverseRelatedResources((ILiteral)source, width - 1, false);
			}
		}

	}

	private void traverseRelatedResources(IProperty prop, int width) {
		s_log.debug("TRAVERSE FROM PROPERTY: " + prop);
		//traverse P-I
		Set<IPropertyMember> propmembers = prop.getMemberIndividuals();
		if (propmembers == null || propmembers.size() == 0) return; 

		int counter = 0;
		for (IPropertyMember propmember : propmembers){
			counter ++;
			IResource source = propmember.getSource();
			if (source instanceof INamedIndividual) traverseRelatedResources((INamedIndividual)source, width, true);
			//is literal
			else 
				traverseRelatedResources((ILiteral)source, width, true);

			IResource target  = propmember.getTarget();
			if (target instanceof INamedIndividual) traverseRelatedResources((INamedIndividual)target, width , true);			
			//is literal
			else 
				traverseRelatedResources((ILiteral)target, width , true);

			if (counter == ExploreEnvironment.MAX_NO_OF_TRAVERSALS) return;
		}

	}


	private Collection<Collection<OWLPredicate>> computeQueries(SimpleDirectedGraph<IResource, KbEdge> resourceGraph){

		List<Stack> prunedGraph = pruneGraph((SimpleDirectedGraph<IResource, KbEdge>)resourceGraph.clone());
		s_log.debug("------PRUNED GRAPH:" + prunedGraph);
		List<SimpleDirectedGraph<IResource, KbEdge>> subgraphs = computeSubgraphs(prunedGraph);
		s_log.debug("------SUBGRAPHS:" + subgraphs);
		Collection<Collection<OWLPredicate>> queries = new ArrayList<Collection<OWLPredicate>>();
		if (subgraphs == null || subgraphs.size() == 0) return null;
		for(SimpleDirectedGraph<IResource, KbEdge> subgraph : subgraphs){
			queries.add(computeQuery(subgraph));
		}
		return queries;
	}

	private List<OWLPredicate> computeQuery(SimpleDirectedGraph<IResource, KbEdge>graph){
		List<OWLPredicate> query = new ArrayList<OWLPredicate>();
		Map<String, Variable> labelVar = new HashMap<String, Variable>(); 
		//reset vars 
		UniqueIdGenerator.getInstance().resetVarIds();

		for (KbEdge edge : graph.edgeSet()){
			IResource v1 = edge.m_vertex1;
			IResource v2 = edge.m_vertex2;
			IProperty p = edge.getProperty();
			if (p.getUri() == ExploreEnvironment.IS_INSTANCE_OF_URI){
				Emergency.checkPrecondition(v2 instanceof IConcept, "v2 instanceof IConcept");
				IResource t;
				if(matchQueryTerm(v1)) t = v1;
				else {
					String label = v1.getLabel();
					Variable var = (Variable)labelVar.get(label); 
					if (var == null) {
						t = getNewVariable();
						labelVar.put(label, (Variable)t);
					}
					else t = var;
				}
				query.add(new ConceptMemberPredicate(v2, t));
			}
			else{
				IResource t1;
				IResource t2;
				if(matchQueryTerm(v1)) t1 = v1;
				else {
					String label = v1.getLabel();
					Variable var = (Variable)labelVar.get(label); 
					if (var == null) {
						t1 = getNewVariable();
						labelVar.put(label, (Variable)t1);
					}
					else t1 = var;
				}
				if(matchQueryTerm(v2)) t2 = v2;
				else {
					String label = v2.getLabel();
					Variable var = (Variable)labelVar.get(label); 
					if (var == null) {
						t2 = getNewVariable();
						labelVar.put(label, (Variable)t2);
					}
					else t2 = var;
				}
				query.add(new PropertyMemberPredicate(p, t1,t2));
			}

		}

		return query;
	}


	/**
	 * Rank queries according to their length 
	 * TODO this is an aprroximation. To obtian more precise ranking, the queries need to be ranked according to the "shortest connecting path". 
	 * @param queries
	 * @return
	 */
	private Collection<Collection<OWLPredicate>> rankQueries(Collection<Collection<OWLPredicate>> queries){
		if(queries == null || queries.size() == 0) return null;
		List<Collection<OWLPredicate>> rQueries = new ArrayList<Collection<OWLPredicate>>();

		outer : for(Collection<OWLPredicate> query : queries){
			if(rQueries.size() == 0) {
				rQueries.add(query);
				continue;
			}
			for (int i = 0; i < rQueries.size(); i++){
				//insert to i-position when smaller 
				if(query.size() <= rQueries.get(i).size()){
					rQueries.add(i, query);
					continue outer;
				}
				//after all possible comparison, query turns out to be larger than all others  
				//therefore add to last position 
				if(i == rQueries.size() - 1) {
					rQueries.add(rQueries.size(), query);
					continue outer;
				}
			}
		}
		return rQueries;
	}

	private Variable getNewVariable(){
		String var = String.valueOf(UniqueIdGenerator.getInstance().getNewVarId());
		return new Variable("x" + var);
	}

	/**
	 * Return the largest connected component, i.e. the largest "subgraph" of the graph. 
	 * @param graph
	 * @return
	 */
	private SimpleDirectedGraph<IResource, KbEdge> getLargestConnectedComponent(SimpleDirectedGraph<IResource, KbEdge> graph){
		if(m_largestResourceGraph != null) return m_largestResourceGraph;

		if(m_conInspector.isGraphConnected()) return graph; 

		List<Set<IResource>> ccs = m_conInspector.connectedSets();
		Pair p = new Pair(new Integer(0), new Object());
		Set<IResource> largestCC = null;
		for (Set cc : ccs){
			if(cc.size() > ((Integer)p.getHead()).intValue()) p = new Pair(new Integer(cc.size()), cc);
			largestCC = (Set<IResource>)p.getTail();
		}

		SimpleDirectedGraph<IResource, KbEdge> subgraph = new SimpleDirectedGraph<IResource, KbEdge>(KbEdge.class);
		for(IResource res : largestCC){
			subgraph.addVertex(res);
			Set<KbEdge> edges = graph.edgesOf(res);
			for(KbEdge edge : edges){
				subgraph.addVertex(edge.m_vertex1);
				subgraph.addVertex(edge.m_vertex2);
				subgraph.addEdge(edge.m_vertex1, edge.m_vertex2,edge);
			}
		}
		return subgraph;
	}
	
	/**
	 * From the list of connecting paths P, we compute and return all the different subgraphs G_s such that G_s 
	 * contains all the elements connected by these paths. For this, different combinations of paths are analyzed and merge to obtain 
	 * such a subgraph. In particular, we start out with the first subgraph containing an arbitrary path p_0. Then, 
	 * we take one another path p from the remaining paths in P. If p is different from any paths in the first subgraph, 
	 * we add p to this first subgraph until the size of this subgraph is equal to 
	 * the number of matching resources. That is, we add further paths until obtaining the first graph that contains all matching resources.  
	 * 
	 *   
	 * @param paths
	 * @return
	 */
	private List<SimpleDirectedGraph<IResource, KbEdge>> computeSubgraphs(List<Stack> paths){
		if(paths == null || paths.size() == 0) return null;
		ArrayList<List<Stack>> subgraphs = new ArrayList<List<Stack>>();
		ArrayList<Stack> subgraph0 = new ArrayList<Stack>();

		//construct subgraph p_0
		subgraph0.add(paths.remove(0));
		//add paths from P to p_0 until obtaining the first subgraph containing all matching elements 

		ArrayList<Stack> subgraph0tmp = new ArrayList<Stack>();
		subgraph0tmp.addAll(subgraph0);
		ArrayList<Stack>pathstmp = new ArrayList<Stack>();
		pathstmp.addAll(paths);
		outer: for(int j = 0; j < pathstmp.size(); j++){
			for(int i = 0; i < subgraph0tmp.size(); i++){
				Stack path1 = pathstmp.get(j);
				Stack path2 = subgraph0tmp.get(i);
				//add furhter p1 to p_0 if start element of p1 match end element of p2 in p_0 or
				//end element of p1 match start element of p2
				if((!path1.get(0).equals(path2.get(0)) || 
						!((KbEdge)path1.get(path1.size()-1)).m_vertex2.equals(((KbEdge)path2.get(path2.size()-1)).m_vertex2)) 
						&&(path1.get(0).equals(path2.get(0)) || 
								((KbEdge)path1.get(path1.size()-1)).m_vertex2.equals(((KbEdge)path2.get(path2.size()-1)).m_vertex2))){
					subgraph0.add(path1);
					paths.remove(path1);
					if(subgraph0.size() == m_matchingResources.size() - 1) break outer; 
				}
			}
		}


		//add other subgraphs
		subgraphs.add(subgraph0);
		outer: for(Stack path : paths){
			for(Stack path0 : subgraph0){
				//construct further subgraphs by subsituting one path p1 in P with one similar p2 in p_0
				//similar: when start and end elements of p1 are equals with corresponding elements in p2
				//TODO: check that the two paths differ at least in one path element 
				if (path.get(0).equals(path0.get(0)) && 
						((KbEdge)path.get(path.size() -1)).m_vertex2.equals(((KbEdge)path0.get(path0.size() -1)).m_vertex2)){
					ArrayList<Stack> tmp = new ArrayList<Stack>();
					tmp.addAll(subgraph0);
					tmp.remove(path0);
					tmp.add(path);
					subgraphs.add(tmp);
					continue outer;
				}
			}
		}

		//convert the paths in subgraphs into a list of SimpleDirectedGraphs
		ArrayList<SimpleDirectedGraph<IResource, KbEdge>> result = new ArrayList<SimpleDirectedGraph<IResource, KbEdge>>();
		for(List<Stack> subgraph : subgraphs){
			SimpleDirectedGraph<IResource, KbEdge> graph = new SimpleDirectedGraph<IResource, KbEdge>(KbEdge.class); 
			for(Stack path : subgraph){
				for (Object e : path){
					if(e instanceof IResource) graph.addVertex((IResource)e);
					else if (e instanceof KbEdge) {
						graph.addVertex(((KbEdge)e).m_vertex1);
						graph.addVertex(((KbEdge)e).m_vertex2);
						graph.addEdge(((KbEdge)e).m_vertex1, ((KbEdge)e).m_vertex2, (KbEdge)e);
					}
				}
			}
			result.add(graph);
		}

		return result;
	}

	/**
	 * Prune the graph such that all possible paths connecting any two matching elements, i.e. elements of 
	 * m_resources, are returned. The subgraph can then be obtained by merging these paths... 
	 * @param graph
	 * @return
	 */
	private List<Stack> pruneGraph(SimpleDirectedGraph<IResource, KbEdge>graph){
		Set elements = new HashSet();
		elements.addAll(graph.vertexSet());
		if (elements == null || elements.size() == 0) return null;
		else elements.addAll(graph.edgeSet());

		Map<Object, Boolean> visited = new HashMap<Object, Boolean>();
		for (Object e : elements){
			visited.put(e, new Boolean(false));
		}

		//DFS with all one arbitrary matching resources to find connection paths 
		List<Stack> paths = new ArrayList<Stack>();
		for(IResource res : m_matchingResources){
			for (Object key : visited.keySet()){
				visited.put(key, new Boolean(false));
			}
			
			paths = dFSResourceGraph(res, visited, graph, paths, new Stack());
		}
		return paths;
	}

	/**
	 * From one given matching element, we DFS to find a second matching element and add the therewith computed 
	 * connecting path to paths. 
	 * @param vertex
	 * @param visited
	 * @param graph
	 * @param paths
	 * @param path
	 * @return
	 */
	private List<Stack> dFSResourceGraph(IResource vertex, Map<Object, Boolean> visited, SimpleDirectedGraph<IResource, KbEdge>graph,
			List<Stack> paths, Stack path){

		visited.remove(vertex);
		visited.put(vertex, new Boolean(true));
		path.push(vertex);
		Set<KbEdge> toBeVisited = new LinkedHashSet<KbEdge>();
		toBeVisited.addAll(graph.outgoingEdgesOf(vertex));
		toBeVisited.addAll(graph.incomingEdgesOf(vertex));
		if(toBeVisited != null && toBeVisited.size() > 0){
			for(KbEdge edge : toBeVisited){
				//traversing through same property subsequently means that that we connect paths over two different instances 
				//this obviously does make sense and thus, need to be avoided 
				if(path.size() >= 2) {
					KbEdge last = (KbEdge) path.get(path.size()- 2);
					if(edge.getProperty().equals(last.getProperty()))
						continue;
				}

				Boolean edgeVisited = visited.get(edge); 
				if(edgeVisited.booleanValue() == false){
					visited.remove(edge);
					visited.put(edge, new Boolean(true));
					path.push(edge);
					IResource to;
					if(vertex.equals(edge.m_vertex2)) to = edge.m_vertex1;
					else to = edge.m_vertex2;

					//add a connecting path to paths 
					if (matchQueryTerm(to) || matchQueryTerm(edge)){
						//a connection path is such that one given matching element is connected with one another different matching element 
						if(path.size() > 1){
							if(!containsPath(paths, path)){
								paths.add((Stack)path.clone());
							}
						}
					}

					Boolean nodeVisited = visited.get(to);
					if(nodeVisited.booleanValue() == false)
						paths = dFSResourceGraph(to, visited, graph, paths, path);
					//pop edge 
					path.pop();
				}
			}
		}
		//pop vertex
		path.pop();
		return paths;
	}

	/**
	 * This is an auxilary method that given the largest graph discovered via exploration, computes the resources in the given resources 
	 * that are actual elements of this graph. That is, it computes all the matching resources, that actually have beeen discovered during 
	 * the exploration. 
	 *
	 */
	private Collection<IResource> computeMatchingResources(SimpleDirectedGraph<IResource, KbEdge> graph, Collection<IResource> ress){
		ArrayList<IResource> result = new ArrayList<IResource>();
		for(IResource res : ress){
			if(graph.containsVertex(res)) result.add(res);
		}
		return result;
	}

	/**
	 * return true if the given paths contain already the given pathstack
	 * @param paths
	 * @param pathstack
	 * @return
	 */
	private boolean containsPath(List<Stack> paths, Stack pathstack){
		if(paths == null || paths.size() == 0 || pathstack == null || pathstack.size() == 0) return false; 
		for(List path : paths){
			if(path.size() == 0) continue;
			//different in size
			if(path.size() != pathstack.size()) continue;
			//different in start or end element 
			if(!path.get(0).equals(pathstack.get(0)) || 
					!((KbEdge)path.get(path.size()-1)).m_vertex2.equals(((KbEdge)pathstack.get(pathstack.size()-1)).m_vertex2)) continue;
			int noOfMatches = 0; 
			int noOfTraversedEdges = 0;
			for(int i = 1; i < path.size(); i++){
				Object r1 = path.get(i);
				Object r2 = pathstack.get(i);
				if(r1 instanceof KbEdge && r2 instanceof KbEdge){
					noOfTraversedEdges++;
					if (((KbEdge)r1).getProperty().equals(((KbEdge)r2).getProperty())) 
						noOfMatches++;
				}	
//				if(r1 instanceof IResource && r2 instanceof IResource){
//				if (!((IResource)r1).equals(((IResource)r2))) return false;
//				}
				if(noOfMatches == noOfTraversedEdges) return true; 
			}
		}
		return false;
	}

	/**
	 * Return if a resource matching one of the query term. This is the case when this resource is equal
	 * to one of the resource in m_resources, the list of matching resources.  
	 * @param resource
	 * @return
	 */
	private boolean matchQueryTerm(Object element){
		Emergency.checkPrecondition(element instanceof KbEdge || element instanceof IResource, 
		"element instanceof KbEdge || element instanceof IResource");
		if (m_resources == null) return false;
		if(element instanceof KbEdge){
			element = ((KbEdge)element).m_property;
		}
		return m_resources.contains(element);
	}


	public class KbEdge extends DefaultEdge {

		private IResource m_vertex1;

		private IResource m_vertex2;

		private IProperty m_property;


		public KbEdge(IResource vertex1, IResource vertex2, IProperty prop){
			m_vertex1 = vertex1;
			m_vertex2 = vertex2;
			m_property = prop;
			Emergency.checkPostcondition(m_vertex1 != null && m_vertex2 != null && m_property != null, "m_vertex1 != null && m_vertex2 != null && m_property != null"); 
		}

		public void setVertex1(IResource vertex1){
			m_vertex1 = vertex1;
		}

		public void setVertex2(IResource vertex2){
			m_vertex2 = vertex2;
		}

		public void setProperty(IProperty property){
			m_property = property;
		}


		public IResource getVertex1(){
			return m_vertex1;
		}

		public IResource getVertex2(){
			return m_vertex2;
		}

		public IProperty getProperty(){
			return m_property;
		}

		public boolean equals(KbEdge edge){

			if (!m_property.equals(edge.getProperty()))  return false;
			if (!m_vertex1.equals(edge.getVertex1())) return false;
			if (!m_vertex2.equals(edge.getVertex2())) return false;
			return true;
		}

		public String toString(){
			if(m_vertex1 != null && m_vertex2 != null && m_property != null) return m_vertex1.toString() + " " + m_property.getLabel() + " "  + m_vertex2.toString();
			else return super.toString();
		}
	}


	private static int ANCESTOR = 0;
	private static int DESCENDANT = 1;	
	private boolean addGraphElements(IResource existing, IResource toBeAdded, IProperty p, int type, DirectedGraph<IResource, KbEdge> graph){
		Emergency.checkPrecondition(type == ANCESTOR || type == DESCENDANT, "type == ANCESTOR || type == DESCENDANT");
		boolean addVertex = false; 
		addVertex = graph.addVertex(toBeAdded);
		if(addVertex) s_log.debug("Vertex " + toBeAdded + " added to the graph!");

		boolean addEdge = false; 
		try{
			if(type == ANCESTOR){
				KbEdge e = new KbEdge(toBeAdded, existing, p);
				addEdge = graph.addEdge(toBeAdded, existing, e);
				if(addEdge) s_log.debug("Edge " + e + " added to the graph!");
			}
			else{
				KbEdge e = new KbEdge(existing,toBeAdded, p);
				addEdge = graph.addEdge(existing, toBeAdded, e);
				if(addEdge) s_log.debug("Edge " + e + " added to the graph!");
			}
			return true;
		}
		//if loops or multiple edges are added to a vertex which is not supported by the simple graph
		catch (IllegalArgumentException e){
			return false;
		}


	}


}