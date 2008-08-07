/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 *
 * $Id: TermFactory.java,v 1.2 2007/04/18 06:55:33 lql Exp $
 */
package com.ibm.semplore.xir;

import com.ibm.semplore.model.Category;
import com.ibm.semplore.model.GeneralCategory;
import com.ibm.semplore.model.Relation;

/**
 * Term factory produces terms given specified object and purpose of this term.
 * 
 * @author zhangjie
 *
 */
public interface TermFactory {

	/**
	 * Create a Term by a {@link GeneralCategory} 
	 * 	that can be used for querying instances of this {@link GeneralCategory}
	 * 
	 * @param cat
	 * @return
	 */
	public Term createTermForInstances(GeneralCategory cat);
	
	/**
	 * Create a Term by a {@link Category}
	 * 	that can be used for querying sub cateogories of this {@link Category}
	 * 
	 * @param cat
	 * @return
	 */
	public Term createTermForSubCategories(Category cat);
	
    /**
     * Create a Term by a {@link Category}
     *  that can be used for querying super cateogories of this {@link Category}
     * 
     * @param cat
     * @return
     */
    public Term createTermForSuperCategories(Category cat);

    /**
	 * Create a Term by a {@link Relation}
	 * 	that can be used for querying sub relations of this {@link Relation}
	 * 
	 * @param rel
	 * @return
	 */
	public Term createTermForSubRelations(Relation rel);
	
    /**
     * Create a Term by a {@link Relation}
     *  that can be used for querying super relations of this {@link Relation}
     * 
     * @param rel
     * @return
     */
    public Term createTermForSuperRelations(Relation rel);
    
	/**
	 * Create a Term by a {@link Relation}
	 * 	that can be used for querying subjects of this {@link Relation}
	 * 
	 * @param rel
	 * @return
	 */
	public Term createTermForSubjects(Relation rel);
	
	/**
	 * Create a Term by a {@link Relation}
	 * 	that can be used for querying objects of this {@link Relation}
	 * 
	 * @param rel
	 * @return
	 */
	public Term createTermForObjects(Relation rel);
	
	/**
	 * Create a Term that can be used for querying root categories
	 * 
	 * @return
	 */
	public Term createTermForRootCategories();
	
	/**
	 * Create a Term that can be used for querying root relations
	 * 
	 * @return
	 */
	public Term createTermForRootRelations();
	
	/**
	 * Create a Term that can be used for querying root attributes
	 * 
	 * @return
	 */
	public Term createTermForRootAttributes();
	
	/**
	 * Create a Term that can be used for querying matching cateogries 
	 * 	giving a keyword
	 * 
	 * @param keyword
	 * @return
	 */
	public Term createTermForKeywordOnCateogries(String keyword);
	
	/**
	 * Create a Term that can be used for querying matching relations 
	 * 	giving a keyword
	 * 
	 * @param keyword
	 * @return
	 */
	public Term createTermForKeywordOnRelations(String keyword);
	
	/**
	 * Create a Term that can be used for querying matching relations 
	 * 	giving a keyword
	 * 
	 * @param keyword
	 * @return
	 */
	public Term createTermForKeywordOnAttributes(String keyword);
	
	/**
	 * Advanced user only.
	 * 
	 * General create method for this factory. 
	 * Create a Term by a raw String and {@link FieldType}
	 * 
	 * CAUTION:
	 * 	If NO suitable create interface available, 
	 * 	and only if user KNOWS the term string representation 
	 * 	in the underlying indexing system and corresponding 
	 *  field type, user can use this interface instead.
	 * 
	 * @param termString the string representation of this term
	 * @param type field type of this term, see {@link FieldType}
	 * @return
	 */
	public Term createTerm(String termString, FieldType type);
	
	/**
	 * Create a empty CompoundTerm.
	 * 
	 * @param type Can be either {@link CompoundTerm.TYPE_AND} 
	 * 			or {@link CompoundTerm.TYPE_OR}  
	 * @return
	 */
	public CompoundTerm createCompoundTerm(int type);
}
