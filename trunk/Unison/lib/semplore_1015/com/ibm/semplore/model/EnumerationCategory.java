/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 *
 * $Id: EnumerationCategory.java,v 1.2 2007/04/18 06:55:33 lql Exp $
 */
package com.ibm.semplore.model;

/**
 * This interface provides descriptive information about an enumeration category, which is a virtual category comprised of some specified instances.
 * @author liu qiaoling
 *
 */
public interface EnumerationCategory extends GeneralCategory
{
    
    /**
     * Returns the instance elements of this enumeration category.
     * @return
     */
    public Instance[] getInstanceElements();
    
    /**
     * Add an instance element to this enumeration category.
     * @param ins
     * @return
     */
    public EnumerationCategory addInstanceElement(Instance ins); 

    /**
     * Returns the number of instance elements.
     * @return
     */
    public int size();
}
