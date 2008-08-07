/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 *
 * $Id: XFacetedSearchServiceImpl.java,v 1.2 2007/04/18 06:55:34 lql Exp $
 */
package com.ibm.semplore.search.impl;

import java.io.IOException;
import java.util.Properties;

import com.ibm.semplore.search.SchemaSearchable;
import com.ibm.semplore.search.XFacetedSearchService;
import com.ibm.semplore.search.XFacetedSearchable;
import com.ibm.semplore.xir.IndexReader;
import com.ibm.semplore.xir.IndexService;
import com.ibm.semplore.xir.impl.IndexFactoryImpl;

/**
 * @author liu Qiaoling
 *
 */
public class XFacetedSearchServiceImpl implements XFacetedSearchService
{
    
    /**
     * some configurations  
     */
    protected Properties config;
    
    /**
     * the index service to obtain several index readers.
     */
    protected IndexService indexService;
    
    /**
     * @param config
     */
    protected XFacetedSearchServiceImpl(Properties config) {
        indexService = IndexFactoryImpl.getInstance().getIndexService(config);
        this.config = config;
    }
        
    /* (non-Javadoc)
     * @see com.ibm.semplore.search.XFacetedSearchService#getSchemaSearchable()
     */
    public SchemaSearchable getSchemaSearchable() throws IOException
    {
        IndexReader indexReader = indexService.getIndexReader(IndexService.IndexType.Instance);
        return new SchemaSearchableImpl(indexReader);
    }

    /* (non-Javadoc)
     * @see com.ibm.semplore.search.XFacetedSearchService#getXFacetedSearchable()
     */
    public XFacetedSearchable getXFacetedSearchable() throws Exception
    {
        IndexReader insIR = indexService.getIndexReader(IndexService.IndexType.Instance);        
        return new XFacetedSearchableImpl(insIR, config);
    }

}
