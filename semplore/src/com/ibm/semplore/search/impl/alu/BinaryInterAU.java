/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 *
 * $Id: BinaryInterAU.java,v 1.2 2007/04/18 06:55:34 lql Exp $
 */
package com.ibm.semplore.search.impl.alu;

import com.ibm.semplore.xir.DocStream;

/**
 * BinaryInterAU provides arithmetic functionality of computing the intersection of two set of docs. 
 * @author liu Qiaoling
 *
 */
public abstract class BinaryInterAU extends ArithmeticUnit
{

    /**
     * The first stream for binary intersection.
     */
    protected DocStream stream1;
    
    /**
     * The second stream for binary intersection.
     */
    protected DocStream stream2;
    
    /**
     * Set the two streams for binary intersection.
     * @param stream1
     * @param stream2
     */
    public void setParameters(DocStream stream1, DocStream stream2) {
        this.stream1 = stream1;
        this.stream2 = stream2;
    }
    
}
