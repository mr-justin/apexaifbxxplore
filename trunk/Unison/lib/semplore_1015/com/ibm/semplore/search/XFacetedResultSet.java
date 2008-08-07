/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 */
package com.ibm.semplore.search;

/**
 * A faceted result set contains the query results along with their facets information.
 * @author liu Qiaoling
 *
 */
public interface XFacetedResultSet extends ResultSet {
	
	/**
	 * Returns information for each category facet of the query results with respect to the result specification in the EFQ.
	 * @return
	 */
	public Facet[] getCategoryFacets();
	
	/**
	 * Returns information for each relation facet of the query results with respect to the result specification in the EFQ.
	 * @return
	 */
	public Facet[] getRelationFacetsGivenSubject();

	public Facet[] getRelationFacetsGivenObject();

	public Facet[] getRelationFacets();
	
}
