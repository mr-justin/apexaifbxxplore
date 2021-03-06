/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 *
 * $Id: ArithmeticUnit.java,v 1.3 2008/01/10 11:13:25 lql Exp $
 */
package com.ibm.semplore.search.impl.alu;

import java.io.IOException;

import com.ibm.semplore.xir.DocStream;

/**
 * ArithmeticUnit provides some basic arithmetic functionality, which allow both accurate and estimated computation. 
 * @author liu Qiaoling
 *
 */
public abstract class ArithmeticUnit
{

    /**
     * Returns the accurate results after accurate computation.
     * @return
     * @throws IOException
     */
    public abstract DocStream getResult() throws IOException;
    
}
