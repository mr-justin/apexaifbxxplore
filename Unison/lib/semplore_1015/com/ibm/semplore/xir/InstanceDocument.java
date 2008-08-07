/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 */
package com.ibm.semplore.xir;

import com.ibm.semplore.model.Attribute;
import com.ibm.semplore.model.Category;
import com.ibm.semplore.model.Instance;
import com.ibm.semplore.model.LiteralsOfProperty;
import com.ibm.semplore.model.LocalCategoryList;
import com.ibm.semplore.model.LocalInstanceList;
import com.ibm.semplore.model.LocalRelationList;
import com.ibm.semplore.model.Relation;

/**
 * This interface builds a virtual document for an instance, based on the categories it belongs to, its relations with other instances, and its attributes. 
 * @author liu Qiaoling
 *
 */
public interface InstanceDocument extends Document {
    
	/**
	 * Get the categories of this instance.
	 * 
	 * @return categories of this instance
	 */
	public LocalCategoryList getCategoriesWithID();
	public LocalRelationList getRelationsWithID();
	public LocalRelationList getInverseRelationsWithID();
	
	public Category[] getCategories();
	/**
     * Returns the relations with this instance as subject
	 * @return The relations with this instance as subject
	 */
	public Relation[] getRelationsGivenSubject();
	
    /**
     * Returns the relations with this instance as object
     * @return The relations with this instance as object
     */
    public Relation[] getRelationsGivenObject();
    
	/**
	 * Get the objects of a given relation with the current instance as subject.
	 * 
	 * @param rel
	 * @return a LocalInstanceList 
	 */
	public LocalInstanceList getObjects(Relation rel);
    
    /**
     * Get the subjects of a given relation with the current instance as object.
     * 
     * @param rel
     * @return a LocalInstanceList 
     */
    public LocalInstanceList getSubjects(Relation rel);
	
    /**
     * Get attributes that this instance has.
     * 
     * @return attributes that this instance has
     */
    public Attribute[] getAttributes();
    
    /**
     * Get the attribute values of the current instance.
     * @param attr
     */
    public String[] getValues(Attribute attr);
    
	/**
	 * Returns the instance corresponding to this document.
	 * @return
	 */
	public Instance getThisInstance();
	
    /**
     * Returns the literals of datatype-properties or annotation-properties of the current instance.
     * @param cat
     * @return
     */
    public LiteralsOfProperty[] getLiteralsOfProperties();

    /**
     * Get the subjects of the universal relation, which is the super relation of all other relations in the ontology.
     * @return
     */
    public LocalInstanceList getSubjectsOfUniversalRelation();
    
    /**
     * Get the objects of the universal relation, which is the super relation of all other relations in the ontology.
     * @return
     */
    public LocalInstanceList getObjectsOfUniversalRelation();
}
