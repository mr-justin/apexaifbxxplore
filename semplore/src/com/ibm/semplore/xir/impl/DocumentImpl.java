/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 *
 * $Id: DocumentImpl.java,v 1.4 2008/09/01 09:53:14 lql Exp $
 */
package com.ibm.semplore.xir.impl;

import com.ibm.semplore.model.SchemaObject;
import com.ibm.semplore.model.SchemaObjectInfo;
import com.ibm.semplore.xir.Document;

/**
 * @author liu qiaoling
 *
 */
public abstract class DocumentImpl implements Document
{
	protected SchemaObject schemaObject;
	
	protected String URI = null;
	
    /**
     * the information of the schema object corresponding to this document
     */
    protected SchemaObjectInfo info;
    
    /* (non-Javadoc)
     * @see com.ibm.semplore.xir.Document#getSchemaObjectInfo()
     */
    public SchemaObjectInfo getSchemaObjectInfo()
    {
        return info;
    }
    
	public String getURI() {
		return URI;
	}    

    /* (non-Javadoc)
     * @see com.ibm.semplore.xir.Document#setSchemaObjectInfo(com.ibm.semplore.model.SchemaObjectInfo)
     */
    public void setSchemaObjectInfo(SchemaObjectInfo info)
    {
        this.info = info;
    }

}
