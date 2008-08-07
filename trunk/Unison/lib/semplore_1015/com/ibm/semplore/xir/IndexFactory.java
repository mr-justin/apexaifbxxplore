/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 */
package com.ibm.semplore.xir;

/**
 * Factory interface for generating index service object; Interface for creating required objects for indexing like documents, categories, relations and fields objects.
 * @author liu Qiaoling
 *
 */
public interface IndexFactory {
	
	/**
	 * Obtains an index service object for a specific application.
	 * @param config
	 * @return
	 */
	public IndexService getIndexService(java.util.Properties config);

}
