package org.aifb.xxplore;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.aifb.xxplore.model.ModelDefinitionContentProvider;
import org.aifb.xxplore.model.ModelDefinitionLabelProvider;
import org.aifb.xxplore.views.definitionviewer.DefinitionViewer;
import org.aifb.xxplore.views.definitionviewer.ITreeNode;
import org.ateam.xxplore.core.ExploreEnvironment;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.part.ViewPart;


public class DefinitionView extends ViewPart{

	public static final String ID = "org.aifb.xxplore.definitionview";

	private ILabelProvider m_labelprovider;
	private ModelDefinitionContentProvider m_provider;
	private DefinitionViewer m_defviewer;
	private Text m_searchtext;
	private Composite m_composite;
	
	
	private Button m_search_d_button,
				m_xxploreButton,
				m_search_f_button,
				m_metasearch_button,
				m_save_button,
				m_addbutton,
				m_clearbutton;	

	private DefinitionViewSelectionProvider m_selectionProvider;


	private void createContextMenu(final TreeViewer treeViewer) {
		treeViewer.getTree().addListener(SWT.MouseDown, new Listener() {
			public void handleEvent(Event event) {
				Point point = new Point(event.x, event.y);
				if (treeViewer.getTree().getMenu() != null) {
					treeViewer.getTree().getMenu().dispose();
				}
				TreeItem item = treeViewer.getTree().getItem(point);
				if (event.button == 3) {
					addContextMenu(item);
				}			
			}
		});
	}

	private void addContextMenu(TreeItem item) {
		
		MenuManager menu = new MenuManager();
		ITreeNode node = (ITreeNode)item.getData();

		for (IAction action : node.getContextMenuActions())
		{
			menu.add(action);
		}

		Menu contextMenu = menu.createContextMenu(m_defviewer.getTree());
		contextMenu.setVisible(true);	
	}

