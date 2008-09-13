/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 *
 * $Id: CategoryRelationExpImpl.java,v 1.2 2007/04/18 06:55:34 lql Exp $
 */
package com.ibm.semplore.model.impl;

import java.util.ArrayList;

import com.ibm.semplore.model.CategoryRelationExp;
import com.ibm.semplore.model.GeneralCategory;
import com.ibm.semplore.model.Relation;

/**
 * @author liu Qiaoling
 *
 */
public class CategoryRelationExpImpl implements CategoryRelationExp
{
    
    /**
     * the expression 
     */
    protected ArrayList exp;
    
    /**
     * 
     */
    protected CategoryRelationExpImpl() {
        exp = new ArrayList();
    }
    
    /* (non-Javadoc)
     * @see com.ibm.semplore.model.CategoryRelationExp#append(com.ibm.semplore.model.GeneralCategory)
     */
    public CategoryRelationExp append(GeneralCategory cat)
    {
        exp.add(cat);
        return this;
    }

    /* (non-Javadoc)
     * @see com.ibm.semplore.model.CategoryRelationExp#append(com.ibm.semplore.model.Relation)
     */
    public CategoryRelationExp append(Relation rel)
    {
        exp.add(rel);
        return this;
    }

    /* (non-Javadoc)
     * @see com.ibm.semplore.model.CategoryRelationExp#get(int)
     */
    public Object get(int index)
    {
        return exp.get(index);
    }

    /* (non-Javadoc)
     * @see com.ibm.semplore.model.CategoryRelationExp#removeLastItem()
     */
    public CategoryRelationExp removeLastItem()
    {
        exp.remove(exp.size()-1);
        return this;
    }

    /* (non-Javadoc)
     * @see com.ibm.semplore.model.CategoryRelationExp#size()
     */
    public int size()
    {
        return exp.size();
    }
    
    public String toString() {
        StringBuffer buf = new StringBuffer("[");
        for (int i=0; i<exp.size(); i++) {
            if (i>0)
                buf.append(", ");
            buf.append(exp.get(i).toString());
        }
        buf.append("]");
        return buf.toString();
    }

}
