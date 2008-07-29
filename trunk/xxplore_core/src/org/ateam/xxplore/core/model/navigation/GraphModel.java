

package org.ateam.xxplore.core.model.navigation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aifb.xxplore.shared.exception.Emergency;
import org.aifb.xxplore.shared.util.Pair;
import org.aifb.xxplore.shared.util.URIHelper;
import org.apache.log4j.Logger;
import org.ateam.xxplore.core.ExploreEnvironment;
import org.ateam.xxplore.core.service.search.KbEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.xmedia.oms.model.api.IConcept;
import org.xmedia.oms.model.api.IDataProperty;
import org.xmedia.oms.model.api.IDatatype;
import org.xmedia.oms.model.api.IEntity;
import org.xmedia.oms.model.api.IHierarchicalSchema;
import org.xmedia.oms.model.api.IHierarchicalSchemaNode;
import org.xmedia.oms.model.api.IIndividual;
import org.xmedia.oms.model.api.ILiteral;
import org.xmedia.oms.model.api.INamedConcept;
import org.xmedia.oms.model.api.IObjectProperty;
import org.xmedia.oms.model.api.IProperty;
import org.xmedia.oms.model.api.IResource;
import org.xmedia.oms.model.api.ISchema;
import org.xmedia.oms.model.impl.NamedConcept;
import org.xmedia.oms.query.OWLPredicate;
import org.xmedia.oms.query.Variable;

import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Schema;
import prefuse.data.Table;
import prefuse.data.Tree;
import prefuse.data.Tuple;


public class GraphModel implements IGraphModel {

	// the three different representation of the graph 
	private Graph m_graph; 

	// the tree representation of the default graph
	private Tree m_tree; 

	private final static Logger s_log = Logger.getLogger(GraphModel.class.getCanonicalName());

	private Table m_nodetable, m_edgetable;

	//a map used to store traversed resources 
	private Map<Long, IResource> m_processed = new HashMap<Long, IResource>();
	
	private IHierarchicalSchema m_schema;
	
	/**
	 * initialize the model from a collection of resources 
	 *
	 */
	public GraphModel(){
		initModel();
	}

	private void initModel(){

		Schema nodeSchema = new Schema(
				new String[]{
						ExploreEnvironment.LABEL, 
						ExploreEnvironment.OID, 
						ExploreEnvironment.URI,
						ExploreEnvironment.RESOURCE,
						ExploreEnvironment.IMAGE,
						ExploreEnvironment.IS_EXPANDED
				},
				new Class[]{
						String.class,
						int.class,
						String.class,
						IResource.class,
						String.class,
						Boolean.class
				});	

		Schema edgeSchema = new Schema(
				new String[]{
						ExploreEnvironment.LABEL, 
						Graph.DEFAULT_SOURCE_KEY,
						Graph.DEFAULT_TARGET_KEY,
						ExploreEnvironment.RESOURCE,
						ExploreEnvironment.IS_LABEL_VISIBLE
				},
				new Class[]{
						String.class, 
						int.class,
						int.class,
						IResource.class,
						Boolean.class
				});	

		m_nodetable = new Table();
		m_nodetable.addColumns(nodeSchema);
		m_edgetable = new Table();
		m_edgetable.addColumns(edgeSchema);

		m_graph = new Graph(m_nodetable, m_edgetable, true, 
				ExploreEnvironment.OID, Graph.DEFAULT_SOURCE_KEY, Graph.DEFAULT_TARGET_KEY);
	}

	public Graph getGraph(){
		return m_graph;
	}

