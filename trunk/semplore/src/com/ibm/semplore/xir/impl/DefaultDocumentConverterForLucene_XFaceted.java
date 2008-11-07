/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 *
 * $Id: DefaultDocumentConverterForLucene_XFaceted.java,v 1.6 2008/09/12 15:37:02 xrsun Exp $
 */
package com.ibm.semplore.xir.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import com.ibm.semplore.model.Category;
import com.ibm.semplore.model.Instance;
import com.ibm.semplore.model.Relation;
import com.ibm.semplore.xir.FieldType;
import com.ibm.semplore.xir.InstanceDocument;

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.semplore.xir.impl.IDocumentConverterForLucene#convert(com.ibm.semplore.xir.InstanceDocument)
	 */
	public Document convert(InstanceDocument insDoc) {

		Document doc = new Document();

		// Store id of document
		doc.add(new Field(FieldType.ID.toString(), String.valueOf(insDoc
				.getThisSchemaObject().getIDofURI()), Field.Store.COMPRESS,
				Field.Index.UN_TOKENIZED));

		// Store uri of document
		if (insDoc.getURI() == null) {
			System.err.println("No URI for "
					+ insDoc.getThisSchemaObject().getIDofURI());
			doc.add(new Field(FieldType.URI.toString(), "null",
					Field.Store.COMPRESS, Field.Index.NO));
//			return doc;
		} else {
			doc.add(new Field(FieldType.URI.toString(), insDoc.getURI(),
					Field.Store.COMPRESS, Field.Index.NO));
		}

		// Store label of document
		Field field = new Field(FieldType.LABEL.toString(), insDoc
				.getSchemaObjectInfo().getLabel(), Field.Store.COMPRESS,
				Field.Index.TOKENIZED);
		field.setBoost(5.0f);
		doc.add(field);

		// give boost to this document, according to triple count
		doc.setBoost((float) Math.log(insDoc.getTripleCount()+2));

		// build index of attributes, term=attribute###value
		LinkedList<AttributeValue> attrVals = insDoc.getAttributes();
		doc.add(new Field(FieldType.ATTRIBUTE_VALUE.toString(),
				new SemploreAnalyzer().tokenStream(
						FieldType.ATTRIBUTE_VALUE, new SemploreTermReader4Attributes(
								attrVals))));

		// build index of text, term=value1+value2+...(tokenized)
		StringBuffer text = new StringBuffer();
		for (AttributeValue attrVal:attrVals) {
			text.append(attrVal.getValue()+" ");
		}
		// ... and label
		text.append(insDoc.getSchemaObjectInfo().getLabel());		
		doc.add(new Field(FieldType.TEXT.toString(), text.toString(), Field.Store.NO,
				Field.Index.TOKENIZED));

		
		if (insDoc.checkThisSchemaObjectType(Instance.class)) {
			// Index category: class hierarchy
			ArrayList<Category> cates = insDoc.getCategories();
			doc.add(new Field(FieldType.INSTANCE_TYPE_OF.toString(),
					new SemploreAnalyzer().tokenStream(
							FieldType.INSTANCE_TYPE_OF, new SemploreTermReader(
									cates))));

			// build index taking pred="Categories(facet)", obj=category local
			// id
			doc.add(new Field(FieldType.CATEGORIES.toString(),
					new SemploreAnalyzer().tokenStream(FieldType.CATEGORIES,
							new SemploreTermPositionReader(null, insDoc,
									FieldType.CATEGORIES))));

			// build index taking pred="Relations(facet)", obj=relation local id
			doc.add(new Field(FieldType.RELATIONS.toString(),
					new SemploreAnalyzer().tokenStream(FieldType.RELATIONS,
							new SemploreTermPositionReader(null, insDoc,
									FieldType.RELATIONS))));

			// build index taking pred="InverseRelations(facet)",
			// obj=inverseRelation local
			// id
			doc.add(new Field(FieldType.INVERSERELATIONS.toString(),
					new SemploreAnalyzer().tokenStream(
							FieldType.INVERSERELATIONS,
							new SemploreTermPositionReader(null, insDoc,
									FieldType.INVERSERELATIONS))));

			// build index taking pred=relation, obj=obj local id
			ArrayList<Relation> rels = insDoc.getRelationsGivenSubject();
			doc.add(new Field(FieldType.INSTANCE_SUBJECT_OF.toString(),
					new SemploreAnalyzer().tokenStream(
							FieldType.INSTANCE_SUBJECT_OF,
							new SemploreTermPositionReader(rels, insDoc,
									FieldType.INSTANCE_SUBJECT_OF))));

			// build index taking pred=inverseRelation, obj=subj local id
			rels = insDoc.getRelationsGivenObject();
			doc.add(new Field(FieldType.INSTANCE_OBJECT_OF.toString(),
					new SemploreAnalyzer().tokenStream(
							FieldType.INSTANCE_OBJECT_OF,
							new SemploreTermPositionReader(rels, insDoc,
									FieldType.INSTANCE_OBJECT_OF))));

		}
		if (insDoc.checkThisSchemaObjectType(Category.class)) {
			doc.add(new Field(FieldType.CATEGORY_ROOT.toString(),
					TERM_STR_FOR_ROOT, Field.Store.NO, Field.Index.TOKENIZED));
		}
		if (insDoc.checkThisSchemaObjectType(Relation.class)) {
			doc.add(new Field(FieldType.RELATION_ROOT.toString(),
					TERM_STR_FOR_ROOT, Field.Store.NO, Field.Index.TOKENIZED));
		}
		return doc;
	}
}
