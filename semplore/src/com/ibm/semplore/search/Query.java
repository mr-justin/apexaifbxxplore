/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 */
package com.ibm.semplore.search;

/**
 * A query defined by formatted text. The Query interface allows to control all aspects of the query behavior, including what metadata is returned with each result.
 * @author liu Qiaoling
 *
 */
public interface Query {
	
	/**
	 * Returns the unique text representation of the query.
	 * @return
	 */
	public String getText();	
    
	/**
	 * Set the number of results to be returned with respect to the complete result set of this query.
	 * @param numberOfResults
	 */
//	public void setRequestedNumberOfResults(int numberOfResults);
    
    /**
     * Get the number of results to be returned with respect to the complete result set of this query.
     * @return
     */
//    public int getRequestedNumberOfResults();
    
}
