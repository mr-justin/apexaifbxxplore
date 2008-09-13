/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 */
package com.ibm.semplore.search;


/**
 * This is the search interface to the index. A Searchable object enables to issue queries and get information about the associated collection.
 * @author liu Qiaoling
 *
 */
public interface Searchable {

	/**
	 * Runs a query and returns a set of results. This method never returns null. If there are no matching results, then the method ResultSet.getLength() will return 0.
	 * @param query
	 * @return
	 */
	public ResultSet search(Query query) throws Exception;
}
