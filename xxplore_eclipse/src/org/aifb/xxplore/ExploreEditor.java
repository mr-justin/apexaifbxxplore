package org.aifb.xxplore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;

import org.aifb.xxplore.model.ExploreEditorContentProvider;
import org.aifb.xxplore.model.ExploreEditorLabelProvider;
import org.aifb.xxplore.views.graphviewer.GraphViewer;
import org.apache.log4j.Logger;
import org.ateam.xxplore.core.ExploreEnvironment;
import org.ateam.xxplore.core.model.definition.IModelDefinition;
import org.ateam.xxplore.core.model.definition.ModelDefinition;
import org.ateam.xxplore.core.service.KbContentProviderService;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ContentViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.dialogs.SaveAsDialog;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.views.contentoutline.ContentOutline;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.ui.views.properties.PropertySheet;
import org.eclipse.ui.views.properties.PropertySheetPage;
import org.xmedia.oms.model.impl.NamedConcept;
import org.xmedia.oms.persistence.IDataSource;




public class ExploreEditor extends MultiPageEditorPart implements ISelectionProvider,  Listener{

	public static final String ID = "org.aifb.xxplore.exploreeditor";

	private final static Logger s_log = Logger.getLogger(ExploreEditor.class);

	private static Hashtable<IEditorInput,IModelDefinition> s_input_defmapping;

	static {

		s_input_defmapping = new Hashtable<IEditorInput, IModelDefinition>();

	}

	private IModelDefinition m_currentdef;


	protected ExploreContentOutlinePage m_contentOutlinePage;

	/**
	 * This is a kludge...
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected IStatusLineManager m_contentOutlineStatusLineManager;


	/**
	 * This is the property sheet page.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected PropertySheetPage m_propertySheetPage;

	/**
	 * The viewer displaying the current graph
	 * 
	 */	
	private GraphViewer m_graphViewer;


	/**
	 * This shows how a tree view works.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private TreeViewer m_treeViewer;

	/**
	 * This shows how a list view works.
	 * A list viewer doesn't support icons.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private ListViewer m_listViewer;

	/**
	 * This shows how a table view works.
	 * A table can be used as a list with icons.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private TableViewer m_tableViewer;

	private ExploreEditorContentProvider m_contentprovider;

	private Viewer[] m_viewers = new Viewer[4];

	/**
	 * This keeps track of the active content viewer, which may be either one of the viewers in the pages or the content outline viewer.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private Viewer m_currentViewer;

	/**
	 * This listens to selection from the currently active viewer 
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private ISelectionChangedListener m_selectionChangedListener;

	/**
	 * This keeps track of all the {@link org.eclipse.jface.viewers.ISelectionChangedListener}s that are listening to this editor.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private Collection<ISelectionChangedListener> m_selectionChangedListeners = new ArrayList<ISelectionChangedListener>();

	/**
	 * This keeps track of the selection of the editor as a whole.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private ISelection m_selection = StructuredSelection.EMPTY;

	/**
	 * Ths listen to selections on other viewers of the workbench
	 */
	private ISelectionListener m_workbenchSelectionListener = new ISelectionListener() {

		public void selectionChanged(IWorkbenchPart sourcepart, ISelection selection) {

			if (sourcepart.getSite().getId().equals(ConceptHierarchyView.ID))
			{	
				if((selection instanceof IStructuredSelection) && !selection.isEmpty())
				{
					NamedConcept concept = (NamedConcept)((StructuredSelection)selection).getFirstElement();      			
					m_graphViewer.setFocus(concept); 	
				}        		
			}
		}

	};

