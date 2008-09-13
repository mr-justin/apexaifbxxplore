/**
 * 
 */
package com.ibm.semplore.btc;

import com.ibm.semplore.search.XFacetedQuery;

/**
 * @author linna
 *
 */
public interface QueryConverter4Semplore {

	/**
	 * Convert a subgraph to a semplore query.
	 * @param graph
	 * @return
	 */
	public XFacetedQuery convertQuery(SubGraph graph);
}
