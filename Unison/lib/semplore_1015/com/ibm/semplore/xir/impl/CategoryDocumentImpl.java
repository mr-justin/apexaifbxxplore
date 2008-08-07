/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 *
 * $Id: CategoryDocumentImpl.java,v 1.2 2007/04/18 06:55:33 lql Exp $
 */
package com.ibm.semplore.xir.impl;

import com.ibm.semplore.imports.OntologyRepository;
import com.ibm.semplore.model.Category;
import com.ibm.semplore.xir.CategoryDocument;

/**
 * @author liu qiaoling
 *
 */
public class CategoryDocumentImpl extends DocumentImpl implements CategoryDocument
{

    /**
     * the category of this document
     */
    protected Category cat;
    
    /**
     * the ontology repository where all data comes from
     */
    protected OntologyRepository ontoRepo;
    
    /**
     * @param cat
     */
    public CategoryDocumentImpl(Category cat, OntologyRepository ontoRepo) {
        this.cat = cat;
        this.ontoRepo = ontoRepo;
    }

    /* (non-Javadoc)
     * @see com.ibm.semplore.xir.CategoryDocument#getSubCateogries()
     */
    public Category[] getSubCateogries()
    {
        return ontoRepo.getSubCategories(cat);
    }

    /* (non-Javadoc)
     * @see com.ibm.semplore.xir.CategoryDocument#getSuperCateogries()
     */
    public Category[] getSuperCateogries()
    {
        return ontoRepo.getSuperCategories(cat);
    }

    /* (non-Javadoc)
     * @see com.ibm.semplore.xir.CategoryDocument#getThisCategory()
     */
    public Category getThisCategory()
    {
        return cat;
    }

    /* (non-Javadoc)
     * @see com.ibm.semplore.xir.CategoryDocument#isRootCategory()
     */
    public boolean isRootCategory()
    {
        Category[] cats = ontoRepo.getSuperCategories(cat);
        if (cats == null || cats.length == 0)
            return true;
        else 
            return false;
    }
    
}
