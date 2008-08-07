/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 *
 * $Id: RelationDocumentImpl.java,v 1.2 2007/04/18 06:55:33 lql Exp $
 */
package com.ibm.semplore.xir.impl;

import com.ibm.semplore.imports.OntologyRepository;
import com.ibm.semplore.model.Relation;
import com.ibm.semplore.xir.RelationDocument;

/**
 * @author liu qiaoling
 *
 */
public class RelationDocumentImpl extends DocumentImpl implements RelationDocument
{

    /**
     * the relation of this document
     */
    protected Relation rel;
    
    /**
     * the ontology repository where all the data comes from
     */
    protected OntologyRepository ontoRepo;
    
    /**
     * @param rel
     * @param ontoRepo
     */
    public RelationDocumentImpl(Relation rel, OntologyRepository ontoRepo) {
        this.rel = rel;
        this.ontoRepo = ontoRepo;
    }
    
    /* (non-Javadoc)
     * @see com.ibm.semplore.xir.RelationDocument#getSubRelations()
     */
    public Relation[] getSubRelations()
    {
        return ontoRepo.getSubRelations(rel);
    }

    /* (non-Javadoc)
     * @see com.ibm.semplore.xir.RelationDocument#getSuperRelations()
     */
    public Relation[] getSuperRelations()
    {
        return ontoRepo.getSuperRelations(rel);
    }

    /* (non-Javadoc)
     * @see com.ibm.semplore.xir.RelationDocument#getThisRelation()
     */
    public Relation getThisRelation()
    {
        return rel;
    }

    /* (non-Javadoc)
     * @see com.ibm.semplore.xir.RelationDocument#isRootRelation()
     */
    public boolean isRootRelation()
    {
        Relation[] rels = ontoRepo.getSuperRelations(rel); 
        if (rels == null || rels.length == 0)
            return true;
        else
            return false;
    }

}
