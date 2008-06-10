package org.aifb.xxplore;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.aifb.xxplore.action.CreateQueryAction;
import org.aifb.xxplore.action.DeleteQueryAction;
import org.aifb.xxplore.core.ExploreEnvironment;
import org.aifb.xxplore.model.StoredQueryViewContentProvider;
import org.aifb.xxplore.model.StoredQueryViewLabelProvider;
import org.aifb.xxplore.storedquery.IQuery;
import org.aifb.xxplore.storedquery.IStoredQueryListElement;
import org.aifb.xxplore.storedquery.StoredQueryListElement;
import org.aifb.xxplore.storedquery.StoredQueryListManager;
import org.aifb.xxplore.storedquery.StoredQueryEditorInput;
import org.aifb.xxplore.storedquery.StoredQueryList;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;
import org.xmedia.oms.query.QueryWrapper;

public class StoredQueryView extends ViewPart {
	
	public static final String ID = "org.aifb.xxplore.storedqueryview";
	private long m_lastSelectionTime_DefintionView;
	private long m_lastSelectionTime_SparqlQueryView;
	
//	private static Logger s_log = Logger.getLogger(StoredQueryView.class);
	
	private TreeViewer m_QueryListViewer;
	private Button m_saveButton;

	private StoredQueryViewContentProvider m_contentProvider;
	private StoredQueryViewLabelProvider m_labelProvider;
	
	private CreateQueryAction m_createQueryAction;
	private static StoredQueryListManager m_queryListManager;
	
	public StoredQueryView(){
        File storedQueryFile = new File(ExploreEnvironment.STOREDQUERY_DIR);
        if (m_queryListManager == null){
        	m_queryListManager = new StoredQueryListManager(storedQueryFile);
        	m_queryListManager.readQueryList();
        }	
	}
	
	private ISelectionListener m_sparqlQueryViewlistener = new ISelectionListener() {
		public void selectionChanged(IWorkbenchPart sourcepart, ISelection selection) {
			if (sourcepart.getSite().getId().equals(SparqlQueryView.ID) || sourcepart.getSite().getId().equals(DefinitionView.ID)){			   
				if((selection instanceof IStructuredSelection) && !selection.isEmpty()){										
					if((((StructuredSelection)selection).getFirstElement() instanceof Integer) && 
							((Integer)((StructuredSelection)selection).getFirstElement() == ExploreEnvironment.STORE)){	
						
						Iterator iter = ((StructuredSelection)selection).iterator();
						//first element of selection event is about the type of the selection
						iter.next();
						//get second element becuase query is in the second element
						IQuery query = (IQuery)iter.next();
						//get third element: selection time
						long selectionTime = (Long)iter.next();
						
						if ((query != null)){
							
							if(sourcepart.getSite().getId().equals(DefinitionView.ID)){								
								if(!alreadyAdded_DefintionView(selectionTime)){
									add(query);
								}
							}
							else if(sourcepart.getSite().getId().equals(SparqlQueryView.ID)){
								if(!alreadyAdded_SparqlQueryView(selectionTime)){
									add(query);
								}
							}
						}							
					}
				}
			}
		}
	};
	
	private StoredQueryViewSelectionProvider m_selectionProvider;
	private Composite m_composite;
	
