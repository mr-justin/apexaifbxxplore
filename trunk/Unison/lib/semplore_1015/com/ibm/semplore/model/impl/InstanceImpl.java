/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 *
 * $Id: InstanceImpl.java,v 1.2 2007/04/18 06:55:34 lql Exp $
 */
package com.ibm.semplore.model.impl;

import com.ibm.semplore.model.Instance;

/**
 * @author liu Qiaoling
 *
 */
public class InstanceImpl extends SchemaObjectImpl implements Instance
{
    
    /**
     * @param URI
     */
    protected InstanceImpl(String URI) {
        super(URI);
    }
    
    public boolean equals(Object obj) {
        if (obj instanceof Instance) {
            return URI == ((Instance)obj).getURI();
        }
        return false;
    }
    
    public int hashCode() {
        return URI.hashCode();
    }
    
    public String toString() {
        return URI;
    }
}
