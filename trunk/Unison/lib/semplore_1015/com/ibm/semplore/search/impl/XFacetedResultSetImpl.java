/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 *
 * $Id: XFacetedResultSetImpl.java,v 1.4 2007/04/29 14:31:20 lql Exp $
 */
package com.ibm.semplore.search.impl;

import com.ibm.semplore.search.Facet;
import com.ibm.semplore.search.XFacetedResultSet;
import com.ibm.semplore.xir.DocStream;
import com.ibm.semplore.xir.IndexReader;

/**
 * @author liu Qiaoling
 *
 */
public class XFacetedResultSetImpl extends ResultSetImpl_TopDocs implements XFacetedResultSet
{
    
    /**
     * category facets information 
     */
    protected Facet[] catFacets;
    
    /**
     * relation facets information
     */
    protected Facet[] relFacets_subj;
    
    protected Facet[] relFacets_obj;
    
    protected Facet[] relFacets;

    /**
     * @param resultStream
     * @param indexReader
     * @param catFacets
     * @param relFacets
     * @throws Exception
     */
    protected XFacetedResultSetImpl(DocStream resultStream, IndexReader indexReader, Facet[] catFacets, Facet[] relFacets_subj, Facet[] relFacets_obj) throws Exception {
    	super(resultStream, indexReader);
        this.catFacets = catFacets;
        this.relFacets_subj = relFacets_subj;
        this.relFacets_obj = relFacets_obj;
    }
    
    protected XFacetedResultSetImpl(DocStream resultStream, IndexReader indexReader, Facet[] catFacets, Facet[] relFacets) throws Exception {
    	super(resultStream, indexReader);
        this.catFacets = catFacets;
        this.relFacets = relFacets;
    }
    
//    /**
//     * @param results the evaluated results, which may not be complete results
//     * @param estCount estmated number of the complete results
//     * @param catFacets
//     * @param relFacets
//     */
//    protected XFacetedResultSetImpl(HitDoc[] results, int estCount, Facet[] catFacets, Facet[] relFacets) {
//        super(results, estCount);
//        this.catFacets = catFacets;
//        this.relFacets = relFacets;
//    }
    
    /* (non-Javadoc)
     * @see com.ibm.semplore.search.XFacetedResultSet#getCategoryFacets()
     */
    public Facet[] getCategoryFacets()
    {
        return catFacets;
    }

    /* (non-Javadoc)
     * @see com.ibm.semplore.search.XFacetedResultSet#getRelationFacets()
     */
    public Facet[] getRelationFacetsGivenSubject()
    {
        return relFacets_subj;
    }

    public Facet[] getRelationFacetsGivenObject()
    {
        return relFacets_obj;
    }

    public Facet[] getRelationFacets()
    {
        return relFacets;
    }

}
