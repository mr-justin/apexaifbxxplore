/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 */
package com.ibm.semplore.model;

/**
 * This interface provides descriptive information about a local instance list which assigns a local id to each instance in the list.
 * @author liu Qiaoling
 *
 */
public interface LocalInstanceList extends LocalList {

    /**
	 * Returns the instance with given index in the list.
     * @param index
     * @return
     */
    public Instance getInstance(int index);
    
}
