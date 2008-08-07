/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 *
 * $Id: InstanceDocumentIterator.java,v 1.2 2007/04/18 06:55:34 lql Exp $
 */
package com.ibm.semplore.imports;

import java.io.IOException;

import com.ibm.semplore.xir.InstanceDocument;

/**
 * An iterator on {@link InstanceDocument}
 * 
 * @author zhangjie
 *
 */
public interface InstanceDocumentIterator {

	/**
	 * Get the next {@link InstanceDocument} in this iterator. Null indicates end.
	 * 
	 * Each returned {@link InstanceDocument} should have its cateogries specified through
	 * 	{@link InstanceDocument.setCategories(Category[] categories)}, 
	 * 	and have triples with this instance as subject specified through 
	 * 	{@link addObjectsOfRelation(Relation rel, LocalInstanceList objectList)}.
	 * 
	 * @return The next {@link InstanceDocument} in this iterator. 
	 * 			Null if end reached.
	 */
	public InstanceDocument next();
	public void close();
}
