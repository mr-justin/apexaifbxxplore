/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 *
 * $Id: AttributeImpl.java,v 1.3 2008/09/01 09:53:14 lql Exp $
 */
package com.ibm.semplore.model.impl;

import com.ibm.semplore.model.Attribute;

/**
 * @author liu Qiaoling
 *
 */
public class AttributeImpl extends SchemaObjectImpl implements Attribute
{

    /**
     * the datatype of this attribute
     */
    protected int datatype;
    
    /**
     * Create an attribute based on its ID and datatype.
     * @param ID
     * @param datatype
     */
    protected AttributeImpl(long ID, int datatype) {
        super(ID);
        this.datatype = datatype;
    }
    protected AttributeImpl(String uri, int datatype) {
        super(uri);
        this.datatype = datatype;
    }
    
    /* (non-Javadoc)
     * @see com.ibm.semplore.model.Attribute#getDatatype()
     */
    public int getDatatype()
    {
        return datatype;
    }

    public String toString() {
    	return getURI() + "(" + datatype + ")";
    }
}
