/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 *
 * $Id: MassUnionThenInterAU.java,v 1.2 2007/04/18 06:55:34 lql Exp $
 */
package com.ibm.semplore.search.impl.alu;

import java.io.IOException;

import com.ibm.semplore.xir.DocPositionStream;
import com.ibm.semplore.xir.DocStream;

/**
 * MassUnionThenInterAU provides arithmetic functionality that given a relation R, a set of subjects S and a set of instances C, it computes the intersection of C and the union of the objects with respect to those subjects. Note that all the objects of the relation R should also be known. 
 * @author liu Qiaoling
 *
 */
public abstract class MassUnionThenInterAU extends ArithmeticUnit
{

    /**
     * the relation R
     */
    protected DocPositionStream relationStream;
    
    /**
     * the set of subjects S
     */
    protected DocStream subjectStream;

    /**
     * the complete objects of relation R
     */
    protected DocStream CobjStream;
    
    /**
     * the set of instances C
     */
    protected DocStream interStream;
    
    /**
     * Set the relation R, the set of subjects S, the set of all the objects of R, and the set of instances C
     * @param relationStream
     * @param subjectStream
     * @param objectStream
     * @param interStream
     */
    public void setParameters(DocPositionStream relationStream, DocStream subjectStream, DocStream CobjStream, DocStream interStream) {
        this.relationStream = relationStream;
        this.subjectStream = subjectStream;
        this.CobjStream = CobjStream;
        this.interStream = interStream;
    }

    /**
     * Get the intersection of the two streams, the intersection values will be the the internal indices
     *  of the list represented by the first argument DocStream. 
     * E.g.  {1,3,4},{1,4,6} will produce int[]{0,2}.(1's index is 0, and 4's index is 2 in the first list)
     * 
     * @param stream1
     * @param stream2
     * @return
     * @throws IOException
     */
    protected int[] intersectionOfUsingInternalID(DocStream stream1, DocStream stream2) throws IOException {
        int[] values = new int[stream1.getLen()>stream2.getLen() ? stream2.getLen() : stream1.getLen()];
        int idx = 0;
        if (values.length > 0) {                
            stream1.init();
            stream2.init();
            while(true){
                if(!stream2.skipTo(stream1.doc()))
                    break;
                if(!stream1.skipTo(stream2.doc()))
                    break;
                if(stream1.doc()==stream2.doc()){
                    values[idx++] = stream1.count()-1;
                    if(!stream1.next()) break;
                }
            }
        }
        int[] result = new int[idx];
        for(int i=0;i<idx;i++)
            result[i] = values[i];
        return result;
    }    
}
