/**
 * 
 */
package com.ibm.semplore.btc;

import java.util.HashMap;

import com.ibm.semplore.search.XFacetedResultSet;

/**
 * @author xrsun
 * 
 */
public interface XFacetedResultSetForMultiDataSources extends XFacetedResultSet {
	/**
	 * Return the data source facets, each facet consists of (data source name,
	 * count).
	 */
	public HashMap<String, Integer> getDataSourceFacets();
	
	/**
	 * Return the data source of the current results.
	 * @return
	 */
	public String getCurrentDataSource();
}
