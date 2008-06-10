/**
 * 
 */
package org.aifb.xxplore;



import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;


import org.aifb.xxplore.core.ExploreEnvironment;
import org.aifb.xxplore.model.ConceptHierachyLabelProvider;

import org.xmedia.oms.model.api.IResource;
import org.xmedia.oms.model.impl.NamedConcept;



public class ConceptHierarchyView extends ViewPart implements ISelectionChangedListener{
	
		
	private TreeViewer m_treeViewer;
	private Display m_disply;	
	private List<TreeItem> m_visitedItems;
	private Logger s_log = Logger.getLogger(ConceptHierarchyView.class);
	
	private ISelectionListener m_listener = new ISelectionListener() {
		
		public void selectionChanged(IWorkbenchPart sourcepart, ISelection selection) {
			
			if (sourcepart.getSite().getId().equals(ConceptHierarchyView.ID)) 
			{
				
				if((selection instanceof IStructuredSelection) && !selection.isEmpty())
				{
					
					NamedConcept concept = (NamedConcept)((StructuredSelection)selection).getFirstElement();  
					setFocus(concept);
					
				}			    
			}
		}
	};
	
	
	public static final String ID = "org.aifb.xxplore.concepthierarchyview";

	
	public void selectionChanged(SelectionChangedEvent event) {
		
		if(event.getSelection() instanceof StructuredSelection)
		{
			
			StructuredSelection selection = (StructuredSelection)event.getSelection();
					
			if(selection.toArray()[1].equals(ExploreEnvironment.CONCEPTUAL_ZOOMING_PERFORMED))
			{
				IResource res = (IResource) selection.toArray()[0];			
				m_treeViewer.reveal(res);								
				setFocus(res);
				
			}
		}		
	}
	
	
	
	@Override
	public void createPartControl(Composite parent) {
		
		m_disply = parent.getDisplay();
		m_visitedItems = new ArrayList<TreeItem>();
		
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		parent.setLayout(layout);
				
		m_treeViewer = new TreeViewer(parent);
		m_treeViewer.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));
		m_treeViewer.setLabelProvider(new ConceptHierachyLabelProvider());
		m_treeViewer.expandAll();
		
		getSite().setSelectionProvider(m_treeViewer);
		getSite().getWorkbenchWindow().getSelectionService().addSelectionListener(m_listener);
		
	}
	
	@Override
	public void setFocus() {		
	}
	
	public TreeViewer getViewer(){
		return m_treeViewer;
	}
	
	private void setFocus(IResource res){
		
		TreeItem item = getTreeItem(res.getLabel());
		
		if(item == null)
		{
			s_log.debug(res.getLabel()+" NOT FOUND IN TREEITEMS");
			return;
		}
		
		if(m_visitedItems.size()!= 0)
		{
			m_visitedItems.get(m_visitedItems.size()-1).setForeground(new Color(m_disply,0,0,200));
						
			if(m_visitedItems.size() >= ExploreEnvironment.HISTORY_LENGTH)
			{
				m_visitedItems.get(0).setForeground(new Color(m_disply,0,0,0));
				m_visitedItems.remove(0);
			}
		}
					
		m_treeViewer.getTree().setTopItem(item);
		m_treeViewer.getTree().setSelection(item);
		
		m_visitedItems.add(item);		
	}
	
	private TreeItem getTreeItem(String label){
		
		List<TreeItem> items = new ArrayList<TreeItem>();
		items.addAll(Arrays.asList(m_treeViewer.getTree().getItems()));
		
		while(!items.isEmpty())
		{
			TreeItem item = items.get(0);	
			
			if(item.getText().equals(label)) {
				return item;
			} else {
				items.addAll(Arrays.asList(item.getItems()));
			}
			
			items.remove(0);
		}
		
		return null;
	}
}
