package com.ibm.semplore.xir.impl;

import java.io.IOException;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;

public class IndexMerger {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws LockObtainFailedException 
	 * @throws CorruptIndexException 
	 */
	public static void main(String[] args) throws CorruptIndexException, LockObtainFailedException, IOException {
		IndexWriter writer = new IndexWriter(args[0], new SemploreAnalyzer());
		Directory[] dirs = new Directory[1];
		dirs[0] = FSDirectory.getDirectory(args[1]);
		writer.addIndexes(dirs);
		writer.optimize();
		writer.close();
	}

}
