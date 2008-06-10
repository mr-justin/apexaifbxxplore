package org.aifb.xxplore.misc;

import org.apache.lucene.document.Document;
import org.xmedia.oms.query.ITuple;

public class ScoredDocumentITupleContainer {
	private ITuple ituple;
	private Document document;
	private float score;

	public static int CONTAINS_DOC = 2;
	public static int CONTAINS_TUPLE = 1;
	public static int CONTAINS_BOTH = CONTAINS_DOC + CONTAINS_TUPLE; 
	
	public ScoredDocumentITupleContainer(ITuple ituple, Document document, float score) {
		super();
		this.ituple = ituple;
		this.document = document;
		this.score = score;
	}

	/**
	 * returns: 
	 * 1 = CONTAINS_DOC if tuple is contained;
	 * 2 = CONTAINS_TUPLE if document is contained;
	 * 3 = CONTAINS_BOTH if both are contained   
	 */
	public int getContainerType(){
		if(document != null && ituple != null) return CONTAINS_BOTH;
		if(document != null) return CONTAINS_DOC;
		if(ituple != null) return CONTAINS_TUPLE;
		return 0;
	}
	
	public Document getDocument() {
		return document;
	}
	public void setDocument(Document document) {
		this.document = document;
	}
	public ITuple getItuple() {
		return ituple;
	}
	public void setItuple(ITuple ituple) {
		this.ituple = ituple;
	}
	public float getScore() {
		return score;
	}
	public void setScore(float score) {
		this.score = score;
	}
}
