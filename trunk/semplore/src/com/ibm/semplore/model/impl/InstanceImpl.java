/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 *
 * $Id: InstanceImpl.java,v 1.3 2008/09/01 09:53:14 lql Exp $
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
    protected InstanceImpl(long id) {
        super(id);
    }
    protected InstanceImpl(String uri) {
        super(uri);
    }
    
    public boolean equals(Object obj) {
        if (obj instanceof InstanceImpl) {
            return id == ((InstanceImpl)obj).id;
        }
        return false;
    }
    
    public int hashCode() {
        return (int)id;
    }
    
    public String toString() {
        return getURI();
    }
}
