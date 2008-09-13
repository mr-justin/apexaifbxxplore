/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 *
 * $Id: SchemaFactoryImpl.java,v 1.5 2008/09/07 06:15:54 lql Exp $
 */
package com.ibm.semplore.model.impl;

import com.ibm.semplore.model.Attribute;
import com.ibm.semplore.model.CatRelGraph;
import com.ibm.semplore.model.Category;
import com.ibm.semplore.model.CategoryRelationExp;
import com.ibm.semplore.model.CompoundCategory;
import com.ibm.semplore.model.EnumerationCategory;
import com.ibm.semplore.model.Instance;
import com.ibm.semplore.model.KeywordCategory;
import com.ibm.semplore.model.LiteralsOfProperty;
import com.ibm.semplore.model.LocalInstanceList;
import com.ibm.semplore.model.Relation;
import com.ibm.semplore.model.SchemaFactory;
import com.ibm.semplore.model.SchemaObjectInfo;
import com.ibm.semplore.model.ValueConstraintCategory;
import com.ibm.semplore.util.Md5_BloomFilter_64bit;

/**
 * @author liu Qiaoling
 *
 */
public class SchemaFactoryImpl implements SchemaFactory
{
    /**
     * public call not allowed
     */
    private SchemaFactoryImpl() {}           
    
    /**
     * the static unique instance of this factory.
     */
    private static final SchemaFactory instance;    
    static{
        instance = new SchemaFactoryImpl();
    }
    
    /**
     * Return the unique instance of this factory.
     * @return
     */
    public static SchemaFactory getInstance(){
        return instance;
    }
    
    /* (non-Javadoc)
     * @see com.ibm.semplore.model.SchemaFactory#createAttribute(java.lang.String, int)
     */
    public Attribute createAttribute(long ID, int datatype)
    {
        return new AttributeImpl(ID, datatype);
    }

    /* (non-Javadoc)
     * @see com.ibm.semplore.model.SchemaFactory#createCategory(java.lang.String)
     */
    public Category createCategory(long id)
    {
        return new CategoryImpl(id);
    }

    /* (non-Javadoc)
     * @see com.ibm.semplore.model.SchemaFactory#createCategoryRelationExp()
     */
    public CategoryRelationExp createCategoryRelationExp()
    {
        return new CategoryRelationExpImpl();
    }
    
    /* (non-Javadoc)
     * @see com.ibm.semplore.model.SchemaFactory#createCatRelGraph()
     */
    public CatRelGraph createCatRelGraph() {
        return new CatRelGraphImpl();
    }

    /* (non-Javadoc)
     * @see com.ibm.semplore.model.SchemaFactory#createCompoundCategory(int)
     */
    public CompoundCategory createCompoundCategory(int compound_type)
    {
        return new CompoundCategoryImpl(compound_type);
    }

    /* (non-Javadoc)
     * @see com.ibm.semplore.model.SchemaFactory#createInstance(java.lang.String)
     */
    public Instance createInstance(long id)
    {
        return new InstanceImpl(id);
    }

    /* (non-Javadoc)
     * @see com.ibm.semplore.model.SchemaFactory#createKeywordCategory(java.lang.String)
     */
    public KeywordCategory createKeywordCategory(String keyword)
    {
        return new KeywordCategoryImpl(keyword);
    }

    /* (non-Javadoc)
     * @see com.ibm.semplore.model.SchemaFactory#createRelation(java.lang.String)
     */
    public Relation createRelation(long id)
    {
        return new RelationImpl(id,false);
    }

    /* (non-Javadoc)
     * @see com.ibm.semplore.model.SchemaFactory#createInverseRelation(java.lang.String)
     */
    public Relation createInverseRelation(long id)
    {
        return new RelationImpl(id, true);
    }

    /* (non-Javadoc)
     * @see com.ibm.semplore.model.SchemaFactory#createValueConstraintCategory(com.ibm.semplore.model.Attribute, java.lang.String, boolean, java.lang.String, boolean)
     */
    public ValueConstraintCategory createValueConstraintCategory(
            Attribute attr, String lowerLimit, boolean isLowerLimitInclusive,
            String upperLimit, boolean isUpperLimitInclusive)
    {
        return new ValueConstraintCategoryImpl(attr, lowerLimit, isLowerLimitInclusive, upperLimit, isUpperLimitInclusive);
    }

    /* (non-Javadoc)
     * @see com.ibm.semplore.model.SchemaFactory#createEnumerationCategory()
     */
    public EnumerationCategory createEnumerationCategory()
    {
        return new EnumerationCategoryImpl();
    }

    /* (non-Javadoc)
     * @see com.ibm.semplore.model.SchemaFactory#createSchemaObjectInfo(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    public SchemaObjectInfo createSchemaObjectInfo(long id, String uri, String label, String summary, String text)
    {
        return new SchemaObjectInfoImpl(id, uri, label, summary, text);
    }

    /* (non-Javadoc)
     * @see com.ibm.semplore.model.SchemaFactory#createLiteralsOfProperty(java.lang.String, java.lang.String)
     */
    public LiteralsOfProperty createLiteralsOfProperty(String pro, String lit)
    {
        return new LiteralsOfPropertyImpl(pro, lit);
    }

    /* (non-Javadoc)
     * @see com.ibm.semplore.model.SchemaFactory#createLocalInstanceList()
     */
    public LocalInstanceList createLocalInstanceList()
    {
        return new LocalInstanceListImpl();
    }

    /* (non-Javadoc)
     * @see com.ibm.semplore.model.SchemaFactory#createUniversalCategory()
     */
    public Category createUniversalCategory()
    {
        return new CategoryImpl(Md5_BloomFilter_64bit.HASH_UNIVERSAL_CATEGORY_URI);
    }

    /* (non-Javadoc)
     * @see com.ibm.semplore.model.SchemaFactory#createUniversalRelation()
     */
    public Relation createUniversalRelation()
    {
        return new RelationImpl(Md5_BloomFilter_64bit.HASH_UNIVERSAL_RELATION_URI, false);
    }

	public KeywordCategory createAttributeKeywordCategory(String attribute, String keyword) {
        return new AttributeKeywordCategoryImpl(attribute, keyword);
	}

}
