/*
 * Project       KnowledgeXXplore
 * Copyright (c) 2006 AIFB Institute 
 *                    University of Karlsruhe
 * All rights reserved.
 *
 */
package org.aifb.xxplore.views.graphviewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Label;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.aifb.xxplore.core.ExploreEnvironment;
import org.aifb.xxplore.core.model.definition.IEntityDefinition;
import org.aifb.xxplore.core.model.definition.ModelDefinition;
import org.aifb.xxplore.core.model.definition.ModelDefinition.DefinitionTuple;
import org.aifb.xxplore.core.model.navigation.IGraphModel;
import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.xmedia.oms.model.api.IConcept;
import org.xmedia.oms.model.api.IDatatype;
import org.xmedia.oms.model.api.IIndividual;
import org.xmedia.oms.model.api.ILiteral;
import org.xmedia.oms.model.api.INamedConcept;
import org.xmedia.oms.model.api.INamedIndividual;
import org.xmedia.oms.model.api.IProperty;
import org.xmedia.oms.model.api.IResource;
import org.xmedia.oms.model.impl.NamedConcept;
import org.xmedia.oms.model.impl.Property;

import prefuse.Constants;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.GroupAction;
import prefuse.action.RepaintAction;
import prefuse.action.animate.ColorAnimator;
import prefuse.action.animate.PolarLocationAnimator;
import prefuse.action.animate.QualityControlAnimator;
import prefuse.action.animate.VisibilityAnimator;
import prefuse.action.assignment.ColorAction;
import prefuse.action.assignment.FontAction;
import prefuse.action.layout.CollapsedSubtreeLayout;
import prefuse.action.layout.Layout;
import prefuse.action.layout.graph.BalloonTreeLayout;
import prefuse.action.layout.graph.ForceDirectedLayout;
import prefuse.action.layout.graph.NodeLinkTreeLayout;
import prefuse.activity.SlowInSlowOutPacer;
import prefuse.controls.Control;
import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Tuple;
import prefuse.data.event.TupleSetListener;
import prefuse.data.expression.parser.ExpressionParser;
import prefuse.data.tuple.DefaultTupleSet;
import prefuse.data.tuple.TupleSet;
import prefuse.data.util.BreadthFirstIterator;
import prefuse.render.AbstractShapeRenderer;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.EdgeRenderer;
import prefuse.render.LabelRenderer;
import prefuse.util.ColorLib;
import prefuse.util.PrefuseLib;
import prefuse.visual.EdgeItem;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualGraph;
import prefuse.visual.VisualItem;
import prefuse.visual.expression.HoverPredicate;
import prefuse.visual.expression.InGroupPredicate;
import org.xmedia.oms.query.Variable;

/**
 * Instances of this class provide a selectable user interface object
 * that displays a graph of items and issues notification when an
 * item in the graph is selected.
 */
public class GraphControl extends Composite {

	private static final String FORCE_DIRECTED_LAYOUT = "force";

	private static final String RADIAL_LAYOUT = "radial";

	private static final String BALLOON_TREE_LAYOUT = "balloon";

	private static final String NODELINK_LAYOUT = "nodelinktree";
	
	private static final int BALLOON_MIN_RADIUS = 30;
	
	private static final boolean IS_FORCE_DIRECTED = false;

	/** the default layout used */
	private static final String DEFAULT_LAYOUT = RADIAL_LAYOUT;
	private String[] m_layouts = { RADIAL_LAYOUT, NODELINK_LAYOUT, BALLOON_TREE_LAYOUT,FORCE_DIRECTED_LAYOUT};

	private int m_currentLayout;

	public static final String GRAPH = "graph";
	public static final String NODES = "graph.nodes";
	public static final String EDGES = "graph.edges"; 
	
	private static final String FOCUSGROUP_VARIABLES = "VARIABLES";
	private static final String FOCUSGROUP_LITERALS = "LITERALS";
	private static final String FOCUSGROUP_DATATYPES = "DATATYPES";
	private static final String FOCUSGROUP_INDIVIDUALS = "INDIVIDUALS";
	private static final String FOCUSGROUP_CONCEPTS = "CONCEPTS";
	
	/** the prefuse component that bridge between the model and the display; it is the visual abstractions of the data **/
	private Visualization m_vis;

	/** the prefuse component responsible for rendering of and interaction with the contents of the visualization **/
	protected Display m_display;

	/** list of prefuse controls for dealing with user interactions */
	private List<Control> m_controls = null ;

	private PathControl m_pathControl;

	private NodeCountControl m_nodeCountControl;

	/** the lable renderer */
	private LabelRenderer m_nodeRenderer;

	/** the edge renderer */
	private EdgeRenderer m_edgeRenderer;

	/** the composite used for embedding AWT into SWT*/
	private Composite m_embedded;

	private boolean m_isInitialized = false;

	private Logger s_log = Logger.getLogger(GraphControl.class.getCanonicalName());


	/**   the Hops of a node, all of nodes inner Hops will be displayed  **/
	private int m_hops = 1;
	
	//switch between the maxnodes-Spinner and the Hops-Spinner
	private boolean m_switch = false;

	private ExtendedGraphDistanceFilter m_distanceFilter;
		
	private IGraphModel m_graphModel;
	

	/** the item that us currently focussed. 
	 *  only a restricted number of nodes related with with this item become visible. 
	 *  that is, this item is used to filter "irrelevant" items.  
	 */
	private VisualItem m_focusedItem;
	
	
	public GraphControl(Composite parent, int style) {
		super(parent, style );		
		setLayout(new FillLayout());
		m_embedded = new Composite(this,SWT.EMBEDDED);
	}

	
	
	public void setFocusedItem(IResource res) {
		
		Node node = m_graphModel.getGraph().getNodeFromKey(res.getOid());
		VisualItem item = m_vis.getVisualItem(GRAPH, node);
		setFocusedItem(item);
		
	}

