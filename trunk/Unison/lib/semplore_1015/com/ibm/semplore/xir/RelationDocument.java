/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 */
package com.ibm.semplore.xir;

import com.ibm.semplore.model.Relation;

/**
 * This interface builds a virtual document for a relation, based on its subrelations and superrelations. 
 * @author liu Qiaoling
 *
 */
public interface RelationDocument extends Document {
	
	/**
	 * Get sub relations of this relation
	 * 
	 * @return sub relations of this relation
	 */
	public Relation[] getSubRelations();
	
	/**
	 * Get super relations of this relation
	 * 
	 * @return super relations of this relation
	 */
	public Relation[] getSuperRelations();
	
	/**
	 * Get if this relation is a root relation
	 * 
	 * @return true if this relation is a root relation
	 */
	public boolean isRootRelation();
	
	/**
	 * Returns the relation corresponding to this document.
	 * @return
	 */
	public Relation getThisRelation();
	
}
