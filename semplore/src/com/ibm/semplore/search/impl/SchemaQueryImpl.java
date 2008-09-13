/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 *
 * $Id: SchemaQueryImpl.java,v 1.3 2007/04/29 14:30:57 lql Exp $
 */
package com.ibm.semplore.search.impl;

import com.ibm.semplore.search.SchemaQuery;
import com.ibm.semplore.xir.CompoundTerm;
import com.ibm.semplore.xir.Term;
import com.ibm.semplore.xir.TermFactory;
import com.ibm.semplore.xir.impl.TermFactoryImpl;

/**
 * @author liu Qiaoling
 *
 */
public class SchemaQueryImpl extends QueryImpl implements SchemaQuery
{
    
    /**
     * over which schema objects to query
     */
    protected int type;
    
    /**
     * keywords for the query
     */
    protected String keywords;
    
    /**
     * the query term for evaluation generated by the query type and keywords
     */
    protected Term term;   
    
    /* (non-Javadoc)
     * @see com.ibm.semplore.search.SchemaQuery#getTerm()
     */
    public Term getTerm() {
        return term;
    }
    
    /* (non-Javadoc)
     * @see com.ibm.semplore.search.SchemaQuery#getType()
     */
    public int getType() {
        return type;
    }
    
    /* (non-Javadoc)
     * @see com.ibm.semplore.search.SchemaQuery#getKeywords()
     */
    public String getKeywords() {
        return keywords;
    }
    
    /* (non-Javadoc)
     * @see com.ibm.semplore.search.SchemaQuery#set(int, java.lang.String)
     */
    public SchemaQuery set(int type, String keywords) {
        this.type = type;
        this.keywords = keywords;        
        TermFactory termFactory = TermFactoryImpl.getInstance();
        CompoundTerm term = termFactory.createCompoundTerm(CompoundTerm.TYPE_AND);
        String[] words = keywords.toLowerCase().split(" ");
        for (int i=0; i<words.length; i++) {
            words[i] = words[i].trim();
            if (type == SchemaQuery.TYPE_CATEGORY)
                term.addTerm(termFactory.createTermForKeywordOnCateogries(words[i]));
            else if (type == SchemaQuery.TYPE_RELATION)
                term.addTerm(termFactory.createTermForKeywordOnRelations(words[i]));
            else if (type == SchemaQuery.TYPE_ATTRIBUTE)
                term.addTerm(termFactory.createTermForKeywordOnAttributes(words[i]));
        }
        this.term = term;
        return this;
    }
    
    public String toString() {
        String[] typeName = new String[]{"","CATEGORY","RELATION","","ATTRIBUTE"};
        return "KEYWORD OF "+keywords+" ON "+typeName[type];
    }
    
}
