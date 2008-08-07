/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 *
 * $Id: TermImpl.java,v 1.2 2007/04/18 06:55:33 lql Exp $
 */
package com.ibm.semplore.xir.impl;

import com.ibm.semplore.xir.FieldType;
import com.ibm.semplore.xir.Term;

/**
 * @author zhangjie
 *
 */
public class TermImpl implements Term {

	protected FieldType type;
	protected String str;
	
	protected TermImpl(String str, FieldType type){
		this.type = type;
		this.str = str;
	}
	
	/* (non-Javadoc)
	 * @see com.ibm.semplore.xir.Term#getFieldType()
	 */
	public FieldType getFieldType() {
		return type;
	}

	/* (non-Javadoc)
	 * @see com.ibm.semplore.xir.Term#getString()
	 */
	public String getString() {
		return str;
	}

}
