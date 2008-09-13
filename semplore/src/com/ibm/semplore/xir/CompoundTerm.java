/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 *
 * $Id: CompoundTerm.java,v 1.2 2007/04/18 06:55:33 lql Exp $
 */
package com.ibm.semplore.xir;


/**
 * A compound term over {@link Term}
 * 
 * @author zhangjie
 *
 */
public interface CompoundTerm extends Term {

	/**
	 * The compound type AND.
	 */
	public static final int TYPE_AND = 1;
	
	/**
	 * The compound type OR.
	 */
	public static final int TYPE_OR = 2;
	
	/**
	 * Returns the component of the CompoundTerm with given index.
	 * @param index
	 * @return
	 */
	public Term getTerm(int index);
		
	/**
	 * Returns the number of terms of this CompoundTerm.
	 * @return
	 */
	public int getSize();
	
	/**
	 * Returns the compound type.
	 * @return
	 */
	public int getCompoundType();

	/**
	 * Add a term to this compound term. 
	 * @param term
	 * @return
	 */
	public CompoundTerm addTerm(Term term);
}
