/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 *
 * $Id: SchemaSearchableImpl.java,v 1.5 2008/09/01 09:53:14 lql Exp $
 */
package com.ibm.semplore.search.impl;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;

import com.ibm.semplore.model.Category;
import com.ibm.semplore.model.Relation;
import com.ibm.semplore.model.SchemaObjectInfo;
import com.ibm.semplore.search.Query;
import com.ibm.semplore.search.ResultSet;
import com.ibm.semplore.search.SchemaQuery;
import com.ibm.semplore.search.SchemaSearchable;
import com.ibm.semplore.xir.DocStream;
import com.ibm.semplore.xir.IndexReader;

/**
 * @author liu Qiaoling
 *
 */
public class SchemaSearchableImpl extends SearchableImpl implements SchemaSearchable
{

    protected IndexReader indexReader;
    
    protected SchemaSearchableImpl(IndexReader indexReader) {
        super();
        this.indexReader = indexReader;
    }    

    /* (non-Javadoc)
     * @see com.ibm.semplore.search.SchemaSearchable#getAttributes()
     */
    public SchemaObjectInfo[] getAttributes() throws IOException 
    {
        DocStream resultStream = indexReader.getDocStream(termFactory.createTermForRootAttributes());
        return this.fromDocResultsToSchemaObjectInfos(resultStream, -1, indexReader);
    }

    /* (non-Javadoc)
     * @see com.ibm.semplore.search.SchemaSearchable#getRootCategories()
     */
    public SchemaObjectInfo[] getRootCategories() throws IOException
    {
        DocStream resultStream = indexReader.getDocStream(termFactory.createTermForRootCategories());
        return this.fromDocResultsToSchemaObjectInfos(resultStream, -1, indexReader);
    }

    /* (non-Javadoc)
     * @see com.ibm.semplore.search.SchemaSearchable#getRootRelations()
     */
    public SchemaObjectInfo[] getRootRelations() throws IOException
    {
        DocStream resultStream = indexReader.getDocStream(termFactory.createTermForRootRelations());
        return this.fromDocResultsToSchemaObjectInfos(resultStream, -1, indexReader);
    }

    /* (non-Javadoc)
     * @see com.ibm.semplore.search.SchemaSearchable#getSubCategories(com.ibm.semplore.model.Category)
     */
    public SchemaObjectInfo[] getSubCategories(Category parentCat) throws IOException
    {
        DocStream resultStream = indexReader.getDocStream(termFactory.createTermForSubCategories(parentCat));
        return this.fromDocResultsToSchemaObjectInfos(resultStream, -1, indexReader);
    }

    protected SchemaObjectInfo[] getSuperCategories(Category childCat) throws IOException
    {
        DocStream resultStream = indexReader.getDocStream(termFactory.createTermForSuperCategories(childCat));
        return this.fromDocResultsToSchemaObjectInfos(resultStream, -1, indexReader);
    }

    protected SchemaObjectInfo[] getSuperRelations(Relation childRel) throws IOException
    {
        DocStream resultStream = indexReader.getDocStream(termFactory.createTermForSuperRelations(childRel));
        return this.fromDocResultsToSchemaObjectInfos(resultStream, -1, indexReader);
    }
    
    /* (non-Javadoc)
     * @see com.ibm.semplore.search.SchemaSearchable#searchSuperCategories(com.ibm.semplore.model.Category)
     */
    public SchemaObjectInfo[] getSuperCategoryPath(Category cat) throws IOException
    {
        LinkedList path = new LinkedList();
        HashSet<Long> set = new HashSet<Long>();
        do {
        	set.add(cat.getIDofURI());
            SchemaObjectInfo[] parents = getSuperCategories(cat);
            if (parents == null || parents.length == 0)
                break;
            cat = schemaFactory.createCategory(parents[0].getID());
            if (set.contains(cat.getIDofURI()))
            	break;
            path.addFirst(parents[0]);                    
        } while (true);
        return (SchemaObjectInfo[])path.toArray(new SchemaObjectInfo[0]);
    }

    /* (non-Javadoc)
     * @see com.ibm.semplore.search.SchemaSearchable#searchSuperRelations(com.ibm.semplore.model.Relation)
     */
    public SchemaObjectInfo[] getSuperRelationPath(Relation rel) throws IOException
    {
        LinkedList<SchemaObjectInfo> path = new LinkedList<SchemaObjectInfo>();
        HashSet<Long> set = new HashSet<Long>();
        do {
        	set.add(rel.getIDofURI());
            SchemaObjectInfo[] parents = getSuperRelations(rel);
            if (parents == null || parents.length == 0)
                break;            
            rel = schemaFactory.createRelation(parents[0].getID());
            if (set.contains(rel.getIDofURI()))
            	break;
            path.addFirst(parents[0]);                    
        } while (true);
        return (SchemaObjectInfo[])path.toArray(new SchemaObjectInfo[0]);
    }
    
    /* (non-Javadoc)
     * @see com.ibm.semplore.search.SchemaSearchable#getSubRelations(com.ibm.semplore.model.Relation)
     */
    public SchemaObjectInfo[] getSubRelations(Relation parentRel) throws IOException
    {
        DocStream resultStream = indexReader.getDocStream(termFactory.createTermForSubRelations(parentRel));
        return this.fromDocResultsToSchemaObjectInfos(resultStream, -1, indexReader);
    }

    /* (non-Javadoc)
     * @see com.ibm.semplore.search.SchemaSearchable#search(com.ibm.semplore.search.SchemaQuery)
     */
    public ResultSet search(SchemaQuery schemaQuery) throws Exception
    {
        if (schemaQuery.getType() == SchemaQuery.TYPE_CATEGORY) {
            DocStream resultStream = indexReader.getDocStream(schemaQuery.getTerm());
            return new ResultSetImpl_TopDocs(resultStream, indexReader).setSnippetKeyword(schemaQuery.getKeywords());
        } else if (schemaQuery.getType() == SchemaQuery.TYPE_RELATION) {
            DocStream resultStream = indexReader.getDocStream(schemaQuery.getTerm());
            return new ResultSetImpl_TopDocs(resultStream, indexReader).setSnippetKeyword(schemaQuery.getKeywords());
        } else if (schemaQuery.getType() == SchemaQuery.TYPE_ATTRIBUTE) {
            DocStream resultStream = indexReader.getDocStream(schemaQuery.getTerm());
            return new ResultSetImpl_TopDocs(resultStream, indexReader).setSnippetKeyword(schemaQuery.getKeywords());
        }
        return null;
    }

    /* (non-Javadoc)
     * @see com.ibm.semplore.search.Searchable#search(com.ibm.semplore.search.Query)
     */
    public ResultSet search(Query query) throws Exception
    {
        if (query instanceof SchemaQuery)
            return search((SchemaQuery)query);
        SchemaQuery schemaQuery = new SchemaQueryImpl().set(SchemaQuery.TYPE_CATEGORY, query.getText());
        return search(schemaQuery);
    }

}
