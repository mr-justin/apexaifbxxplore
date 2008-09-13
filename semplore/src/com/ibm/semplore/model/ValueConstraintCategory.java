/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 */
package com.ibm.semplore.model;

/**
 * This interface provides descriptive information about a value constraint category, which is a virtual category comprised of instances whose attribute values match with the constraint.
 * @author liu Qiaoling
 *
 */
public interface ValueConstraintCategory extends GeneralCategory {

	/**
	 * Returns the attribute of this constraint.
	 * @return
	 */
	public Attribute getAttribute();
	
	/**
	 * Returns the lower limit of this constraint.
	 * @return
	 */
	public String getLowerLimit();
	
	/**
	 * Returns the upper limit of this constraint.
	 * @return
	 */
	public String getUpperLimit();
	
	/**
	 * Returns whether the lower limit is inclusive.
	 * @return
	 */
	public boolean isLowerLimitInclusive();
	
	/**
	 * Returns whether the upper limit is inclusive.
	 * @return
	 */
	public boolean isUpperLimitInclusive();
	
	/**
	 * Set the attribute, lower limit and upper limit of this constraint.
	 * @param attr
	 * @param lowerLimit
	 * @param isLowerLimitInclusive
	 * @param UpperLimit
	 * @param isUpperLimitInclusive
	 */
	public void set(Attribute attr, String lowerLimit, boolean isLowerLimitInclusive, String upperLimit, boolean isUpperLimitInclusive);
	
}
