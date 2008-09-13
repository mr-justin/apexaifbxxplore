/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 */
package com.ibm.semplore.model;

/**
 * This interface provides descriptive information about a relation.
 * @author liu Qiaoling
 *
 */
public interface Relation extends SchemaObject {

    /**
     * Returns whether this relation is universal relation, which is the super relations of all relations.
     * @return
     */
    public boolean isUniversal();
    
    /**
     * Returns whether this relation is inverse relation of URI.
     * @return
     */
    public boolean isInverse();

}
