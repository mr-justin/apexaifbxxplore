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
	 * @param relax whether relax Category and Attribute constraints
	 * @return
	 */
	public XFacetedQuery convertQuery(SubGraph graph, boolean relax);
}
