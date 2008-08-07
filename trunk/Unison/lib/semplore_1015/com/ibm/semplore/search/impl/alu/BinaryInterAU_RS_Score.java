/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 *
 * $Id: BinaryInterAU_RS.java,v 1.2 2007/04/18 06:55:34 lql Exp $
 */
package com.ibm.semplore.search.impl.alu;

import java.io.IOException;

import com.ibm.semplore.search.impl.MEMDocStream_Score;
import com.ibm.semplore.xir.DocStream;

/**
 * BinaryInterAU_RS provides arithmetic functionality of computing the intersection of two set of docs, using Reservoir Sampling for estimated computation. 
 * @author liu Qiaoling
 *
 */
public class BinaryInterAU_RS_Score extends BinaryInterAU
{

    /**
     * the default value of sample size
     */
    protected final int DEFAULT_SAMPLE_SIZE = 1000;
    
    /**
     * the sample size 
     */
    protected int sample_size = DEFAULT_SAMPLE_SIZE;
    
    /**
     * Set the sample size. 
     * @param sample_size
     */
    public void setSampleSize(int sample_size) {
        this.sample_size = sample_size;
    }
    
    /* (non-Javadoc)
     * @see com.ibm.semplore.search.impl.alu.ArithmeticUnit#getResult()
     */
    public DocStream getResult() throws IOException
    {
    	int length = stream1.getLen()>stream2.getLen() ? stream2.getLen() : stream1.getLen();
    	float[] score = new float[length];
        int[] values = new int[length];
        int idx = 0;
        if (length > 0) {                
            stream1.init();
            stream2.init();
            while (true){
                if (stream2.doc() < stream1.doc() && !stream2.skipTo(stream1.doc()))
                    break;            
                if (stream1.doc() < stream2.doc() && !stream1.skipTo(stream2.doc()))
                    break;
                if (stream1.doc() == stream2.doc()){
                	score[idx] = stream1.score() * stream2.score();
                    values[idx++] = stream2.doc();
                    if (!stream1.next())
                        break;
                }
            }   
        }
        return new MEMDocStream_Score(values, score, idx);        
    }

}
