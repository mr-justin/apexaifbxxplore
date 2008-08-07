/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 */
package com.ibm.semplore.search;

import com.ibm.semplore.xir.DocStream;

/**
 * This interface provides descriptive information about a cache hint for some query results, which could be used by SearchHelper.
 * @author liu Qiaoling
 *
 */
public interface CacheHint {

    /**
     * Return the stream reader of this cache.
     * @return
     */
    public DocStream getStream();
    
}
