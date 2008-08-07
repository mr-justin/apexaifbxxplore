/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 */
package com.ibm.semplore.imports;

import com.ibm.semplore.model.Attribute;
import com.ibm.semplore.model.Category;
import com.ibm.semplore.model.LiteralsOfProperty;
import com.ibm.semplore.model.Relation;

/**
 * OntologyRepository provides an interface to obtain the whole ontology(TBox+ABox). 
 * @author liu Qiaoling
 *
 */
public interface OntologyRepository {
	
	//////////////////////TBox/////////////////////////    
	/**
	 * Returns the size of the category set.
	 * @return
	 */
	public int getCategorySize();
	
	/**
	 * Returns the size of the relation set.
	 * @return
	 */
	public int getRelationSize();
	
    /**
     * Returns the size of the attribute set.
     * @return
     */
    public int getAttributeSize();
    
	/**
	 * Returns all the categories in the ontology.
	 * @return
	 */
	public Category[] getCategories();
	
	/**
	 * Returns all the super categories of the given category. 
	 * @param son
	 * @return
	 */
	public Category[] getSuperCategories(Category son);
	
	/**
	 * Returns all the sub categories of the given category. 
	 * @param parent
	 * @return
	 */
	public Category[] getSubCategories(Category parent);
	
	/**
	 * Returns all the relations in the ontology.
	 * @return
	 */
	public Relation[] getRelations();
	
	/**
	 * Returns all the superrelations of the given relation.
	 * @param son
	 * @return
	 */
	public Relation[] getSuperRelations(Relation son);
	
	/**
	 * Returns all the subrelations of the given relation.
	 * @param parent
	 * @return
	 */
	public Relation[] getSubRelations(Relation parent);
	    
    /**
     * Returns all the attributes in the ontology.
     * @return
     */
    public Attribute[] getAttributes();
    
	//////////////////////ABox/////////////////////////
	/**
	 * Returns the size of the instance set.
	 * @return
	 */
	public int getInstanceSize();
	
    /**
     * Returns the size of relation triple set.
     * @return
     */
    public int getTripleSize();
    
    /**
     * Returns the size of rdf:type triple set.
     * @return
     */
    public int getInstanceOfTripleSize();

    /**
     * Returns the literals of annotation properties of given category.
     * @param cat
     * @return
     */
    public LiteralsOfProperty[] getLiteralsOfProperties(Category cat);

    /**
     * Returns the literals of annotation properties of given relation.
     * @param rel
     * @return
     */
    public LiteralsOfProperty[] getLiteralsOfProperties(Relation rel);
    
}
