package org.aifb.xxplore.views.graphviewer;


import java.awt.event.MouseEvent;
import java.util.Arrays;

import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;

import org.aifb.xxplore.action.CollapseNodeAction;
import org.aifb.xxplore.action.ExpandNodeAction;
import org.aifb.xxplore.action.GetDataTypesNodeAction;
import org.aifb.xxplore.action.HideDataTypesNodeAction;
import org.aifb.xxplore.model.IExploreEditorContentProvider;
import org.aifb.xxplore.views.dnd.ElementTransfer;
import org.aifb.xxplore.views.dnd.ExploreDragController;
import org.apache.log4j.Logger;
import org.ateam.xxplore.core.ExploreEnvironment;
import org.ateam.xxplore.core.model.definition.ModelDefinition;
import org.ateam.xxplore.core.model.navigation.IGraphModel;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.ContentViewer;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.xmedia.oms.model.api.IConcept;
import org.xmedia.oms.model.api.IDatatype;
import org.xmedia.oms.model.api.IIndividual;
import org.xmedia.oms.model.api.ILiteral;
import org.xmedia.oms.model.api.IResource;
import org.xmedia.oms.query.Variable;

import prefuse.controls.ControlAdapter;
import prefuse.controls.FocusControl;
import prefuse.controls.ZoomControl;
import prefuse.visual.VisualItem;


/**
 * A concrete viewer based on an graph control. It is to be instantiated with a SWT graph control 
 * and configured with a content provider, label provider, element filter (optional), and 
 * element sorter (optional).
 */
public class GraphViewer extends ContentViewer{

	private ISelection m_selection; 

	//this is only needed to get access to the main thread in which the viewer is created
	//the main thread then will be used to run GUI related codes, e.g. fireSelectionChanged   
	private Display m_display; 

	/**
	 * The viewer's control.
	 */
	private GraphControl m_graphControl;
	
	private Logger s_log = Logger.getLogger(GraphViewer.class.getCanonicalName());
	

	/**
	 * Creates a graph viewer on a graph control under the given
	 * parent. Yet, the viewer has no input, no content provider, a default label provider, no
	 * sorter, and no filters.
	 * 
	 * @param parent
	 *            the parent control
	 */
	public GraphViewer(Composite parent) {
		this(parent, SWT.MULTI | SWT.BORDER);//| SWT.H_SCROLL | SWT.V_SCROLL 
	}

	/**
	 * Creates a tree viewer on a newly-created tree control under the given
	 * parent. The viewer has no input, no content provider, a default label provider, no
	 * sorter, and no filters.
	 * 
	 * @param parent
	 *            the parent control
	 * @param style
	 *            the SWT style bits used to create the tree.
	 */
	public GraphViewer(Composite parent, int style) {
		this(new GraphControl(parent, style));
	}

	/**
	 * Creates a tree viewer on the given tree control. The viewer has no input,
	 * no content provider, a default label provider, no sorter, and no filters.
	 * 
	 * @param tree
	 *            the tree control
	 */
	public GraphViewer(GraphControl graph) {
		
		super();
		m_graphControl = graph;		
		hookControl(m_graphControl);
		m_display = Display.getCurrent();
		
	}

	@Override
	public void setContentProvider(IContentProvider provider) {
		Assert.isTrue(provider instanceof IExploreEditorContentProvider);
		super.setContentProvider(provider);
	}

	@Override
	public Control getControl() {
		return m_graphControl;
	}


	@Override
	public ISelection getSelection() {
		return m_selection;
	}

	@Override
	/**
	 * Overide parent method to avoid refresh after label provider setted 
	 */
	public void setLabelProvider(IBaseLabelProvider labelProvider) {
		IBaseLabelProvider oldProvider = getLabelProvider();
		// If it hasn't changed, do nothing.
		// This also ensures that the provider is not disposed
		// if set a second time.
		if (labelProvider == oldProvider) {
			return;
		} else {
			oldProvider = labelProvider;
			//as opposed to parent method, we do not add any listener to the label provider; have to support this later when needed 
		}

	}

	@Override
	/**
	 * this method is called by the content provider when the underlying
	 * model has been changed substantially. Substantially means that the model definition 
	 * and the resulting graph has change in the structure. Therefore, the graph has been 
	 * recalculated by the provider and the control needs to be updated completely with the 
	 * new graph. 
	 *  
	 * 
	 */
	public void refresh() {		
		IGraphModel model = ((IExploreEditorContentProvider)getContentProvider()).getGraphModel();
		m_graphControl.refresh(model);		
	}


