/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 *
 * $Id: LiteralsOfProperty.java,v 1.2 2007/04/18 06:55:33 lql Exp $
 */
package com.ibm.semplore.model;

/**
 * This interface provides descriptive information about literals of some
 * property.
 * 
 * @author liu qiaoling
 * 
 */
public interface LiteralsOfProperty {

    /**
     * Returns the URI of the property.
     * 
     * @return
     */
    public String getProperty();

    /**
     * Returns the literal string.
     * 
     * @return
     */
    public String getLiteral();

}
