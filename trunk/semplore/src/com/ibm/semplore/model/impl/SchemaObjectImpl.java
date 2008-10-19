/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 *
 * $Id: SchemaObjectImpl.java,v 1.3 2008/09/01 09:53:14 lql Exp $
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
//    protected String URI;
    
    /**
     * the id of the schema object based on its URI
     */
    protected long id;
    
    protected String uri;
            
    /**
     * Create the id of this schema object, based on its id.
     * @param id
     */
    protected SchemaObjectImpl(long id) {
    	this.uri = String.valueOf(id);
        this.id = id;
    }
    protected SchemaObjectImpl(String uri) {
        this.uri = uri;
    }
    
    /* (non-Javadoc)
     * @see com.ibm.semplore.model.SchemaObject#getURI()
     */
//    public String getURI()
//    {
//        return URI;
//    }

    /* (non-Javadoc)
     * @see com.ibm.semplore.model.SchemaObject#getIDofURI()
     */
    public long getIDofURI() 
    {
//        if (id == null)
//            id = MD5Encrypt.MD5Encode(URI, 16);
        return id;
    }
    
    public String getURI() {
    	return uri;
    }
}
