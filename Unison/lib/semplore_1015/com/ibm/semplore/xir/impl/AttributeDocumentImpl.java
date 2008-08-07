/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 *
 * $Id: AttributeDocumentImpl.java,v 1.2 2007/04/18 06:55:33 lql Exp $
 */
package com.ibm.semplore.xir.impl;

import com.ibm.semplore.imports.OntologyRepository;
import com.ibm.semplore.model.Attribute;
import com.ibm.semplore.xir.AttributeDocument;

/**
 * @author liu qiaoling
 *
 */
public class AttributeDocumentImpl extends DocumentImpl implements AttributeDocument
{

    /**
     * the attribute of this document
     */
    protected Attribute attr;
    
    /**
     * the ontology repository where all data comes from
     */
    protected OntologyRepository ontoRepo;
    
    /**
     * @param attr
     * @param ontoRepo
     */
    public AttributeDocumentImpl(Attribute attr, OntologyRepository ontoRepo) {
        this.attr = attr;
        this.ontoRepo = ontoRepo;
    }
    
    /* (non-Javadoc)
     * @see com.ibm.semplore.xir.AttributeDocument#getThisAttribute()
     */
    public Attribute getThisAttribute()
    {
        return attr;
    }

}