	public void addData(ISchema schema) {
		
		Emergency.checkPrecondition((schema != null) && (schema instanceof IHierarchicalSchema), "schema != null && schema instanceof IHierarchicalSchema");
		
		m_schema = (IHierarchicalSchema)schema;		
		
		//the object queue 
		List<IHierarchicalSchemaNode> queue = new LinkedList<IHierarchicalSchemaNode>();
		//get the top elements and put them in the queue
		queue.add(0, ((IHierarchicalSchema)schema).getTopNode()); 

		//index the total number of nodes processed so far
		int index = 0; 
		int row; 		
		m_processed.clear();

		//traverse schema and add nodes and edges to graph 
		do {
			//get the current object to take into account
			IHierarchicalSchemaNode node = queue.get(index); 
			Set<INamedConcept> concepts = node.getConcepts();
			for (INamedConcept concept : concepts) {
				//do nothing wiht bottom concept; this is actually a hack... 
				if(concept.equals(NamedConcept.BOTTOM) || m_processed.containsKey(concept.getOid())){
					continue;
				}

				Node graphNode = m_graph.getNodeFromKey(concept.getOid()); 
				if(graphNode == null){
					row = m_nodetable.addRow();
					initNode(m_nodetable.getTuple(row), concept);
					graphNode = m_graph.getNodeFromKey(concept.getOid()); 
				}

				Set<IHierarchicalSchemaNode> parentNodes = node.getParents();
				Set<INamedConcept> parentConcepts = new HashSet<INamedConcept>();
				for (IHierarchicalSchemaNode parentNode : parentNodes) {
					parentConcepts.addAll(parentNode.getConcepts());
				}

				// add edges to parent concepts
				for (INamedConcept parentConcept : parentConcepts) {
					Node parentGraphNode = m_graph.getNodeFromKey(parentConcept.getOid());
					if(parentGraphNode == null){
						row = m_nodetable.addRow();
						initNode(m_nodetable.getTuple(row), parentConcept);
						parentGraphNode = m_graph.getNodeFromKey(parentConcept.getOid());
					}

//					if(m_graph.getEdge(graphNode, parentGraphNode) == null){
//						row = m_edgetable.addRow();
//						initEdge(m_edgetable.getTuple(row), concept, parentConcept, null,
//								ExploreEnvironment.SUBCLASS_OF);
//					}
				};

				// add properties as edges
				Set<Pair> propAndRanges = concept.getPropertiesAndRangesFrom();
				for (Pair p : propAndRanges) {
					
					IResource resource = (IResource)p.getTail();
					IProperty property = (IProperty)p.getHead();
					
					if (property instanceof IObjectProperty) {
						
						Node neigborGraphNode = m_graph.getNodeFromKey(resource.getOid());
						
						if(neigborGraphNode == null){
							initNode(m_nodetable.getTuple(m_nodetable.addRow()), resource);
							neigborGraphNode = m_graph.getNodeFromKey(resource.getOid());
						}
						
						if(m_graph.getEdge(graphNode, neigborGraphNode) == null) {
							
							initEdge(m_edgetable.getTuple(m_edgetable.addRow()), concept, resource, property, property.getLabel());
						}
					}
					else if(property instanceof IDataProperty){
						
						Node neigborGraphNode = m_graph.getNodeFromKey(resource.getOid());
						if(neigborGraphNode == null){
							
							initNode(m_nodetable.getTuple(m_nodetable.addRow()), resource);
							neigborGraphNode = m_graph.getNodeFromKey(resource.getOid());
						}
						
						Edge edge;
						
						if((edge = m_graph.getEdge(graphNode, neigborGraphNode)) == null) {
							initEdge(m_edgetable.getTuple(m_edgetable.addRow()), concept, resource, property, property.getLabel());
						}
						else{
							
							String thisLabel = URIHelper.truncateUri(property.getLabel());
							
							if(!edge.getString(ExploreEnvironment.LABEL).equals(thisLabel)){			
								
								initEdge(m_edgetable.getTuple(m_edgetable.addRow()), concept, resource, property, property.getLabel());
							}
						}
					}
				}

				//get child nodes and add them to queue 
				Set<IHierarchicalSchemaNode> childNodes = node.getChilds();
				for (IHierarchicalSchemaNode childnode : childNodes){
					for(IConcept c : childnode.getConcepts()){
						if (!m_processed.containsKey(c.getOid())) {
							queue.add(childnode);
						}
					}
				}

				m_processed.put(concept.getOid(), concept);

			}
			index++;
		}
		while (index < queue.size());
	}

	public void addData(Collection data) {
//		Emergency.checkPrecondition(data != null, "data != null");

//		for (Object res : data){
//		// consider only resources not already processed 
//		if (res instanceof IResource && !m_processed.containsKey(((IResource)res).getOid())){
//		if(res instanceof INamedConcept ) processClass((INamedConcept)res);
//		else if(res instanceof IProperty) processProperty((IProperty)res);
//		else if (res instanceof INamedIndividual) processInstance((INamedIndividual)res);

//		m_processed.put(((IResource)res).getOid(), (IResource)res);
//		}
//		} 
	}

