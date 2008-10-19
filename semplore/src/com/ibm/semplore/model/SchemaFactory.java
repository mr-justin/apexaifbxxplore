/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 */
package com.ibm.semplore.model;

/**
 * Factory interface for generating category, relation, instance and other
 * schema related objects.
 * 
 * @author liu Qiaoling
 * 
 */
public interface SchemaFactory {

    /**
     * Create a schema category based on given ID.
     * 
     * @param id
     * @return
     */
    public Category createCategory(long id);
    public Category createCategory(String uri);

    /**
     * Create the universal category which is comprised of all instances in the ontology.
     * @return
     */
    public Category createUniversalCategory();
    
    /**
     * Create an enumeration category;
     * 
     * @return
     */
    public EnumerationCategory createEnumerationCategory();

    /**
     * Create a keyword category based on given keyword.
     * 
     * @param keyword
     * @return
     */
    public KeywordCategory createKeywordCategory(String keyword);

    /**
     * Create a keyword category for an attribute based on given attribute and keyword.
     * @param attribute
     * @param keyword
     * @return
     */
    public KeywordCategory createAttributeKeywordCategory(String attribute, String keyword);
    
    /**
     * Create a value constraint category based on given attribute, lower limit
     * and upper limit.
     * 
     * @param attr
     * @param lowerLimit
     * @param isLowerLimitInclusive
     * @param UpperLimit
     * @param isUpperLimitInclusive
     * @return
     */
    public ValueConstraintCategory createValueConstraintCategory(
        Attribute attr, String lowerLimit, boolean isLowerLimitInclusive,
        String UpperLimit, boolean isUpperLimitInclusive);

    /**
     * Create a compound category based on given compound type, which may be AND
     * or OR.
     * 
     * @param compound_type
     * @return
     */
    public CompoundCategory createCompoundCategory(int compound_type);

    /**
     * Create a relation based on given ID.
     * 
     * @param id
     * @return
     */
    public Relation createRelation(long id);
    public Relation createRelation(String uri);
    
    /**
     * Create an inverse relation of the ID.
     * @param id
     * @return
     */
    public Relation createInverseRelation(long id);
    public Relation createInverseRelation(String uri);

    /**
     * Create the universal relation which is the super relation of all relations in the ontology.
     * 
     * @return
     */
    public Relation createUniversalRelation();
    
    /**
     * Create an attribute based on given ID and datatype.
     * 
     * @param ID
     * @param datatype
     * @return
     */
    public Attribute createAttribute(long ID, int datatype);
    public Attribute createAttribute(String uri, int datatype);

    /**
     * Create an instance based on given id.
     * 
     * @param URI
     * @return
     */
    public Instance createInstance(long id);
    public Instance createInstance(String uri);

    /**
     * Create a category-relation-expression of exact the form
     * [C1,R1,C2,R2,C3,R3,...].
     * 
     * @return
     */
    public CategoryRelationExp createCategoryRelationExp();

    /**
     * Create a graph in which nodes are categories and edges are relations.
     * @return
     */
    public CatRelGraph createCatRelGraph();
    
    /**
     * Create information of a schema object.
     * 
     * @param uri
     * @param label
     * @param summary
     * @param text
     * @return
     */
    public SchemaObjectInfo createSchemaObjectInfo(long id, String uri, String label,
        String summary, String text);

    /**
     * Create a local instance list.
     * 
     * @return
     */
    public LocalInstanceList createLocalInstanceList();

    /**
     * Create a property-literal pair.
     * 
     * @return
     */
    public LiteralsOfProperty createLiteralsOfProperty(String pro, String lit);

}
