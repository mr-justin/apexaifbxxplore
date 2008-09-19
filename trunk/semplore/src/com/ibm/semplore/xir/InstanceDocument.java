/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 */
package com.ibm.semplore.xir;

import java.util.ArrayList;
import java.util.LinkedList;

import com.ibm.semplore.model.Category;
import com.ibm.semplore.model.LocalCategoryList;
import com.ibm.semplore.model.LocalInstanceList;
import com.ibm.semplore.model.LocalRelationList;
import com.ibm.semplore.model.Relation;
import com.ibm.semplore.model.SchemaObject;
import com.ibm.semplore.xir.impl.AttributeValue;

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
	
	public ArrayList<Category> getCategories();
	/**
     * Returns the relations with this instance as subject
	 * @return The relations with this instance as subject
	 */
	public ArrayList<Relation> getRelationsGivenSubject();
	
    /**
     * Returns the relations with this instance as object
     * @return The relations with this instance as object
     */
    public ArrayList<Relation> getRelationsGivenObject();
    
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
	 * Returns the instance corresponding to this document.
	 * @return
	 */
	public SchemaObject getThisSchemaObject();
	
	public boolean checkThisSchemaObjectType(Class type);
	
	/**
	 * Return all the attributes in form of (String attribute, String value);
	 * @return
	 */
	public LinkedList<AttributeValue> getAttributes();
}
