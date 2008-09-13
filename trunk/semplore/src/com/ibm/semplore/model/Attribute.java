/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 */
package com.ibm.semplore.model;

/**
 * This interface provides descriptive information about an attribute.
 * @author liu Qiaoling
 *
 */
public interface Attribute extends SchemaObject {
		
	/**
	 * the datatype of string
	 */
	public static int DATATYPE_STRING = 1;
	
	/**
	 * the datatype of int
	 */
	public static int DATATYPE_INT = 2;
	
	/**
	 * the datatype of double
	 */
	public static int DATATYPE_DOUBLE = 3;
	
	/**
	 * Returns the datatype of this attribute.
	 * @return
	 */
	public int getDatatype();
}
