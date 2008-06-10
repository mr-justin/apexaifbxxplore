package org.xmedia.oms.query;

import java.util.Set;

public class QueryResult implements IQueryResult {

	private String[] m_vars;
		
	private Set<ITuple> m_result; 

	public QueryResult(){};
	
	public QueryResult(Set<ITuple> result, String[] vars){
		m_vars = vars;
		m_result = result;
	}

	public String[] getQueryVariables() {
		
		return m_vars;
	}

	public void setQueryVariables(String[] vars){
		m_vars = vars;
	}
		
	public Set<ITuple> getResult() {
		
		return m_result;
	}

	public void setResult(Set<ITuple> result) {
		
		m_result = result;
	}
	
	public void addResult(ITuple result) {
		
		m_result.add(result);
	}

	public int getColumn(String colName) {
		for (int i = 0; i < m_vars.length; i++)
			if (m_vars[i].equals(colName))
				return i;
		return -1;
	}	
}
