/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 */
package com.ibm.semplore.xir;

import java.io.IOException;


/**
 * This interface allows to write data to the index.
 * @author liu Qiaoling
 *
 */
public interface IndexWriter {
	
	/**
	 * Add a document to the index.
	 * @param doc the document to be added to the index.
	 */
	public void addDocument(Document doc) throws IOException;
	
	/**
	 * Close the index writer.
	 */
	public void close() throws IOException ;
}