	private ISelectionListener m_defViewlistener = new ISelectionListener() {

		@SuppressWarnings("unchecked")
		public void selectionChanged(IWorkbenchPart sourcepart, ISelection selection) {

			// listening only to selection changes in the DefinitionView
			if (sourcepart.getSite().getId().equals(DefinitionView.ID)) 
			{			   
				if((selection instanceof IStructuredSelection) && !selection.isEmpty())
				{					
					if( (((StructuredSelection)selection).getFirstElement() instanceof Integer) &&
							((Integer)((StructuredSelection)selection).getFirstElement() == ExploreEnvironment.XXPLORE))
					{

						Iterator<Object> iter = ((StructuredSelection)selection).iterator();
						//first element of selection event is about the type of the selection
						iter.next();
						
						//get second element is QueryWrapper > not needed here ...
						ModelDefinition def = (ModelDefinition)iter.next();
						
						//get third element: selection time
						long selectionTime = (Long)iter.next();
						
						if(!m_graphViewer.alreadyAdded(selectionTime)){
							m_graphViewer.dispDefinitionViewInput(def);
						}
					}

					if((((StructuredSelection)selection).getFirstElement() instanceof Integer) &&
							((Integer)((StructuredSelection)selection).getFirstElement() == ExploreEnvironment.CLEAR))
					{
						m_graphViewer.dispDefinitionViewInput(null);	
					}
				}
			}
		}
	};

	/**
	 * This listens for when the relevant part become active 
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected IPartListener m_partListener = new IPartListener() {
		public void partActivated(IWorkbenchPart p) {
			if (p instanceof ContentOutline) {
				if (((ContentOutline)p).getCurrentPage() == m_contentOutlinePage) {				
					m_contentOutlinePage.init(m_contentOutlinePage.getSite()); 					
				}
			}
			else if (p instanceof PropertySheet) {
				if (((PropertySheet)p).getCurrentPage() == m_propertySheetPage) {
					//do smth
				}
			}
			else if (p == ExploreEditor.this) {
				handleActivate();
			}
		}

		public void partBroughtToTop(IWorkbenchPart p) {}

		public void partClosed(IWorkbenchPart part) {}

		public void partDeactivated(IWorkbenchPart p) {}

		public void partOpened(IWorkbenchPart part) {
			if (part instanceof DefinitionView){
				DefinitionView view = (DefinitionView)part; 
				view.getViewer().setInput(m_currentdef);
			}
			else if (part instanceof DocumentResultView){
				DocumentResultView view = (DocumentResultView)part; 
				view.getViewer().setInput(m_currentdef);
			}
			else if (part instanceof FactResultView){
				FactResultView view = (FactResultView)part; 
				view.getViewer().setInput(m_currentdef);
			}
			else if (part instanceof ConceptHierarchyView){
				addSelectionChangedListener((ISelectionChangedListener)part);
				((ConceptHierarchyView)part).getViewer().setContentProvider(m_contentprovider.new ConceptHierachyViewContentProvider());
				((ConceptHierarchyView)part).getViewer().setInput(m_currentdef);
			}
		}
	};



	/**
	 * This is called during startup.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void init(IEditorSite site, IEditorInput editorInput) throws PartInitException {
		if (!(editorInput instanceof IStorageEditorInput)){
			System.err.println("error in input type"+ editorInput);
			throw new PartInitException("Invalid Input ");		
		}

		setSite(site);		
		setInput(editorInput);
		setPartName(editorInput.getName());

		site.setSelectionProvider(this);
		site.getPage().addPartListener(m_partListener);
		site.getPage().addSelectionListener(m_workbenchSelectionListener);
		site.getPage().addSelectionListener(m_defViewlistener);

		m_contentOutlinePage = new ExploreContentOutlinePage();
		// set the content outline page so that the selection is displayed in the content outline listener
		addSelectionChangedListener(m_contentOutlinePage);

	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void dispose() {

		getSite().getPage().removePartListener(m_partListener);
		getSite().getPage().removeSelectionListener(m_workbenchSelectionListener);
		getSite().getPage().removeSelectionListener(m_defViewlistener);

		if (m_propertySheetPage != null) {
			m_propertySheetPage.dispose();
		}

		if (m_contentOutlinePage != null) {
			m_contentOutlinePage.dispose();
		}

		super.dispose();
	}

	/**
	 * Handles activation of the editor or it's associated views.
	 * @generated
	 */
	private void handleActivate() {		
		this.setFocus();
	}


	/**
	 * implement event handling method of Listener
	 * @see Listener
	 */
	public void handleEvent(Event event) {

		if(event.type == SWT.Activate) {
			// when treeviewer has been activated 
			if((m_treeViewer != null) && (event.widget == m_treeViewer.getControl())){
				if(s_log.isDebugEnabled()) {
					s_log.debug("tree viewer activated...");
				}

				//do something
			}
		}

		// when graphviewer activated
		//...

	}


