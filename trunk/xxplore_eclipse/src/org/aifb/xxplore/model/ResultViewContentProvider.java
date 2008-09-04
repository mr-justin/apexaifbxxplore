package org.aifb.xxplore.model;

import java.io.File;

import org.aifb.xxplore.views.DocumentResultsViewer;
import org.apache.lucene.search.Hits;
import org.ateam.xxplore.core.ExploreEnvironment;
import org.ateam.xxplore.core.model.definition.IModelDefinition;
import org.ateam.xxplore.core.model.definition.ModelDefinition;
import org.ateam.xxplore.core.service.search.DocumentIndexService;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Display;

public class ResultViewContentProvider implements IContentProvider {

	private String m_docResultViewQuery;
	private DocumentIndexService m_search_service;
	private ModelDefinition m_def; 
	private DocumentResultsViewer m_viewer;
	private long m_lastSelectionTime;
	private int m_NoDocumentResults;
	
//	private static Logger s_log = Logger.getLogger(ResultViewContentProvider.class);

	
	public ResultViewContentProvider(DocumentIndexService service){
		m_search_service = service;		
		makeFileIndex(true);
		m_lastSelectionTime = -1;
		m_NoDocumentResults = 0;
	}
	
	public boolean alreadyAdded(long selectionTime){
		
		if(m_lastSelectionTime == selectionTime){
			return true;
		}
		else{
			m_lastSelectionTime = selectionTime;
			return false;
		}
	}
	
	public void resetSelectionTime(){
		m_lastSelectionTime = -1;
	}

	public void dispose() {
		m_search_service.disposeService();
		m_def = null;
		m_viewer = null;
	}

	public Hits getQueryResults(){	

		Hits result = null;
		String query = getDocumentResultViewQuery();
		
		if (query !=null){
			
			result = m_search_service.searchDocuments(query);
			m_NoDocumentResults = result.length();

		}

		return result;
	}
	
	public int getNoOfDocSearchResults(){
		return m_NoDocumentResults;
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if(!(viewer instanceof DocumentResultsViewer)) return; 

		m_viewer = (DocumentResultsViewer)viewer;

		//new input is null 
		if (newInput == null){
			if(oldInput != null && oldInput instanceof IModelDefinition){
				//old graph model no longer valid --> need not to be observed anymore
				//((IModelDefinition) oldInput).removeModelChangeListener(this);
			}
			//update the model and the viewer 
			m_def = null;
		}
		//if not the same input
		if(oldInput != newInput){
			if(oldInput != null){
				//old graph model no longer valid --> need not to be observed anymore
				//((IModelDefinition) oldInput).removeModelChangeListener(this);
			}

			if(newInput != null && newInput != m_def){
				//input must be a model defintion
				Assert.isTrue(newInput instanceof IModelDefinition);

				//update input and model
				m_def = (ModelDefinition)newInput;

				//listen to changes of new imput
				//((IModelDefinition) newInput).addModelChangeListener(this);
			}
		}
		m_viewer.refresh();
	}

	public void setDocumentResultViewQuery(String query){
		m_docResultViewQuery = query;
	}
	
	public String getDocumentResultViewQuery(){
		return m_docResultViewQuery;
	}

	public String getQuery(){
		return m_def.getQuery();
	}
	

	///public void modelChanged(ModelChangeEvent event) 
	//{
		// do only something if the changes concerns the query
		//if (event.getSource() == m_def && event.getType() == ModelChangeEvent.QUERY_CHANGE){
		//	m_viewer.refresh();
	//	}

	//}
	
	private void makeFileIndex(final boolean creatNew){
		Display.getCurrent().asyncExec(new Runnable(){
			public void run(){
				m_search_service.indexFiles(new File(ExploreEnvironment.DOC_DIR),creatNew,ExploreEnvironment.DOC_INDEX_DIR);
			}
		});
	}
}
