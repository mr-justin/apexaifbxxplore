/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 *
 * $Id: LuceneIndexWriter.java,v 1.2 2007/04/18 06:55:34 lql Exp $
 */
package com.ibm.semplore.xir.impl;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;

import com.ibm.semplore.xir.AttributeDocument;
import com.ibm.semplore.xir.CategoryDocument;
import com.ibm.semplore.xir.InstanceDocument;
import com.ibm.semplore.xir.RelationDocument;

/**
 * Abstract class for lucene based index writer. Index strategy is left to
 * concrete class implementation.
 * 
 * @author zhangjie
 * 
 */
public abstract class LuceneIndexWriter implements
		com.ibm.semplore.xir.IndexWriter {

	protected IndexWriter fsWriter;

	protected String indexPath;

	protected IDocumentConverterForLucene docConverter;

	protected int mergeFactor = org.apache.lucene.index.IndexWriter.DEFAULT_MERGE_FACTOR;

	protected int maxFieldLength = org.apache.lucene.index.IndexWriter.DEFAULT_MAX_FIELD_LENGTH;

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
	}

//	public LuceneIndexWriter(String indexMethod, String indexDirPath)
//			throws IOException {
//		if (indexDirPath == null)
//			throw new IllegalArgumentException();
//		if (indexMethod == IndexServiceImplFor3.INDEX_METHOD_DOC_LEVEL)
//			docConverter = new DocumentConverterForLucene_DocLev();
//		else if (indexMethod == IndexServiceImplFor3.INDEX_METHOD_UID)
//			docConverter = new DocumentConverterForLucene_UID();
//		else
//			this.docConverter = new DocumentConverterForLucene_RID();
//		indexPath = indexDirPath;
//
//		// ensure directory exists
//		File idxDir = new File(indexDirPath);
//		if (!idxDir.exists())
//			idxDir.mkdirs();
//		if (!idxDir.exists() || !idxDir.canRead()) {
//			throw new IOException(
//					"Document directory '"
//							+ idxDir.getAbsolutePath()
//							+ "' does not exist or is not readable, please check the path");
//		}
//
//		this.fsWriter = new IndexWriter(indexPath, new SemploreAnalyzer(), true);
//	}

	public void setMergeFactor(int mergeFactor) {
		if (mergeFactor < 2)
			throw new IllegalArgumentException(
					"mergeFactor cannot be less than 2");
		this.mergeFactor = mergeFactor;
		this.fsWriter.setMergeFactor(mergeFactor);
	}

	public void setMaxFieldLength(int maxFieldLength) {
		this.maxFieldLength = maxFieldLength;
		this.fsWriter.setMaxFieldLength(maxFieldLength);
	}

	public Directory getIndexDirectory() {
		return fsWriter.getDirectory();
	}

	public String getIndexPath() {
		return indexPath;
	}

	public void addIndexes(Directory[] dirs) throws IOException {

		fsWriter.addIndexes(dirs);
		fsWriter.optimize();
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
		else if (doc instanceof RelationDocument)
			return docConverter.convert((RelationDocument) doc);
		else if (doc instanceof CategoryDocument)
			return docConverter.convert((CategoryDocument) doc);
		else if (doc instanceof AttributeDocument)
			return docConverter.convert((AttributeDocument) doc);
		return null;
	}

}