	@Override
	public void createPartControl(Composite parent) {
		
		m_composite = parent;
		
		Composite container = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		container.setLayout(layout);
		getSite().getWorkbenchWindow().getSelectionService().addSelectionListener(m_sparqlQueryViewlistener);
		
		m_QueryListViewer = new TreeViewer(container, SWT.BORDER);
		m_QueryListViewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
		m_contentProvider = new StoredQueryViewContentProvider(this);

		m_QueryListViewer.setContentProvider(m_contentProvider);
		m_labelProvider = new StoredQueryViewLabelProvider();
		m_QueryListViewer.setLabelProvider(m_labelProvider);
		m_QueryListViewer.setInput(m_queryListManager.getQueryList());
		m_QueryListViewer.addDoubleClickListener(new IDoubleClickListener(){
			public void doubleClick(DoubleClickEvent event){
				IStructuredSelection selection = (IStructuredSelection)event.getSelection();
				String editorId = "org.aifb.xxplore.storedqueryeditor";
				IWorkbenchPage workbenchPage = getViewSite().getPage();
				if (selection.getFirstElement() instanceof IStoredQueryListElement){
					IStoredQueryListElement stroredQueryListElement = (IStoredQueryListElement)selection.getFirstElement();
					try {
						workbenchPage.openEditor(new StoredQueryEditorInput(stroredQueryListElement,m_contentProvider, false), editorId);
					} catch (PartInitException e) {
						e.printStackTrace();
					}
				}
			}
		});
		fillViewAction();
		fillContextMenu();
		
		Button fsearch = new Button(container, SWT.PUSH | SWT.CENTER);
		fsearch.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		fsearch.setText("f-search");
		fsearch.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				long selectiontime = System.currentTimeMillis();
				IStructuredSelection sel = (IStructuredSelection)m_QueryListViewer.getSelection();
				if (sel != null) {
					IStoredQueryListElement storedQueryListElement = (IStoredQueryListElement)sel.getFirstElement();
					
					Object[] selection = {ExploreEnvironment.F_SEARCH, storedQueryListElement.getQuery().toSPARQL(), selectiontime, null};
					m_selectionProvider.setSelection(new StructuredSelection(selection));
				}
			}
		});
		
		Button msearch = new Button(container, SWT.PUSH | SWT.CENTER);
		msearch.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		msearch.setText("m-search");
		msearch.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				long selectiontime = System.currentTimeMillis();
				IStructuredSelection sel = (IStructuredSelection)m_QueryListViewer.getSelection();
				if (sel != null) {
					IStoredQueryListElement storedQueryListElement = (IStoredQueryListElement)sel.getFirstElement();
					QueryWrapper query = new QueryWrapper(storedQueryListElement.getQuery().toSPARQL(), storedQueryListElement.getQuery().getSelectedVariables().toArray(new String[]{}));
					Object[] selection = {ExploreEnvironment.META_SEARCH, query, selectiontime, storedQueryListElement.getQuery().getMetaFilter()};
					m_selectionProvider.setSelection(new StructuredSelection(selection));
				}
			}
		});

		m_saveButton = new Button(container,SWT.PUSH | SWT.CENTER);
		m_saveButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		m_saveButton.setText("Save");
		m_saveButton.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{	
				m_queryListManager.saveQueryList();
				giveFeedback();
			}

		});
		
		m_selectionProvider = new StoredQueryViewSelectionProvider();
		getSite().setSelectionProvider(m_selectionProvider);	
	}

	private void add(IQuery query){
		
		IStoredQueryListElement stroredQueryListElement = new StoredQueryListElement(query, m_queryListManager.genUniqueTaskId());
		m_queryListManager.getQueryList().addQuery(stroredQueryListElement);
		String editorId = "org.aifb.xxplore.storedqueryeditor";
		IWorkbenchPage workbenchPage = getViewSite().getPage();
		try {
			workbenchPage.openEditor(new StoredQueryEditorInput(stroredQueryListElement,m_contentProvider, false), editorId);
		} catch (PartInitException e) {
			e.printStackTrace();
		}
		getViewer().refresh();
	}
	
	@Override
	public void setFocus() {
		
	}
	
	private void giveFeedback(){
		MessageBox mb = new MessageBox(m_composite.getShell(), SWT.ICON_WORKING | SWT.OK);
		mb.setText("StoredQueryView");
		mb.setMessage("  Query saved!    ");
		mb.open();
	}
	
	private boolean alreadyAdded_DefintionView(long selectionTime){		
		
		if(m_lastSelectionTime_DefintionView == selectionTime){
			return true;
		}
		else{
			m_lastSelectionTime_DefintionView = selectionTime;
			return false;
		}
	}
	
	private boolean alreadyAdded_SparqlQueryView(long selectionTime){
		
		if(m_lastSelectionTime_SparqlQueryView == selectionTime){
			return true;
		}
		else{
			m_lastSelectionTime_SparqlQueryView = selectionTime;
			return false;
		}
	}
	
	public void fillViewAction(){
		m_createQueryAction = new CreateQueryAction(this);
		m_createQueryAction.setText("Add Query");
		m_createQueryAction.setToolTipText("Add a new Query");
		IActionBars bars = getViewSite().getActionBars();
		IToolBarManager toolBar = bars.getToolBarManager();
		toolBar.add(m_createQueryAction);
	}
	
	public void fillContextMenu(){
		MenuManager menuManager = new MenuManager();
		menuManager.add(new DeleteQueryAction(this));
		Tree tree = m_QueryListViewer.getTree();
		Menu menu = menuManager.createContextMenu(tree);
		tree.setMenu(menu);
	}
	
	public StoredQueryListManager getQueryListManager(){
		return m_queryListManager;
	}
	
	public StoredQueryList getQueryList(){
		return m_queryListManager.getQueryList();
	}
	
	public TreeViewer getViewer(){
		return m_QueryListViewer;
	}
	
	public StoredQueryViewContentProvider getContentProvider(){
		return m_contentProvider;
	}
	
	private class StoredQueryViewSelectionProvider implements ISelectionProvider{

		private StructuredSelection m_selection;
		private Collection<ISelectionChangedListener> m_selectionListeners;	


		private StoredQueryViewSelectionProvider() {
			super();
		}


		/**
		 * 
		 * @return a Collection which contains all added selectionChangedListeners
		 */
		private Collection<ISelectionChangedListener> getSelectionListeners(){

			if (m_selectionListeners == null) {
				m_selectionListeners = new ArrayList<ISelectionChangedListener>();
			}

			return m_selectionListeners;
		}

		/**
		 * fires a SelectionChangedEvent to all added SelectionChangedListeners
		 *
		 */
		private void fireSelectionChanged(){

			Iterator iter = getSelectionListeners().iterator();

			while(iter.hasNext()) 
			{
				ISelectionChangedListener listener = (ISelectionChangedListener) iter.next();
				listener.selectionChanged(new SelectionChangedEvent(this, getSelection()));
			}
		}

		/**
		 * 
		 * @see org.eclipse.jface.viewers.ISelectionProvider#addSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
		 */
		public void addSelectionChangedListener(ISelectionChangedListener listener) {
			getSelectionListeners().add(listener);
		}

		/**
		 * 
		 * @see org.eclipse.jface.viewers.ISelectionProvider#getSelection()
		 */
		public ISelection getSelection() {
			return m_selection;
		}

		/**
		 * 
		 * @see org.eclipse.jface.viewers.ISelectionProvider#removeSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
		 */
		public void removeSelectionChangedListener(ISelectionChangedListener listener){
			getSelectionListeners().remove(listener);
		}

		/**
		 * only accepts StructuredSelections
		 * @see org.eclipse.jface.viewers.ISelectionProvider#setSelection(org.eclipse.jface.viewers.ISelection)
		 */
		public void setSelection(ISelection selection){

			m_selection = (StructuredSelection) selection;
			fireSelectionChanged();			

		}
	}
}
