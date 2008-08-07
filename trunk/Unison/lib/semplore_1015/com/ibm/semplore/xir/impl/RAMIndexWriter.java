/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 *
 * $Id: RAMIndexWriter.java,v 1.2 2007/04/18 06:55:33 lql Exp $
 */
package com.ibm.semplore.xir.impl;

import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.RAMDirectory;

/**
 * A general RAM index writer
 * 
 * @author zhangjie
 *
 */
public class RAMIndexWriter extends IndexWriter {

	private RAMDirectory ram = null;
	private int numOfDocAdded = 0;
	
	public RAMIndexWriter(RAMDirectory ram, Analyzer a, boolean create) throws IOException {
		super(ram, a, create);
		this.ram = ram;
	}
	
	public long getRAMSize() throws IOException {
		String[] files = ram.list();
		long size = 0;
		for(int i=0;i<files.length;i++)
			size += ram.fileLength(files[i]);
		return size;
	}

	public void addDocument(Document doc) throws IOException {
		// TODO Auto-generated method stub
		super.addDocument(doc);
		numOfDocAdded++;
	}
	
	public int getNumOfDocAdded(){
		return numOfDocAdded;
	}
	
	public RAMDirectory getRAMDirectory(){
		return ram;
	}
	
	
}
