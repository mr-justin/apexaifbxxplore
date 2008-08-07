/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 */
package com.ibm.semplore.xir;

import com.ibm.semplore.model.Category;

/**
 * This interface builds a virtual document for a category, based on its subcategories and supercategories. 
 * @author liu Qiaoling
 *
 */
public interface CategoryDocument extends Document {

	/**
	 * Get sub categories of this category
	 * 
	 * @return sub categories of this category
	 */
	public Category[] getSubCateogries();
	
	/**
	 * Get super categories of this category
	 * 
	 * @return super categories of this category
	 */
	public Category[] getSuperCateogries();
	
	/**
	 * Get if this cateogry is a root category
	 * 
	 * @return true if this category is a root category
	 */
	public boolean isRootCategory();
	
	/**
	 * Returns the category corresponding to this document.
	 * @return
	 */
	public Category getThisCategory();
}
