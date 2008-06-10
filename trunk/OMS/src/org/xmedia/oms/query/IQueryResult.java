package org.xmedia.oms.query;

import java.util.Set;


public interface IQueryResult {
	
	public String[] getQueryVariables();
	
	public Set<ITuple> getResult();

	public int getColumn(String colName);
}
