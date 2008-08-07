/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 */
package com.ibm.semplore.model;

/**
 * This interface provides descriptive information about a keyword category, which is a virtual category comprised of instances whose text descriptions match with the keyword.
 * @author liu Qiaoling
 *
 */
public interface KeywordCategory extends GeneralCategory {
	
	/**
	 * Returns the keyword of this category.
	 * @return
	 */
	public String getKeyword();

	/**
	 * Set the keyword of this category.
	 * @param keyword
	 */
	public void setKeyword(String keyword);
	
}
