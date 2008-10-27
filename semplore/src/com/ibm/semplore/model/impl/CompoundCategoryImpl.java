/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 *
 * $Id: CompoundCategoryImpl.java,v 1.2 2007/04/18 06:55:34 lql Exp $
 */
package com.ibm.semplore.model.impl;

import java.util.HashSet;
import java.util.Iterator;

import com.ibm.semplore.model.Category;
import com.ibm.semplore.model.CompoundCategory;
import com.ibm.semplore.model.GeneralCategory;

/**
 * @author liu Qiaoling
 *
 */
public class CompoundCategoryImpl implements CompoundCategory
{

    /**
     * the component categories 
     */
    protected HashSet components;
    
    /**
     * the compound type 
     */
    protected int type;
        
    /**
     * @param type
     */
    protected CompoundCategoryImpl(int type) {
        this.type = type;
        components = new HashSet();
    }
    
    /* (non-Javadoc)
     * @see com.ibm.semplore.model.CompoundCategory#addComponentCategory(com.ibm.semplore.model.GeneralCategory)
     */
    public CompoundCategory addComponentCategory(GeneralCategory cat)
    {
    	//CompoundCategory and UniversalCategory has problem together
    	//TODO TYPE_OR unresolved
    	if (type != TYPE_AND || !(cat instanceof Category) || !((Category)cat).isUniversal())
    		components.add(cat);
        return this;
    }

    /* (non-Javadoc)
     * @see com.ibm.semplore.model.CompoundCategory#getComponentCategory(int)
     */
    public GeneralCategory[] getComponentCategories()
    {
        return (GeneralCategory[])components.toArray(new GeneralCategory[0]);
    }

    /* (non-Javadoc)
     * @see com.ibm.semplore.model.CompoundCategory#getCompoundType()
     */
    public int getCompoundType()
    {
        return type;
    }

    /* (non-Javadoc)
     * @see com.ibm.semplore.model.CompoundCategory#getSize()
     */
    public int size()
    {
        return components.size();
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        String[] typename = new String[]{"", " AND ", " OR "};
        boolean first = true;
        Iterator it = components.iterator();
        while (it.hasNext()) {
            if (!first)
                buf.append(typename[type]);
            buf.append(it.next());
            first = false;
        }
        return buf.toString();
    }    
    
    public int hashCode() {
        return components.size();
    }
    
    public boolean equals(Object obj) {
        if (obj instanceof CompoundCategoryImpl) {
            CompoundCategoryImpl sobj = (CompoundCategoryImpl)obj;
            if (type != sobj.type)
                return false;
            if (components.size() != sobj.components.size())
                return false;
            Iterator it = components.iterator();
            while (it.hasNext()) {
                if (!sobj.components.contains(it.next()))
                    return false;
            }
            return true;
        }
        return false;
    }

	@Override
	public CompoundCategory removeComponentCategory(GeneralCategory cat) {
		components.remove(cat);
		return this;
	}
}