	public void setFocusedItem(VisualItem item) {
						
		if(item != null)
		{				
			//clean up
			hideElements();
			cleanExpandedItems();
			m_nodeCountControl.enableDistanceFilter();
			
			//add the focussed itelem to the path control
			m_focusedItem = item;
			if(!m_focusedItem.isVisible()) {
				PrefuseLib.updateVisible(m_focusedItem, Boolean.TRUE);
			}
			m_pathControl.addEntry(m_focusedItem);
		
			//mark item in the graph as focus_items for filtering purposes 
			m_vis.getGroup(Visualization.FOCUS_ITEMS).setTuple(item);
															
			//expand and highlight			
			expandNode(m_focusedItem);
			
			List<VisualItem> itemsToSort = getNeighbors(m_focusedItem);
			itemsToSort.add(item);
			
			for(VisualItem nextItem : itemsToSort){				
				IResource res = (IResource)nextItem.get(ExploreEnvironment.RESOURCE);
				addToFocusGroup(res);
			}
		}
	}
	
	public boolean isFocusedItem(VisualItem item){

		return m_focusedItem.equals(item) ? Boolean.TRUE : Boolean.FALSE;

	}
	
	public VisualItem getFocusedItem(){
		return m_focusedItem;
	}

	
	private ArrayList<Tuple> m_addedItems;
	private IResource m_addedRoot;
	private int m_modeldefHashCode = -1;
	private Long m_selectionTime = 0L;
	
	protected void dispDefinitionViewInput(ModelDefinition def){
		
		
		//clean up		
		updateFocusGroup(new ArrayList<VisualItem>(),FOCUSGROUP_CONCEPTS,Boolean.TRUE);
		updateFocusGroup(new ArrayList<VisualItem>(),FOCUSGROUP_VARIABLES,Boolean.TRUE);
		updateFocusGroup(new ArrayList<VisualItem>(),FOCUSGROUP_LITERALS,Boolean.TRUE);
		updateFocusGroup(new ArrayList<VisualItem>(),FOCUSGROUP_DATATYPES,Boolean.TRUE);
		updateFocusGroup(new ArrayList<VisualItem>(),FOCUSGROUP_INDIVIDUALS,Boolean.TRUE);
		cleanExpandedItems();
		hideElements();
				
		//remove old items
		for(Tuple tuple : m_addedItems)
		{									
			try
			{
				IResource res = (IResource)tuple.get(ExploreEnvironment.RESOURCE);
				
				if(!(res instanceof IConcept))
				{				
					if(tuple instanceof Node)
					{
						m_pathControl.disableEntry(m_vis.getVisualItem(GRAPH, tuple));
						m_graphModel.getGraph().removeNode((Node)tuple);					
						s_log.debug("Node '"+res+"' successfully deleted.");
					}
					else
					{
						if(tuple instanceof Edge)
						{
							m_graphModel.getGraph().removeEdge((Edge)tuple);
							s_log.debug("Edge '"+res+"' successfully deleted.");
						}
					}
				}
			}
			catch(Exception e){}
		}
				
		m_addedItems.clear();

		if((def == null) || (def.getCompleteDefinitionTuples().size() == 0))
		{
			if((def != null) && def.hasIncompleteTuple())
			{
//				MessageBox messageBox = new MessageBox(m_composite.getShell(), SWT.ICON_ERROR | SWT.OK);
//				messageBox.setMessage("Tuple definition is incomplete. Please correct tuple and try again.");
//				messageBox.open();
			}
			
			m_addedRoot = null;
			m_modeldefHashCode = 0;
			m_selectionTime = 0L;
			
			s_log.debug("Modeldefinition was empty. Focusing on TOP-Concept.");
			setFocusedItem(NamedConcept.TOP);
			
			return;
		}
		
		m_modeldefHashCode = def.hashCode();

		
		Iterator<DefinitionTuple> iter = def.getCompleteDefinitionTuples().iterator();
		DefinitionTuple deftuple;
		
		//set variable als root 
		Variable rootResource = new Variable(def.getVariableName());
		Node root = m_graphModel.addNode(rootResource);
		addToFocusGroup(rootResource);
		m_addedItems.add(root);	
		m_addedRoot = rootResource;
		
		m_focusedItem = m_vis.getVisualItem(GRAPH, root);
		m_vis.getGroup(Visualization.FOCUS_ITEMS).setTuple(m_focusedItem);		
		m_pathControl.addEntry(m_focusedItem);
		
		while(iter.hasNext())
		{			
			deftuple = iter.next();
			
			if (deftuple.getObjectDefinition() instanceof ModelDefinition)
			{
				ArrayList<IResource> res = getResources((ModelDefinition)deftuple.getObjectDefinition(), null);
				res.add(0,deftuple.getRelationDefinition().getDefinition());
								
				for(int i = res.size()-1; i >= 0; i--)
				{
					//res.get(i) is supposed to be an edge
					if(res.get(i) instanceof IProperty)
					{

						if(i == 0)
						{
							Edge edge = null;
								
							if(i+1 < res.size())
							{
								edge = m_graphModel.addEdge(rootResource, 
													res.get(i+1), 
													(IProperty)res.get(i), 
													((Property)res.get(i)).getLabel());
																	
							}
							else
							{
									
								Node var = m_graphModel.addNode(new Variable(((ModelDefinition)deftuple.getObjectDefinition()).getVariableName()));
									
								edge = m_graphModel.addEdge(rootResource, 
														(Variable)var.get(ExploreEnvironment.RESOURCE), 
														(IProperty)res.get(i), 
														((Property)res.get(i)).getLabel());									
								
								addToFocusGroup(var);
								m_addedItems.add(var);									
							}
								
							m_addedItems.add(edge);
						}
						else
						{
							Edge edge = null;
							
							if(!(res.get(i+1) instanceof IProperty))
							{					
								
								Node node = null;
								
								if(!(res.get(i-1) instanceof IProperty))
								{
									node = m_graphModel.addNode(res.get(i-1));
									m_addedItems.add(node);
									
									if(i+1 < res.size())
									{
										edge = m_graphModel.addEdge(res.get(i-1), 
															res.get(i+1), 
															(IProperty)res.get(i), 
															((Property)res.get(i)).getLabel());
													
									}
									else
									{
										Node var = m_graphModel.addNode(new Variable(((ModelDefinition)deftuple.getObjectDefinition()).getVariableName()));
											
										edge = m_graphModel.addEdge(res.get(i-1), 
															(Variable)var.get(ExploreEnvironment.RESOURCE), 
															(IProperty)res.get(i), 
															((Property)res.get(i)).getLabel());									
										
										addToFocusGroup(var);
										m_addedItems.add(var);		
									}
									
									m_addedItems.add(edge);
								}
								//more than one edge between the two nodes ...
								else
								{
									List<IProperty> props = getNextProperties(res,i);
									int j = i - props.size() + 1;
									
									if(j != 0)
									{
										node = m_graphModel.addNode(res.get(j));
										m_addedItems.add(node);
									}
									
									for(IProperty prop : props)
									{
										if(j == 0)
										{
											edge = m_graphModel.addEdge(rootResource, 
																res.get(i+1), 
																prop, 
																prop.getLabel());	
										}
										else
										{
											edge = m_graphModel.addEdge(res.get(j), 
																res.get(i+1), 
																prop, 
																prop.getLabel());	
										}
										
										m_addedItems.add(edge);
									}
									
									//update i
									i = j - 1;
								}								
							}							
						}
					}
					//res.get(i) is node
					else
					{
						if(!(res.get(i) instanceof IConcept))
						{
							
							m_graphModel.addNode(res.get(i));
							Node node = m_graphModel.getGraph().getNodeFromKey(res.get(i).getOid());
							PrefuseLib.updateVisible(m_vis.getVisualItem(GRAPH, node), Boolean.TRUE);
	
							m_addedItems.add(node);
							addToFocusGroup(res.get(i));
														
						}
						else
						{
							PrefuseLib.updateVisible(m_vis.getVisualItem(GRAPH, m_graphModel.getGraph().getNodeFromKey(res.get(i).getOid())), Boolean.TRUE);
						}
					}
				}
				
			}
			else
			{
				if(deftuple.getObjectDefinition() instanceof IEntityDefinition)
				{
					IResource object = ((IEntityDefinition)deftuple.getObjectDefinition()).getDefinition(); 
					
					if(!(object instanceof IConcept))
					{
						//add target node ...
						Node node = m_graphModel.addNode(object);
						m_addedItems.add(node);
						addToFocusGroup(object);
						
					}
					else
					{
						PrefuseLib.updateVisible(m_vis.getVisualItem(GRAPH, m_graphModel.getGraph().getNodeFromKey(object.getOid())),Boolean.TRUE);
					}
					
					//add link, since object is a DefinitionTuple, link source is the root
					Edge edge = m_graphModel.addEdge(rootResource, 
												object, 
												deftuple.getRelationDefinition().getDefinition(), 
												((Property)deftuple.getRelationDefinition().getDefinition()).getLabel());

					m_addedItems.add(edge);		
				}
			}
		}
		
		
		m_nodeCountControl.disableDistanceFilter();		
		m_vis.run("filter");
		
	}
	
