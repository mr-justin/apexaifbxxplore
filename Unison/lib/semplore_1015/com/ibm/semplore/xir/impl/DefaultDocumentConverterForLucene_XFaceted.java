/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 *
 * $Id: DefaultDocumentConverterForLucene.java,v 1.2 2007/04/18 06:55:34 lql Exp $
 */
package com.ibm.semplore.xir.impl;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import com.ibm.semplore.model.Category;
import com.ibm.semplore.model.LocalCategoryList;
import com.ibm.semplore.model.LocalInstanceList;
import com.ibm.semplore.model.LocalRelationList;
import com.ibm.semplore.model.Relation;
import com.ibm.semplore.xir.AttributeDocument;
import com.ibm.semplore.xir.CategoryDocument;
import com.ibm.semplore.xir.FieldType;
import com.ibm.semplore.xir.InstanceDocument;
import com.ibm.semplore.xir.RelationDocument;

/**
 * 
 * @author zhangjie
 *
 */
public class DefaultDocumentConverterForLucene_XFaceted implements
		IDocumentConverterForLucene {
	
	/**
	 * Use this string as term for root SchemaObject
	 */
	protected static final String TERM_STR_FOR_ROOT = "root";

	/* (non-Javadoc)
	 * @see com.ibm.semplore.xir.impl.IDocumentConverterForLucene#convert(com.ibm.semplore.xir.InstanceDocument)
	 */
	public Document convert(InstanceDocument insDoc) {
		
		Document doc = new Document();
		
		// Store id of document
		doc.add(new Field(FieldType.ID.toString(), insDoc.getThisInstance().getIDofURI(), Field.Store.YES, Field.Index.TOKENIZED));//Why tokenized?
		
		// Store uri of document
		doc.add(new Field(FieldType.URI.toString(), insDoc.getThisInstance().getURI(), Field.Store.COMPRESS, Field.Index.NO));
		
        // Store label of document
        doc.add(new Field(FieldType.LABEL.toString(), insDoc.getSchemaObjectInfo().getLabel(), Field.Store.COMPRESS, Field.Index.NO));
        
        // Store summary of document
        doc.add(new Field(FieldType.SUMMARY.toString(), insDoc.getSchemaObjectInfo().getSummary(), Field.Store.COMPRESS, Field.Index.NO));

        // build index taking text as field value: for keyword search
  		doc.add(new Field(FieldType.TEXT.toString(), insDoc.getSchemaObjectInfo().getTextDescription().toLowerCase(), Field.Store.COMPRESS, Field.Index.TOKENIZED));
		
		// Index category: class hierarchy
		Category[] cates = insDoc.getCategories();
		StringBuffer buffer = new StringBuffer();
    	for(int j=0;j<cates.length;j++)
    		buffer.append(cates[j].getIDofURI()+" ");
		doc.add(new Field(FieldType.INSTANCE_TYPE_OF.toString(), buffer.toString(), Field.Store.NO, Field.Index.TOKENIZED));
		
		
		/**
		 * ***********************Modified by QM************************
		 * */
		//build index taking sub{"Categories"+category,"Categories"+category
		buffer = new StringBuffer();
		LocalCategoryList cat = insDoc.getCategoriesWithID();
		for (int j = 0; j<cat.size(); j++) 
				buffer.append(SemploreAnalyzer.getTermString(FieldType.CATEGORIES.toString(), String.valueOf(cat.getLocalID(j))) + " ");
		doc.add(new Field(FieldType.CATEGORIES.toString(), buffer.toString(), Field.Store.NO, Field.Index.TOKENIZED));
		
		
//		build index taking sub{"Relations"+relation,"Relations"+relation
		buffer = new StringBuffer();
		LocalRelationList rel = insDoc.getRelationsWithID();
		for (int j = 0; j<rel.size(); j++) 
				buffer.append(SemploreAnalyzer.getTermString(FieldType.RELATIONS.toString(), String.valueOf(rel.getLocalID(j))) + " ");
		doc.add(new Field(FieldType.RELATIONS.toString(), buffer.toString(), Field.Store.NO, Field.Index.TOKENIZED));
		
//		build index taking sub{"InverseRelations"+inverseRelation,"..Relations"+..relation

		
		buffer = new StringBuffer();
		rel = insDoc.getInverseRelationsWithID();
		for (int j = 0; j<rel.size(); j++) 
				buffer.append(SemploreAnalyzer.getTermString(FieldType.INVERSERELATIONS.toString(), String.valueOf(rel.getLocalID(j))) + " ");
		doc.add(new Field(FieldType.INVERSERELATIONS.toString(), buffer.toString(), Field.Store.NO, Field.Index.TOKENIZED));
		
		
		/**
		 * ***********************Modified by QM************************
		 * */
		
		
		// build index taking 'sub{r+objRid, r+objRid}' as field value 
		Relation[] rels = insDoc.getRelationsGivenSubject();
		LocalInstanceList list = null;
		buffer = new StringBuffer();
		for(int p=0;p<rels.length;p++){		
			list = insDoc.getObjects(rels[p]);
			for (int j=0; j<list.size(); j++) 
				buffer.append(
						SemploreAnalyzer.getTermString( rels[p].getIDofURI(), String.valueOf(list.getLocalID(j))) + " ");		
		}
//        Relation universalRel = SchemaFactoryImpl.getInstance().createUniversalRelation();
//        list = insDoc.getObjectsOfUniversalRelation();
//        for (int j=0; j<list.size(); j++)
//            buffer.append(
//                    SemploreAnalyzer.getTermString( universalRel.getIDofURI(), String.valueOf(list.getLocalID(j))) + " ");      
		doc.add(new Field(FieldType.INSTANCE_SUBJECT_OF.toString(), buffer.toString(), Field.Store.NO, Field.Index.TOKENIZED));
		
		
		// build index taking 'obj{r+subjRid, r+subjRid}' as field value 
		rels = insDoc.getRelationsGivenObject();
		buffer = new StringBuffer();
		for(int p=0;p<rels.length;p++){			
			list = insDoc.getSubjects(rels[p]);			
			for(int j=0;j<list.size();j++) {
				buffer.append(
						SemploreAnalyzer.getTermString( rels[p].getIDofURI(), String.valueOf(list.getLocalID(j))) + " ");
            }
		}
//        list = insDoc.getSubjectsOfUniversalRelation();
//        for (int j=0; j<list.size(); j++)
//            buffer.append(
//                    SemploreAnalyzer.getTermString( universalRel.getIDofURI(), String.valueOf(list.getLocalID(j))) + " ");      
		doc.add(new Field(FieldType.INSTANCE_OBJECT_OF.toString(), buffer.toString(), Field.Store.NO, Field.Index.TOKENIZED));
		
//		// build index for each attributes: each as a field, values as terms
//		Attribute[] attrs = insDoc.getAttributes();		
//		for(int i=0;i<attrs.length;i++){
//			String[] values = insDoc.getValues(attrs[i]);
//			buffer = new StringBuffer();
//			for(int j=0;j<values.length;j++)
//				buffer.append(values[j]+" ");
//			doc.add(new Field(FieldType.toFieldType(attrs[i]).toString(), buffer.toString(), Field.Store.NO, Field.Index.TOKENIZED));
//		}
		
		return doc;
	}

	/* (non-Javadoc)
	 * @see com.ibm.semplore.xir.impl.IDocumentConverterForLucene#convert(com.ibm.semplore.xir.RelationDocument)
	 */
	public Document convert(RelationDocument relDoc) {
		
		Document doc = new Document();
		
		// Store id of document
		doc.add(new Field(FieldType.ID.toString(), relDoc.getThisRelation().getIDofURI(), Field.Store.YES, Field.Index.NO));
		
		// Store uri of document
		doc.add(new Field(FieldType.URI.toString(), relDoc.getThisRelation().getURI(), Field.Store.COMPRESS, Field.Index.NO));
		
        // Store label of document
        doc.add(new Field(FieldType.LABEL.toString(), relDoc.getSchemaObjectInfo().getLabel(), Field.Store.COMPRESS, Field.Index.NO));
        
        // Store summary of document
        doc.add(new Field(FieldType.SUMMARY.toString(), relDoc.getSchemaObjectInfo().getSummary(), Field.Store.COMPRESS, Field.Index.NO));

		// build index taking text as field value: for keyword search
		doc.add(new Field(FieldType.TEXT.toString(), relDoc.getSchemaObjectInfo().getTextDescription().toLowerCase(), Field.Store.COMPRESS, Field.Index.TOKENIZED));
		
		// Index super relations: super relation as term
		Relation[] rels = relDoc.getSuperRelations();
		StringBuffer buffer = new StringBuffer();
    	for(int j=0;j<rels.length;j++)
    		buffer.append(rels[j].getIDofURI()+" ");
		doc.add(new Field(FieldType.RELATION_SUB_OF.toString(), buffer.toString(), Field.Store.NO, Field.Index.TOKENIZED));
		
        // Index sub relations: sub relation as term
        rels = relDoc.getSubRelations();
        buffer = new StringBuffer();
        for(int j=0;j<rels.length;j++)
            buffer.append(rels[j].getIDofURI()+" ");
        doc.add(new Field(FieldType.RELATION_SUPER_OF.toString(), buffer.toString(), Field.Store.NO, Field.Index.TOKENIZED));
        
		// Index this relation if it is a root relation
		if(relDoc.isRootRelation()){
			doc.add(new Field(FieldType.RELATION_ROOT.toString(),TERM_STR_FOR_ROOT, Field.Store.NO, Field.Index.TOKENIZED));
		}
		
		return doc;
	}

	/* (non-Javadoc)
	 * @see com.ibm.semplore.xir.impl.IDocumentConverterForLucene#convert(com.ibm.semplore.xir.CategoryDocument)
	 */
	public Document convert(CategoryDocument catDoc) {
		
		Document doc = new Document();
		
        // Store id of document
        doc.add(new Field(FieldType.ID.toString(), catDoc.getThisCategory().getIDofURI(), Field.Store.YES, Field.Index.NO));
        
        // Store uri of document
        doc.add(new Field(FieldType.URI.toString(), catDoc.getThisCategory().getURI(), Field.Store.COMPRESS, Field.Index.NO));
        
        // Store label of document
        doc.add(new Field(FieldType.LABEL.toString(), catDoc.getSchemaObjectInfo().getLabel(), Field.Store.COMPRESS, Field.Index.NO));
        
        // Store summary of document
        doc.add(new Field(FieldType.SUMMARY.toString(), catDoc.getSchemaObjectInfo().getSummary(), Field.Store.COMPRESS, Field.Index.NO));

        // build index taking text as field value: for keyword search
        doc.add(new Field(FieldType.TEXT.toString(), catDoc.getSchemaObjectInfo().getTextDescription().toLowerCase(), Field.Store.COMPRESS, Field.Index.TOKENIZED));
        
		// Index super categories: super category as term
		Category[] cats = catDoc.getSuperCateogries();
		StringBuffer buffer = new StringBuffer();
    	for(int j=0;j<cats.length;j++)
    		buffer.append(cats[j].getIDofURI()+" ");
		doc.add(new Field(FieldType.CATEGORY_SUB_OF.toString(), buffer.toString(), Field.Store.NO, Field.Index.TOKENIZED));
		
        // Index sub categories: sub category as term
        cats = catDoc.getSubCateogries();
        buffer = new StringBuffer();
        for(int j=0;j<cats.length;j++)
            buffer.append(cats[j].getIDofURI()+" ");
        doc.add(new Field(FieldType.CATEGORY_SUPER_OF.toString(), buffer.toString(), Field.Store.NO, Field.Index.TOKENIZED));
        
		// Index this category if it is a root category
		if(catDoc.isRootCategory()){
			doc.add(new Field(FieldType.CATEGORY_ROOT.toString(),TERM_STR_FOR_ROOT, Field.Store.NO, Field.Index.TOKENIZED));
		}
		
		return doc;
	}

	/* (non-Javadoc)
	 * @see com.ibm.semplore.xir.impl.IDocumentConverterForLucene#convert(com.ibm.semplore.xir.AttributeDocument)
	 */
	public Document convert(AttributeDocument attrDoc) {
		
		Document doc = new Document();
		
		// Store id of document
		doc.add(new Field(FieldType.ID.toString(), attrDoc.getThisAttribute().getIDofURI(), Field.Store.YES, Field.Index.NO));
		
        // Store uri of document
        doc.add(new Field(FieldType.URI.toString(), attrDoc.getThisAttribute().getURI(), Field.Store.COMPRESS, Field.Index.UN_TOKENIZED));
        
        // Store label of document
        doc.add(new Field(FieldType.LABEL.toString(), attrDoc.getSchemaObjectInfo().getLabel(), Field.Store.COMPRESS, Field.Index.NO));
        
        // Store summary of document
        doc.add(new Field(FieldType.SUMMARY.toString(), attrDoc.getSchemaObjectInfo().getSummary(), Field.Store.COMPRESS, Field.Index.NO));

        // build index taking text as field value: for keyword search
        doc.add(new Field(FieldType.TEXT.toString(), attrDoc.getSchemaObjectInfo().getTextDescription().toLowerCase(), Field.Store.COMPRESS, Field.Index.TOKENIZED));
        
		return doc;
	}

}
