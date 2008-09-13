/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 */
package com.ibm.semplore.search;

import com.ibm.semplore.model.SchemaObjectInfo;

/**
 * The ResultSet interface contains the search results for the executed query. A ResultSet instance is returned by the search method of the Searchable interface upon the completion of query evaluation. In addition to the results of the query, the ResultSet contains the number of search results that are available.
 * @author liu Qiaoling
 *
 */
public interface ResultSet {

	/**
	 * Returns the number of search results returned by this ResultSet, according to the range specification in the query.
	 * @return
	 */
	public int getLength();

	/**
	 * Returns the search result with given index as its rank.
	 * @param index
	 * @return
	 */
	public SchemaObjectInfo getResult(int index) throws Exception;

	/**
	 * Returns the score of the search result with given index as its rank.
	 * @param index
	 * @return
	 */
	public double getScore(int index) throws Exception; 
    
    /**
     * Returns the doc id of the search result with given index as its rank.
     * @param index
     * @return
     */
    public int getDocID(int index) throws Exception;
    
    public String getSnippet(int index) throws Exception;
    
    public ResultSet setSnippetKeyword(String keyword);		
}
