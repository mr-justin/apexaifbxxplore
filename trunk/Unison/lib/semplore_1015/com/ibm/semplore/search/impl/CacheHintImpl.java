/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 *
 * $Id: CacheHintImpl.java,v 1.3 2007/04/26 09:09:40 lql Exp $
 */
package com.ibm.semplore.search.impl;

import com.ibm.semplore.search.CacheHint;
import com.ibm.semplore.search.ResultSet;
import com.ibm.semplore.xir.DocStream;

/**
 * @author liu Qiaoling
 *
 */
public class CacheHintImpl implements CacheHint
{
    /**
     * the cache data
     */
    protected int[] data;

    /**
     * @param resultSet
     */
    protected CacheHintImpl(ResultSet resultSet) {
    	try {
	        int size = resultSet.getLength();
	        data = new int[size];
	        for (int i=0; i<size; i++) 
	            data[i] = resultSet.getDocID(i);
    	} catch(Exception e) {
    		e.printStackTrace();
    	}
    }
    
    /* (non-Javadoc)
     * @see com.ibm.semplore.search.CacheHint#getStream()
     */
    public DocStream getStream()
    {
        return new MEMDocStream(data, data.length);
    }

}
