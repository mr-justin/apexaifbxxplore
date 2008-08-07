/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 *
 * $Id: HitsDocStream.java,v 1.2 2007/04/18 06:55:33 lql Exp $
 */
package com.ibm.semplore.xir.impl;

import java.io.IOException;

import org.apache.lucene.search.Hits;

import com.ibm.semplore.xir.DocStream;

/**
 * Encapsulate a {@link Hits} of a lucene query into a {@link DocStream}.
 * In order to construct this DocStream, the constructor parameter is a {@link Hits}
 * 	class instance which MUST be ordered by lucene id.
 * 
 * @author zhangjie
 *
 */
public class HitsDocStream implements DocStream {

	protected Hits hits = null;
	protected int count = 0;
	
	protected HitsDocStream(Hits hits){
		this.hits = hits;
	}
	
	/* (non-Javadoc)
	 * @see com.ibm.semplore.xir.DocStream#count()
	 */
	public int count() {
		return count;
	}

	/* (non-Javadoc)
	 * @see com.ibm.semplore.xir.DocStream#doc()
	 */
	public int doc() {
		try{
			return hits.id(count-1);
		}catch(IOException e){
			throw new RuntimeException("IOException", e);
		}
	}

	/* (non-Javadoc)
	 * @see com.ibm.semplore.xir.DocStream#getEstimatedNumberOfCompleteResults()
	 */
	public int getEstimatedNumberOfCompleteResults() {
		return hits.length();
	}

	/* (non-Javadoc)
	 * @see com.ibm.semplore.xir.DocStream#getLen()
	 */
	public int getLen() {
		return hits.length();
	}

	/* (non-Javadoc)
	 * @see com.ibm.semplore.xir.DocStream#init()
	 */
	public void init() throws IOException {
		next();
	}

	/* (non-Javadoc)
	 * @see com.ibm.semplore.xir.DocStream#next()
	 */
	public boolean next() throws IOException {
		if( count==hits.length() ) return false;
		count++;
		return true;
	}

	/* (non-Javadoc)
	 * @see com.ibm.semplore.xir.DocStream#score()
	 */
	public float score() {
		try{
			return hits.score(count-1);
		}catch(IOException e){
			throw new RuntimeException("IOException", e);
		}
	}

	/* (non-Javadoc)
	 * @see com.ibm.semplore.xir.DocStream#skipTo(int)
	 */
	public boolean skipTo(int target) throws IOException {
        while (doc() < target) {
            if (!next())
                return false;
        }
        return true;
	}

	/* (non-Javadoc)
	 * @see com.ibm.semplore.xir.DocStream#skipToIndex(int)
	 */
	public boolean skipToIndex(int idx) throws IOException {
		idx++;
		if(idx<1 || idx>hits.length())
			return false;
		count = idx;
		return true;
	}

	public Object clone() {
        HitsDocStream cl = new HitsDocStream(hits);
        cl.count = 0;
        return cl;
	}
}
