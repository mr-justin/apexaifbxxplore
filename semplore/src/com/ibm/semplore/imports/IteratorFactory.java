/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 *
 * $Id: IteratorFactory.java,v 1.2 2007/04/18 06:55:34 lql Exp $
 */
package com.ibm.semplore.imports;

import java.util.Iterator;
import java.util.Properties;

/**
 * An interator factory that create {@link Iterator} with given
 * {@link Properties} constraints.
 * 
 * @author zhangjie
 * 
 */
public interface IteratorFactory {

    /**
     * Property name for indicating the range start (inclusive).
     */
    public static final String RANGE_START = "range_start";

    /**
     * Property name for indicating the range end (exclusive).
     */
    public static final String RANGE_END = "range_end";

    /**
     * Property name for indicating the interval.
     */
    public static final String INTERVAL = "interval";

    /**
     * Create an iterator with given constraints. Typically, constraints are
     * {@link #RANGE_START} and {@link #RANGE_END}, these two constaints may be
     * integers indicating instances with id in this specified range should be
     * returned in the iterator.
     * 
     * Implementation of this interface should check the prop parameter to get
     * the {@link #RANGE_START} and {@link #RANGE_END} values.
     * 
     * @param prop Contains the constraints names and values
     * @return an {@link InstanceDocumentIterator} that iterates on eligible
     *         {@link Instance} in a wrapped form {@link InstanceDocument}
     */
    public InstanceDocumentIterator createInstanceDocumentIterator(
        Properties prop);
}
