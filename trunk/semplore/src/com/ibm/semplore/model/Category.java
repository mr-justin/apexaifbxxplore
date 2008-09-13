/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 */
package com.ibm.semplore.model;

/**
 * This interface provides descriptive information about a category(in the ontology).
 * 
 * @author liu Qiaoling
 *
 */
public interface Category extends SchemaObject,GeneralCategory {
	
    /**
     * Returns whether this category is universal category, which is comprised of all instances.
     * @return
     */
    public boolean isUniversal();
    
}
