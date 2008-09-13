/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 *
 * $Id: IDocumentConverterForLucene.java,v 1.3 2008/09/01 09:53:14 lql Exp $
 */
package com.ibm.semplore.xir.impl;

/**
 * Interface for convert a {@link com.ibm.semplore.xir.Document} to a lucene 
 * 	{@link org.apache.lucene.document.Document}.
 * 
 * @author zhangjie
 *
 */
public interface IDocumentConverterForLucene {

	public org.apache.lucene.document.Document convert(
			com.ibm.semplore.xir.InstanceDocument insDoc);
	
}
