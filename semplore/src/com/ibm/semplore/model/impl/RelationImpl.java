/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 *
 * $Id: RelationImpl.java,v 1.5 2008/09/01 09:53:14 lql Exp $
 */
package com.ibm.semplore.model.impl;

import com.ibm.semplore.model.Relation;
import com.ibm.semplore.util.Md5_BloomFilter_64bit;

/**
 * @author liu Qiaoling
 *
 */
public class RelationImpl extends SchemaObjectImpl implements Relation
{
    protected boolean inverse = false;
    
    /**
     * @param URI
     */
    protected RelationImpl(long id, boolean inverse) {
        super(id);
        this.inverse = inverse;
    }
    
    public String toString() {
    	if (inverse)
    		return id+"(inverse)";
        return String.valueOf(id);
    }

    public boolean isUniversal()
    {
        return id == Md5_BloomFilter_64bit.HASH_UNIVERSAL_RELATION_URI;
    }

    public boolean isInverse()
    {
        return inverse;
    }    
}
