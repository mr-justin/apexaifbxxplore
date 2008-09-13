/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 */
package com.ibm.semplore.model;

/**
 * This interface provides descriptive information about an expression of category and relation in exactly this form: [C1, R1, C2, R2, C3, R3,...], which can end with both C or R.
 * @author liu Qiaoling
 *
 */
public interface CategoryRelationExp extends CatRelConstraint {

	/**
	 * Append a category to the end of this expression.
	 * @param cat
	 */
	public CategoryRelationExp append(GeneralCategory cat);
	
	/**
	 * Append a relation to the end of this expression.
	 * @param rel
	 */
	public CategoryRelationExp append(Relation rel);
	
	/**
	 * Remove the last item from this expression, which may be a category C or a relation.
	 */
	public CategoryRelationExp removeLastItem();
    
    /**
     * Returns the object(category or relation) in the expression with given index.
     * @param index
     * @return
     */
    public Object get(int index);
    
    /**
     * Returns the size of the expression, that is the number of objects in it.
     * @return
     */
    public int size();
    
}