	public void addData(SimpleDirectedGraph<IResource, KbEdge>resourcegraph, Collection<OWLPredicate> predicategraph){

//		int row; 		
//		for(IResource v : resourcegraph.vertexSet()){
//			row = m_nodetable.addRow();
//			initNode(m_nodetable.getTuple(row), v);
//			s_log.debug("Node " + v + " has been added.");
//		}
//		for(KbEdge e : resourcegraph.edgeSet()){
//			row = m_edgetable.addRow();
//			IResource source = e.getVertex1();
//			IResource target = e.getVertex2();
//			IProperty prop = e.getProperty();
//			initEdge(m_edgetable.getTuple(row), source, target, prop, prop.getLabel());
//			s_log.debug("Edge " + source + " " + prop + " " + target + " has been added.");
//		}
//
//		if(predicategraph == null) return;
//		for (OWLPredicate pred : predicategraph) {
//			if (pred instanceof PropertyMemberPredicate){
//				m_highlightedResources.add(((PropertyMemberPredicate)pred).getProperty());
//				m_highlightedResources.add(((PropertyMemberPredicate)pred).getFirstTerm());
//				m_highlightedResources.add(((PropertyMemberPredicate)pred).getSecondTerm());
//			}
//			else{ //Concept member 
//				m_highlightedResources.add(Property.IS_INSTANCE_OF);
//				m_highlightedResources.add(((ConceptMemberPredicate)pred).getConcept());
//				m_highlightedResources.add(((ConceptMemberPredicate)pred).getTerm());
//			}
//		}
	}

	public Node addNode(IResource res) {
		
		if(res instanceof IProperty)
		{
			
			s_log.debug("IResource '"+res+"' is IProperty. Is not supposed to be a node.");
			return null;
		}
					
		if(m_graph.getNodeFromKey(res.getOid()) != null)
		{

			s_log.debug("Graph already contained node corresponding to IResource '"+res+"'");
			return null;
		}
		
		int row = m_nodetable.addRow();
		initNode(m_nodetable.getTuple(row), res);
		
		s_log.debug("Added node corresponding to IResource '"+res+"' to graph.");
		return m_graph.getNode(row);
	}

	public Edge addEdge(IResource from, IResource to, IProperty property, String label) {
		
		if(from instanceof IProperty)
		{
			
			s_log.debug("IResource '"+from+"' is instance of IProperty.");
			return null;
		}
		
		if(to instanceof IProperty)
		{
			
			s_log.debug("IResource '"+to+"' is instance of IProperty.");
			return null;
		}
		
		if(m_graph.getNodeFromKey(from.getOid()) == null)
		{

			s_log.debug("No node corresponding to IResource '"+from+"'");
			return null;
		}
		
		if(m_graph.getNodeFromKey(to.getOid()) == null)
		{

			s_log.debug("No node corresponding to IResource '"+to+"'");
			return null;
		}
		
		if(m_graph.getEdge(m_graph.getNodeFromKey(from.getOid()), m_graph.getNodeFromKey(to.getOid())) != null)
		{
			
			s_log.debug("Graph already contained edge IResource '"+from+"' to IResource '"+to+"'");
			return null;
		}
		
		int row = m_edgetable.addRow();
		initEdge(m_edgetable.getTuple(row), from, to, property, label);
		
		s_log.debug("Added edge from IResource '"+from+"' to IResource '"+to+"' to graph");
		return m_graph.getEdge(row);
	}

	public List<Node> getElements(){
			
		List<Node> nodes = new ArrayList<Node>();
		if (m_graph != null){
			for(int n = m_graph.getNodeCount(),i=0; n > 0; n--,i++){
			//add all nodes to list
				nodes.add(m_graph.getNode(i));
			}	
		}
		
		return nodes;
	}
		
	@SuppressWarnings("unchecked")
	public Node[] getChildren(Node parentNode){
				
		List<Node> children = new ArrayList<Node>();
		if (m_tree == null) {
			m_tree = m_graph.getSpanningTree();
		}

		if (m_tree != null){
			if (parentNode != null) {
				for(int n = parentNode.getChildCount(),i=0; n > 0; n--,i++){
					children.add(parentNode.getChild(i));
				}
			}
		}
			
		return children.toArray(new Node[0]);
	}
	