	@Override
	public void setSelection(ISelection selection, boolean reveal) {	
		m_selection= selection;
		final ISelectionProvider provider = this; 

		//running fireSelectionChanged within the AWT thread would resulted in a SWTException 
		//GUI related events must be run in the main thread which can be accessed via the m_display object
		m_display.syncExec(new Runnable(){
			public void run(){
				fireSelectionChanged(new SelectionChangedEvent(provider, m_selection));
			}
		});

	}

	public boolean alreadyAdded(long selection_time){
		
		if(m_graphControl.getSelectionTime() == selection_time){
			return true;
		}
		else{
			m_graphControl.setSelectionTime(selection_time);
			return false;
		}
	}
	
	/**
	 * delegate to the control
	 * @param definition
	 */
	public void dispDefinitionViewInput(ModelDefinition def){
		
		if(def == null)
		{
			if(m_graphControl.getModelDefHashCode() != 0)
			{
				m_graphControl.dispDefinitionViewInput(def);
				s_log.debug("Deleted Modeldefinition from graph.");
			}
		}
		else
		{
			if((m_graphControl.getModelDefHashCode() != def.hashCode()))
			{
				m_graphControl.dispDefinitionViewInput(def);
				s_log.debug("ModelDefinition '"+def+"' added to graph.");
			}
			else
			{
				if(m_graphControl.getAddedRoot() != null){
					m_graphControl.setFocusedItem(m_graphControl.getAddedRoot());
					s_log.debug("ModelDefinition '"+def+"' already added to graph. Focus on added root.");
				}			
			}
		}	
	}

	/**
	 * delegate to the control
	 * @param res
	 */
	public void setFocus(IResource res){
		m_graphControl.setFocusedItem(res);
	}

	//private void setFocusOnDefinitionView(){

		//if(m_graphControl.getAddedRoot() == null)
			//return;
		
		//setFocus(m_graphControl.getAddedRoot());
	//}
	
	protected void initDrag(final ExploreDragController dragcontrol){

		int operations = DND.DROP_COPY;
		final Control fcontrol = m_graphControl; 
		final DragSource source = new DragSource(fcontrol, operations);

		Transfer[] types = new Transfer[] {ElementTransfer.getInstance(), TextTransfer.getInstance()};
		source.setTransfer(types);

		source.addDragListener(dragcontrol);			 	   

		fcontrol.addDisposeListener(new DisposeListener(){
			public void widgetDisposed(DisposeEvent e) {			
				source.dispose();
			}
		});

	}

	private void addContextMenu(VisualItem item) {
		
		IResource res = (IResource)item.get(ExploreEnvironment.RESOURCE);

		final MenuManager menu = new MenuManager();
		ExpandNodeAction expandNodeAction = new ExpandNodeAction(item,this);
		CollapseNodeAction collapseNodeAction = new CollapseNodeAction(item,(GraphControl)getControl());
		

		if((res instanceof IConcept) || (res instanceof Variable) || (res instanceof IIndividual))
		{
			if(!item.isExpanded())
			{
				expandNodeAction.setEnabled(Boolean.TRUE);
				collapseNodeAction.setEnabled(Boolean.FALSE);
			}
			else
			{
				expandNodeAction.setEnabled(Boolean.FALSE);
				collapseNodeAction.setEnabled(Boolean.TRUE);
			}
			
			menu.add(expandNodeAction);
			menu.add(collapseNodeAction);
		}
		else
		{			
			if(res instanceof ILiteral)
			{
				HideDataTypesNodeAction hideDataTypesNodeAction = new HideDataTypesNodeAction(item,this);			
				GetDataTypesNodeAction getDataTypesNodeAction = new GetDataTypesNodeAction(item,this);
					
				if(!item.isExpanded())
				{
					getDataTypesNodeAction.setEnabled(Boolean.TRUE);
					hideDataTypesNodeAction.setEnabled(Boolean.FALSE);
				}
				else
				{
					getDataTypesNodeAction.setEnabled(Boolean.FALSE);
					hideDataTypesNodeAction.setEnabled(Boolean.TRUE);
				}
				
				menu.add(getDataTypesNodeAction);
				menu.add(hideDataTypesNodeAction);
			}
		}
	
		
		getControl().getDisplay().asyncExec(new Runnable() {
			public void run() {
				Menu contextMenu = menu.createContextMenu(m_graphControl);
				contextMenu.setVisible(true);

			}
		});	
	}

