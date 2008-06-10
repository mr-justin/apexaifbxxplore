package org.aifb.xxplore.model;


import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


import org.aifb.xxplore.DefinitionView;
import org.aifb.xxplore.SparqlQueryView;
import org.aifb.xxplore.shared.util.URIHelper;
import org.aifb.xxplore.storedquery.IQueryMetaFilter;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.TableColumn;
import org.xmedia.oms.metaknow.ComplexProvenance;
import org.xmedia.oms.model.api.IEntity;
import org.xmedia.oms.model.api.INamedIndividual;
import org.xmedia.oms.model.api.IResource;
import org.xmedia.oms.persistence.PersistenceUtil;
import org.xmedia.oms.persistence.dao.IDaoManager;
import org.xmedia.oms.query.IQueryEvaluator;
import org.xmedia.oms.query.IQueryResult;
import org.xmedia.oms.query.ITuple;
import org.xmedia.oms.query.QueryResult;
import org.xmedia.oms.query.QueryWrapper;import org.xmedia.oms.query.ResourceTuple;


public class FactResultsContentProvider implements IStructuredContentProvider {

	
	private IQueryResult m_queryResult;
	private TableViewer m_viewer;
	
	private long m_lastSelectionTime_Search_DefinitionView = -1;;
	private long m_lastSelectionTime_Search_SparqlQueryView = -1;;
	private long m_lastSelectionTime_Search_StoredQueryView = -1;;
	
	private long m_lastSelectionTime_Clear;	
	
	public FactResultsContentProvider(TableViewer viewer) {		
		m_viewer = viewer;		
	}
	
	public Object[] getElements(Object inputElement) {
		if (!isEmpty(m_queryResult)) {	
			return m_queryResult.getResult().toArray();			
		} else {
			return new Object [0];
		}
	}

