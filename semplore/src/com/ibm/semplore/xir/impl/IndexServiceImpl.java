/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 *
 * $Id: IndexServiceImpl.java,v 1.7 2008/09/08 09:05:33 lql Exp $
 */
package com.ibm.semplore.xir.impl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Properties;

import com.ibm.semplore.config.Config;
import com.ibm.semplore.imports.InstanceDocumentIterator;
import com.ibm.semplore.imports.IteratableOntologyRepository;
import com.ibm.semplore.imports.IteratorFactory;
import com.ibm.semplore.imports.OntologyRepository;
import com.ibm.semplore.model.SchemaFactory;
import com.ibm.semplore.model.SchemaObjectInfo;
import com.ibm.semplore.model.impl.SchemaFactoryImpl;
import com.ibm.semplore.xir.Document;
import com.ibm.semplore.xir.IndexReader;
import com.ibm.semplore.xir.IndexService;
import com.ibm.semplore.xir.IndexWriter;
import com.ibm.semplore.xir.InstanceDocument;

/**
 * @author zhangjie
 * 
 */
public class IndexServiceImpl implements IndexService {

	protected Properties config = null;

	protected SchemaFactory schemaFactory;

	protected String indexPath = null;

	protected String instanceIndexPath = null;

	protected String categoryIndexPath = null;

	protected String relationIndexPath = null;

	protected String attributeIndexPath = null;

	protected int availableMemorySize = 1 << 10;

	protected int allowedNumberOfThreads = 1;

	// protected int memorySizeForEachThread = availableMemorySize /
	// allowedNumberOfThreads;

	/**
	 * Protected constructor
	 */
	protected IndexServiceImpl(Properties config) {
		this.config = config;
		indexPath = config.getProperty(Config.INDEX_PATH);
		schemaFactory = SchemaFactoryImpl.getInstance();

		int mSize;
		try {
			mSize = Integer.parseInt(config.getProperty(Config.MEMORY_SIZE));
		} catch (Exception e) {
			mSize = availableMemorySize;
			// throw new IllegalArgumentException("Must give property values for
			// memory size and number of threads."+
			// " Do this by add a property in the parameter with name '" +
			// Config.MEMORY_SIZE + "'.");
		}
		if (indexPath == null)
			throw new IllegalArgumentException(
					"Index path can not be null. Do this by add a property in the parameter with name '"
							+ Config.INDEX_PATH + "'");

		instanceIndexPath = indexPath + System.getProperty("file.separator")
				+ "instance";
		categoryIndexPath = indexPath + System.getProperty("file.separator")
				+ "category";
		relationIndexPath = indexPath + System.getProperty("file.separator")
				+ "relation";
		attributeIndexPath = indexPath + System.getProperty("file.separator")
				+ "attribute";

		setAvailableMemorySize(mSize);
		setInstanceIndexPath(instanceIndexPath);
		setCategoryIndexPath(categoryIndexPath);
		setRelationIndexPath(relationIndexPath);
		setAttributeIndexPath(attributeIndexPath);
	}

	protected void setAvailableMemorySize(int size) {
		if (size < (1 << 10))
			throw new IllegalArgumentException();
		availableMemorySize = size;
		// memorySizeForEachThread = availableMemorySize /
		// allowedNumberOfThreads;
	}

	protected void setAllowedNumberOfThreads(int num) {
		if (num < 1)
			throw new IllegalArgumentException();
		allowedNumberOfThreads = num;
		// memorySizeForEachThread = availableMemorySize /
		// allowedNumberOfThreads;
	}

	/**
	 * Set the instance index path
	 * 
	 * @param path
	 */
	protected void setInstanceIndexPath(String path) {
		instanceIndexPath = path;
	}

	/**
	 * set the category index path
	 * 
	 * @param path
	 */
	protected void setCategoryIndexPath(String path) {
		categoryIndexPath = path;
	}

	/**
	 * set the relation index path
	 * 
	 * @param path
	 */
	protected void setRelationIndexPath(String path) {
		relationIndexPath = path;
	}

