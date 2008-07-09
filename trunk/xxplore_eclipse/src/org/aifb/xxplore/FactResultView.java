package org.aifb.xxplore;

import java.util.Iterator;

import org.aifb.xxplore.SparqlQueryView.SparqlHelper;
import org.aifb.xxplore.core.ExploreEnvironment;
import org.aifb.xxplore.model.FactResultsContentProvider;
import org.aifb.xxplore.model.FactResultsLabelProvider;
import org.aifb.xxplore.storedquery.IQueryMetaFilter;
import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;
import org.xmedia.oms.query.QueryWrapper;

public class FactResultView extends ViewPart {


	public static final String ID = "org.aifb.xxplore.factresultview";

	@SuppressWarnings("unused")
	private static Logger s_log = Logger.getLogger(FactResultView.class);

	
	private Composite m_composite;
	private TableViewer m_resultsViewer;
	private String sparqlQuery = "";
	private FactResultsContentProvider m_resultsContentProvider;
	private FactResultsLabelProvider m_resultsLabelProvider;
	private SashForm m_splitter;


	private ISelectionListener m_defViewlistener = new ISelectionListener() {

		@SuppressWarnings("unchecked")
		public void selectionChanged(IWorkbenchPart sourcepart, ISelection selection) {

			// listening only to selection changes in the DefinitionView/SparqlQueryView
			if (sourcepart.getSite().getId().equals(DefinitionView.ID)
					||sourcepart.getSite().getId().equals(SparqlQueryView.ID)
					|| sourcepart.getSite().getId().equals(StoredQueryView.ID)
					|| sourcepart.getSite().getId().equals(FiatNewsSearchView.ID)) 
			{			   
				if((selection instanceof IStructuredSelection) && !selection.isEmpty())
				{					
					if((((StructuredSelection)selection).getFirstElement() instanceof Integer))
					{
						if(((Integer)((StructuredSelection)selection).getFirstElement() == ExploreEnvironment.F_SEARCH)){
							
							Iterator<Object> iter = ((StructuredSelection)selection).iterator();
							//first element of selection event is about the type of the selection
							iter.next();
							
							//get second element: the query
							Object second_object = iter.next(); 
							QueryWrapper query = null;
							
							//get third element: selection time
							long selectionTime = (Long)iter.next();
							
							if(sourcepart.getSite().getId().equals(StoredQueryView.ID)){
								
								String query_string = SparqlHelper.cleanQuery((String)second_object);			
								String[] vars = SparqlHelper.getVars(query_string);						
								query = new QueryWrapper(query_string,vars);
								
								if(!m_resultsContentProvider.alreadyAdded(StoredQueryView.ID, selectionTime)){
									
									m_resultsContentProvider.clear();
									m_resultsContentProvider.search(query);
									giveFeedBack(ExploreEnvironment.F_SEARCH,StoredQueryView.ID, false);
								}
								
							}
							else if(sourcepart.getSite().getId().equals(SparqlQueryView.ID)){
								
								query = (QueryWrapper)second_object;
								
								if(!m_resultsContentProvider.alreadyAdded(SparqlQueryView.ID, selectionTime)){
									
									m_resultsContentProvider.clear();
									m_resultsContentProvider.search(query);
									giveFeedBack(ExploreEnvironment.F_SEARCH,SparqlQueryView.ID, false);
								}
							}
							else if(sourcepart.getSite().getId().equals(DefinitionView.ID)){
								
								query = (QueryWrapper)second_object;
								
								if(!m_resultsContentProvider.alreadyAdded(DefinitionView.ID, selectionTime)){
									
									m_resultsContentProvider.clear();
									m_resultsContentProvider.search(query);
									giveFeedBack(ExploreEnvironment.F_SEARCH, DefinitionView.ID, false);
								}
							}
							else if(sourcepart.getSite().getId().equals(FiatNewsSearchView.ID)){
								
								query = (QueryWrapper)second_object;
								
								if(!m_resultsContentProvider.alreadyAdded(FiatNewsSearchView.ID, selectionTime)){
									
									m_resultsContentProvider.clear();
									m_resultsContentProvider.search(query);
									giveFeedBack(ExploreEnvironment.F_SEARCH, FiatNewsSearchView.ID, false);
								}
							}
						}
						else if(((Integer)((StructuredSelection)selection).getFirstElement() == ExploreEnvironment.META_SEARCH)){
							
							Iterator<Object> iter = ((StructuredSelection)selection).iterator();
							//first element of selection event is about the type of the selection
							iter.next();
							
							//get second element becuase query is in the second element
							QueryWrapper query = (QueryWrapper)iter.next();
							
							//get third element: selection time
							long selectionTime = (Long)iter.next();
							
							//get last element: filter
							Object filter = iter.next();
							IQueryMetaFilter f = null;
							if (filter instanceof IQueryMetaFilter)
								f = (IQueryMetaFilter)filter;
							if(sourcepart.getSite().getId().equals(DefinitionView.ID)){
								
								if(!m_resultsContentProvider.alreadyAdded(DefinitionView.ID, selectionTime)){
									
									m_resultsContentProvider.clear();
									m_resultsContentProvider.meta_search(query,(IQueryMetaFilter)filter);
									giveFeedBack(ExploreEnvironment.META_SEARCH,sourcepart.getSite().getId(),
											f == null || (!f.getRequireProvenances() && f.getAgents().size() == 0 && f.getSources().size() == 0 && f.getConfidenceDegree() == null && f.getDate() == null));
								}
								
							}
							else if(sourcepart.getSite().getId().equals(SparqlQueryView.ID)){
								
								if(!m_resultsContentProvider.alreadyAdded(SparqlQueryView.ID, selectionTime)){
									
									m_resultsContentProvider.clear();
									m_resultsContentProvider.meta_search(query,(IQueryMetaFilter)filter);
									giveFeedBack(ExploreEnvironment.META_SEARCH,sourcepart.getSite().getId(),
											f == null || (!f.getRequireProvenances() && f.getAgents().size() == 0 && f.getSources().size() == 0 && f.getConfidenceDegree() == null && f.getDate() == null));
								}
							}
							else if(sourcepart.getSite().getId().equals(StoredQueryView.ID)){
								
								if(!m_resultsContentProvider.alreadyAdded(StoredQueryView.ID, selectionTime)){
									
									m_resultsContentProvider.clear();
									m_resultsContentProvider.meta_search(query,(IQueryMetaFilter)filter);
									giveFeedBack(ExploreEnvironment.META_SEARCH,sourcepart.getSite().getId(),
											f == null || (!f.getRequireProvenances() && f.getAgents().size() == 0 && f.getSources().size() == 0 && f.getConfidenceDegree() == null && f.getDate() == null));
								}
							}	
						}						
						else if(((Integer)((StructuredSelection)selection).getFirstElement() == ExploreEnvironment.CLEAR)){
							
							Iterator<Object> iter = ((StructuredSelection)selection).iterator();
							//first element of selection event is about the type of the selection
							iter.next();
							
							//get second element: selection time
							long selectionTime = (Long)iter.next();
							
//							TODO make changes like above ... 
							if(!m_resultsContentProvider.alreadyCleared(selectionTime)){
								m_resultsContentProvider.clear();
							}
						}
					}
				}
			}
		}

	};


