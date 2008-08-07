/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 *
 * $Id: DocumentImpl.java,v 1.2 2007/04/18 06:55:34 lql Exp $
 */
package com.ibm.semplore.xir.impl;

import com.ibm.semplore.model.SchemaObjectInfo;
import com.ibm.semplore.xir.Document;

/**
 * @author liu qiaoling
 *
 */
public abstract class DocumentImpl implements Document
{
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

    /* (non-Javadoc)
     * @see com.ibm.semplore.xir.Document#setSchemaObjectInfo(com.ibm.semplore.model.SchemaObjectInfo)
     */
    public void setSchemaObjectInfo(SchemaObjectInfo info)
    {
        this.info = info;
    }

}
