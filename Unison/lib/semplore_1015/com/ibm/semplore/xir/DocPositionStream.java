/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 *
 * $Id: DocPositionStream.java,v 1.2 2007/04/18 06:55:33 lql Exp $
 */
package com.ibm.semplore.xir;

import java.io.IOException;

/**
 * A reader into the position stream of a dual lined position structure.
 * @author liu Qiaoling
 *
 */
public interface DocPositionStream extends DocStream {

    /**
     * @return The position length of current position stream for a certain term in
     *  a certain document
     */
    public int genPositionLen();
        
    /**
     * @return true if next position is available in the position stream
     */
    public boolean hasNextPosition();
    
    /**
     * Read the next position value 
     * CAUTION: do test if next position is available first using {@link hasNextPosition()}
     * 
     * @return The next position value
     * @throws IOException
     */
    public int nextPosition() throws IOException;
    
    /**
     * @return The current position
     */
    public int getPosition();
        
    /**
     * Skip on postions in the position stream. The consequence is that
     *  the pointer will point to the place >= target
     * 
     * @param target Target to skip to
     * @return false if no position >= target in the positions
     * @throws IOException
     */
    public boolean skipPositionTo(int target) throws IOException;

}