	private List<IProperty> getNextProperties(ArrayList<IResource> res, int i){
		
		List<IProperty> out = new ArrayList<IProperty>();

		for(int j = i; j >= 0; j--)
		{			
			if(res.get(j) instanceof IProperty)
			{
				out.add((IProperty)res.get(j));
			}
			else
			{
				break;
			}
		}
			
		return out;
	}
	
	
	protected IResource getAddedRoot(){
		return m_addedRoot;
	}
	
	protected int getModelDefHashCode(){
		return m_modeldefHashCode;
	}
	
	protected Long getSelectionTime(){
		return m_selectionTime;
	}
	
	protected void setSelectionTime(Long selectionTime){
		m_selectionTime = selectionTime;
	}
	
	private ArrayList<IResource> getResources(ModelDefinition def, ArrayList<IResource> resources){
		
		if (resources == null) {
			resources = new ArrayList<IResource>();
		}

		Iterator<DefinitionTuple> iter = def.getCompleteDefinitionTuples().iterator();		
		DefinitionTuple tuple;
		
		while(iter.hasNext())
		{			
			tuple = iter.next();

			if (tuple.getObjectDefinition() instanceof ModelDefinition)
			{
				resources.add(tuple.getRelationDefinition().getDefinition());
				resources = getResources((ModelDefinition)tuple.getObjectDefinition(), resources);
			}
			else
			{
				resources.add(new Variable(def.getVariableName()));
				resources.add(tuple.getRelationDefinition().getDefinition());
				resources.add(((IEntityDefinition)tuple.getObjectDefinition()).getDefinition());
			}
		}

		return resources; 	
	}
	
	private void addToFocusGroup(Node node){
		
		if(node.canGet(ExploreEnvironment.RESOURCE, IResource.class)) {
			addToFocusGroup((IResource)node.get(ExploreEnvironment.RESOURCE));
		}
		
	}
	
	private void addToFocusGroup(IResource res){
		
		Node node = m_graphModel.getGraph().getNodeFromKey(res.getOid());
		
		if(res instanceof Variable) {
			m_vis.getFocusGroup(FOCUSGROUP_VARIABLES).addTuple(m_vis.getVisualItem(GRAPH, node));
		} else if(res instanceof ILiteral) {
			m_vis.getFocusGroup(FOCUSGROUP_LITERALS).addTuple(m_vis.getVisualItem(GRAPH, node));
		} else if(res instanceof IDatatype) {
			m_vis.getFocusGroup(FOCUSGROUP_DATATYPES).addTuple(m_vis.getVisualItem(GRAPH, node));
		} else if((res instanceof IIndividual) || (res instanceof INamedIndividual)) {
			m_vis.getFocusGroup(FOCUSGROUP_INDIVIDUALS).addTuple(m_vis.getVisualItem(GRAPH, node));
		} else if((res instanceof IConcept) || (res instanceof INamedConcept)) {
			m_vis.getFocusGroup(FOCUSGROUP_CONCEPTS).addTuple(m_vis.getVisualItem(GRAPH, node));
		}
		else {
			s_log.debug("IResource '"+res+"' could not be identified.");
		}
	}

	public void setHopsOfDistance(int Hops){
		m_hops = Hops;
	}
	
	public int getHopsOfDistance(){
		return m_hops;
	}
	
	/**
	 * Refresh control because the underlying graph has changed
	 * in structure. 
	 *
	 */
	public void refresh(IGraphModel graph){
		
		if (graph == null) {
			return;
		}
		if (!m_isInitialized) {
			init();
		}		

		//set the new graph 
		setGraph(graph);

		//repaint, filter, animate
		m_vis.run("filter");

	}

