/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 *
 * $Id: EnumerationCategoryImpl.java,v 1.2 2007/04/18 06:55:34 lql Exp $
 */
package com.ibm.semplore.model.impl;

import java.util.HashSet;
import java.util.Iterator;

import com.ibm.semplore.model.EnumerationCategory;
import com.ibm.semplore.model.Instance;

/**
 * @author liu qiaoling
 *
 */
public class EnumerationCategoryImpl implements EnumerationCategory
{

    /**
     * the instance elements of this enumeration category.
     */
    protected HashSet instances;
    
    /**
     * @param instances
     */
    protected EnumerationCategoryImpl() {
        instances = new HashSet();
    }
    
    /* (non-Javadoc)
     * @see com.ibm.semplore.model.EnumerationCategory#getInstanceElement(int)
     */
    public Instance[] getInstanceElements()
    {
        return (Instance[])instances.toArray(new Instance[0]);
    }

    /* (non-Javadoc)
     * @see com.ibm.semplore.model.EnumerationCategory#setElements(com.ibm.semplore.model.Category[])
     */
    public EnumerationCategory addInstanceElement(Instance ins)
    {
        instances.add(ins);
        return this;
    }

    public int size()
    {
        return instances.size();
    }

    public int hashCode() {
        return instances.size();
    }
    
    public String toString() {
        StringBuffer buf = new StringBuffer("{");
        boolean first = true;
        Iterator it = instances.iterator();
        while (it.hasNext()) {
            if (!first)
                buf.append(", ");
            buf.append(it.next());
            first = false;
        }
        buf.append("}");
        return buf.toString();
    }
    
    public boolean equals(Object obj) {
        if (obj instanceof EnumerationCategoryImpl) {
            EnumerationCategoryImpl sobj = (EnumerationCategoryImpl)obj;
            if (size() != sobj.size())
                return false;
            Iterator it = instances.iterator();
            while (it.hasNext()) {
                if (!sobj.instances.contains(it.next()))
                    return false;
            }
            return true;
        }
        return false;
    }
}
