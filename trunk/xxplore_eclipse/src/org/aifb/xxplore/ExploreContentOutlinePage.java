package org.aifb.xxplore;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.aifb.xxplore.core.ExploreEnvironment;
import org.aifb.xxplore.model.ElementContentProvider;
import org.aifb.xxplore.model.ElementLabelProvider;
import org.aifb.xxplore.views.dnd.ElementTransfer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;
import org.xmedia.oms.model.api.IResource;


public class ExploreContentOutlinePage extends ContentOutlinePage {


	private static final String CONTENT_OUTLINE_PAGE_CONTEXT = "ContentOutlinePageContext";	

	private ElementContentProvider m_elementcontentprovider;
	private ISelection oldselection = null;
	
	private ISelectionListener m_workbenchSelectionListener = new ISelectionListener() {

		public void selectionChanged(IWorkbenchPart sourcepart, ISelection selection) {

			if (sourcepart.getSite().getId().equals(ConceptHierarchyView.ID) || sourcepart.getSite().getId().equals(FactResultView.ID))
			{	
				if((selection instanceof StructuredSelection) && !selection.isEmpty() && !selection.equals(oldselection))
				{
					changeSelection((StructuredSelection)selection);
				}        		
			}
		}

	};
	
	
	public ExploreContentOutlinePage() {
		super();

	}

	@Override
	public void dispose() {
		if (m_elementcontentprovider!=null) {
			m_elementcontentprovider.dispose();
		}
		m_elementcontentprovider=null;
		super.dispose();
	}

	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);

		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(),
				CONTENT_OUTLINE_PAGE_CONTEXT);		
		
		m_elementcontentprovider= new ElementContentProvider();

		getTreeViewer().setContentProvider(m_elementcontentprovider);
		getTreeViewer().setLabelProvider(new ElementLabelProvider());
		
		getSite().getWorkbenchWindow().getSelectionService().addSelectionListener(m_workbenchSelectionListener);
		getSite().setSelectionProvider(getTreeViewer());

		initDrag(getTreeViewer().getControl());
		
	}


	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		// if it has been fired by the ExploreEditor or Viewer
		if (event.getSelectionProvider().getClass() == ExploreEditor.class){
			
			StructuredSelection selection = (StructuredSelection)event.getSelection();
			
			if((selection != null) && (!selection.isEmpty())){
				
				if ((getTreeViewer() != null) && selection.toArray()[1].equals(ExploreEnvironment.ITEM_ACTIVATED_PERFORMED)){					
					changeSelection(selection);				    
				}
			}
		}
	}
	
	private void changeSelection(StructuredSelection selection){
		
		oldselection = selection;
		getTreeViewer().setInput(selection);
		getTreeViewer().expandAll();
		Tree tree = getTreeViewer().getTree();
		tree.setTopItem(tree.getItem(0));
		
	}

	private void initDrag(Control control){
		int operations = DND.DROP_COPY |DND.DROP_DEFAULT; 
		final DragSource source = new DragSource(control, operations);

		Transfer[] types = new Transfer[] {ElementTransfer.getInstance(), TextTransfer.getInstance()};
		source.setTransfer(types);

		control.addDisposeListener(new DisposeListener(){
			public void widgetDisposed(DisposeEvent e) {			
				source.dispose();
			}
		});

		source.addDragListener(new DragSourceAdapter(){

			@Override
			public void dragStart(DragSourceEvent event) {
				
				if (getTreeViewer().getSelection().isEmpty()){
					event.doit = false;
				}
			}


			@Override
			public void dragSetData(DragSourceEvent event) {
				ISelection selection = getSelection();
				if(selection instanceof TreeSelection){
					Iterator elements = ((TreeSelection)selection).iterator();
					List<IResource> ress = new LinkedList<IResource>();
					while (elements.hasNext()){
						Object element = elements.next();
						IResource resource = null;
						if (element instanceof ElementContentProvider.OutlineTreeNode){
							if (((ElementContentProvider.OutlineTreeNode)element).getValue() instanceof IResource){
								resource = (IResource) ((ElementContentProvider.OutlineTreeNode)element).getValue(); 
							}
						}
						if (resource != null) {
							ress.add(resource);
						} 
					}
					if (ress.size() > 0) {
						event.data = ress;
					} else {
						event.doit = false;
					}

				}


			}


			@Override
			public void dragFinished(DragSourceEvent event) {

			}	
		});			 	   


	}

}
