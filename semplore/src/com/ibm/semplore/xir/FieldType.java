/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 *
 * $Id: FieldType.java,v 1.5 2008/09/07 06:16:28 lql Exp $
 */
package com.ibm.semplore.xir;

import com.ibm.semplore.model.Attribute;


/**
 * Enumeration on field types. All possible fields in the index
 * 	 are accessed from this class.
 * 
 * @author zhangjie
 *
 */
public class FieldType {
	
	/**
	 * Define the FieldType for {@link Attribute}.
	 * 
	 * @param attr
	 * @return
	 */
	public static FieldType toFieldType(String attr){
		return new FieldType(attr, FOR.Attribute);
	}
	
	private final String type;
	private final FOR f;
	private FieldType(String t, FOR f){	type = t; this.f = f; }
	
	/**
	 * @return The string representation of this field
	 */
	public String toString(){ return type; }
	/**
	 * Get which kind of index this field is designed for, see {@link FOR}
	 * 
	 * @return Which kind of index this field is designed for, see {@link FOR}
	 */
	public FOR forIndex(){ return f; }
	
	
	
	///////////////////////General Field Types///////////////////////////
	/**
	 * Field tag for id, should be stored field (not for search)
	 */
	public static final FieldType ID = new FieldType("id", FOR.All);
	/**
	 * Field tag for URI 
	 */
	public static final FieldType URI = new FieldType("uri", FOR.All);	
	/**
	 * Field tag for text, for keyword search
	 */
	public static final FieldType TEXT = new FieldType("text", FOR.All);
    /**
     * Field tag for text, for keyword search
     */
    public static final FieldType SUMMARY = new FieldType("summary", FOR.All);
    /**
     * Field tag for text, for keyword search
     */
    public static final FieldType LABEL = new FieldType("label", FOR.All);
	/**
	 * Field tag for categories for xfacet search.
	 */
    public static final FieldType CATEGORIES = new FieldType("categories", FOR.Instance);
    public static final FieldType RELATIONS = new FieldType("relations", FOR.Instance);
    public static final FieldType INVERSERELATIONS = new FieldType("inverserelations", FOR.Instance);
	
	///////////////////////Instance Index Field Types///////////////////////////
	/**
	 * Field tag for type-of-categories (a category that has this instance is a term of this field)
	 */
	public static final FieldType INSTANCE_TYPE_OF = new FieldType("type_of", FOR.Instance);
	/**
	 * Field tag for subject_of_triples, all triples with this instance as subject will be in this field
	 * 	 (in our implmentation, the relation of the triple is a term of this field, objects are positions)
	 */
	public static final FieldType INSTANCE_SUBJECT_OF = new FieldType("subject_of", FOR.Instance);	
    /**
     * Field tag for object_of_triples, all triples with this instance as object will be in this field
     *   (in our implmentation, the relation of the triple is a term of this field, subjects are positions)
     */
    public static final FieldType INSTANCE_OBJECT_OF = new FieldType("object_of", FOR.Instance);
    /**
     * r+o as term, s as doc
     */
    public static final FieldType INSTANCE_NO_POS_SUBJ_AS_DOC = new FieldType("no_pos_subj_as_doc", FOR.Instance);  
    /**
     * r+s as term, o as doc
     */
    public static final FieldType INSTANCE_NO_POS_OBJ_AS_DOC = new FieldType("no_pos_obj_as_doc", FOR.Instance);  
	
	
	///////////////////////Category Index Field Types///////////////////////////
	/**
	 * Field tag for super categories (a super category of this cateogry is a term of this field)
	 */
	public static final FieldType CATEGORY_SUB_OF = new FieldType("super_categories", FOR.Category);
    /**
     * Field tag for sub categories (a sub category of this cateogry is a term of this field)
     */
    public static final FieldType CATEGORY_SUPER_OF = new FieldType("sub_categories", FOR.Category);
	/**
	 * Field tag for root categories (a mocked string 'root' is the only possible term of this field if
	 * 	this Category is a root Category)
	 */
	public static final FieldType CATEGORY_ROOT = new FieldType("root_categories", FOR.Category);
	
	
	
	///////////////////////Relation Index Field Types///////////////////////////
	/**
	 * Field tag for super relations (a super relation of this relation is a term of this field)
	 */
	public static final FieldType RELATION_SUB_OF = new FieldType("super_relations", FOR.Relation);
    /**
     * Field tag for sub relations (a sub relation of this relation is a term of this field)
     */
    public static final FieldType RELATION_SUPER_OF = new FieldType("sub_relations", FOR.Relation);
	/**
	 * Field tag for root relations (a mocked string 'root' is the only possible term of this field if
	 * 	this relation is a root relation)
	 */
	public static final FieldType RELATION_ROOT = new FieldType("root_relations", FOR.Relation);
	
	
	
	///////////////////////Attribute Index Field Types///////////////////////////
	/**
	 * Field tag for super attributes (a super attribute of this attribute is a term of this field)
	 */
	public static final FieldType ATTRIBUTE_SUB_OF = new FieldType("super_attributes", FOR.Attribute);
	/**
	 * Field tag for root attributes (a mocked string 'root' is the only possible term of this field if
	 * 	this attribute is a root attribute)
	 */
	public static final FieldType ATTRIBUTE_ROOT = new FieldType("root_attributes", FOR.Attribute);
	/**
	 * Field tag for range type of this attribute (the range type (see {@link Attribute}) of this attribute is a term of this field)
	 * This field should be stored field that field value can be got by giving attribute 
	 */
	public static final FieldType ATTRIBUTE_RANGE_TYPE = new FieldType("attribute_range_type", FOR.Attribute);
	
	
	/**
	 * Enumeration on for which kind of index a {@link FieldType} is designed.
	 * 
	 * @author zhangjie
	 *
	 */
	public static final class FOR {
		
		private FOR(String f){}
		
		public static final FOR All = new FOR("all");
		public static final FOR Instance = new FOR("instance");
		public static final FOR Category = new FOR("category");
		public static final FOR Relation = new FOR("relation");
		public static final FOR Attribute = new FOR("attribute");
	}
}