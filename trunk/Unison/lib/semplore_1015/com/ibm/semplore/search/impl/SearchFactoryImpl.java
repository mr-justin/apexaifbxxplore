/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 *
 * $Id: SearchFactoryImpl.java,v 1.3 2007/05/07 05:27:27 lql Exp $
 */
package com.ibm.semplore.search.impl;

import java.util.Properties;

import com.ibm.semplore.model.SchemaObjectInfo;
import com.ibm.semplore.search.CacheHint;
import com.ibm.semplore.search.Facet;
import com.ibm.semplore.search.ResultSet;
import com.ibm.semplore.search.SchemaQuery;
import com.ibm.semplore.search.SearchFactory;
import com.ibm.semplore.search.SearchHelper;
import com.ibm.semplore.search.XFacetedQuery;
import com.ibm.semplore.search.XFacetedResultSpec;
import com.ibm.semplore.search.XFacetedSearchService;

/**
 * @author liu Qiaoling
 *
 */
public class SearchFactoryImpl implements SearchFactory
{
    /**
     * public call not allowed
     */
    private SearchFactoryImpl() {}           
    
    /**
     * the static unique instance of this factory.
     */
    private static final SearchFactory instance;    
    static{
        instance = new SearchFactoryImpl();
    }
    
    /**
     * Return the unique instance of this factory.
     * @return
     */
    public static SearchFactory getInstance(){
        return instance;
    }
    
    /* (non-Javadoc)
     * @see com.ibm.semplore.search.SearchFactory#createCacheHint(com.ibm.semplore.search.ResultSet)
     */
    public CacheHint createCacheHint(ResultSet resultSet)
    {
        return new CacheHintImpl(resultSet);
    }

    /* (non-Javadoc)
     * @see com.ibm.semplore.search.SearchFactory#createSchemaQuery()
     */
    public SchemaQuery createSchemaQuery()
    {
        return new SchemaQueryImpl();
    }

    /* (non-Javadoc)
     * @see com.ibm.semplore.search.SearchFactory#createSearchHelper()
     */
    public SearchHelper createSearchHelper()
    {
        return new SearchHelperImpl();
    }

    /* (non-Javadoc)
     * @see com.ibm.semplore.search.SearchFactory#createXFacetedQuery()
     */
    public XFacetedQuery createXFacetedQuery()
    {
        return new XFacetedQueryImpl();
    }

    /* (non-Javadoc)
     * @see com.ibm.semplore.search.SearchFactory#createXFacetedResultSpec()
     */
    public XFacetedResultSpec createXFacetedResultSpec()
    {
        return new XFacetedResultSpecImpl();
    }

    /* (non-Javadoc)
     * @see com.ibm.semplore.search.SearchFactory#getXFacetedSearchService(java.util.Properties)
     */
    public XFacetedSearchService getXFacetedSearchService(Properties config)
    {
        return new XFacetedSearchServiceImpl(config);
    }

	/* (non-Javadoc)
	 * @see com.ibm.semplore.search.SearchFactory#createFacet(com.ibm.semplore.model.SchemaObjectInfo, int)
	 */
	public Facet createFacet(SchemaObjectInfo facetInfo, int count) {
		return new FacetImpl(facetInfo, count);
	}

}