	/** method used to initalise the visualisation and the composite embedding the display
	 * 
	 */	
	private void init() {
		
		m_distanceFilter = new ExtendedGraphDistanceFilter(GRAPH, m_hops);
		m_nodeCountControl = new NodeCountControl(this);

		//init prefuse visualization
		initVisualization();

		m_visitedItems = new ArrayList<VisualItem>();
		m_addedItems = new ArrayList<Tuple>();
		m_datatypes = new HashMap<ILiteral,ArrayList<Node>>();
				
		//get the frame to embed the AWT component of prefuse into SWT
		Frame frame = SWT_AWT.new_Frame(m_embedded);
		org.eclipse.swt.widgets.Display display = m_embedded.getDisplay();

		Rectangle clientArea = m_embedded.getClientArea();
		final Rectangle bounds = display.map(m_embedded, null, clientArea);

		m_pathControl = new PathControl(this);
		m_nodeCountControl = new NodeCountControl(this);

		//init prefuse display
		frame.setLayout(new BorderLayout());
		frame.add(m_pathControl.getContainer(), BorderLayout.NORTH);
		frame.add(initDisplay(bounds.width, bounds.height), BorderLayout.CENTER);
		frame.add(m_nodeCountControl.getContainer(), BorderLayout.SOUTH);
		frame.pack();
		frame.setVisible(true);

		m_vis.run("filter");
		m_isInitialized = true;

	}

	private void initVisualization() {
		m_vis = new Visualization();
		
//		m_vis.setInteractive(EDGES, null, true);

		m_vis.addFocusGroup(FOCUSGROUP_CONCEPTS, new DefaultTupleSet());
		m_vis.addFocusGroup(FOCUSGROUP_VARIABLES, new DefaultTupleSet());
		m_vis.addFocusGroup(FOCUSGROUP_LITERALS, new DefaultTupleSet());
		m_vis.addFocusGroup(FOCUSGROUP_DATATYPES, new DefaultTupleSet());
		m_vis.addFocusGroup(FOCUSGROUP_INDIVIDUALS, new DefaultTupleSet());
		
		/// init renderer 
		m_nodeRenderer = new LabelRenderer();
		m_nodeRenderer.setRenderType(AbstractShapeRenderer.RENDER_TYPE_FILL);
		m_nodeRenderer.setHorizontalAlignment(Constants.LEFT);
		m_nodeRenderer.setRoundedCorner(8,8);
		m_nodeRenderer.setImageField(ExploreEnvironment.IMAGE);
		m_edgeRenderer = new LabeledEdgeRenderer(Constants.EDGE_TYPE_CURVE, Constants.EDGE_ARROW_FORWARD);

		
		DefaultRendererFactory drf = (DefaultRendererFactory) m_vis.getRendererFactory();
		drf.setDefaultRenderer(m_nodeRenderer);
		drf.setDefaultEdgeRenderer(m_edgeRenderer);

		ColorAction nodeColorAction = new ColorAction(NODES, VisualItem.FILLCOLOR, ColorLib.rgb(200, 200, 255));

		nodeColorAction.add(new InGroupPredicate(FOCUSGROUP_VARIABLES), ColorLib.hex("#A2CD5A"));
		nodeColorAction.add(new InGroupPredicate(FOCUSGROUP_LITERALS), ColorLib.hex("#ED9121"));
		nodeColorAction.add(new InGroupPredicate(FOCUSGROUP_DATATYPES), ColorLib.hex("#EECFA1"));
		nodeColorAction.add(new InGroupPredicate(FOCUSGROUP_INDIVIDUALS), ColorLib.hex("#C39CC3"));
		nodeColorAction.add(new InGroupPredicate(FOCUSGROUP_CONCEPTS), ColorLib.rgb(200, 200, 255));
		nodeColorAction.add(new HoverPredicate(), ColorLib.rgb(100, 100, 255));


		ColorAction edgeColorAction = new ColorAction(EDGES, VisualItem.FILLCOLOR,ColorLib.rgb(200, 200, 255));
		edgeColorAction.add(new HoverPredicate(), new ColorAction(EDGES, VisualItem.STROKECOLOR, ColorLib.rgba(100, 100, 255, 255)));

		ColorAction nTextColorAction = new ColorAction(NODES, VisualItem.TEXTCOLOR, ColorLib.rgb(0, 0, 0));
		nTextColorAction.add(ExpressionParser.predicate(VisualItem.EXPANDED+" == TRUE"),ColorLib.hex("#FF2400"));
		
		ColorAction eTextColorAction = new ColorAction(EDGES, VisualItem.TEXTCOLOR, ColorLib.gray(200));

		FontAction nodes = new FontAction(NODES, new Font("SansSerif", Font.PLAIN, 13));
		nodes.add(ExpressionParser.predicate(VisualItem.EXPANDED+" == TRUE"), new Font("SansSerif", Font.ITALIC, 13));

		ActionList paint = new ActionList();
		paint.add(nodes);
		paint.add(new FontAction(EDGES, new Font("SansSerif", Font.PLAIN, 14)));
		paint.add(nTextColorAction);
		paint.add(eTextColorAction);		
		paint.add(nodeColorAction);
		paint.add(edgeColorAction);
		m_vis.putAction("paint", paint);

		ActionList highlighting = new ActionList();
		highlighting.add(nodeColorAction);
		highlighting.add(edgeColorAction);

		highlighting.add(new RepaintAction());
		highlighting.add(paint);
		m_vis.putAction("highlightcolors", highlighting);

		refresh();
		
		// when the focus tuple set changes (ie. the user clicks on a node), run filter action
		m_vis.getGroup(Visualization.FOCUS_ITEMS).addTupleSetListener(new TupleSetListener() {
			public void tupleSetChanged(TupleSet t, Tuple[] add, Tuple[] rem) {
				m_vis.run("filter");
			}
		});

	}
		
