/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 */
package com.ibm.semplore.xir;

import java.io.IOException;


/**
 * This interface allows to read data from an index.
 * @author liu Qiaoling
 *
 */
public interface IndexReader {
	
	/**
	 * get a document stream from the index with given term
	 * 
	 * @param term The term which the documents should match
	 * @return
	 * @throws IOException
	 */
	public DocStream getDocStream(Term term) throws IOException ;
	    
    /**
     * Get a document stream from the index with given term by relevance order.
     * @param term
     * @return
     * @throws IOException
     */
    public DocStream getDocStreamByRelevanceOrder(Term term) throws IOException;
    
	/**
	 * get a document & position stream from the index given term
	 * 
	 * @param term The term which the documents should match
	 * @return
	 * @throws IOException
	 */
	public DocPositionStream getDocPositionStream(Term term) throws IOException ;
	
	/**
	 * Get the field values of a document by giving the document id in the 
	 * 	underlying indexing system.
	 * 
	 * E.g., user can use this interface to get URI.
	 * 
	 * @param docID The id of the document maintained by the underlying indexing system
	 * @param types The field types
	 * @return The string representation of the field values
	 * @throws IOException
	 */
	public String[] getFieldValues(int docID, FieldType[] types) throws IOException ;
	
	/**
	 * Close the index reader.
	 */
	public void close() throws IOException ;
    
    /**
     * Returns the class of doc in this index, which may be Instance.class, Category.class, Relation.class and Attribute.class.
     * @return
     */
    public Class getDocClass();
	
}
