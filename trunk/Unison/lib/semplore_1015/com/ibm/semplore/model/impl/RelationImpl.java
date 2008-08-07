/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 *
 * $Id: RelationImpl.java,v 1.3 2007/04/18 06:55:34 lql Exp $
 */
package com.ibm.semplore.model.impl;

import com.ibm.semplore.model.Relation;

/**
 * @author liu Qiaoling
 *
 */
public class RelationImpl extends SchemaObjectImpl implements Relation
{
    
    /**
     * the uri of universal relation
     */
    protected static final String UNIVERSAL_RELATION_URI = "UNIVERSAL_RELATION";
    
    protected boolean inverse = false;
    
    /**
     * @param URI
     */
    protected RelationImpl(String URI, boolean inverse) {
        super(URI);
        this.inverse = inverse;
    }
    
    public String toString() {
    	if (inverse)
    		return URI+"(inverse)";
        return URI;
    }

    public boolean isUniversal()
    {
        return URI.equals(this.UNIVERSAL_RELATION_URI);
    }

    public boolean isInverse()
    {
        return inverse;
    }    
}
