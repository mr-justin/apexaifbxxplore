/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 *
 * $Id: SearchableImpl.java,v 1.3 2007/04/26 09:15:33 lql Exp $
 */
package com.ibm.semplore.search.impl;

import java.io.IOException;

import com.ibm.semplore.model.SchemaFactory;
import com.ibm.semplore.model.SchemaObjectInfo;
import com.ibm.semplore.model.impl.SchemaFactoryImpl;
import com.ibm.semplore.xir.DocStream;
import com.ibm.semplore.xir.FieldType;
import com.ibm.semplore.xir.IndexReader;
import com.ibm.semplore.xir.TermFactory;
import com.ibm.semplore.xir.impl.TermFactoryImpl;

/**
 * @author liu Qiaoling
 *
 */
public abstract class SearchableImpl implements com.ibm.semplore.search.Searchable
{

    /**
     * The term factory to create term-related objects.
     */
    protected TermFactory termFactory;
    
    /**
     * the schema factory to create schema-related objects. 
     */
    protected SchemaFactory schemaFactory;
        
    /**
     * 
     */
    protected SearchableImpl() {
        termFactory = TermFactoryImpl.getInstance();
        schemaFactory = SchemaFactoryImpl.getInstance();
    }

    /**
     * Convert a result stream of docs to result list of specific schema object(Category, Relation, Instance, Attribute). 
     * @param resultStream
     * @param cls the class of the specific schema object
     * @param indexReader the index reader for accessing index of the specific schema object
     * @return
     * @throws IOException
     */
//    public SchemaResult[] fromDocResultsToSchemaResults(DocStream resultStream, int requestedNumberOfResults, IndexReader indexReader) throws IOException {
//        if (requestedNumberOfResults <= 0 || requestedNumberOfResults > resultStream.getLen())
//            requestedNumberOfResults = resultStream.getLen();
//        SchemaResult[] results = new SchemaResult[requestedNumberOfResults];
//        resultStream.init();
//        for (int i=0; i<requestedNumberOfResults; i++) {
//            SchemaObjectInfo schemaObject = getInfo(indexReader, resultStream.doc());
//            results[i] = new SchemaResult(resultStream.doc(), resultStream.score(), schemaObject);
//            resultStream.next();
//        }
//        return results;
//    }
    
    /**
     * Convert a result stream of docs to result list of specific schema object(Category, Relation, Instance, Attribute). 
     * @param resultStream
     * @param requestedNumberOfResults
     * @param indexReader  the index reader for accessing index of the specific schema object
     * @return
     * @throws IOException
     */
    public SchemaObjectInfo[] fromDocResultsToSchemaObjectInfos(DocStream resultStream, int requestedNumberOfResults, IndexReader indexReader) throws IOException {
        if (requestedNumberOfResults <= 0 || requestedNumberOfResults > resultStream.getLen())
            requestedNumberOfResults = resultStream.getLen();
        SchemaObjectInfo[] results = new SchemaObjectInfo[requestedNumberOfResults];
        resultStream.init();
        for (int i=0; i<requestedNumberOfResults; i++) {
            results[i] = getInfo(indexReader, resultStream.doc());
            resultStream.next();
        }
        return results;
    }
    
    protected SchemaObjectInfo getInfo(IndexReader indexReader, int docID) throws IOException{
        FieldType[] fields = new FieldType[]{FieldType.URI, FieldType.LABEL, FieldType.SUMMARY, FieldType.TEXT};
        String[] values = indexReader.getFieldValues(docID, fields);
        return schemaFactory.createSchemaObjectInfo(values[0], values[1], values[2], values[3]);
    }

}
