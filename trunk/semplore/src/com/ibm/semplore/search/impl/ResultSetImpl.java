/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 *
 * $Id: ResultSetImpl.java,v 1.6 2008/09/01 09:53:14 lql Exp $
 */
package com.ibm.semplore.search.impl;

import java.io.IOException;

import com.ibm.semplore.model.SchemaFactory;
import com.ibm.semplore.model.SchemaObjectInfo;
import com.ibm.semplore.model.impl.SchemaFactoryImpl;
import com.ibm.semplore.search.ResultSet;
import com.ibm.semplore.xir.DocStream;
import com.ibm.semplore.xir.FieldType;
import com.ibm.semplore.xir.IndexReader;

/**
 * @author liu Qiaoling
 *
 */
public class ResultSetImpl implements ResultSet
{
	protected final int INTERVAL = 100;
	
	protected SchemaFactory schemaFactory = SchemaFactoryImpl.getInstance();
	
	protected DocStream resultStream = null;
	
	protected IndexReader indexReader = null;
	
    /**
     * Result array, each result would be a schema object.
     */
    protected HitDoc[] hitDocs = null;
    
    protected int start = 0;
    
    protected ResultSetImpl(DocStream resultStream, IndexReader indexReader) throws Exception {
        this.resultStream = resultStream;
        this.indexReader = indexReader;
        resultStream.init();
        if (resultStream.getLen()>0)
        	hitDocs = getMoreDocs(0);
        start = 0;
    }
        
    protected HitDoc[] getMoreDocs(int fromIndex) throws Exception {
    	if (resultStream.count()>=fromIndex+2) {
    		resultStream = (DocStream)resultStream.clone();
    		resultStream.init();
    	}
    	while (resultStream.count()<fromIndex+1) {
    		if (!resultStream.next())
    			return null;
    	}
        HitDoc[] results = new HitDoc[INTERVAL];
        for (int i=0; i<INTERVAL; i++) {
            SchemaObjectInfo schemaObject = getInfo(indexReader, resultStream.doc());
            results[i] = new HitDoc(resultStream.doc(), resultStream.score(), schemaObject);
            if (!resultStream.next())
            	break;
        }
        return results;
    }
    
    protected SchemaObjectInfo getInfo(IndexReader indexReader, int docID) throws IOException{
        FieldType[] fields = new FieldType[]{FieldType.ID, FieldType.URI, FieldType.LABEL, FieldType.SUMMARY, FieldType.TEXT};
        String[] values = indexReader.getFieldValues(docID, fields);
        return schemaFactory.createSchemaObjectInfo(Long.valueOf(values[0]), values[1], values[2], values[3], values[4]);
    }

    protected HitDoc getHitDoc(int n) throws Exception {
    	if (n>=resultStream.getLen())
    		throw new IndexOutOfBoundsException("Not a valid hit number: "+n);
    	
    	if (n>=start && n<start+INTERVAL)
    		return hitDocs[n-start];
    	
    	hitDocs = getMoreDocs(n);
    	start = n;
    	return hitDocs[n-start];
    }
    
    /* (non-Javadoc)
     * @see com.ibm.semplore.search.ResultSet#getLength()
     */
    public int getLength()
    {
        return resultStream.getLen();
    }

    /* (non-Javadoc)
     * @see com.ibm.semplore.search.ResultSet#getResult(int)
     */
    public SchemaObjectInfo getResult(int index) throws Exception
    {       
        return getHitDoc(index).schemaObj;
    }

    /* (non-Javadoc)
     * @see com.ibm.semplore.search.ResultSet#getScore(int)
     */
    public double getScore(int index) throws Exception
    {
        return getHitDoc(index).score;
    }

    /* (non-Javadoc)
     * @see com.ibm.semplore.search.ResultSet#getDocID(int)
     */
    public int getDocID(int index) throws Exception
    {
        return getHitDoc(index).doc;
    }

	public String getSnippet(int index) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public ResultSet setSnippetKeyword(String keyword) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DocStream getResultStream() {
		return resultStream;
	}

}

/**
 * SchemaResult keeps information of a result: schema object, id and score.
 * @author liu Qiaoling
 *
 */
class HitDoc {
    
    /**
     * score of the result
     */
    public double score;
    
    /**
     * doc id of the result
     */
    public int doc;
    
    /**
     * schema object of the result
     */
    public SchemaObjectInfo schemaObj;
    
    /**
     * @param doc
     * @param score
     * @param schemaObj
     */
    public HitDoc(int doc, double score, SchemaObjectInfo schemaObj) {
        this.doc = doc;
        this.score = score;
        this.schemaObj = schemaObj;
    }
}

