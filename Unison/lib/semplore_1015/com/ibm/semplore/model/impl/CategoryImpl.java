/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 *
 * $Id: CategoryImpl.java,v 1.2 2007/04/18 06:55:34 lql Exp $
 */
package com.ibm.semplore.model.impl;

import com.ibm.semplore.model.Category;

/**
 * @author liu Qiaoling
 *
 */
public class CategoryImpl extends SchemaObjectImpl implements Category
{
    /**
     * the uri of universal category
     */
    protected static final String UNIVERSAL_CATEGORY_URI = "UNIVERSAL_CATEGORY";

    /**
     * @param URI
     */
    protected CategoryImpl(String URI) {
        super(URI);
    }
    
    public String toString() {
        return URI;
    }
    
    public int hashCode() {
        return URI.hashCode();
    }
    
    public boolean equals(Object obj) {
        if (obj instanceof CategoryImpl) {
            return URI == ((CategoryImpl)obj).URI;
        }
        return false;
    }

    public boolean isUniversal()
    {
        return URI.equals(this.UNIVERSAL_CATEGORY_URI);
    }
}