	/**
	 * Set the viewer as the current viewer and listen to 
	 * selection of that viewer.
	 * @generated
	 */
	public void setCurrentViewer(Viewer viewer) {
		// If it is changing...
		if (m_currentViewer != viewer) {
			if (m_selectionChangedListener == null) {

				// Create the listener on demand
				m_selectionChangedListener =
					new ISelectionChangedListener() {

					// notifies those things that are affected by the section.
					public void selectionChanged(SelectionChangedEvent selectionChangedEvent) {
						setSelection(selectionChangedEvent.getSelection());

					}
				};
			}

			// Stop listening to the old one.
			if (m_currentViewer != null) {
				m_currentViewer.removeSelectionChangedListener(m_selectionChangedListener);
			}

			// Start listening to the new one.
			if (viewer != null) {
				viewer.addSelectionChangedListener(m_selectionChangedListener);
			}

			// Remember it.
			m_currentViewer = viewer;

			// Set the editors selection based on the current viewer's selection.

			if ((m_currentViewer != null) && (m_currentViewer.getSelection() != null)){
				setSelection(m_currentViewer.getSelection());
			}
		}
	}




	/**
	 * This is the method used by the framework to install your own controls.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void createPages() {

		if(s_log.isDebugEnabled()) {
			s_log.debug("Pages of ExploreEditor is about to be created...");
		}

		createPage(new GraphViewer(getContainer()), m_currentdef);
		createPage(new TreeViewer(getContainer()),  m_currentdef);
		createPage(new TableViewer(getContainer()),  m_currentdef);
		createPage(new ListViewer(getContainer()),  m_currentdef);

		// set the first tab as the active page (which is the GraphViewer)
		setActivePage(0);

	}

	private void createPage(ContentViewer viewer, Object input){

		if(s_log.isDebugEnabled()) {
			s_log.debug("Page of " + viewer.getClass() + "is about to be created...");
		}

		if (m_contentprovider == null){

			KbContentProviderService content = new KbContentProviderService();
			m_contentprovider = new ExploreEditorContentProvider(content);
		}

		viewer.setContentProvider(m_contentprovider);

		viewer.setLabelProvider(new ExploreEditorLabelProvider());

		viewer.setInput(input);

		//listen to the activation of viewer's control
		if (viewer.getControl() != null) {
			viewer.getControl().addListener(SWT.Activate, this);
		}

		//add to page
		int pageIndex = addPage(viewer.getControl());
		m_viewers[pageIndex]=viewer;

		if (viewer instanceof ListViewer){
			m_listViewer = (ListViewer)viewer;
			setPageText(pageIndex, "List");
			//setPageImage(pageIndex, getImage(list));
		}

		if (viewer instanceof GraphViewer){
			m_graphViewer = (GraphViewer)viewer;
			setPageText(pageIndex, "Graph");
			//setPageImage(pageIndex, getImage(graph));
		}

		if (viewer instanceof TableViewer){
			m_tableViewer = (TableViewer)viewer;
			setPageText(pageIndex, "Table");
			//setPageImage(pageIndex, getImage(table));
		}

		if (viewer instanceof TreeViewer){
			m_treeViewer = (TreeViewer)viewer;
			setPageText(pageIndex, "Tree");
			//setPageImage(pageIndex, getImage(tree));
		}

	}


	/**
	 * This creates a context menu for the viewer and adds a listener as well registering the menu for extension.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void createContextMenu(ContentViewer viewer) {
		MenuManager contextMenu = new MenuManager();
		contextMenu.add(new Separator("seperator"));
		contextMenu.setRemoveAllWhenShown(true);

		Menu menu= contextMenu.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(contextMenu, viewer);

	}

	/**
	 * This is used to track the active viewer.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected void pageChange(int pageIndex) {
		super.pageChange(pageIndex);
		//get current viewer 
		//setSelectionToViewer()

	}

	/**
	 * This is how the framework determines which interfaces we implement.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object getAdapter(Class key) {
		if (key.equals(IContentOutlinePage.class)) {			
			return m_contentOutlinePage;
		}
		else if (key.equals(IPropertySheetPage.class)) {
			return getPropertySheetPage();
		}
		return super.getAdapter(key);
	}


	/**
	 * This accesses a cached version of the property sheet.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public IPropertySheetPage getPropertySheetPage() {
		if (m_propertySheetPage == null) {
			m_propertySheetPage = new PropertySheetPage();			
		}

		return m_propertySheetPage;
	}

	/**
	 * This returns the viewer as required by the {@link IViewerProvider} interface.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Viewer getViewer() {
		return m_currentViewer;
	}

	/**
	 * This implements {@link org.eclipse.jface.viewers.ISelectionProvider} to return this editor's overall selection.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ISelection getSelection() {
		return m_selection;
	}

	public ExploreEditorContentProvider getContentProvider(){
		return m_contentprovider;
	}

	public IModelDefinition getModelDefinition(){
		return m_currentdef;
	}


	/**
	 * This is for implementing {@link IEditorPart} and should save the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void doSave(IProgressMonitor progressMonitor) {

	}

	/**
	 * This returns true because it is supported by this editor.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isSaveAsAllowed() {
		return true;
	}

	/**
	 * Save the editor input. 
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void doSaveAs() {
		SaveAsDialog saveAsDialog= new SaveAsDialog(getSite().getShell());
		saveAsDialog.open();
		IPath path= saveAsDialog.getResult();
		if (path != null) {
			IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
			if (file != null) {
				//Save here
			}
		}
	}





	/**
	 * This implements {@link org.eclipse.jface.viewers.ISelectionProvider}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		m_selectionChangedListeners.add(listener);
	}

	/**
	 * This implements {@link org.eclipse.jface.viewers.ISelectionProvider}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		m_selectionChangedListeners.remove(listener);
	}


	/**
	 * This implements {@link org.eclipse.jface.viewers.ISelectionProvider} to set this editor's overall selection.
	 * Calling this result will notify the listeners.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setSelection(ISelection selection) {
		if(selection != null){
			m_selection = selection;
			if (m_selection == null) {m_selection = new StructuredSelection();}

			for (Iterator<ISelectionChangedListener> listeners = 
				m_selectionChangedListeners.iterator(); listeners.hasNext(); ) {
				listeners.next().selectionChanged(
						new SelectionChangedEvent(this, m_selection));
			}
		}
	}

	/**
	 * This sets the selection into whichever viewer is active.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private void setSelectionToViewer(Collection collection) {
		final Collection theSelection = collection;


		if ((theSelection != null) && !theSelection.isEmpty()) {
			// maybe should run this deferred
			// to give the editor a chance to process the viewer update events
			// and hence to update the views first.

			Runnable runnable =
				new Runnable() {
				public void run() {
					// Try to select the items in the current content viewer of the editor.
					//
					if (m_currentViewer != null) {
						m_currentViewer.setSelection(new StructuredSelection(theSelection.toArray()), true);

					}
				}
			};
			runnable.run();
		}
	}

	@Override
	protected void setActivePage(int pageIndex) {	
		super.setActivePage(pageIndex);
		setCurrentViewer(m_viewers[pageIndex]);
	}	

	@Override
	protected void setInput(IEditorInput input) {			
		if (input != getEditorInput()) {
			super.setInput(input);
			try {

				IModelDefinition def =  createInput(input);

				s_input_defmapping.put (input,def);

				if (def != null){ 
					//remove old listener
					m_currentdef = def; 

					DocumentResultView resultform = (DocumentResultView)getSite().getPage().findView(DocumentResultView.ID);
					if (resultform != null) {
						resultform.getViewer().setInput(m_currentdef);
					}

					DefinitionView definition = (DefinitionView)getSite().getPage().findView(DefinitionView.ID);
					if (definition != null) {
						definition.getViewer().setInput(m_currentdef);
					}

					FactResultView factresult = (FactResultView)getSite().getPage().findView(FactResultView.ID);
					if (factresult != null) {
						factresult.getViewer().setInput(m_currentdef);
					}
				}

			} catch (CoreException e) {
				e.printStackTrace();
			}
		}		
	}

	protected IModelDefinition createInput(IEditorInput input)throws CoreException{		
		IModelDefinition modeldefinition = null;

		if (input instanceof IFileEditorInput){

			IDataSource ds = ExplorePlugin.getDatasource(
					((IFileEditorInput)input).getFile().getFullPath().toString());

			modeldefinition = new ModelDefinition(ds);					
		}
		return modeldefinition;
	}

}
