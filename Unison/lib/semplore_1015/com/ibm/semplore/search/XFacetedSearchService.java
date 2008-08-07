/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 */
package com.ibm.semplore.search;

import java.io.IOException;


/**
 * Use the SearchService object to obtain a Searchable object which is associated with a searchable collection on the search server.
 * @author liu Qiaoling
 *
 */
public interface XFacetedSearchService {
		
	/**
	 * Obtains an extended faceted searchable object to a collection. 
	 * @return
	 */
	public XFacetedSearchable getXFacetedSearchable() throws Exception;
	
	/**
	 * Obtains a schema searchable object to a collection. 
	 * @return
	 */
	public SchemaSearchable getSchemaSearchable() throws IOException;
	
}
