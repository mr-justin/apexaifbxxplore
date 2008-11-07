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
	 * If relax is true, all categories will be changed to
	 * universal category, all attributes will be changed to
	 * keyword constraints.
	 * @param graph
	 * @param relax whether relax Category and Attribute constraints
	 * @return
	 */
	public XFacetedQuery convertQuery(SubGraph graph, boolean relax);
}
