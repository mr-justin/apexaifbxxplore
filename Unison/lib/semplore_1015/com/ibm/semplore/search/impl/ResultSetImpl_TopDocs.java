package com.ibm.semplore.search.impl;

import java.io.IOException;

import org.apache.lucene.search.TopDocCollector;
import org.apache.lucene.search.TopDocs;

import com.ibm.semplore.model.SchemaFactory;
import com.ibm.semplore.model.SchemaObjectInfo;
import com.ibm.semplore.model.impl.SchemaFactoryImpl;
import com.ibm.semplore.search.ResultSet;
import com.ibm.semplore.xir.DocStream;
import com.ibm.semplore.xir.FieldType;
import com.ibm.semplore.xir.IndexReader;

public class ResultSetImpl_TopDocs implements ResultSet {

	protected SchemaFactory schemaFactory = SchemaFactoryImpl.getInstance();

	protected int topcount;

	protected DocStream resultStream;

	protected IndexReader indexReader = null;

	protected TopDocs topDocs;

	protected int total;

	protected ResultSetImpl_TopDocs(DocStream resultStream,
			IndexReader indexReader) throws Exception {
		this(resultStream, indexReader, 50);
	}

	protected ResultSetImpl_TopDocs(DocStream resultStream,
			IndexReader indexReader, int topCount) throws Exception {
		this.resultStream = resultStream;
		this.indexReader = indexReader;
		topcount = topCount;
		generateTopDocs();
	}

	private TopDocCollector collector;

	private void generateTopDocs() throws Exception {
		collector = new TopDocCollector(topcount);
		resultStream.init();
		for (int i=0; i<resultStream.getLen(); i++) {
			collector.collect(resultStream.doc(), resultStream.score());
			resultStream.next();
		};
		topDocs = collector.topDocs();
		total = topDocs.totalHits;
	}

	public int getDocID(int index) throws Exception {
		if (index >= topcount) {
			topcount = index * 2;
			generateTopDocs();
		}
		return topDocs.scoreDocs[index].doc;
	}

	public int getEstimatedNumberOfCompleteResults() {
		return total;
	}

	public int getLength() {
		return total;
	}

	protected SchemaObjectInfo getInfo(IndexReader indexReader, int docID)
			throws IOException {
		FieldType[] fields = new FieldType[] { FieldType.URI, FieldType.LABEL,
				FieldType.SUMMARY, FieldType.TEXT };
		String[] values = indexReader.getFieldValues(docID, fields);
		return schemaFactory.createSchemaObjectInfo(values[0], values[1],
				values[2], values[3]);
	}

	public SchemaObjectInfo getResult(int index) throws Exception {
		int id = getDocID(index);
		return getInfo(indexReader, id);
	}

	public double getScore(int index) throws Exception {
		getDocID(index);
		return topDocs.scoreDocs[index].score;
	}

}
