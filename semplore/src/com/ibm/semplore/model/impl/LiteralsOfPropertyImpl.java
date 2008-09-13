/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 *
 * $Id: LiteralsOfPropertyImpl.java,v 1.2 2007/04/18 06:55:34 lql Exp $
 */
package com.ibm.semplore.model.impl;

import com.ibm.semplore.model.LiteralsOfProperty;

/**
 * @author liu qiaoling
 * 
 */
public class LiteralsOfPropertyImpl implements LiteralsOfProperty {
    /**
     * the literal
     */
    protected String literal;

    /**
     * the property
     */
    protected String property;

    /**
     * @param pro
     * @param lits
     */
    protected LiteralsOfPropertyImpl(String pro, String lit) {
        literal = lit;
        property = pro;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ibm.semplore.model.LiteralsOfProperty#getLiterals()
     */
    public String getLiteral() {
        return literal;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ibm.semplore.model.LiteralsOfProperty#getProperty()
     */
    public String getProperty() {
        return property;
    }

}
