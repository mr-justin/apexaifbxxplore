/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 *
 * $Id: AttributeImpl.java,v 1.2 2007/04/18 06:55:34 lql Exp $
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
     * Create an attribute based on its URI and datatype.
     * @param URI
     * @param datatype
     */
    protected AttributeImpl(String URI, int datatype) {
        super(URI);
        this.datatype = datatype;
    }
    
    /* (non-Javadoc)
     * @see com.ibm.semplore.model.Attribute#getDatatype()
     */
    public int getDatatype()
    {
        return datatype;
    }

}