	/**
	 * The constructor.
	 */
	public FactResultView() {}

	/**
	 * This is a callback that will allow us to create the viewer and
	 * initialize it.
	 */
	@Override
	public void createPartControl(Composite parent) {
		
		m_composite = parent;
		
		m_splitter = new SashForm(parent, SWT.NONE);
		GridData gridData = new GridData();
		gridData.verticalAlignment = GridData.FILL;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;

		m_resultsViewer = new TableViewer(m_splitter, SWT.FULL_SELECTION | SWT.SINGLE);
		m_resultsViewer.getTable().setHeaderVisible(true);
		m_resultsViewer.getTable().setLayoutData(gridData);

		m_resultsContentProvider = new FactResultsContentProvider(m_resultsViewer);
		m_resultsLabelProvider = new FactResultsLabelProvider();
		m_resultsViewer.setContentProvider(m_resultsContentProvider);
		m_resultsViewer.setLabelProvider(m_resultsLabelProvider);

		getSite().setSelectionProvider(m_resultsViewer);
		getSite().getPage().addPartListener(m_partListener);
		getSite().getPage().addSelectionListener(m_defViewlistener);

	}

	protected IPartListener m_partListener = new IPartListener(){
		public void partOpened(IWorkbenchPart part){
			if (part instanceof SparqlQueryView) {
				((SparqlQueryView)part).setText(sparqlQuery);
			}
		}

		public void partActivated(IWorkbenchPart part) {}

		public void partBroughtToTop(IWorkbenchPart part) {}

		public void partClosed(IWorkbenchPart part) {}

		public void partDeactivated(IWorkbenchPart part) {}
	};

	
	private void giveFeedBack(int search_type, String source_id, boolean metaFilterEmpty){
		
		String title = null;
		String message = null;
		String searchTypeText = null;
		
		int noResults = m_resultsContentProvider.getNumberOfMatches();
		
		switch(search_type){
			case ExploreEnvironment.F_SEARCH : searchTypeText = "fact-search"; break;
			case ExploreEnvironment.META_SEARCH: searchTypeText = "meta-search"; break;
		}
		
		switch(noResults){
			case 0 : message = "No results found. Please rephrase query."; break;
			default : message = (metaFilterEmpty ? "Meta filter was empty. " : "") + "Found "+noResults+" results using "+searchTypeText+" search. Displaying results in factresult view.";
		}
		
		if(source_id.equals(DefinitionView.ID)){
			title = "DefinitionView";
		}
		else if(source_id.equals(SparqlQueryView.ID)){
			title = "SparqlQueryView";
		}
		else if (source_id.equals(StoredQueryView.ID)) {
			title = "StoredQueryView";
		}
		else if (source_id.equals(FiatNewsSearchView.ID)) {
			title = "FiatNewsSearchView";
		}
		
		MessageBox mb = new MessageBox(m_composite.getShell(), SWT.ICON_WORKING | SWT.OK);
		mb.setText(title);
		mb.setMessage(message);
		mb.open();
		
	}
	/**
	 * Passing the focus request to the form.
	 */
	@Override
	public void setFocus() {
//		m_resultsViewer.getControl().setFocus();
	}

	public Viewer getViewer(){
		return m_resultsViewer;
	}

	@Override
	public void setContentDescription(String desc) {
		super.setContentDescription(desc);
	}
}