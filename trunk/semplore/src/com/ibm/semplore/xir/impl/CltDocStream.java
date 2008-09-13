/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 *
 * $Id: CltDocStream.java,v 1.2 2008/09/01 09:53:14 lql Exp $
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
public class CltDocStream implements DocStream {

	protected MemCollector clt = null;
	protected int count = 0;
	
	public CltDocStream(MemCollector clt){
		this.clt = clt;
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
			return clt.getId(count-1);
		}catch(Exception e){
			throw new RuntimeException("IOException", e);
		}
	}

	/* (non-Javadoc)
	 * @see com.ibm.semplore.xir.DocStream#getLen()
	 */
	public int getLen() {
		return clt.size();
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
		if( count==clt.size() ) return false;
		count++;
		return true;
	}

	/* (non-Javadoc)
	 * @see com.ibm.semplore.xir.DocStream#score()
	 */
	public float score() {
		try{
			return clt.getScore(count-1)/clt.getMaxScore()*(float)0.9;//\alpha=0.9
		}catch(Exception e){
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
		if(idx<1 || idx>clt.size())
			return false;
		count = idx;
		return true;
	}

	public Object clone() {
        CltDocStream cl = new CltDocStream(clt);
        cl.count = 0;
        return cl;
	}
}
