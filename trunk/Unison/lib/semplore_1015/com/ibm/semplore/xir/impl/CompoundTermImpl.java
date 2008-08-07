/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 *
 * $Id: CompoundTermImpl.java,v 1.2 2007/04/18 06:55:33 lql Exp $
 */
package com.ibm.semplore.xir.impl;

import java.util.ArrayList;

import com.ibm.semplore.xir.CompoundTerm;
import com.ibm.semplore.xir.FieldType;
import com.ibm.semplore.xir.Term;

/**
 * @author zhangjie
 *
 */
public class CompoundTermImpl implements CompoundTerm {

	private int type;
	private ArrayList termList;
	
	protected CompoundTermImpl(int type){
		if(type != CompoundTerm.TYPE_AND &&
				type != CompoundTerm.TYPE_OR )
			throw new IllegalArgumentException("Parameter for constructor of "+
					this.getClass()+" must be AND or OR.");
		this.type = type;
		termList = new ArrayList();
	}
	
	/* (non-Javadoc)
	 * @see com.ibm.semplore.xir.CompoundTerm#addTerm(com.ibm.semplore.xir.Term)
	 */
	public CompoundTerm addTerm(Term term) {
		termList.add(term);
		return this;
	}

	/* (non-Javadoc)
	 * @see com.ibm.semplore.xir.CompoundTerm#getCompoundType()
	 */
	public int getCompoundType() {
		return type;
	}

	/* (non-Javadoc)
	 * @see com.ibm.semplore.xir.CompoundTerm#getSize()
	 */
	public int getSize() {
		return termList.size();
	}

	/* (non-Javadoc)
	 * @see com.ibm.semplore.xir.CompoundTerm#getTerm(int)
	 */
	public Term getTerm(int index) {
		return (Term)termList.get(index);
	}

	/* (non-Javadoc)
	 * @see com.ibm.semplore.xir.Term#getFieldType()
	 */
	public FieldType getFieldType() {
		FieldType f = null;
		for(int i=0;i<termList.size();i++){
			Term t = getTerm(i);
			if(t.getFieldType()==null) return null;
			if(f == null) f = t.getFieldType();
			else if( !f.equals(t.getFieldType()) ) 
				return null;
		}
		return f;
	}

	/* (non-Javadoc)
	 * @see com.ibm.semplore.xir.Term#getString()
	 */
	public String getString() {
		throw new UnsupportedOperationException();
	}

}
