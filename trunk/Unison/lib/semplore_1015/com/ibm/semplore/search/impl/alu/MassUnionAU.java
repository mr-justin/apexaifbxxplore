/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 *
 * $Id: MassUnionAU.java,v 1.2 2007/04/18 06:55:34 lql Exp $
 */
package com.ibm.semplore.search.impl.alu;

import com.ibm.semplore.xir.DocPositionStream;
import com.ibm.semplore.xir.DocStream;

/**
 * MassUnionAU provides arithmetic functionality that given a relation R and a set of subjects S, it computes the union of the objects with respect to those subjects. Note that all the objects of the relation R should also be known. 
 * @author liu Qiaoling
 *
 */
public abstract class MassUnionAU extends ArithmeticUnit
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
     * Set the relation R, the set of subjects S, and the set of all the objects of R
     * @param relationStream
     * @param subjectStream
     * @param objectStream
     */
    public void setParameters(DocPositionStream relationStream, DocStream subjectStream, DocStream CobjStream) {
        this.relationStream = relationStream;
        this.subjectStream = subjectStream;
        this.CobjStream = CobjStream;
    }

}
