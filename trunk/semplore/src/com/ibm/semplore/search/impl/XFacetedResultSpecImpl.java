/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 *
 * $Id: XFacetedResultSpecImpl.java,v 1.2 2007/04/18 06:55:34 lql Exp $
 */
package com.ibm.semplore.search.impl;

import com.ibm.semplore.model.SchemaObjectInfo;
import com.ibm.semplore.search.XFacetedResultSpec;

/**
 * @author liu Qiaoling
 *
 */
public class XFacetedResultSpecImpl implements XFacetedResultSpec
{
    
    /**
     * the category facets
     */
    protected SchemaObjectInfo[] categories;
    
    /**
     * the relation facets
     */
    protected SchemaObjectInfo[] relations;    
    
    /**
     * whether to exactly count results that match with a category facet
     */
    protected boolean isCatExactCount;
    
    /**
     * whether to exactly count results that match with a relation facet
     */
    protected boolean isRelExactCount;

    /* (non-Javadoc)
     * @see com.ibm.semplore.search.XFacetedResultSpec#getCategoryFacets()
     */
    public SchemaObjectInfo[] getCategoryFacetsInfo()
    {
        return categories;
    }

    /* (non-Javadoc)
     * @see com.ibm.semplore.search.XFacetedResultSpec#getRelationFacets()
     */
    public SchemaObjectInfo[] getRelationFacetsInfo()
    {
        return relations;
    }

    /* (non-Javadoc)
     * @see com.ibm.semplore.search.XFacetedResultSpec#isCategoryExactCount()
     */
    public boolean isCategoryExactCount()
    {
        return this.isCatExactCount;
    }

    /* (non-Javadoc)
     * @see com.ibm.semplore.search.XFacetedResultSpec#isRelationExactCount()
     */
    public boolean isRelationExactCount()
    {
        return this.isRelExactCount;
    }

    /* (non-Javadoc)
     * @see com.ibm.semplore.search.XFacetedResultSpec#setCategoryFacetsInfo(boolean, com.ibm.semplore.model.Category[])
     */
    public void setCategoryFacetsInfo(boolean exactCount, SchemaObjectInfo[] categories)
    {
        this.isCatExactCount = exactCount;
        this.categories = categories;
    }

    /* (non-Javadoc)
     * @see com.ibm.semplore.search.XFacetedResultSpec#setRelationFacetsInfo(boolean, com.ibm.semplore.model.Relation[])
     */
    public void setRelationFacetsInfo(boolean exactCount, SchemaObjectInfo[] relations)
    {
        this.isRelExactCount = exactCount;
        this.relations = relations;
    }

}
