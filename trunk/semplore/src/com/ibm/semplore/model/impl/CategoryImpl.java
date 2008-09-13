/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 *
 * $Id: CategoryImpl.java,v 1.4 2008/09/01 09:53:14 lql Exp $
 */
package com.ibm.semplore.model.impl;

import com.ibm.semplore.model.Category;
import com.ibm.semplore.util.Md5_BloomFilter_64bit;

/**
 * @author liu Qiaoling
 *
 */
public class CategoryImpl extends SchemaObjectImpl implements Category
{

    /**
     * @param URI
     */
    protected CategoryImpl(long id) {
        super(id);
    }
    
    public String toString() {
        return String.valueOf(id);
    }
    
    public int hashCode() {
        return (int)id;
    }
    
    public boolean equals(Object obj) {
        if (obj instanceof CategoryImpl) {
            return id == ((CategoryImpl)obj).id;
        }
        return false;
    }

    public boolean isUniversal()
    {
        return id == Md5_BloomFilter_64bit.HASH_UNIVERSAL_CATEGORY_URI;
    }
}
