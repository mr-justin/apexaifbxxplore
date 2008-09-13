/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 */
package com.ibm.semplore.search;

import com.ibm.semplore.model.SchemaObjectInfo;


/**
 * Factory interface is used to obtain search service (search host), query objects. Obtaining a search service and a query objects allow finding available searchable objects and to search using the query object.
 * @author liu Qiaoling
 *
 */
public interface SearchFactory {
	
	/**
	 * Obtains an extended faceted search service object for a specific application.
	 * @param config
	 * @return
	 */
	public XFacetedSearchService getXFacetedSearchService(java.util.Properties config);
	
	/**
	 * Create a search helper.
	 * @return
	 */
	public SearchHelper createSearchHelper();
	
	/**
	 * Create an extended faceted query.
	 * @return
	 */
	public XFacetedQuery createXFacetedQuery();	
    
    /**
     * Create a schema query.
     * @return
     */
    public SchemaQuery createSchemaQuery();
    
    /**
	 * Create a result specification for an extended faceted query.
	 * @return
	 */
	public XFacetedResultSpec createXFacetedResultSpec();
	
	/**
	 * Create a cache hint of some query results, so that to be used by SearchHelper.
	 * @param resultSet
	 * @return
	 */
	public CacheHint createCacheHint(ResultSet resultSet);
	
	/**
	 * Create a facet based on facet info and count.
	 * @param facetInfo
	 * @param count
	 * @return
	 */
	public Facet createFacet(SchemaObjectInfo facetInfo, int count);
    
}