	private void refresh(){
		
		Layout layout = getGraphLayout(DEFAULT_LAYOUT, GRAPH, true);
		CollapsedSubtreeLayout subLayout = new CollapsedSubtreeLayout(GRAPH);

		ActionList layoutAction = new ActionList();
		layoutAction.add(layout);
		layoutAction.add(subLayout);
		m_vis.putAction("layout", layoutAction);

		ActionList filter = new ActionList();
		filter.add(new TreeRootAction(GRAPH));
		m_distanceFilter.setDistance(m_hops);
		filter.add(m_distanceFilter);
		
		m_vis.putAction("filter", filter);
		

		ActionList animate = new ActionList(1250);
		animate.setPacingFunction(new SlowInSlowOutPacer());
		animate.add(new QualityControlAnimator());
		animate.add(new VisibilityAnimator(GRAPH));
		animate.add(new PolarLocationAnimator(NODES));
		animate.add(new ColorAnimator(NODES));
		animate.add(new RepaintAction());
		m_vis.putAction("animate", animate);
		
		
		m_vis.alwaysRunAfter("filter", "layout");
		m_vis.alwaysRunAfter("layout", "paint");
		m_vis.alwaysRunAfter("paint", "animate");
				
	}
		
	
	private Component initDisplay(int width, int height) {

		m_display = new Display(m_vis);
		m_display.setForeground(Color.YELLOW);
		m_display.setBackground(Color.WHITE);

		for (Control control : m_controls){
			m_display.addControlListener(control);
		}

		JPanel panel = new JPanel(){
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void update(Graphics g) {
				paint(g);
			}
		};
		panel.setLayout(new BorderLayout());
		panel.add(m_display,BorderLayout.CENTER);
		panel.setSize(width,height);
		
	
		return panel;
	}


	public void repaint(IGraphModel graph) {
		if (graph == null) {
			return;
		}

		m_vis.repaint();
		System.out.println("control repaint:");
	}

	/**
	 * method allowing the setting of the graph
	 * @param g the graph to be used
	 */
	private boolean setGraph(IGraphModel g) {
		m_vis.removeGroup(GRAPH);
		m_graphModel = g;

		Graph graph = g.getGraph();
		if(graph != null) {
			//Predicate predicate = getGraphPredicate();
			VisualGraph vg = m_vis.addGraph(GRAPH, graph);//, predicate);
			m_vis.setValue(EDGES, null, VisualItem.INTERACTIVE, Boolean.TRUE);
			VisualItem f = (VisualItem) vg.getNode(0);		
			setFocusedItem(f);
			//m_nodeCountControl.setAvailableNodes(m_vis.getGroup(NODES).getTupleCount());

			return true;
		} else {
			return false;
		}
	}


	/**************************** LAYOUTS ********************************/
	public void switchLayoutAction() {
		m_currentLayout = (m_currentLayout + 1) % m_layouts.length;
		System.out.println("current layout: " + m_layouts[m_currentLayout]);
		Layout layout = getGraphLayout(m_layouts[m_currentLayout], GRAPH, true);
		CollapsedSubtreeLayout subLayout = new CollapsedSubtreeLayout(GRAPH);
		ActionList layoutAction = new ActionList();
		layoutAction.add(layout);
		layoutAction.add(subLayout);

		m_vis.putAction("layout", layoutAction);
		m_vis.alwaysRunAfter("filter", "layout");
		m_vis.alwaysRunAfter("layout", "paint");
		m_vis.alwaysRunAfter("paint", "animate");
		
		m_vis.run("filter");
	}

	/**
	 * method to get the graph layout used
	 * @param key the key for the graph layout
	 * @param group 
	 * @param enforcebounds
	 * @return
	 */
	private Layout getGraphLayout(String key, String group, boolean enforcebounds) {
		if (NODELINK_LAYOUT.equals(key)){
			Layout layout = new NodeLinkTreeLayout(group,prefuse.Constants.ORIENT_LEFT_RIGHT, 10.0,1.0,10.0); 
			layout.setVisualization(m_vis);
			return layout;
		}
		if (RADIAL_LAYOUT.equals(key)){
			Layout layout = new ExtendedRadialTreeLayout(group); 
			layout.setVisualization(m_vis);
			((ExtendedRadialTreeLayout)layout).setAutoScale(true);
			return layout;
		}
		if (BALLOON_TREE_LAYOUT.equals(key)){
			Layout layout = new BalloonTreeLayout(group,BALLOON_MIN_RADIUS); 
			layout.setVisualization(m_vis);
			return layout;
		}
		if (FORCE_DIRECTED_LAYOUT.equals(key)){
			Layout layout = new ForceDirectedLayout(group,enforcebounds, IS_FORCE_DIRECTED); 
			layout.setVisualization(m_vis);
			return layout;
		}		
		return null;
	}
	/************ LOCAL ACTION CLASSES ********************************/
	/**
	 * This action is required for the used of CollapsedSubtreeLayout. It gets the spanning tree for the current focus item. 
	 */
	private class TreeRootAction extends GroupAction {
		public TreeRootAction(String graphGroup) {
			super(graphGroup);
		}
		@Override
		public void run(double frac) {
			TupleSet focus = m_vis.getGroup(Visualization.FOCUS_ITEMS);
			if ( (focus==null) || (focus.getTupleCount() == 0) ) {
				return;
			}

			Graph g = (Graph)m_vis.getGroup(m_group);
			Node f = null;
			Iterator tuples = focus.tuples();
			while (tuples.hasNext() && !g.containsTuple(f=(Node)tuples.next()))
			{
				f = null;
			}
			if ( f == null ) {
				return;
			}
			g.getSpanningTree(f);
		}
	}

	//not used
	@SuppressWarnings("unused")
	private class FocusVisibilityAction extends GroupAction {
		protected VisualItem m_focusItem = null;
		private String m_edgeGroup, m_nodeGroup;
		private GraphControl m_control;

		Logger s_log = Logger.getLogger(FocusVisibilityAction.class.getCanonicalName());

		public FocusVisibilityAction(String nodeGroup, String edgeGroup, GraphControl control) {
			m_nodeGroup = nodeGroup;
			m_edgeGroup = edgeGroup;
			m_control = control;
		}

		private VisualItem getFocusItem() {
			TupleSet focus = getVisualization().getGroup(Visualization.FOCUS_ITEMS);
			if ((focus == null) || (focus.getTupleCount() == 0)) {
				return null;
			}
			assert focus.getTupleCount() == 1;

			for (Iterator i = focus.tuples(); i.hasNext(); ) {
				Tuple t = (Tuple)i.next();
				if (t instanceof VisualItem) {
					return (VisualItem)t;
				}
			}

			return null;
		}

		private IResource getResource() {
			if (getFocusItem() != null) {
				return (IResource)getFocusItem().get(ExploreEnvironment.RESOURCE);
			}
			return null;
		}

