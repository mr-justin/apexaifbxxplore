/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 *
 * $Id: AttributeKeywordCategoryImpl.java,v 1.1 2008/09/07 06:15:54 lql Exp $
 */
package com.ibm.semplore.model.impl;

import com.ibm.semplore.model.AttributeKeywordCategory;

/**
 * @author liu Qiaoling
 *
 */
public class AttributeKeywordCategoryImpl extends KeywordCategoryImpl implements AttributeKeywordCategory
{

    protected String attribute;    
    
    /**
     * @param keyword
     */
    protected AttributeKeywordCategoryImpl(String attribute, String keyword) {
    	super(keyword);
    	this.attribute = attribute;
    }
    
    public String toString() {
        return "ATTRIBUTE KEYWORD OF "+attribute+":"+keyword;
    }
    
    public boolean equals(Object obj) {
        if (obj instanceof AttributeKeywordCategoryImpl) {
            return (keyword.equals(((AttributeKeywordCategoryImpl)obj).keyword) && (attribute.equals(((AttributeKeywordCategoryImpl)obj).attribute)));
        }
        return false;
    }
    
    public int hashCode() {
        return keyword.hashCode();
    }

	public String getAttribute() {
		return attribute;
	}
}
