/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 */
package com.ibm.semplore.xir;

import com.ibm.semplore.model.SchemaObjectInfo;

/**
 * This interface describes a virtual document for indexing.
 * @author liu Qiaoling
 *
 */
public interface Document {
    
	/**
	 * Returns the URI of the schema object corresponding to this document.
	 * @return
	 */
	public String getURI();
	
    /**
     * Returns the information of the schema object corresponding to this document.
     * @return
     */
    public SchemaObjectInfo getSchemaObjectInfo();
    
    /**
     * Set the information of the schema object corresponding to this document.
     * @param info
     */
    public void setSchemaObjectInfo(SchemaObjectInfo info);
    
}
