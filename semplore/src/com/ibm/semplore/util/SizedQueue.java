/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 *
 * $Id: SizedQueue.java,v 1.2 2007/04/18 06:55:34 lql Exp $
 */
package com.ibm.semplore.util;

import sun.misc.Queue;

/**
 * Queue with size limit
 * 
 * @author zhangjie
 *
 */
public class SizedQueue extends Queue{
	/**
	 * Default queue size limit
	 */
	private static final int DEFAULT_QUEUE_SIZE_LIMIT = 10;
	
	private int size = 0;
	private int sizeLimit = DEFAULT_QUEUE_SIZE_LIMIT;
	
	
	/**
	 * The size of this queue will be given default value
	 */
	public SizedQueue() {
		super();
	}

	/**
	 * @param s The size of this queue
	 */
	public SizedQueue(int s) {
		this();
		if(size<=0) throw new IllegalArgumentException();
		sizeLimit = s;
	}

	/* (non-Javadoc)
	 * @see sun.misc.Queue#dequeue()
	 */
	public Object dequeue() throws InterruptedException {
		size--;
		return super.dequeue();
		
	}

	/* (non-Javadoc)
	 * @see sun.misc.Queue#enqueue(java.lang.Object)
	 */
	public synchronized void enqueue(Object arg0) {
		if(size==sizeLimit)
			return;
		super.enqueue(arg0);
		size++;
	}
	
	public synchronized int getSize(){
		return size;
	}
	
	public synchronized boolean canEnqueue(){
		return size<sizeLimit;
	}
}