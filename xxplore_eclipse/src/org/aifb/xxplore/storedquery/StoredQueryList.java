package org.aifb.xxplore.storedquery;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class StoredQueryList implements Serializable {

    private static final long serialVersionUID = 3618984485791021105L;
    
    private List<IStoredQueryListElement> m_queries = new ArrayList<IStoredQueryListElement>();
    
    public void addQuery(IStoredQueryListElement query) {
    	m_queries.add(query);
    }
    
    public boolean deleteQuery(IStoredQueryListElement query) {
    	return deleteQueryHelper(m_queries, query);
	}
    
    private boolean deleteQueryHelper(List<IStoredQueryListElement> queries, IStoredQueryListElement t) {
    	for (IStoredQueryListElement query : queries) {
    		if (query.getHandle().equals(t.getHandle())) {
    			queries.remove(query);
    			return true;
    		}	
		}
    	return false;
	}
    
    public IStoredQueryListElement getTaskForHandle(String handle) {
        return findTaskHelper(m_queries, handle);
    } 
    
    private IStoredQueryListElement findTaskHelper(List<? extends IStoredQueryListElement> elements, String handle) {
        for (IStoredQueryListElement element : elements) {
        	if(element instanceof IStoredQueryListElement){
        		if(element.getHandle().compareTo(handle) == 0)
        			return (IStoredQueryListElement)element;
        	}	
        }
        return null;
    }
    
    public List<IStoredQueryListElement> getQueryList() {
        return m_queries;
    }

    public int findLargestQueryHandle() {
    	int max = 0;
    	max = Math.max(largestQueryHandleHelper(m_queries), max);
    	return max;
    }
    
    private int largestQueryHandleHelper(List<IStoredQueryListElement> queries) {
    	int ihandle = 0;
    	int max = 0;
    	for (IStoredQueryListElement q : queries) {
    		String string = q.getHandle().substring(q.getHandle().indexOf('-')+1, q.getHandle().length());
    		if (string != "") {
    			ihandle = Integer.parseInt(string);
    		}
    		max = Math.max(ihandle, max);
    	}
    	return max;
    }
    
	public void clear() {
		m_queries.clear();
	}	
}
