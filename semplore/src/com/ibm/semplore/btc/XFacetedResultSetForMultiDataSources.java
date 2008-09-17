/**
 * 
 */
package com.ibm.semplore.btc;

import java.util.ArrayList;

import com.ibm.semplore.search.XFacetedResultSet;

/**
 * @author xrsun
 *
 */
public interface XFacetedResultSetForMultiDataSources extends XFacetedResultSet {
	/**
	 * @return the list of datasource names
	 */
	public ArrayList<String> getDataSourceFacet();
}
