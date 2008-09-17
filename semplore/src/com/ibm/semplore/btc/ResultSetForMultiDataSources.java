package com.ibm.semplore.btc;

import java.util.HashMap;

import com.ibm.semplore.search.XFacetedResultSet;

public interface ResultSetForMultiDataSources extends XFacetedResultSet {

	/**
	 * Return the data source facets, each facet consists of (data source name,
	 * count).
	 * 
	 * @return
	 */
	public HashMap<String, Integer> getDataSourceFacets();
}
