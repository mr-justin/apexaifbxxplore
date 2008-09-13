/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 *
 * $Id: TermFactoryImpl.java,v 1.6 2008/09/12 10:18:17 xrsun Exp $
 */
package com.ibm.semplore.xir.impl;

import com.ibm.semplore.model.AttributeKeywordCategory;
import com.ibm.semplore.model.Category;
import com.ibm.semplore.model.CompoundCategory;
import com.ibm.semplore.model.EnumerationCategory;
import com.ibm.semplore.model.GeneralCategory;
import com.ibm.semplore.model.Instance;
import com.ibm.semplore.model.KeywordCategory;
import com.ibm.semplore.model.Relation;
import com.ibm.semplore.xir.CompoundTerm;
import com.ibm.semplore.xir.FieldType;
import com.ibm.semplore.xir.Term;
import com.ibm.semplore.xir.TermFactory;

/**
 * @author zhangjie
 *
 */
public class TermFactoryImpl implements TermFactory {

	private final static TermFactoryImpl instance;
	
	static{
		instance = new TermFactoryImpl();
	}
	
	public static TermFactory getInstance(){
		return instance;
	}
	
	/* (non-Javadoc)
	 * @see com.ibm.semplore.xir.TermFactory#createCompoundTerm(int)
	 */
	public CompoundTerm createCompoundTerm(int type) {
		return new CompoundTermImpl(type);
	}

	/* (non-Javadoc)
	 * @see com.ibm.semplore.xir.TermFactory#createTerm(java.lang.String, com.ibm.semplore.xir.FieldType)
	 */
	public Term createTerm(String termString, FieldType type) {
		return new TermImpl(termString, type);
	}

	/* (non-Javadoc)
	 * @see com.ibm.semplore.xir.TermFactory#createTermForInstances(com.ibm.semplore.model.GeneralCategory)
	 */
	public Term createTermForInstances(GeneralCategory cat) {
		if( cat instanceof Category )
			return createTerm( String.valueOf(((Category)cat).getIDofURI()), FieldType.INSTANCE_TYPE_OF );
		if (cat instanceof AttributeKeywordCategory)
			return createTerm( ((AttributeKeywordCategory)cat).getKeyword(), FieldType.toFieldType(((AttributeKeywordCategory)cat).getAttribute()) );
		if( cat instanceof KeywordCategory )
			return createTerm( ((KeywordCategory)cat).getKeyword(), FieldType.TEXT );
		if( cat instanceof CompoundCategory ){
			CompoundCategory comCat = (CompoundCategory)cat;
			int type;
			if(comCat.getCompoundType() == CompoundCategory.TYPE_AND) 
				type = CompoundTerm.TYPE_AND;
			else if(comCat.getCompoundType() == CompoundCategory.TYPE_OR) 
				type = CompoundTerm.TYPE_OR;
			else throw new RuntimeException("CompoundCategory type should be either AND or OR.");
			
			CompoundTerm term = createCompoundTerm(type);
            GeneralCategory[] cats = comCat.getComponentCategories();
			for(int i=0;i<cats.length;i++){
				term.addTerm( createTermForInstances(cats[i]) );
			}
			return term;
		}
        if (cat instanceof EnumerationCategory) {
            EnumerationCategory ecat = (EnumerationCategory)cat;
            Instance[] instances = ecat.getInstanceElements();
            CompoundTerm term = createCompoundTerm(CompoundTerm.TYPE_OR);
            for (int i=0; i<instances.length; i++) {
                term.addTerm(this.createTerm(String.valueOf(instances[i].getIDofURI()), FieldType.ID));                
            }
            return term;
        }
		//TODO ValueConstraintedCateogry remains to be implemented
		throw new RuntimeException("No implementation for the creation of Term from "+cat.getClass()+".");
	}

	/* (non-Javadoc)
	 * @see com.ibm.semplore.xir.TermFactory#createTermForKeywordOnAttributes(java.lang.String)
	 */
	public Term createTermForKeywordOnAttributes(String keyword) {
		return createTerm( keyword, FieldType.TEXT );
	}

	/* (non-Javadoc)
	 * @see com.ibm.semplore.xir.TermFactory#createTermForKeywordOnCateogries(java.lang.String)
	 */
	public Term createTermForKeywordOnCateogries(String keyword) {
		return createTerm( keyword, FieldType.TEXT );
	}

	/* (non-Javadoc)
	 * @see com.ibm.semplore.xir.TermFactory#createTermForKeywordOnRelations(java.lang.String)
	 */
	public Term createTermForKeywordOnRelations(String keyword) {
		return createTerm( keyword, FieldType.TEXT );
	}

	/* (non-Javadoc)
	 * @see com.ibm.semplore.xir.TermFactory#createTermForRootAttributes()
	 */
	public Term createTermForRootAttributes() {
		return createTerm( DefaultDocumentConverterForLucene_XFaceted.TERM_STR_FOR_ROOT, FieldType.ATTRIBUTE_ROOT );
	}

	/* (non-Javadoc)
	 * @see com.ibm.semplore.xir.TermFactory#createTermForRootCategories()
	 */
	public Term createTermForRootCategories() {
		return createTerm( DefaultDocumentConverterForLucene_XFaceted.TERM_STR_FOR_ROOT, FieldType.CATEGORY_ROOT );
	}

	/* (non-Javadoc)
	 * @see com.ibm.semplore.xir.TermFactory#createTermForRootRelations()
	 */
	public Term createTermForRootRelations() {
		return createTerm( DefaultDocumentConverterForLucene_XFaceted.TERM_STR_FOR_ROOT, FieldType.RELATION_ROOT );
	}

	/* (non-Javadoc)
	 * @see com.ibm.semplore.xir.TermFactory#createTermForSubCategories(com.ibm.semplore.model.Category)
	 */
	public Term createTermForSubCategories(Category cat) {
		return createTerm( String.valueOf(cat.getIDofURI()), FieldType.CATEGORY_SUB_OF );
	}

	/* (non-Javadoc)
	 * @see com.ibm.semplore.xir.TermFactory#createTermForSubRelations(com.ibm.semplore.model.Relation)
	 */
	public Term createTermForSubRelations(Relation rel) {
		return createTerm( String.valueOf(rel.getIDofURI()), FieldType.RELATION_SUB_OF );
	}

	/* (non-Javadoc)
	 * @see com.ibm.semplore.xir.TermFactory#createTermForSubjects(com.ibm.semplore.model.Relation)
	 */
	public Term createTermForSubjects(Relation rel) {
		return createTerm( String.valueOf(rel.getIDofURI()), FieldType.INSTANCE_SUBJECT_OF );
	}

	/* (non-Javadoc)
	 * @see com.ibm.semplore.xir.TermFactory#createTermForObjects(com.ibm.semplore.model.Relation)
	 */
	public Term createTermForObjects(Relation rel) {
		return createTerm( String.valueOf(rel.getIDofURI()), FieldType.INSTANCE_OBJECT_OF );
	}

    public Term createTermForSuperCategories(Category cat)
    {
        return createTerm( String.valueOf(cat.getIDofURI()), FieldType.CATEGORY_SUPER_OF );
    }

    public Term createTermForSuperRelations(Relation rel)
    {
        return createTerm( String.valueOf(rel.getIDofURI()), FieldType.RELATION_SUPER_OF );
    }
}