	public boolean hasChildren(Node parentNode){
		
		if (m_tree == null) {
			m_tree = m_graph.getSpanningTree();
		}

		if (m_tree != null){
			if (parentNode != null) {
				Node childNode = m_tree.getFirstChild(parentNode);
				if(childNode != null){
					return true;
				}
			}
		}
		
		return false;
	}
	
	@SuppressWarnings("unchecked")
	public Node getParent(Node node){
		
		if (m_tree == null) {
			m_tree = m_graph.getSpanningTree();
		}

		if (m_tree != null){
			if (node != null){
				return node.getParent();
			}
		}
		
		return null;
	}
	
	/**
	 * helper method to initialize tuple with data 
	 * @param tuple 
	 * @param res can be null, when is tuple is a property 
	 * @param label can be null. Is not null when tuple is a property. 
	 */
	private void initNode(Tuple tuple, IResource res){

		if (res != null) {
			if(res instanceof IEntity) {
				tuple.setString(ExploreEnvironment.URI, ((IEntity)res).getUri());
			}
			//set uri of the underlying tuple 
			tuple.setInt(ExploreEnvironment.OID, res.getOid().intValue());
			tuple.setString(ExploreEnvironment.LABEL, URIHelper.truncateUri(res.getLabel()));

			//also store the resource in the tuple for later references
			tuple.set(ExploreEnvironment.RESOURCE, res);

			if (res instanceof INamedConcept) {
				tuple.set(ExploreEnvironment.IMAGE, "file://icons/concept.png");
			}
			else if (res instanceof IIndividual){
				tuple.set(ExploreEnvironment.IMAGE, "file://icons/sample.gif");
			}
			else if (res instanceof IDatatype){
				tuple.set(ExploreEnvironment.IMAGE, "file://icons/sample.gif");
			}			
			else if (res instanceof ILiteral){
				tuple.set(ExploreEnvironment.IMAGE, "file://icons/sample.gif");
			}
			else if (res instanceof Variable){
				tuple.set(ExploreEnvironment.IMAGE, "file://icons/sample.gif");
			}

			tuple.set(ExploreEnvironment.IS_EXPANDED, Boolean.FALSE);
		}
	}

