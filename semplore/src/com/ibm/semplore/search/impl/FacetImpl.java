/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 *
 * $Id: FacetImpl.java,v 1.3 2008/01/10 11:13:07 lql Exp $
 */
package com.ibm.semplore.search.impl;

import com.ibm.semplore.model.SchemaObjectInfo;
import com.ibm.semplore.search.Facet;

/**
 * @author liu qiaoling
 *
 */
public class FacetImpl implements Facet
{

    /**
     * the information of this facet 
     */
    protected SchemaObjectInfo facetInfo;
    
    /**
     * the count of results that match with the facet
     */
    protected int count;
    
    protected boolean inverse;
    
    /**
     * @param facetInfo
     * @param count
     */
    protected FacetImpl(SchemaObjectInfo facetInfo, int count) {
        this.facetInfo = facetInfo;
        this.count = count; 
    }
    
    protected FacetImpl(SchemaObjectInfo facetInfo, int count, boolean inverse) {
        this.facetInfo = facetInfo;
        this.count = count; 
        this.inverse = inverse;
    }
    
    /* (non-Javadoc)
     * @see com.ibm.semplore.search.Facet#getCount()
     */
    public int getCount()
    {
        return count;
    }

    /* (non-Javadoc)
     * @see com.ibm.semplore.search.Facet#getInfo()
     */
    public SchemaObjectInfo getInfo()
    {
        return facetInfo;
    }
    
    public boolean isInverseRelation() {
    	return inverse;
    }

}