		@Override
		public void run(double frac) {
			VisualItem focus = getFocusItem();
			IResource focusResource = getResource();

			int nodeCount = 0;
			BreadthFirstIterator bfs = new BreadthFirstIterator();
			bfs.init(focus, 500, Constants.NODE_TRAVERSAL);
			while (bfs.hasNext()) {
				
				NodeItem node = (NodeItem)bfs.next();
				nodeCount++;

				node.setDOI(bfs.getDepth(node));

				if (node.equals(focus)) {
					s_log.debug("focus node " + node);
					PrefuseLib.updateVisible(node, true);
					node.setExpanded(true);
					continue;
				}

				//if (nodeCount > m_control.getMaxNumberOfNodes()){ //&&
						//!m_mustShowItems.contains(m_vis.getVisualItem("graph", node))){
					//PrefuseLib.updateVisible(node, false);
				//}
				//else {
				//	PrefuseLib.updateVisible(node, true);
				//}

//				for (Iterator i = node.inNeighbors(); i.hasNext(); ) {
//				Node neighbor = (Node)i.next();
//				IResource res = (IResource)neighbor.get(ExploreEnvironment.RESOURCE);
//				if (res != null && res.equals(focusResource)) {
//				PrefuseLib.updateVisible(node, true);
//				}
//				}

//				if (focusResource instanceof IIndividual) {
//				for (Iterator i = node.outEdges(); i.hasNext(); ) {
//				EdgeItem outEdge = (EdgeItem)i.next();
//				if (outEdge.getTargetItem().equals(focus) 
//				&& ExploreEnvironment.IS_INSTANCE_OF.equals(outEdge.get(ExploreEnvironment.LABEL)))
//				PrefuseLib.updateVisible(node, true);
//				}
//				}
			}

			// iterate over nodes
			
			TupleSet nodes = getVisualization().getGroup(m_nodeGroup);
			for (Iterator n = nodes.tuples(); n.hasNext(); ) {
				NodeItem node = (NodeItem)n.next();

				node.setExpanded(false);

				if (node.isEndVisible()) {
					for (Iterator e = node.edges(); e.hasNext(); ) {
						EdgeItem edge = (EdgeItem)e.next();
						if (edge.getTargetItem().isEndVisible() && (edge.getTargetItem().getDOI() > node.getDOI())) {
							s_log.debug("expanded " + node);
							node.setExpanded(true);
							break;
						}
					}
				}
			}
			
			showEdges();
			 
			// iterate over edges
			//TupleSet edges = m_vis.getGroup(m_edgeGroup);
			//for (Iterator e = edges.tuples(); e.hasNext(); ) {
			//EdgeItem edge = (EdgeItem)e.next();
////			PrefuseLib.updateVisible(edge, false);
//			edge.setVisible(false);

			// edges between visible nodes should be visible, all others are invisible
			//if (edge.getSourceItem().isVisible() && edge.getTargetItem().isVisible())
			//PrefuseLib.updateVisible(edge, true);
			//else
			//	PrefuseLib.updateVisible(edge, false);
			//}
		}
	}

	/************** the predicates used to define filter possibly, e.g. used for switching modes ***********/
//	private abstract class FocusedPredicate extends AbstractPredicate {
//		protected IResource m_focus;
//
//		public void setFocusedResource(IResource focus) {
//			m_focus = focus;
//		}
//	}
//
//	private FocusedPredicate m_completeGraphPredicate = new FocusedPredicate() {
//		@Override
//		public boolean getBoolean(Tuple t) {
//			s_log.debug("focused: " + m_focus);
//			return true;
//		}
//	};

//	private FocusedPredicate m_instanceGraphPredicate = new FocusedPredicate() {
//		@Override
//		public boolean getBoolean(Tuple t) {
//			Object o = t.get(ExploreEnvironment.RESOURCE);
//
//			if (o != null) {
//				if (!(o instanceof IResource))
//					return true;
//
//				if (o instanceof IIndividual)
//					return true;
//				else
//					return false;
//			}
//			else {
//				return false;
//			}
//		}
//	};

//	private FocusedPredicate m_schemaGraphPredicate = new FocusedPredicate() {
//		@Override
//		public boolean getBoolean(Tuple t) {
//			Object o = t.get(ExploreEnvironment.RESOURCE);
//
//			if (o != null) {
//				if (!(o instanceof IResource))
//					return true;
//
//				if (o instanceof INamedConcept || o instanceof IProperty)
//					return true;
//				else
//					return false;
//			}
//			else {
//				String label = t.getString(ExploreEnvironment.LABEL);
//				if (label.equals(ExploreEnvironment.SUBCLASS_OF))
//					return true;
//				return false;
//			}
//		}
//	};

//	private int m_currentMode = 0;

//	public void switchModeAction() {
//		System.out.print("switching mode: " + m_currentMode);
//		m_currentMode = (m_currentMode + 1) % m_graphPredicates.length;
//		System.out.println(" -> " + m_currentMode);
//	}
	
//	private FocusedPredicate[] m_graphPredicates = {m_completeGraphPredicate, 
//			m_schemaGraphPredicate, m_instanceGraphPredicate};
//
//	public FocusedPredicate getGraphPredicate() {
//		return m_graphPredicates[m_currentMode];
//	}



	//private int m_currentMode = 0;

	//private FocusedPredicate[] m_graphPredicates = {m_completeGraphPredicate, 
			//m_schemaGraphPredicate, m_instanceGraphPredicate};

	//public FocusedPredicate getGraphPredicate() {
		//return m_graphPredicates[m_currentMode];
	//}
	
	public boolean expandNode(VisualItem item){
		
		IResource res = (IResource)item.get(ExploreEnvironment.RESOURCE);
		
		if((res instanceof IConcept) || (res instanceof Variable) || (res instanceof IIndividual))
		{						
			if(!item.isVisible()) {
				PrefuseLib.updateVisible(item, Boolean.TRUE);
			}
			
			if(item.isExpanded())
			{
				s_log.debug("VisualItem '"+res.getLabel()+"' was already expanded.");
				return Boolean.FALSE;
			}
			
			if(isFocusedItem(item))
			{
				m_nodeCountControl.expand();
			}
			else
			{			
				List<VisualItem> neighbors = getNeighbors(item);
						
				for(VisualItem vis : neighbors)
				{				
					if(!vis.isVisible())
					{
						PrefuseLib.updateVisible(vis, Boolean.TRUE);
						addToFocusGroup((Node)vis);
					}
				}
				
				showEdges();
				item.setExpanded(Boolean.TRUE);
				
				m_nodeCountControl.disableDistanceFilter();						
				m_vis.run("filter");			
			}
		}
			
		return Boolean.TRUE;
	}
	
