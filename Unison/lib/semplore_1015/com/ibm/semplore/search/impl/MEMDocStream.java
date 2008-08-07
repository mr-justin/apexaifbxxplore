/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 *
 * $Id: MEMDocStream.java,v 1.3 2007/04/26 09:09:47 lql Exp $
 */
package com.ibm.semplore.search.impl;

import java.io.IOException;
import java.util.Iterator;

import com.ibm.semplore.xir.DocStream;

/**
 * MEMDocStream provides maintenance of a doc stream in memory.
 * @author liu Qiaoling
 *
 */
public class MEMDocStream implements DocStream
{

    /**
     * the doc array 
     */
    protected int[] docs;
    
    protected int len;
        
    /**
     * the position in the array
     */
    protected int pos;
            
    /**
     * Create a MEMDocStream based on the given doc array and length.
     * @param basedocs
     * @param len
     */
    public MEMDocStream(int[] basedocs, int len) {
        docs = basedocs;
        this.len = len;
    }
    
    /* (non-Javadoc)
     * @see com.ibm.semplore.search.impl.DocStream#clone()
     */
    public Object clone()
    {
        MEMDocStream cl = new MEMDocStream(docs, len);
        cl.pos = 0;
        return cl;
    }

    /* (non-Javadoc)
     * @see com.ibm.semplore.search.impl.DocStream#count()
     */
    public int count()
    {
        return pos+1;
    }
        
    /* (non-Javadoc)
     * @see com.ibm.semplore.search.impl.DocStream#doc()
     */
    public int doc()
    {
        return docs[pos];
    }

    /* (non-Javadoc)
     * @see com.ibm.semplore.search.impl.DocStream#getLen()
     */
    public int getLen()
    {
        return len;
    }

    /* (non-Javadoc)
     * @see com.ibm.semplore.search.impl.DocStream#init()
     */
    public void init()
    {
        pos = 0;
    }

    /* (non-Javadoc)
     * @see com.ibm.semplore.search.impl.DocStream#next()
     */
    public boolean next() throws IOException
    {
        pos++;
        if (pos < len) 
            return true;
        return false;        
    }

    /* (non-Javadoc)
     * @see com.ibm.semplore.search.impl.DocStream#score()
     */
    public float score()
    {
        return 1;
    }

    /* (non-Javadoc)
     * @see com.ibm.semplore.search.impl.DocStream#skipTo(int)
     */
    public boolean skipTo(int target) throws IOException
    {
        while (doc() < target) {
            if (!next())
                return false;
        }
        return true;
    }

    /* (non-Javadoc)
     * @see com.ibm.semplore.xir.DocStream#skipToIndex(int)
     */
    public boolean skipToIndex(int idx) throws IOException 
    {
        pos = idx;
        if (pos < len)
            return true;
        return false;
    }

}