	/**
	 * Hook to the prefuse controls to handle incoming user events. 
	 * @param guiElement
	 */
	private void hookControl(GraphControl guiElement){
		super.hookControl(guiElement);

		FocusControl focusControl = new FocusControl(2){
			@Override
			public void itemClicked(VisualItem item, MouseEvent e){

				if(!item.isInGroup(GraphControl.NODES)) {
					return;
				}

				super.itemClicked(item, e);
				
				if (SwingUtilities.isLeftMouseButton(e)) 
				{
					if (item.canGet(ExploreEnvironment.RESOURCE, IResource.class))
					{
						IResource res = (IResource) item.get(ExploreEnvironment.RESOURCE);						
						Object[] out = new Object[2];
	
						//conceptual browsing performed 
						if (e.getClickCount() == 2)
						{
							out[1] = ExploreEnvironment.CONCEPTUAL_ZOOMING_PERFORMED;
							m_graphControl.setFocusedItem(item);
						}
						//item activated performed 
						else
						{
							out[1] = ExploreEnvironment.ITEM_ACTIVATED_PERFORMED;
						}
	
						out[0] = res;
						setSelection(new StructuredSelection(out), false);
					}
				}
				else if (SwingUtilities.isRightMouseButton(e))
				{
					addContextMenu(item);
				}
			}
		};
		
		ControlAdapter controlAdapter = new ControlAdapter(){
			
			@Override
			public void itemPressed(VisualItem item, java.awt.event.MouseEvent e){
				
				IResource res = (IResource) item.get(ExploreEnvironment.RESOURCE);

				Object[] out = new Object[2];
				out[0] = res;
				out[1] = ExploreEnvironment.ITEM_PRESSED;
				
				setSelection(new StructuredSelection(out));
			}
			
		};

		//FocusControl selectionControl = new FocusControl(Visualization.SELECTED_ITEMS);
		//init Hover Control
		ExploreHoverController hoverControl = new ExploreHoverController("highlightcolors");
		//init Drag Control
		ExploreDragController exploredragcontrol = new ExploreDragController( this , m_graphControl);
		initDrag(exploredragcontrol);

		ToolTipListener toolTipListener = new ToolTipListener();
		
		prefuse.controls.Control[] controls = new prefuse.controls.Control[]{
				focusControl,
				hoverControl,
				controlAdapter,
				exploredragcontrol,
				//selectionControl,
				//new DragControl(),
				//new PanControl(),
				new ZoomControl(),
				//new WheelZoomControl(),
				//new ZoomToFitControl(),
//				new NeighborHighlightControl(),
				toolTipListener,

		};
		guiElement.addControls(Arrays.asList(controls));

	}

	public class ToolTipListener extends ControlAdapter{
		

		public ToolTipListener(){
					
			ToolTipManager.sharedInstance().setEnabled(true);
			ToolTipManager.sharedInstance().setInitialDelay(150);
			
		}
		
		
		@Override
		public void itemEntered(VisualItem vis, MouseEvent e) {
			
			String value = new String();		
			ToolTipManager.sharedInstance().setDismissDelay(1000);
			
			IResource res = (IResource)vis.get(ExploreEnvironment.RESOURCE);
			
			if(vis.isInGroup(GraphControl.NODES))
			{				
				value += "<html>";
				value += "<b>Type:</b> ";
				
				if(res instanceof Variable) {
					value += "Variable";
				} else if(res instanceof ILiteral) {
					value += "Literal";
				} else if(res instanceof IDatatype) {
					value += "Datatype";
				} else if(res instanceof IIndividual) {
					value += "Individual";
				} else if(res instanceof IConcept) {
					value += "Concept";
				}
				
				value += "</html>";
							
			}
			else
			{				
				return;			
			}
			
			m_graphControl.m_display.setToolTipText(value);
		}
		
		@Override
		public void itemExited(VisualItem v, MouseEvent e)
		{			
			m_graphControl.m_display.setToolTipText(null);		    
		}
	}
}
