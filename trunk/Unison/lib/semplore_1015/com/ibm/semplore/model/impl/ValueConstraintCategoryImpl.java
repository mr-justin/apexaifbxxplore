/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 *
 * $Id: ValueConstraintCategoryImpl.java,v 1.2 2007/04/18 06:55:34 lql Exp $
 */
package com.ibm.semplore.model.impl;

import com.ibm.semplore.model.Attribute;
import com.ibm.semplore.model.ValueConstraintCategory;

/**
 * @author liu Qiaoling
 *
 */
public class ValueConstraintCategoryImpl implements ValueConstraintCategory
{
    
    /**
     * the attribute on which value constraint is put
     */
    protected Attribute attr;
    
    /**
     * the lower limit of the value constraint
     */
    protected String lowerLimit;
    
    /**
     * the upper limit of the value constraint
     */
    protected String upperLimit;
    
    /**
     * whether the lower limit is inclusive
     */
    protected boolean isLowerLimitInclusive;
    
    /**
     * whether the upper limit is inclusive
     */
    protected boolean isUpperLimitInclusive;
    
    /**
     * @param attr
     * @param lowerLimit
     * @param isLowerLimitInclusive
     * @param UpperLimit
     * @param isUpperLimitInclusive
     */
    protected ValueConstraintCategoryImpl(
            Attribute attr, String lowerLimit, boolean isLowerLimitInclusive,
            String upperLimit, boolean isUpperLimitInclusive) {
        set(attr, lowerLimit, isLowerLimitInclusive, upperLimit, isUpperLimitInclusive);
    }

    /* (non-Javadoc)
     * @see com.ibm.semplore.model.ValueConstraintCategory#getAttribute()
     */
    public Attribute getAttribute()
    {
        return attr;
    }

    /* (non-Javadoc)
     * @see com.ibm.semplore.model.ValueConstraintCategory#getLowerLimit()
     */
    public String getLowerLimit()
    {
        return lowerLimit;
    }

    /* (non-Javadoc)
     * @see com.ibm.semplore.model.ValueConstraintCategory#getUpperLimit()
     */
    public String getUpperLimit()
    {
        return upperLimit;
    }

    /* (non-Javadoc)
     * @see com.ibm.semplore.model.ValueConstraintCategory#isLowerLimitInclusive()
     */
    public boolean isLowerLimitInclusive()
    {
        return isLowerLimitInclusive;
    }

    /* (non-Javadoc)
     * @see com.ibm.semplore.model.ValueConstraintCategory#isUpperLimitInclusive()
     */
    public boolean isUpperLimitInclusive()
    {
        return isUpperLimitInclusive;
    }

    /* (non-Javadoc)
     * @see com.ibm.semplore.model.ValueConstraintCategory#set(com.ibm.semplore.model.Attribute, java.lang.String, boolean, java.lang.String, boolean)
     */
    public void set(Attribute attr, String lowerLimit,
            boolean isLowerLimitInclusive, String upperLimit,
            boolean isUpperLimitInclusive)
    {
        this.attr = attr;
        this.lowerLimit = lowerLimit;
        this.upperLimit = upperLimit;
        this.isLowerLimitInclusive = isLowerLimitInclusive;
        this.isUpperLimitInclusive = isUpperLimitInclusive;
    }

    public String toString() {
        return "CONSTRAINT ON "+attr.getURI();
    }
    
    public int hashCode() {
        return (attr+" "+lowerLimit+" "+upperLimit+" "+isLowerLimitInclusive+" "+isUpperLimitInclusive).hashCode();
    }
    public boolean equals(Object obj) {
        if (obj instanceof ValueConstraintCategoryImpl) {
            ValueConstraintCategoryImpl sobj = (ValueConstraintCategoryImpl)obj;
            return attr == sobj.attr 
                && lowerLimit == sobj.lowerLimit 
                && upperLimit == sobj.upperLimit 
                && isLowerLimitInclusive == sobj.isLowerLimitInclusive 
                && isUpperLimitInclusive == sobj.isUpperLimitInclusive;
        }
        return false;
    }
        
}
