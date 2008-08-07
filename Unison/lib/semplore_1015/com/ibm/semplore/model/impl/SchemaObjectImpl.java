/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 *
 * $Id: SchemaObjectImpl.java,v 1.2 2007/04/18 06:55:34 lql Exp $
 */
package com.ibm.semplore.model.impl;

import com.ibm.semplore.model.SchemaObject;
import com.ibm.semplore.util.MD5Encrypt;

/**
 * @author liu Qiaoling
 *
 */
public abstract class SchemaObjectImpl implements SchemaObject
{
    
    /**
     * the URI of this schema object
     */
    protected String URI;
    
    /**
     * the id of the schema object based on its URI
     */
    protected String id;
            
    /**
     * Create the id of this schema object, based on its URI.
     * @param URI
     */
    protected SchemaObjectImpl(String URI) {
        this.URI = URI;
    }
    
    /* (non-Javadoc)
     * @see com.ibm.semplore.model.SchemaObject#getURI()
     */
    public String getURI()
    {
        return URI;
    }

    /* (non-Javadoc)
     * @see com.ibm.semplore.model.SchemaObject#getIDofURI()
     */
    public String getIDofURI() 
    {
        if (id == null)
            id = MD5Encrypt.MD5Encode(URI, 16);
        return id;
    }
    
}