	/**
	 * set the attribute index path
	 * 
	 * @param path
	 */
	protected void setAttributeIndexPath(String path) {
		attributeIndexPath = path;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.semplore.xir.IndexService#createIndexWriter(com.ibm.semplore.xir.IndexService.IndexType,
	 *      boolean)
	 */
	public IndexWriter createIndexWriter(IndexType indexType, boolean create)
			throws IOException {
		String indexPath = null;
		if (indexType.equals(IndexType.Instance))
			indexPath = instanceIndexPath;
		else if (indexType.equals(IndexType.Category))
			indexPath = categoryIndexPath;
		else if (indexType.equals(IndexType.Relation))
			indexPath = relationIndexPath;
		else if (indexType.equals(IndexType.Attribute))
			indexPath = attributeIndexPath;

		// availableMemorySize -= memorySizeForEachThread;
		// allowedNumberOfThreads--;
		// return new ProduceConsumeIndexWriter(indexPath,
		// memorySizeForEachThread);
		return new LuceneIndexWriter(indexPath);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.semplore.xir.IndexService#getIndexReader(com.ibm.semplore.xir.IndexService.IndexType)
	 */
	public IndexReader getIndexReader(IndexType indexType) throws IOException {
		return new IndexReaderImpl(instanceIndexPath, indexType);
	}

	/**
	 * Get the information of the schema object with given uri and annotation
	 * properties based on configuration.
	 * 
	 * @param uri
	 * @param apv
	 * @return
	 */
	protected SchemaObjectInfo getSchemaObjectInfo(long id, String uri,
			HashMap<String, String> attributes) {
		String[] labelProperties = config.getProperty(Config.LABEL,
				"http://www.w3.org/2000/01/rdf-schema#label").split("[ \t,]");
		String label = null;
//		String[] summaryProperties = config
//				.getProperty(
//						Config.SUMMARY,
//						"http://www.w3.org/2000/01/rdf-schema#label http://www.w3.org/2000/01/rdf-schema#comment")
//				.split("[ \t,]");
		if (attributes != null) {
			//check label
			for (String lp : labelProperties) {
				if (attributes.containsKey(lp)) {
					label = attributes.get(lp);
					break;
				}
			}
		}
		return schemaFactory.createSchemaObjectInfo(id, uri, label, null, null);
	}

	/**
	 * Record the time of adding a document.
	 * 
	 * @param indexWriter
	 * @param doc
	 * @param time
	 */
	protected void addDocument(IndexWriter indexWriter, Document doc,
			long[] time) throws IOException {
		long tmp = System.currentTimeMillis();
		indexWriter.addDocument(doc);
		time[0] += System.currentTimeMillis() - tmp;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.semplore.xir.IndexService#buildWholeIndex(com.ibm.semplore.imports.IteratableOntologyRepository)
	 */
	public void buildWholeIndex(IteratableOntologyRepository ontoRepo)
			throws IOException {
		IndexWriter indexWriter;
		System.out.println("\nbegin to build whole index...");
		long time_begin = System.currentTimeMillis();
		long time_b, time_e;
		long[] time_index = new long[1], time_SOR = new long[1];
		// createOntologyFeatureFile(ontoRepo, "ontologyFeature.txt");

		// index writer for instance
		System.out.print("indexing instances...");
		time_b = System.currentTimeMillis();
		indexWriter = createIndexWriter(IndexService.IndexType.Instance, true);
		Properties props = new Properties();
		props.setProperty(IteratorFactory.INTERVAL, String.valueOf(50000));
		InstanceDocumentIterator insDocIter = ontoRepo
				.createInstanceDocumentIterator(props);
		InstanceDocument insDoc = null;
		int tol = 0;
		int count = 0;
		long time_last = time_b;
		while (insDocIter.hasNext()) {
			long tmp = System.currentTimeMillis();
			insDoc = insDocIter.next();
			time_SOR[0] += System.currentTimeMillis() - tmp;
			
			SchemaObjectInfo info = getSchemaObjectInfo(insDoc
					.getThisSchemaObject().getIDofURI(), insDoc.getURI(),
					insDoc.getAttributes());
			insDoc.setSchemaObjectInfo(info);
			addDocument(indexWriter, insDoc, time_index);
			
			tol++;
			count++;
			if (count >= 10000 || tol == 1) {
				System.out.println(tol + " indexed("
						+ (System.currentTimeMillis() - time_last) + "ms)...");
				time_last = System.currentTimeMillis();
				count = 0;
			}
		}
		insDocIter.close();
		// indexWriter.close();
		time_e = System.currentTimeMillis();
		System.out.println((time_e - time_b) + " ms");

		indexWriter.close();

		long time_end = System.currentTimeMillis();
		System.out.println("indexing finished in " + (time_end - time_begin)
				+ " ms");
		System.out.println("\nsemplore index time: " + time_index[0] + "ms "
				+ 1.0 * time_index[0] / (time_end - time_begin));
		System.out.println("SOR time: " + time_SOR[0] + "ms " + 1.0
				* time_SOR[0] / (time_end - time_begin));
		System.out.println("time ignored: "
				+ (time_end - time_begin - time_index[0] - time_SOR[0]) + "ms "
				+ 1.0 * (time_end - time_begin - time_index[0] - time_SOR[0])
				/ (time_end - time_begin));
	}

	protected void createOntologyFeatureFile(OntologyRepository ontoRepo,
			String filename) {
		indexPath = config.getProperty(Config.INDEX_PATH);
		File f = new File(indexPath);
		if (!f.exists())
			f.mkdirs();
		try {
			PrintWriter out = new PrintWriter(new FileWriter(indexPath
					+ System.getProperty("file.separator") + filename));
			out.println("categorySize = " + ontoRepo.getCategorySize());
			out.println("relationSize = " + ontoRepo.getRelationSize());
			out.println("attributeSize = " + ontoRepo.getAttributeSize());
			out.println("instanceSize = " + ontoRepo.getInstanceSize());
			out.println("tripleSize = " + ontoRepo.getTripleSize());
			out.println("typeOf = " + ontoRepo.getInstanceOfTripleSize());
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
