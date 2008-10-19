/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 */
package com.ibm.semplore.model;

/**
 * This interface provides descriptive information about category, relation, attribute, and instance of an ontology.
 * @author liu Qiaoling
 *
 */
public interface SchemaObject {
	
    /**
     * Returns the id of this schema object, based on its URI.
     * @return
     */
    public long getIDofURI();
    
    /**
     * Returns the URI of this schema object.
     * @return
     */
    public String getURI();
        
}