	private List<VisualItem> getNeighbors(VisualItem vis){
		
		List<VisualItem> out = new ArrayList<VisualItem>();
		
		Node node = m_graphModel.getGraph().getNode(vis.getRow());		
		Iterator iter = node.neighbors();
		
		while(iter.hasNext())
		{
			out.add(m_vis.getVisualItem(GRAPH,(Node)iter.next()));			
		}
				
		return out;
	}
	
	
	//items already visited - used only by collapseNode()
	private List<VisualItem> m_visitedItems;
	
	public boolean collapseNode(VisualItem vis){
		
		IResource res = (IResource)vis.get(ExploreEnvironment.RESOURCE);
		
		if((res instanceof IConcept) || (res instanceof Variable) || (res instanceof IIndividual))
		{		
			if(!vis.isExpanded())
			{
				s_log.debug("VisualItem '"+res.getLabel()+"' was not expanded.");
				return Boolean.FALSE;
			}
			
			if(isFocusedItem(vis))
			{
				m_nodeCountControl.collapse();
			}
			else
			{
				List<VisualItem> neighbors = getNeighbors(vis);
				
				for(VisualItem neighbor : neighbors)
				{										
					//please, no loops ...
					if(!m_visitedItems.contains(neighbor))
					{			
						if(!isFocusedItem(neighbor)&& !isDirectlyConnectedToRoot(neighbor))
						{
							//neighbor node is not expanded
							if(!neighbor.isExpanded())
							{
								PrefuseLib.updateVisible(neighbor, Boolean.FALSE);
							}
							//neighbor node is expanded
							else
							{
								m_visitedItems.add(vis);
								
								collapseNode(neighbor);
								PrefuseLib.updateVisible(neighbor, Boolean.FALSE);
								
							}
						}
					}
				}
				
				m_visitedItems.clear();
				
				hideEdges();			
				vis.setExpanded(Boolean.FALSE);
	
				
				m_nodeCountControl.disableDistanceFilter();
				m_vis.run("filter");	
			}
		}
		
		return Boolean.TRUE;
	}
		
	
	private Map<ILiteral,ArrayList<Node>> m_datatypes;
	
	public boolean getDatatypes(VisualItem vis){
		
		IResource res = (IResource)vis.get(ExploreEnvironment.RESOURCE);
		
		if(res instanceof ILiteral)
		{
			if(vis.isExpanded())
			{
				return Boolean.FALSE;
			}
			else
			{
				ILiteral lit = (ILiteral)res;
				Set<IDatatype> datatypes = lit.getDatatypes();
				Iterator<IDatatype> iter = datatypes.iterator();
				
				ArrayList<Node> datatypeNodes = new ArrayList<Node>();
				
				while(iter.hasNext())
				{
					IDatatype dat = iter.next();
					Node node = m_graphModel.addNode(dat);
					PrefuseLib.updateVisible(m_vis.getVisualItem(GRAPH, node), Boolean.TRUE);
					m_addedItems.add(node);
					addToFocusGroup(dat);
					
					datatypeNodes.add(node);
				}
				
				m_datatypes.put(lit, datatypeNodes);
			}
		}
		else
		{
			return Boolean.FALSE;
		}
		
		return Boolean.TRUE;
	}
	
	public boolean hideDatatypes(VisualItem vis){
		
		IResource res = (IResource)vis.get(ExploreEnvironment.RESOURCE);
		
		if(res instanceof ILiteral)
		{
			if(!vis.isExpanded())
			{
				return Boolean.FALSE;
			}
			else
			{
				ILiteral lit = (ILiteral)res;
				
				if(m_datatypes.containsKey(lit))
				{
					List<Node> datatypeNodes = m_datatypes.get(lit);
				
					for(Node node : datatypeNodes)
					{
						try
						{
							m_graphModel.getGraph().removeNode(node);					
							s_log.debug("Node '"+res+"' successfully deleted.");
						}
						catch(Exception e){}
					}
				
					m_datatypes.remove(lit);
				}
				else
				{
					return Boolean.FALSE;
				}
			}
		}
		else
		{
			return Boolean.FALSE;
		}
		
		return Boolean.TRUE;
	}
	
	private boolean isDirectlyConnectedToRoot(VisualItem vis){
		
		List<VisualItem> neighbors = getNeighbors(vis);
		
		while(!neighbors.isEmpty())
		{
			if(isFocusedItem(neighbors.get(0))) {
				return Boolean.TRUE;
			}
			
			neighbors.remove(0);
		}
			
		return Boolean.FALSE;
	}
	
			
	/**
	 *  Edges between visible nodes should be visible, all others invisible
	 */
	
	private void showEdges(){
		
		TupleSet edges = m_vis.getGroup(EDGES);
		Iterator iter = edges.tuples();
		
		while(iter.hasNext())
		{
			EdgeItem edge = (EdgeItem)iter.next();
		
			if (edge.getSourceItem().isVisible() && edge.getTargetItem().isVisible()) {
				PrefuseLib.updateVisible(edge, Boolean.TRUE);
			} else {
				PrefuseLib.updateVisible(edge, Boolean.FALSE);
			}
		}
		
	}
	
	private void hideEdges(){
		
		TupleSet edges = m_vis.getGroup(EDGES);
		Iterator iter = edges.tuples();
		
		while(iter.hasNext())
		{
			EdgeItem edge = (EdgeItem)iter.next();
		
			if (!(edge.getSourceItem().isVisible() && edge.getTargetItem().isVisible())) {
				PrefuseLib.updateVisible(edge, Boolean.FALSE);
			} else {
				PrefuseLib.updateVisible(edge, Boolean.TRUE);
			}
		}
		
	}
		
	private void cleanExpandedItems(){

		TupleSet nodes = m_vis.getGroup(NODES);
		Iterator iter = nodes.tuples();
		
		while(iter.hasNext())
		{
			((VisualItem)iter.next()).setExpanded(Boolean.FALSE);
		}		
	}
	
