/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 *
 * $Id: IFillableDocProvider.java,v 1.2 2007/04/18 06:55:33 lql Exp $
 */
package com.ibm.semplore.xir.impl;

import org.apache.lucene.document.Document;

/**
 * Allows user to add {@link org.apache.lucene.document.Document}
 *	to this provider that can be get through next() later.
 * 
 * @author zhangjie
 *
 */
public interface IFillableDocProvider extends IDocumentProvider {

	/**
	 * Add a document into the provider.
	 * 
	 * @param doc
	 */
	public void addDocument(Document doc);
}
