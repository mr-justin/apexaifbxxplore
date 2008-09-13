/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 */
package com.ibm.semplore.search;

import com.ibm.semplore.model.SchemaObjectInfo;

/**
 * This interface provides a facets specification for the results of an extended faceted query.
 * @author liu Qiaoling
 *
 */
public interface XFacetedResultSpec {
	
	/**
	 * Set constrain of several category facets.
	 * @param exactCount whether to exactly count the results that match with the facets.
	 * @param categories the facets.
	 */
	public void setCategoryFacetsInfo(boolean exactCount, SchemaObjectInfo[] categories);
	
	/**
	 * Set constrain of several relation facets.
	 * @param exactCount whether to exactly count the results that match with the facets.
	 * @param relations the facets.
	 */
	public void setRelationFacetsInfo(boolean exactCount, SchemaObjectInfo[] relations);
	
    /**
     * Returns the category facets.
     * @return
     */
    public SchemaObjectInfo[] getCategoryFacetsInfo();
    
    /**
     * Returns the relation facets;
     * @return
     */
    public SchemaObjectInfo[] getRelationFacetsInfo();
    
    /**
     * Returns whether to exactly count the results that match with the category facets.
     * @return
     */
    public boolean isCategoryExactCount();
    
    /**
     * Returns whether to exactly count the results that match with the relation facets.
     * @return
     */
    public boolean isRelationExactCount();
    
}