	private void updateFocusGroup(List<VisualItem> items, String focusGroup, Boolean clear){
		
		if(clear) {
			m_vis.getFocusGroup(focusGroup).clear();
		}
		
		for(VisualItem vis : items) {
			m_vis.getFocusGroup(focusGroup).addTuple(vis);
		}
			
	}
	
	private void hideElements(){
		
		TupleSet graph = m_vis.getGroup(GRAPH);
		Iterator iter = graph.tuples();
		
		while(iter.hasNext())
		{
			Tuple tuple = (Tuple) iter.next();
			VisualItem vis = m_vis.getVisualItem(GRAPH, tuple);
			
			PrefuseLib.updateVisible(vis, Boolean.FALSE);
		}	
	}
	
	/**************************** CONTROLS ********************************/
	/**
	 *  public method allowing the setting of the controls to be used for the manipulation
	 *  of the graph by the user
	 * @param controls - list of controls from the prefuse library 
	 */
	public void addControls(List<Control> controls){
		m_controls= controls;
	}
	

	private class NodeCountControl implements ChangeListener{
		
		private Container m_container;
		private SpinnerNumberModel m_spinnerModel;
		private int m_maxNodes = 15;
		private GraphControl m_graphControl;
		private JSpinner m_Reichweite;
		private SpinnerNumberModel m_ReichweiteModel;
		
		private Label m_hopsLabel;
		
				
		public NodeCountControl(GraphControl control) {
			
			m_graphControl = control;
			m_spinnerModel = new SpinnerNumberModel(m_maxNodes, 1, Integer.MAX_VALUE, 1);
			m_spinnerModel.addChangeListener(this);
			
			m_ReichweiteModel = new SpinnerNumberModel(1, 0, Integer.MAX_VALUE, 1);
			m_Reichweite = new JSpinner(m_ReichweiteModel);
			m_Reichweite.setSize(20, m_Reichweite.getHeight());
			m_ReichweiteModel.addChangeListener(this);
			

			m_container = new Container();
			m_container.setLayout(new FlowLayout(FlowLayout.RIGHT));
			
			m_hopsLabel = new Label("Hop Distance:");
			
			m_container.add(m_hopsLabel);
			m_container.add(m_Reichweite);
			
		}

		public Container getContainer() {
			return m_container;
		}
		
		//Used by expandNode(), if the focused item is to be expanded
		protected void expand(){
			
			//focused item is expanded
			getFocusedItem().setExpanded(Boolean.TRUE);
			
			//use GraphDistanceFilter for expanding
			m_ReichweiteModel.setValue(1);
			new ChangeEvent(m_ReichweiteModel);
			
		}
		
		//Used by collapseNode(), if the focused item is to be collapsed
		protected void collapse(){
			
			//focused item is not expanded any longer
			getFocusedItem().setExpanded(Boolean.FALSE);
			
			//use GraphDistanceFilter for collapsing
			m_ReichweiteModel.setValue(0);
			new ChangeEvent(m_ReichweiteModel);
			
		}
		
		protected void enableDistanceFilter(){
			
			m_hopsLabel.setEnabled(Boolean.TRUE);
			m_distanceFilter.setEnabled(Boolean.TRUE);
			
		}
		
		protected void disableDistanceFilter(){
			
			m_hopsLabel.setEnabled(Boolean.FALSE);
			m_distanceFilter.setEnabled(Boolean.FALSE);
			
		}
		
		public void stateChanged(ChangeEvent event) {
			
			if (event.getSource() == (m_spinnerModel)) 
			{			
				if (m_switch) 
				{
					refresh();
					m_vis.run("filter");				
				}
			} 
			else if (event.getSource() == m_ReichweiteModel) 
			{
						
				m_switch = false;

				enableDistanceFilter();
				m_graphControl.setHopsOfDistance((Integer) m_ReichweiteModel.getValue());									
				m_distanceFilter.setDistance(m_hops);
				
				refresh();
				m_vis.run("filter");
			}
		}
	}


	private class PathControl implements MouseListener {
		
		private Container m_container;
		private Map<Component,VisualItem> m_component2Item;
		private GraphControl m_graphControl;


		public PathControl(GraphControl graphControl) {
			m_component2Item = new HashMap<Component,VisualItem>();
			m_graphControl = graphControl;

			m_container = new Container();
			m_container.setLayout(new FlowLayout(FlowLayout.LEFT));

			Label historyLabel =  new Label("History: ");
			historyLabel.setForeground(Color.BLUE);
			m_container.add(historyLabel);
		}

		public Container getContainer() {
			return m_container;
		}

		public void addEntry(VisualItem item) {

			Label label = new Label("[" + item.getString(ExploreEnvironment.LABEL) + "]");
			label.setVisible(true);
			label.setForeground(Color.BLUE);
			label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			label.addMouseListener(this);
			
						
			//item has not been visited before
			m_component2Item.put(label, item);			
			m_container.add(label);
			
			if (m_container.getComponentCount()-1 > ExploreEnvironment.HISTORY_LENGTH) {
				m_container.remove(1);
			}	
			
			m_container.validate();
		}

		//not used!
		public void popEntries(Component component) {
			for (Component c : m_container.getComponents()) {
				if (c.equals(component)) {
					m_container.remove(c);
					m_component2Item.remove(c);
					break;
				}
			}
		}
		
		public void disableEntry(VisualItem item){
			
			Integer[] idx = getLabel(item);
			
			for(int index : idx)
			{
				((Label)m_container.getComponents()[index]).setEnabled(Boolean.FALSE);
			}
			
		}
		
		private Integer[] getLabel(VisualItem item){
			
			ArrayList<Integer> out = new ArrayList<Integer>();
			Component[] comp = m_container.getComponents();
			
			for(int i = 0; i < comp.length; i++)
			{
				if(((Label)comp[i]).getText().equals("[" + item.getString(ExploreEnvironment.LABEL) + "]")) {
					out.add(i);
				}
			}
			

			return out.toArray(new Integer[0]);
		}

		public void mouseClicked(MouseEvent event) {
			
			Component source = (Component)event.getSource();
			m_graphControl.setFocusedItem(m_component2Item.get(source));

		}

		public void mouseEntered(MouseEvent arg0) {
		}

		public void mouseExited(MouseEvent arg0) {
		}

		public void mousePressed(MouseEvent arg0) {
		}

		public void mouseReleased(MouseEvent arg0) {
		}
	}
}