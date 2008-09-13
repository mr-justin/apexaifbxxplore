/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 */
package com.ibm.semplore.search;


/**
 * SearchHelper provides an interface for maintenance of additional information for query evaluation, for example cache, so that to improve performance. It is usually used according to a query.
 * @author liu Qiaoling
 *
 */
public interface SearchHelper {
	
	/**
	 * The hint type of category cache, for cache maintenance of results of some category.
	 */
	public static int CATEGORY_CACHE_HINT = 1;
	
	/**
	 * The hint type of start cache, for cache maintenance of results of some previous query so that we can use the cache as a start in the next query.
	 */
	public static int START_CACHE_HINT = 2;
	
	/**
	 * Returns the hint value with given hint type and key.
	 * @param hint_type
	 * @param hint_key
	 * @return the hint
	 */
	public Object getHint(int hint_type, Object hint_key);
	
	/**
	 * Set a hint of given type, key and value.
	 * @param hint_type
	 * @param hint_key
	 * @param hint
	 */
	public void setHint(int hint_type, Object hint_key, Object hint);
	
}
