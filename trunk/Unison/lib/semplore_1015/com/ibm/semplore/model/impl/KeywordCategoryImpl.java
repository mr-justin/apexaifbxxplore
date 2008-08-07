/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 *
 * $Id: KeywordCategoryImpl.java,v 1.3 2007/05/16 06:56:45 lql Exp $
 */
package com.ibm.semplore.model.impl;

import com.ibm.semplore.model.KeywordCategory;

/**
 * @author liu Qiaoling
 *
 */
public class KeywordCategoryImpl implements KeywordCategory
{

    /**
     * the keyword of this category
     */
    protected String keyword;    
    
    /**
     * @param keyword
     */
    protected KeywordCategoryImpl(String keyword) {
        this.keyword = keyword.toLowerCase();
    }
    
    /* (non-Javadoc)
     * @see com.ibm.semplore.model.KeywordCategory#getKeyword()
     */
    public String getKeyword()
    {
        return keyword;
    }

    /* (non-Javadoc)
     * @see com.ibm.semplore.model.KeywordCategory#setKeyword(java.lang.String)
     */
    public void setKeyword(String keyword)
    {
        this.keyword = keyword;
    }

    public String toString() {
        return "KEYWORD OF "+keyword;
    }
    
    public boolean equals(Object obj) {
        if (obj instanceof KeywordCategoryImpl) {
            return keyword == ((KeywordCategoryImpl)obj).keyword;
        }
        return false;
    }
    
    public int hashCode() {
        return keyword.hashCode();
    }
}
