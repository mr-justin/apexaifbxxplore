/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 *
 * $Id: DocStream.java,v 1.3 2008/01/10 11:15:03 lql Exp $
 */
package com.ibm.semplore.xir;

import java.io.IOException;

/**
 * DocStream provides an interface for reading a document stream.
 * @author liu Qiaoling
 *
 */
public interface DocStream extends Cloneable {
    /**
     * Returns the length of the document stream.
     * @return the length of the document stream.
     */
    public int getLen();
    
    /**
     * Returns the id of the current document.
     * @return the id of the current document.
     */
    public int doc();
    
    /**
     * Returns the score of the current document.
     * @return
     */
    public float score();
    
    /**
     * @return The number of items that have been read in the stream currently
     */
    public int count();

    /**
     * Initialization.
     * @throws IOException
     */
    public void init() throws IOException;
    
    /**
     * Move to the next document in the stream.
     * @return false iff there is no next document in the stream.
     * @throws IOException
     */
    public boolean next() throws IOException;
    
    /**
     * Jump to the first document with id >= target in the stream.
     * @param target the target id to skip to.
     * @return false iff there is no document with id >= target in the stream.
     * @throws IOException
     */
    public boolean skipTo(int target) throws IOException;
    
    /**
     * Skip in the stream to the idx-th item, that the current doc will be the idx-th.(idx start from 0)
     * 
     * @param idx The subscript to be reached
     * @return True if current pointer is exactly at the parameter position
     */
    public boolean skipToIndex(int idx) throws IOException ;
    
    /**
     * Returns a clone of this DocStream.
     * @return
     */
    public Object clone();
    
}
