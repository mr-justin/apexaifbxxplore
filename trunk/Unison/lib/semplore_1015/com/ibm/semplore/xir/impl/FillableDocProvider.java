/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 *
 * $Id: FillableDocProvider.java,v 1.2 2007/04/18 06:55:34 lql Exp $
 */
package com.ibm.semplore.xir.impl;

import org.apache.lucene.document.Document;

import com.ibm.semplore.util.SizedQueue;

/**
 * A fillable lucene format document provider.
 * User is allowed to add document to this provider, the out sequence
 * 	will be the same as the in sequence, as a Queue.(Queue has a limit size)
 * 
 * Thread-safe caution:
 * 		Thread will be blocked if enqueue when queue is full;
 * 		    also will be blocked if dequeue when queue is empty.
 * 
 * @author zhangjie
 *
 */
public class FillableDocProvider implements IFillableDocProvider {

	protected final SizedQueue queue;
	
	protected FillableDocProvider(){
		queue = new SizedQueue();
	}
	
	protected FillableDocProvider(int size){
		queue = new SizedQueue(size);
	}
	
	/* (non-Javadoc)
	 * @see com.ibm.semplore.xir.impl.IDocumentProvider#addDocument(org.apache.lucene.document.Document)
	 */
	public void addDocument(Document doc) {
		synchronized( queue ){
			try{
				while(!queue.canEnqueue())
					queue.wait();
				queue.enqueue(doc);
				queue.notifyAll();
			}catch(InterruptedException e){
				System.out.println("Thread interrupted.");
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.ibm.semplore.xir.impl.IDocumentProvider#next()
	 */
	public Document next() {
		Document doc = null;
		synchronized( queue ){
			try{
				while(queue.isEmpty())
					queue.wait();
				doc = (Document)queue.dequeue();
				queue.notifyAll();
			}catch(InterruptedException e){
				System.out.println("Thread interrupted.");
			}
		}
		return doc;
	}

}
