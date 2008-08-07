/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 *
 * $Id: IDocumentProvider.java,v 1.2 2007/04/18 06:55:34 lql Exp $
 */
package com.ibm.semplore.xir.impl;

import org.apache.lucene.document.Document;

/**
 * Interface for {@link org.apache.lucene.document.Document} provider.
 * Implementation of this interface should allow user to get 
 * 	{@link org.apache.lucene.document.Document} one by one through 
 * 	next() untill a null value is returned.
 * 
 * @author zhangjie
 *
 */
public interface IDocumentProvider {
	
	/**
	 * Get next document available.
	 * 
	 * @return The next document available, null otherwise.
	 */
	public Document next();
}
