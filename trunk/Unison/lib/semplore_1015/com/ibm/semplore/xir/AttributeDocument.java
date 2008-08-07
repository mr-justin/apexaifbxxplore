/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 */
package com.ibm.semplore.xir;

import com.ibm.semplore.model.Attribute;

/**
 * This interface builds a virtual document for an attribute. 
 * @author liu Qiaoling
 *
 */
public interface AttributeDocument extends Document {
	
	/**
	 * Returns the attribute corresponding to this document.
	 * @return
	 */
	public Attribute getThisAttribute();
	
}
