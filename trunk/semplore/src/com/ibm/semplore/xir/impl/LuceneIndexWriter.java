/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 *
 * $Id: LuceneIndexWriter.java,v 1.4 2008/09/01 09:53:14 lql Exp $
 */
package com.ibm.semplore.xir.impl;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;

import com.ibm.semplore.xir.InstanceDocument;

/**
 * Abstract class for lucene based index writer. Index strategy is left to
 * concrete class implementation.
 * 
 * @author zhangjie
 * 
 */
public class LuceneIndexWriter implements com.ibm.semplore.xir.IndexWriter {

	protected IndexWriter fsWriter;

	protected String indexPath;

	protected IDocumentConverterForLucene docConverter;

	public LuceneIndexWriter(String indexDirPath) throws IOException {
		if (indexDirPath == null)
			throw new IllegalArgumentException();
		this.docConverter = new DefaultDocumentConverterForLucene_XFaceted();
		indexPath = indexDirPath;

		// ensure directory exists
		File idxDir = new File(indexDirPath);
		if (!idxDir.exists())
			idxDir.mkdirs();
		if (!idxDir.exists() || !idxDir.canRead()) {
			throw new IOException(
					"Document directory '"
							+ idxDir.getAbsolutePath()
							+ "' does not exist or is not readable, please check the path");
		}

		this.fsWriter = new IndexWriter(indexPath, new SemploreAnalyzer(), true);
		// true: to overwrite existing index
		// false: to continue indexing on top of existing index
		this.fsWriter.setMaxFieldLength(Integer.MAX_VALUE);
	}

	public Directory getIndexDirectory() {
		return fsWriter.getDirectory();
	}

	public String getIndexPath() {
		return indexPath;
	}

	public void close() throws IOException {
		fsWriter.optimize();
		fsWriter.close();
	}

	/**
	 * Convert a {@link com.ibm.semplore.xir.Document} to a Lucene indexable
	 * document
	 * 
	 * @param doc
	 * @return
	 */
	protected Document convert(com.ibm.semplore.xir.Document doc) {
		if (doc instanceof InstanceDocument)
			return docConverter.convert((InstanceDocument) doc);
		return null;
	}

	public void addDocument(com.ibm.semplore.xir.Document doc)
			throws IOException {
		this.fsWriter.addDocument(convert(doc));
	}

}