	/**
	 * 
	 * helper method to initialize the given arc with data 
	 * @param tuple
	 * @param source
	 * @param target
	 * @param property
	 * @param label
	 */
	private void initEdge(Tuple tuple, IResource source, IResource target, IProperty property, String label){
		Emergency.checkPrecondition((tuple != null) && (source != null) && (target != null), 
		"tuple != null && source != null && target != null");

		//set name and uri of the underlying tuple 
		tuple.setInt(Graph.DEFAULT_SOURCE_KEY, source.getOid().intValue());
		tuple.setInt(Graph.DEFAULT_TARGET_KEY, target.getOid().intValue());
		tuple.set(ExploreEnvironment.IS_LABEL_VISIBLE, Boolean.FALSE);


		if (property != null){
			//also store the resource in the tuple for later references
			tuple.set(ExploreEnvironment.RESOURCE, property);
			tuple.setString(ExploreEnvironment.LABEL, URIHelper.truncateUri(property.getLabel()));
		}

		else if ((label != null) && (label.length() > 0)) {
			tuple.setString(ExploreEnvironment.LABEL, label);
		}
	}

	
//	private void processClass(INamedConcept concept){

//	//add a node
//	int row = m_nodetable.addRow();
//	initNode(m_nodetable.getTuple(row), concept);

//	m_processed.put(concept.getOid(), concept);


//	Set<IConcept> superConcepts = concept.getSuperconcepts();

//	if (superConcepts == null || superConcepts.size() == 0)
//	System.out.println("added (" + concept.getOid() + " " + concept.getOid() + ") " + concept.getUri() + " to m_processed");

//	if (superConcepts != null) {
//	for (IConcept superConcept : superConcepts) {
//	if (m_processed.containsKey(superConcept.getOid())) {
//	row = m_edgetable.addRow();
//	initEdge(m_edgetable.getTuple(row), superConcept, concept, null, ExploreEnvironment.SUBCLASS_OF);
//	}
//	}
//	}

//	Set<IConcept> subConcepts = concept.getSubconcepts();
//	if (subConcepts != null) {
//	for (IConcept subConcept : subConcepts) {
//	if (subConcept instanceof INamedConcept && m_processed.containsKey(((INamedConcept)subConcept).getUri())) {
//	row = m_edgetable.addRow();
//	initEdge(m_edgetable.getTuple(row), concept, subConcept, null, ExploreEnvironment.SUBCLASS_OF);
//	}
//	}
//	}
//	}

//	private void processProperty(IProperty res){
//	//process property domains 
//	Set<INamedConcept> domains = res.getDomains();
//	if (domains != null){
//	for(INamedConcept clazz : domains){

//	if (!m_processed.containsKey(clazz.getOid())){
//	//add a node
//	int row = m_nodetable.addRow();
//	initNode(m_nodetable.getTuple(row), clazz);

//	m_processed.put(clazz.getOid(), clazz);
//	}
//	}
//	}

//	//process property ranges 
//	Set<? extends IResource> ranges = res.getRanges();
//	if (ranges != null){
//	for(IResource r : ranges){
//	if (!m_processed.containsKey(((INamedConcept)r).getOid())){
//	if(r instanceof INamedConcept){
//	//add a node
//	int row = m_nodetable.addRow();
//	initNode(m_nodetable.getTuple(row), r);

//	m_processed.put(((INamedConcept)r).getOid(), r);		
//	}
//	//datatype
//	else{
//	//add a node
//	int row = m_nodetable.addRow();
//	initNode(m_nodetable.getTuple(row), r);

//	m_processed.put(((INamedConcept)r).getOid(), r);		
//	}

//	}
//	}
//	}
//	}

//	private void processInstance(INamedIndividual res){

//	//add a node
//	int row = m_nodetable.addRow();
//	initNode(m_nodetable.getTuple(row), res);

//	m_processed.put(res.getOid(), res);

//	//process types of this instance
//	Set<IConcept> clazzs = ((INamedIndividual)res).getTypes();
//	if(clazzs != null){
//	for(IConcept clazz : clazzs){

//	if (!m_processed.containsKey(clazz.getOid())){
//	//add a node
//	row = m_nodetable.addRow();
//	initNode(m_nodetable.getTuple(row), clazz);

//	m_processed.put(res.getOid(), clazz);
//	}

//	//add edge to the complete graph
//	row = m_edgetable.addRow();
//	initEdge(m_edgetable.getTuple(row), res, clazz, null, ExploreEnvironment.SUBCLASS_OF); 

//	}
//	}

	//process properties of this instance
//	Map<String, IIndividual> props = ((IIndividual)res).getObjectPropertyValues();

//	if (props != null){
//	for(int i = 0; i < props.size(); i++){

//	}
//	}
//	}


	/*
	 * The following methods are used by the ConceptHierachyViewContentProvider only.
	 *  
	 */
	@SuppressWarnings("unchecked")
	public IResource[] getChildrenConceptHierachyView(IResource res){
		
		ArrayList<IResource> out = new ArrayList<IResource>();
		
		if(res instanceof INamedConcept)
		{
			IHierarchicalSchemaNode schemaNode = (IHierarchicalSchemaNode) m_schema.getNode((INamedConcept)(res));
			
			Set<IHierarchicalSchemaNode> children = schemaNode.getChilds();
			
			for(IHierarchicalSchemaNode child : children)
			{
				
				Set<INamedConcept> concepts = child.getConcepts();
				
				for(INamedConcept concept : concepts)
				{	
					if(!concept.equals(NamedConcept.BOTTOM)) {
						out.add(concept);
					}
					
				}					
			}
		}
		
		return out.toArray(new IResource[0]);
						
	}
	
	@SuppressWarnings("unchecked")
	public IResource getParentConceptHierachyView(IResource res){
			
		if(res instanceof INamedConcept)
		{
			IHierarchicalSchemaNode schemaNode = (IHierarchicalSchemaNode) m_schema.getNode((INamedConcept)(res));
			
			Set<IHierarchicalSchemaNode> parents = schemaNode.getParents();
			
			for(IHierarchicalSchemaNode parent : parents)
			{			
				Set<INamedConcept> concepts = parent.getConcepts();
				
				for(INamedConcept concept : concepts) {
					return concept;
				}
								
			}
		}
		
		return null;
		
	}
	
	public IResource[] getElementsConceptHierachyView(){
	
		return new IResource[]{NamedConcept.TOP};
		
	}
}
