/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 *
 * $Id: IDocumentConverterForLucene.java,v 1.2 2007/04/18 06:55:34 lql Exp $
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
	
	public org.apache.lucene.document.Document convert(
			com.ibm.semplore.xir.RelationDocument relDoc);
	
	public org.apache.lucene.document.Document convert(
			com.ibm.semplore.xir.CategoryDocument catDoc);
	
	public org.apache.lucene.document.Document convert(
			com.ibm.semplore.xir.AttributeDocument attrDoc);
}
