/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 */
package com.ibm.semplore.search;

import com.ibm.semplore.search.impl.AUManager;
import com.ibm.semplore.xir.DocStream;



/**
 * This is the faceted search interface to the index. A FacetedSearchable object enables to issue queries with facets constraints that could be proposed on both categories and relations, and get information about the associated collection.
 * @author liu Qiaoling
 *
 */
public interface XFacetedSearchable extends Searchable {
	
	/**
	 * Runs a faceted query and returns a set of results and their facets information. This method never returns null. If there are no matching results, then the method FacetedResultSet.getLength() will return 0.
	 * @param facetedQuery
	 * @return
	 */
	public XFacetedResultSet search(XFacetedQuery facetedQuery) throws Exception;
	
	/**
	 * Runs a faceted query and returns a set of results and their facets information, with the help of SearchHelper, which for example may contain some cache for better performance of the query evaluation. This method never returns null. If there are no matching results, then the method FacetedResultSet.getLength() will return 0.
	 * @param facetedQuery
	 * @param searchHelper
	 * @return
	 */
	public XFacetedResultSet search(XFacetedQuery facetedQuery, SearchHelper searchHelper) throws Exception;
	
	/**
	 * Runs a faceted query and returns the result docstream.
	 * @param facetedQuery
	 * @return
	 */
	public DocStream evaluate(XFacetedQuery facetedQuery) throws Exception;
	
	/**
	 * Runs a faceted query and returns the result docstream, with the help of SearchHelper, which for example may contain some cache for better performance of the query evaluation. This method never returns null. 
	 * @param facetedQuery
	 * @param searchHelper
	 * @return
	 */
	public DocStream evaluate(XFacetedQuery facetedQuery, SearchHelper searchHelper) throws Exception;
	
	/**
	 * Return the arithmetic unit manager to comput massUnion and intersection.
	 * @return
	 */
	public AUManager getAUManager();
}
