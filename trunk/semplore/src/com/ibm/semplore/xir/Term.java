/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 *
 * $Id: Term.java,v 1.2 2007/04/18 06:55:33 lql Exp $
 */
package com.ibm.semplore.xir;


/**
 * General interface representing a term in the inverted index system.
 * A Term can be throw to {@link IndexReader} for query.
 * A Term contains a string representation and field information.
 * A Term can only be created by {@link TermFactory}.
 * 
 * @author zhangjie
 *
 */
public interface Term {

	/**
	 * Get the string representation of this term.
	 *  
	 * If this instance is a CompoundTerm, exception will be thrown.
	 * 
	 * @return The string representation of this term
	 */
	public String getString();
	
	/**
	 * Return the FieldType of this term.
	 * 
	 * If this term is a CompoundTerm, and FieldType of all terms in this compound term are the same, return it. 
	 * 	Otherwise, return null.
	 * 
	 * @return The field type of this term
	 */
	public FieldType getFieldType();
}
