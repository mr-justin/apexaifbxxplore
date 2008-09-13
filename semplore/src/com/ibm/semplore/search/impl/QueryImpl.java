/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 *
 * $Id: QueryImpl.java,v 1.3 2007/04/26 10:16:03 lql Exp $
 */
package com.ibm.semplore.search.impl;

import com.ibm.semplore.search.Query;

/**
 * @author liu Qiaoling
 *
 */
public abstract class QueryImpl implements Query
{

    /**
     * the text respresentation of the whole query
     */
    protected String text;

    /**
     * number of results to be returned
     */
//    protected int numberOfResults;
    
    /* (non-Javadoc)
     * @see com.ibm.semplore.search.Query#getText()
     */
    public String getText()
    {
        return text;
    }
    
    /* (non-Javadoc)
     * @see com.ibm.semplore.search.Query#setRequestedNumberOfResults(int)
     */
//    public void setRequestedNumberOfResults(int numberOfResults)
//    {
//        this.numberOfResults = numberOfResults;
//    }
    
    /* (non-Javadoc)
     * @see com.ibm.semplore.search.Query#getRequestedNumberOfResults()
     */
//    public int getRequestedNumberOfResults() {
//        return numberOfResults;
//    }

}