	@Override
	public void createPartControl(Composite parent){
		
		m_composite = parent;

		Composite container = new Composite(parent, SWT.BORDER);
		container.setLayout(new FormLayout());

		//creating adding button
		FormData data5 = new FormData();
		data5.top = new FormAttachment(0,2);
		data5.left = new FormAttachment(80,0);
		data5.right = new FormAttachment(100,-2);		
		m_addbutton = new Button(container,SWT.BUTTON1);
		m_addbutton.setText("Add");
		m_addbutton.setLayoutData(data5);
		
		FormData data1 = new FormData();
		data1.top  = new FormAttachment(0,2);	
		data1.left = new FormAttachment(0,2);
		data1.right = new FormAttachment(80,-2);
		data1.bottom = new FormAttachment(m_addbutton,0,SWT.BOTTOM);
		m_searchtext = new Text(container, SWT.BORDER);
		m_searchtext.setLayoutData(data1);

		m_searchtext.setText("");
		m_searchtext.setToolTipText("Use this Text field to find a concept name more quickly");

		//creating xxplore button
		FormData data3 = new FormData();
		data3.top = new FormAttachment(90,-1);
		data3.left = new FormAttachment(0,2);
		data3.right = new FormAttachment(25,-1);		
		data3.bottom = new FormAttachment(100,-2);
		m_xxploreButton = new Button(container, SWT.BUTTON1);
		m_xxploreButton.setText("x-plore");
		m_xxploreButton.setLayoutData(data3);

		//creating search-d button
		FormData data4 = new FormData();
		data4.top = new FormAttachment(90,-1);
		data4.left = new FormAttachment(25,1);
		data4.right = new FormAttachment(40,-2);		
		data4.bottom = new FormAttachment(100,-2);
		m_search_d_button = new Button(container,SWT.BUTTON1);
		m_search_d_button.setText("d-s");
		m_search_d_button.setLayoutData(data4);
		
		//creating search-f button
		FormData data7 = new FormData();
		data7.top = new FormAttachment(90,-1);
		data7.left = new FormAttachment(40,1);
		data7.right = new FormAttachment(55,-2);		
		data7.bottom = new FormAttachment(100,-2);
		m_search_f_button = new Button(container,SWT.BUTTON1);
		m_search_f_button.setText("f-s");
		m_search_f_button.setLayoutData(data7);
		
		//creating search-m button
		FormData data8 = new FormData();
		data8.top = new FormAttachment(90,-1);
		data8.left = new FormAttachment(55,1);
		data8.right = new FormAttachment(70,-2);		
		data8.bottom = new FormAttachment(100,-2);
		m_metasearch_button = new Button(container,SWT.BUTTON1);
		m_metasearch_button.setText("m-s");
		m_metasearch_button.setLayoutData(data8);
		
		//creating save button
		FormData data9 = new FormData();
		data9.top = new FormAttachment(90,-1);
		data9.left = new FormAttachment(70,1);
		data9.right = new FormAttachment(85,-2);		
		data9.bottom = new FormAttachment(100,-2);
		m_save_button = new Button(container,SWT.BUTTON1);
		m_save_button.setText("save");
		m_save_button.setLayoutData(data9);
		
		//creating clear button
		FormData data6 = new FormData();
		data6.top = new FormAttachment(90,-1);
		data6.left = new FormAttachment(85,1);
		data6.right = new FormAttachment(100,-2);		
		data6.bottom = new FormAttachment(100,-2);
		m_clearbutton = new Button(container,SWT.BUTTON1);
		m_clearbutton.setText("clear");
		m_clearbutton.setLayoutData(data6);
		
		m_defviewer = new DefinitionViewer(container,SWT.BORDER);

		FormData data2 = new FormData();
		data2.top = new FormAttachment(m_addbutton,2);
		data2.left = new FormAttachment(0,2);
		data2.right = new FormAttachment(100,-2);		
		data2.bottom = new FormAttachment(m_xxploreButton,-2);

		m_defviewer.getControl().setLayoutData(data2);		
		
		m_provider = ModelDefinitionContentProvider.ModelDefinitionContentProviderSingleTonHolder.getInstance();
		
		m_defviewer.setContentProvider(m_provider);
		m_labelprovider = new ModelDefinitionLabelProvider();
		m_defviewer.setLabelProvider(m_labelprovider);
		m_defviewer.setAutoExpandLevel(AbstractTreeViewer.ALL_LEVELS);

		m_selectionProvider = new DefinitionViewSelectionProvider();
		getSite().setSelectionProvider(m_selectionProvider);	

		createContextMenu(m_defviewer);
		
		m_addbutton.addSelectionListener(new SelectionAdapter(){

			@Override
			public void widgetSelected(SelectionEvent e) {	
				onAddSelection(e);
				m_provider.updateResult();
				
				if(m_provider.getModelDefinition().getDLQuery() != null){
//					notify SparqlView
					long selectiontime = System.currentTimeMillis();
					Object[] selection = {ExploreEnvironment.ADD, m_provider.getModelDefinition().getDLQuery(), selectiontime};				
					m_selectionProvider.setSelection(new StructuredSelection(selection));
				}
				
				m_defviewer.refresh();
			}
		});
		
		m_xxploreButton.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				//add to model if not been added before 
				onAddSelection(e);
				/*
				 * Please note, time of selection is sent too, since ISelection
				 * mechanism is not only activated when clicking on the button
				 * but also when switching between the views ...
				 */
				long selectiontime = System.currentTimeMillis();
				
				if(m_provider.getModelDefinition() != null){
					
//					notify GraphView
					Object[] selection = {ExploreEnvironment.XXPLORE, m_provider.getModelDefinition(),selectiontime};				
					m_selectionProvider.setSelection(new StructuredSelection(selection));
				}
					
				m_defviewer.refresh();
			}
		});
		
		m_metasearch_button.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				//add to model if not been added before 
				onAddSelection(e);
				m_provider.updateResult();
				
				if(m_provider.getModelDefinition().getDLQuery() != null){
					
					//notify FactResultView
					long selectiontime = System.currentTimeMillis();
					Object[] selection = {ExploreEnvironment.META_SEARCH, m_provider.getModelDefinition().getDLQuery(),selectiontime, null};
					m_selectionProvider.setSelection(new StructuredSelection(selection));
				}
			
				//refresh
				m_defviewer.refresh();				
			}
		});
		
		m_search_f_button.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				//add to model if not been added before 
				onAddSelection(e);
				m_provider.updateResult();
				
				
				if(m_provider.getModelDefinition().getDLQuery() != null){
					
//					notify FactResultView
					long selectiontime = System.currentTimeMillis();
					Object[] selection = {ExploreEnvironment.F_SEARCH, m_provider.getModelDefinition().getDLQuery(),selectiontime};
					m_selectionProvider.setSelection(new StructuredSelection(selection));
				}
				
				//refresh
				m_defviewer.refresh();				
			}
		});
		
		m_search_d_button.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {

				//notify FactResultView
				
				if(m_searchtext.getText() != null){
					
					long selectiontime = System.currentTimeMillis();
					Object[] selection = {ExploreEnvironment.D_SEARCH, m_provider.getModelDefinition().getDLQuery(),selectiontime,m_searchtext.getText()};
					m_selectionProvider.setSelection(new StructuredSelection(selection));
					
					m_searchtext.setText("");
				}
				
				//refresh
				m_defviewer.refresh();				
			}
		});
		
		m_clearbutton.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				long selectiontime = System.currentTimeMillis();
				Object[] selection = {ExploreEnvironment.CLEAR, selectiontime};
				
				m_selectionProvider.setSelection(new StructuredSelection(selection));
				m_provider.getModelDefinition().clear();
				m_provider.clear();
				
				m_provider.inputChanged(m_defviewer, null, m_provider.getModelDefinition());
				m_searchtext.setText("");
				
				m_defviewer.refresh();

			}
		});

		m_save_button.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				//add to model if not been added before 
				onAddSelection(e);
				m_provider.updateResult();
				
				if(m_provider.getQuery() != null){
					
//					notify StoredQueryView
					long selectiontime = System.currentTimeMillis();
					Object[] selection = {ExploreEnvironment.STORE, m_provider.getQuery(), selectiontime};
					m_selectionProvider.setSelection(new StructuredSelection(selection));
					
					giveFeedback();
				}
				
				//refresh
				m_defviewer.refresh();				
			}
		});
	}
	
	private void giveFeedback(){
		MessageBox mb = new MessageBox(m_composite.getShell(), SWT.ICON_WORKING | SWT.OK);
		mb.setText("DefinitionView");
		mb.setMessage("  Query saved! Please see StoredQueryView.   ");
		mb.open();
	}

	private void onAddSelection(SelectionEvent e) {	

		if ((m_searchtext.getText() == null) || m_searchtext.getText().equals("")) {
			return;
		}
		
		if(m_provider.getModelDefinition() != null)
		{
			m_provider.getModelDefinition().clear();
		}
		
		m_provider.updateDefinition(m_searchtext.getText());

		m_provider.updateIntepretation();
		m_searchtext.setText("");
		m_searchtext.setFocus();
	}

	public Viewer getViewer(){
		return m_defviewer;
	}

	@Override
	public void setFocus() {
		m_defviewer.getControl().setFocus();
	}


	private class DefinitionViewSelectionProvider implements ISelectionProvider{

		private StructuredSelection m_selection;
		private Collection<ISelectionChangedListener> m_selectionListeners;	


		private DefinitionViewSelectionProvider() {
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
