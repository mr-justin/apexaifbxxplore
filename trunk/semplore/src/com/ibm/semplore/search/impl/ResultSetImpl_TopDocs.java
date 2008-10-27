package com.ibm.semplore.search.impl;

import java.io.IOException;
import java.io.StringReader;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.TopDocCollector;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;

import com.ibm.semplore.model.SchemaFactory;
import com.ibm.semplore.model.SchemaObjectInfo;
import com.ibm.semplore.model.impl.SchemaFactoryImpl;
import com.ibm.semplore.search.ResultSet;
import com.ibm.semplore.xir.DocStream;
import com.ibm.semplore.xir.FieldType;
import com.ibm.semplore.xir.IndexReader;

public class ResultSetImpl_TopDocs implements ResultSet {

	protected String keyword = null;
	
	protected SchemaFactory schemaFactory = SchemaFactoryImpl.getInstance();

	protected int topcount;

	protected DocStream resultStream;

	protected IndexReader indexReader = null;

	protected TopDocs topDocs;

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
			collector.collect(resultStream.doc(), (float)(resultStream.score()));
			resultStream.next();
		};
		topDocs = collector.topDocs();
	}

	public int getDocID(int index) throws Exception {
		if (index >= topcount) {
			topcount = index * 2;
			generateTopDocs();
		}
		return topDocs.scoreDocs[index].doc;
	}

	public int getEstimatedNumberOfCompleteResults() {
		return resultStream.getLen();
	}

	public int getLength() {
		return resultStream.getLen();
	}

	protected SchemaObjectInfo getInfo(IndexReader indexReader, int docID)
			throws IOException {
		FieldType[] fields = new FieldType[] { FieldType.ID, FieldType.URI, FieldType.LABEL,
				FieldType.SUMMARY, FieldType.TEXT };
		String[] values = indexReader.getFieldValues(docID, fields);
		return schemaFactory.createSchemaObjectInfo(Long.valueOf(values[0]), values[1],
				values[2], values[3], values[4]);
	}

	public SchemaObjectInfo getResult(int index) throws Exception {
		int id = getDocID(index);
		return getInfo(indexReader, id);
	}

	public double getScore(int index) throws Exception {
		getDocID(index);
		return topDocs.scoreDocs[index].score / topDocs.scoreDocs[0].score;
	}

	public String getSnippet(int index) throws Exception {
		StandardAnalyzer analyzer = new StandardAnalyzer();
		QueryParser qp = new QueryParser(FieldType.TEXT.toString(), analyzer);
		SchemaObjectInfo info = getResult(index);
		String text = info.getTextDescription();
		String label = info.getLabel().toLowerCase();
		//remove abundant labels
		if (!label.equals("")) {
			while (text.indexOf(label)==0) 
				text = text.substring(label.length()).trim();
		}
		
		if (keyword!=null) {
			try {
				Highlighter hl = new Highlighter(new QueryScorer(qp.parse(keyword)));
				TokenStream ts = analyzer.tokenStream(text, new StringReader(text));
				String snippet = hl.getBestFragment(ts, text);
				if (snippet!=null)
					return snippet;
			} catch (Exception e) {
//				e.printStackTrace();
			}
		}
		return text;
	}

	public ResultSet setSnippetKeyword(String keyword) {
		this.keyword = keyword;
		return this;
	}

	public DocStream getResultStream() throws IOException {
		return resultStream;
	}

}
