package org.aifb.xxplore;

import java.util.Iterator;

import org.aifb.xxplore.model.ResultViewContentProvider;
import org.aifb.xxplore.views.DocumentResultsViewer;
import org.apache.log4j.Logger;
import org.ateam.xxplore.core.ExploreEnvironment;
import org.ateam.xxplore.core.service.search.DocumentIndexService;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;


public class DocumentResultView extends ViewPart{
	
	
	
	private ISelectionListener m_defViewlistener = new ISelectionListener() {
		
		@SuppressWarnings("unchecked")
		public void selectionChanged(IWorkbenchPart sourcepart, ISelection selection) {
			
			// listening only to selection changes in the DefinitionView
			if (sourcepart.getSite().getId().equals(DefinitionView.ID)||
					sourcepart.getSite().getId().equals(SparqlQueryView.ID)) 
			{			   
				if((selection instanceof IStructuredSelection) && !selection.isEmpty())
				{										
					if((((StructuredSelection)selection).getFirstElement() instanceof Integer) &&
							((Integer)((StructuredSelection)selection).getFirstElement() == ExploreEnvironment.D_SEARCH))
					{
						
						Iterator<Object> iter = ((StructuredSelection)selection).iterator();
						//first element of selection event is about the type of the selection
						iter.next();
						
						//get second element is QueryWrapper > not needed here ...
						iter.next();
						
						//get third element: selection time
						long selectionTime = (Long)iter.next();
						
//						fourth element: query
						String query = (String)iter.next();
							
						if(!m_provider.alreadyAdded(selectionTime))
						{
							m_provider.setDocumentResultViewQuery(query);
							m_viewer.refresh();
							
							giveFeedback();
						}
					}
					if((((StructuredSelection)selection).getFirstElement() instanceof Integer) &&
							((Integer)((StructuredSelection)selection).getFirstElement() == ExploreEnvironment.CLEAR))
					{
						m_viewer.clear();
						m_provider.resetSelectionTime();
						m_viewer.refresh();
					}	
				}
			}
		}
	};

	private Composite m_composite;
	private DocumentResultsViewer m_viewer;
	private ResultViewContentProvider m_provider; 
	private static Logger s_log = Logger.getLogger(DocumentResultView.class);
	
	public static final String ID = "org.aifb.xxplore.documentresultview";	

	
	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	@Override
	public void createPartControl(Composite parent) 
	{
		m_composite = parent;
		
		m_viewer = new DocumentResultsViewer(parent);
		m_provider = new ResultViewContentProvider(DocumentIndexService.getInstance());
		
		m_viewer.setContentProvider(m_provider);
		getSite().getWorkbenchWindow().getSelectionService().addSelectionListener(m_defViewlistener);
		
		s_log.debug("DocumentResultView created.");
	}
	
	/**
	 * Passing the focus request to the form.
	 */
	@Override
	public void setFocus() {
		m_viewer.getControl().setFocus();
	}

	public Viewer getViewer(){
		return m_viewer;
	}

	private void giveFeedback(){

		String title = "DefinitionView";
		String message = null;

		int NoResults = m_provider.getNoOfDocSearchResults();

		switch(NoResults){
		case 0 : message = "No results found. Please rephrase query."; break;
		default : message = "Found "+NoResults+" results using document-search. Displaying results in document view.";
		}

		MessageBox mb = new MessageBox(m_composite.getShell(), SWT.ICON_WORKING | SWT.OK);
		mb.setText(title);
		mb.setMessage(message);
		mb.open();

	}
}