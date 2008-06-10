package org.aifb.xxplore.misc;

import java.util.Observable;
import org.aifb.xxplore.DefinitionView;
import org.aifb.xxplore.core.ExploreEnvironment;
import org.aifb.xxplore.core.model.definition.IModelDefinition;
import org.aifb.xxplore.core.model.definition.ModelDefinition;
import org.aifb.xxplore.core.service.query.NextLuceneQueryService;
import org.apache.log4j.Logger;
import org.apache.lucene.search.Hits;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.xmedia.oms.persistence.PersistenceUtil;
import org.xmedia.oms.persistence.dao.IDaoManager;
import org.xmedia.oms.query.IQueryEvaluator;
import org.xmedia.oms.query.IQueryResult;
import org.xmedia.oms.query.QueryWrapper;

public class CommonSearchResultObservable extends Observable {
	private static Logger s_log = Logger.getLogger(CommonSearchResultObservable.class);

	private ISelectionListener m_defViewlistener = new ISelectionListener() {
		public void selectionChanged(IWorkbenchPart sourcepart, ISelection selection) {
			// listening only to selection changes in the DefinitionView
			if (sourcepart.getSite().getId().equals(DefinitionView.ID)) {
				
				if(selection instanceof IStructuredSelection && !selection.isEmpty()) {
					int event = (Integer)((StructuredSelection)selection).getFirstElement();
					if (ExploreEnvironment.D_SEARCH == event ||
							ExploreEnvironment.F_SEARCH == event) {
						if(instance != null && instance.countObservers()>0) {
							instance.updateDocumentResult();
							instance.updateFactResult();
							instance.setChanged();
							instance.notifyObservers(event);
						}
					} else if (ExploreEnvironment.CLEAR == event) {
						instance.documentResult = null;
						instance.factResult = null;
						instance.setChanged();
						instance.notifyObservers(event);						
					}
				}
			}
		}
	};
	private NextLuceneQueryService m_search_service = NextLuceneQueryService.getInstance();

	private ModelDefinition m_def;

	private Hits documentResult;
	private IQueryResult factResult;
		
	/* make it a singleton (search only once, however the number of views using this object) */
	private static CommonSearchResultObservable instance; 
	private CommonSearchResultObservable() {}
	
	/**
	 * @return Singlton-instance of CommonSearchResultObservable
	 */
	public static CommonSearchResultObservable getInstance() {
		if (CommonSearchResultObservable.instance == null) {
			instance = new CommonSearchResultObservable();
		}	
		return instance;
	}
	
	/**
	 * this Method is needed to get the listener for registering 
	 */
	public ISelectionListener getDefinitionViewListener() {
		return m_defViewlistener;
	}
	
	/**
	 * the ModelDefinition has to be set in order to get the queries for the Searchrequests
	 */
	public void setModelDefinition(IModelDefinition modDef) {
		m_def = (ModelDefinition) modDef;
	}
	
	/**
	 * this is where you can get the new result of the Document search,
	 * after a notification of the observer
	 * @return Hits hits (Set with ranking)
	 */
	public Hits getDocumentResult(){ /* called by viewer.refresh */
		return documentResult;
	}
	
	/**
	 * this is where you can get the new result of the Fact search,
	 * after a notification of the observer
	 * @return IQueryResult
	 */
	public IQueryResult getFactResult(){ /* called by viewer.refresh */
		return factResult;
	}
	
	private void updateFactResult() {
		Assert.isNotNull(m_def);
		QueryWrapper queryWrapper = m_def.getDLQuery();
		if (queryWrapper != null) {
			IQueryEvaluator eval;
			try {
				eval = PersistenceUtil.getDaoManager().getAvailableEvaluator(IDaoManager.SPARQL_QUERYTYPE);
				factResult = eval.evaluate(queryWrapper);
			} catch (Throwable e) { /*TODO ClassNotFoundException hier muss irngedwas passieren*/
				factResult = null;
				//e.printStackTrace();
			}
		} else {
			factResult = null;
		}
	}
	
	private void updateDocumentResult() {
		Assert.isNotNull(m_def);
		Hits result = null;
		String query = m_def.getQuery();
		if (query != null){
			result = m_search_service.searchDocuments(query);
			
			if (s_log.isDebugEnabled()) s_log.debug("Search ends with following results: " + result);
		}
		documentResult = result;
	}
}