	public void dispose() {}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput){}
	
	public void search(QueryWrapper queryWrapper){
		
		if (queryWrapper != null) 
		{
			IQueryEvaluator eval;
			try 
			{
				eval = PersistenceUtil.getDaoManager().getAvailableEvaluator(IDaoManager.SPARQL_QUERYTYPE);
				m_queryResult = eval.evaluate(queryWrapper);	
			} 
			catch (Exception e) 
			{
				e.printStackTrace();
			}

			if(!isEmpty(m_queryResult)) {
				update_tableColumns(false);
			}
		}
		else
		{
			m_queryResult = null;
		}

	}
		
	public void meta_search(QueryWrapper queryWrapper, IQueryMetaFilter filter){
		
		if (queryWrapper != null){

			IQueryEvaluator eval;
			try 
			{
				System.out.println("meta_search: " + queryWrapper.getQuery());
				eval = PersistenceUtil.getDaoManager().getAvailableEvaluator(IDaoManager.SPARQL_QUERYTYPE);
				if (filter == null || (!filter.getRequireProvenances() && filter.getAgents().size() == 0 && filter.getSources().size() == 0 && filter.getConfidenceDegree() == null && filter.getDate() == null))
					m_queryResult = eval.evaluate(queryWrapper);
				else {
					m_queryResult = eval.evaluateWithProvenance(queryWrapper);
					int cpIdx = m_queryResult.getColumn("cb");
	
					Set<ITuple> iTuples = new HashSet<ITuple>();
					String[] colNames = new String [queryWrapper.getSelectVariables().length];
					for (int i = 0; i < queryWrapper.getSelectVariables().length; i++) {
						colNames[i] = queryWrapper.getSelectVariables()[i].replaceFirst("\\?", "");
					}
	
					for (ITuple t : m_queryResult.getResult()) {
						ComplexProvenance cp = (ComplexProvenance)t.getElementAt(cpIdx);
	
						if ((filter == null) || passedFilter(filter, cp)) {
							System.out.println("passed: " + t);
							Object[] oTuple = new Object[queryWrapper.getSelectVariables().length];
							for (int i = 0; i < queryWrapper.getSelectVariables().length; i++) {
								oTuple[i] = t.getElementAt(m_queryResult.getColumn(colNames[i]));
							}
	
							iTuples.add(new ResourceTuple(colNames.length, oTuple, colNames, cp));
						} else {
							System.out.println("filtered: " + t);
						}
					}
	
					m_queryResult = new QueryResult(iTuples, colNames);
				}
			} 
			catch (Exception e) 
			{
//				e.printStackTrace();
				System.out.println("no results");
				m_queryResult = new QueryResult(new HashSet<ITuple>(), new String[0]);
			}

			if(!isEmpty(m_queryResult)) {
				update_tableColumns(true);
			}
		}
		else
		{
			m_queryResult = null;
		}
	}
	
	private boolean passedFilter(IQueryMetaFilter filter, ComplexProvenance cp) {
		if (filter.getConfidenceDegree() != null) {
			if (cp.getComplexConfidenceDegree().size() > 0) {
				double avgConfidence = 0;
				for (Double confidence : cp.getComplexConfidenceDegree()) {
					avgConfidence += confidence;
				}
				avgConfidence /= cp.getComplexConfidenceDegree().size();
			
				if (avgConfidence < filter.getConfidenceDegree()) {
					return false;
				}
			} else {
				return false;
			}
		}
		
		if (filter.getDate() != null) {
			if (cp.getComplexCreationDate().size() > 0) {
				Date maxDate = new Date(0);
				for (Date date : cp.getComplexCreationDate()) {
					if (date.after(maxDate)) {
						maxDate = date;
					}
				}
				
				if (maxDate.before(filter.getDate())) {
					return false;
				}
			} else {
				return false;
			}
		}
		
		if (filter.getAgents().size() > 0) {
			if (cp.getComplexAgent().size() > 0) {
				// TODO 
				Set<String> filterAgentUris = new HashSet<String>();
				for (INamedIndividual e : filter.getAgents()) {
					filterAgentUris.add(e.getUri().substring(e.getUri().indexOf("#") + 1));
				}
				
				for (INamedIndividual e : cp.getComplexAgent()) {
					if (!filterAgentUris.contains(e.getUri())) {
						return false;
					}
				}
			} else {
				return false;
			}
		}
		
		if (filter.getSources().size() > 0) {
			if (cp.getComplexSource().size() > 0) {
				// TODO 
				Set<String> filterSourceUris = new HashSet<String>();
				for (IEntity e : filter.getSources()) {
					filterSourceUris.add(e.getUri().substring(e.getUri().indexOf("#") + 1));
				}
				
				for (IEntity e : cp.getComplexSource()) {
					if (!filterSourceUris.contains(e.getUri())) {
						return false;
					}
				}
			} else {
				return false;
			}
		}
		
		return true;
	}
	
	private boolean isEmpty(IQueryResult result){
		
		if (result == null) {
			return true;
		} else {
			return (result.getQueryVariables().length != 0) && (result.getResult().size() != 0) ? false : true;
		}
	}
	
	private void update_tableColumns(boolean addMetaColumns){
		
//		TODO display meta-data
		
		disposeOldColumns();

		for (String var : m_queryResult.getQueryVariables()) 
		{
			TableColumn tc = new TableColumn(m_viewer.getTable(), SWT.LEFT);
			tc.setText(var);
		}
		if (addMetaColumns) {
			TableColumn tc1 = new TableColumn(m_viewer.getTable(), SWT.LEFT);
			tc1.setText("confidence");
			TableColumn tc2 = new TableColumn(m_viewer.getTable(), SWT.LEFT);
			tc2.setText("creation date");
			TableColumn tc3 = new TableColumn(m_viewer.getTable(), SWT.LEFT);
			tc3.setText("agents");
			TableColumn tc4 = new TableColumn(m_viewer.getTable(), SWT.LEFT);
			tc4.setText("sources");
		}
					
		m_viewer.refresh();
		
		if(m_viewer.getTable() != null)
		{
			for (TableColumn tc : m_viewer.getTable().getColumns()) 
			{
				tc.pack();
			}
		}
	}
	
	public void clear(){
		
			m_queryResult = null;
			m_viewer.getTable().removeAll();
			disposeOldColumns();
			
			m_viewer.refresh();
	}
	
	private void disposeOldColumns(){
		for (TableColumn tc : m_viewer.getTable().getColumns()){
			tc.dispose();
		}
	}
	
	public int getNumberOfMatches(){
		return m_queryResult.getResult().size();
	}
	
	public boolean alreadyAdded(String id, long selectionTime){
		
		if(id.equals(DefinitionView.ID)){
		
			if(m_lastSelectionTime_Search_DefinitionView == selectionTime){
				return true;
			}
			else{
				m_lastSelectionTime_Search_DefinitionView = selectionTime;
				return false;
			}
		}
		else if(id.equals(SparqlQueryView.ID)){
			
			if(m_lastSelectionTime_Search_SparqlQueryView == selectionTime){
				return true;
			}
			else{
				m_lastSelectionTime_Search_SparqlQueryView = selectionTime;
				return false;
			}
		}
//		StoredQueryView
		else{
			
			if(m_lastSelectionTime_Search_StoredQueryView == selectionTime){
				return true;
			}
			else{
				m_lastSelectionTime_Search_StoredQueryView = selectionTime;
				return false;
			}
		}
	}
	
	public boolean alreadyCleared(long selectionTime){
		return m_lastSelectionTime_Clear == selectionTime ? true : false;
	}
}
