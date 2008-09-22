/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 */
package com.ibm.semplore.model;

/**
 * This interface provides descriptive information about a compound category, which may be a conjunction or disjunction of several categories.
 * @author liu Qiaoling
 *
 */
public interface CompoundCategory extends GeneralCategory {

	/**
	 * The compound type AND for conjunction of two categories.
	 */
	public static final int TYPE_AND = 1;
	
	/**
	 * The compound type OR for disjunction of two categories.
	 */
	public static final int TYPE_OR = 2;
	
	/**
	 * Returns the components of the compound category.
	 * @return
	 */
	public GeneralCategory[] getComponentCategories();
		
	/**
	 * Returns the number of components of this compound category.
	 * @return
	 */
	public int size();
	
	/**
	 * Returns the compound type.
	 * @return
	 */
	public int getCompoundType();

	/**
	 * Add a component to this compound category. 
	 * @param cat
	 * @return
	 */
	public CompoundCategory addComponentCategory(GeneralCategory cat);
	
	/**
	 * Remove a component.
	 * @param cat
	 * @return
	 */
	public CompoundCategory removeComponentCategory(GeneralCategory cat);
	
}
