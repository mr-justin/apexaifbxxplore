/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 *
 * $Id: XFacetedQueryImpl.java,v 1.2 2007/04/18 06:55:34 lql Exp $
 */
package com.ibm.semplore.search.impl;

import com.ibm.semplore.model.CatRelConstraint;
import com.ibm.semplore.search.XFacetedQuery;
import com.ibm.semplore.search.XFacetedResultSpec;

/**
 * @author liu Qiaoling
 *
 */
public class XFacetedQueryImpl extends QueryImpl implements XFacetedQuery
{
    
    /**
     * The query expression.
     */
    protected CatRelConstraint queryCons;
    
    /**
     * The result specification of facets.
     */
    protected XFacetedResultSpec resultSpec;
    
    /**
     * The search target in the query expression. 
     */
    protected int target;    
    
    /* (non-Javadoc)
     * @see com.ibm.semplore.search.XFacetedQuery#getQueryConstraint()
     */
    public CatRelConstraint getQueryConstraint()
    {
        return queryCons;
    }

    /* (non-Javadoc)
     * @see com.ibm.semplore.search.XFacetedQuery#getResultSpec()
     */
    public XFacetedResultSpec getResultSpec()
    {
        return resultSpec;
    }

    /* (non-Javadoc)
     * @see com.ibm.semplore.search.XFacetedQuery#getSearchTarget()
     */
    public int getSearchTarget()
    {
        return target;
    }

    /* (non-Javadoc)
     * @see com.ibm.semplore.search.XFacetedQuery#setQueryConstraint(com.ibm.semplore.model.CatRelConstraint)
     */
    public void setQueryConstraint(CatRelConstraint cons)
    {
        queryCons = cons;
    }

    /* (non-Javadoc)
     * @see com.ibm.semplore.search.XFacetedQuery#setResultSpec(com.ibm.semplore.search.XFacetedResultSpec)
     */
    public void setResultSpec(XFacetedResultSpec resultSpec)
    {
        this.resultSpec = resultSpec;
    }

    /* (non-Javadoc)
     * @see com.ibm.semplore.search.XFacetedQuery#setSearchTarget(int)
     */
    public void setSearchTarget(int target)
    {
        this.target = target;
    }

}
