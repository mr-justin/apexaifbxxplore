/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 */
package com.ibm.semplore.search;

import com.ibm.semplore.model.SchemaObjectInfo;

/**
 * This interface provides descriptive information about a facet that the query results need to be matched with.
 * @author liu Qiaoling
 *
 */
public interface Facet {
	
	/**
	 * Returns the count of query results that match with this facet.
	 * @return
	 */
	public int getCount();
	
    /**
     * Returns the infomation of this facet.
     * @return
     */
    public SchemaObjectInfo getInfo();
    
    /**
     *Returns the mark of inverse relation. 
     */
    public boolean isInverseRelation();

}
