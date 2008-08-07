/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 */
package com.ibm.semplore.xir;

import java.io.IOException;

import com.ibm.semplore.imports.IteratableOntologyRepository;

/**
 * This interface provides indexing services.
 * @author liu Qiaoling
 *
 */
public interface IndexService {
	
	/**
	 * Enumeration on index types within a {@link IndexService}.
	 * 
	 * @author zhangjie
	 *
	 */
	public static final class IndexType {
		
		private IndexType(String f){}
		
		/**
		 * Tag for index taking an instance as document
		 */
		public static final IndexType Instance = new IndexType("instance");
		/**
		 * Tag for index taking a cateogry as document
		 */
		public static final IndexType Category = new IndexType("category");
		/**
		 * Tag for index taking a relation as document
		 */
		public static final IndexType Relation = new IndexType("relation");
		/**
		 * Tag for index taking an attribute as document
		 */
		public static final IndexType Attribute = new IndexType("attribute");
	}
	
	/**
	 * Returns an index reader, given the index path.
	 * @param indexType the index id that want to read, can only be one of
	 * 	
	 * @return
	 */
	public IndexReader getIndexReader(IndexType indexType) throws IOException;

	/**
	 * Returns an index writer, given the index path.
	 * @param indexType  the index id that want to write
	 * @param create True to overwrite existing, false to add on existing
	 * @return
	 */
	public IndexWriter createIndexWriter(IndexType indexType, boolean create) throws IOException;
    
    /**
     * Build the whole index based on the given ontology repository: instance index, category index, relation index and attribute index.
     * @param ontoRepo
     * @throws IOException
     */
    public void buildWholeIndex(IteratableOntologyRepository ontoRepo) throws IOException;
    
}
