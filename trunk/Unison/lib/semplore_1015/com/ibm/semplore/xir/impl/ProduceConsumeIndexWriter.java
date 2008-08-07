/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 *
 * $Id: ProduceConsumeIndexWriter.java,v 1.2 2007/04/18 06:55:34 lql Exp $
 */
package com.ibm.semplore.xir.impl;

import java.io.IOException;

import org.apache.lucene.document.Document;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;

/**
 * An index writer that employs the producer-consumer mode. 
 * 	One thread produces {@link RAMDirectory}, while another one consumes it.
 * 
 * Users of this class can declare several instances of this class. They can parallelly
 * 	build index and finally performs merge operation.
 * 
 * @author zhangjie
 *
 */
public class ProduceConsumeIndexWriter 
	extends LuceneIndexWriter implements Runnable 
{
    protected IDocumentProvider dataGen ;
    protected long maxMemUsageLimit ;
    protected JobDrop jobDrop ;
    
    /**
     * @param indexDirPath The index path
     * @param maxMemUsageLimit the maximum memory usage limit for this writer, in bytes
     * @throws IOException
     */
    public ProduceConsumeIndexWriter(
            String indexDirPath, 
            long maxMemUsageLimit ) throws IOException
    {
    	super(indexDirPath);
        if ( maxMemUsageLimit < 0 ) 
            throw new IllegalArgumentException();        
        this.maxMemUsageLimit = maxMemUsageLimit / 3;
        this.jobDrop = new JobDrop();
        dataGen = new FillableDocProvider();
    }
    
    /**
     * @param indexMethod
     * @param indexDirPath The index path
     * @param maxMemUsageLimit the maximum memory usage limit for this writer, in bytes
     * @throws IOException
     */
//    public ProduceConsumeIndexWriter(String indexMethod, 
//            String indexDirPath, 
//            long maxMemUsageLimit ) throws IOException
//    {
//        super(indexMethod, indexDirPath);
//        if ( maxMemUsageLimit < 0 ) 
//            throw new IllegalArgumentException();        
//        this.maxMemUsageLimit = maxMemUsageLimit / 3;
//        this.jobDrop = new JobDrop();
//        dataGen = new FillableDocProvider();
//    }
    
    /**
     * Set the document provider
     * 
     *@param g The index documents provider
     * 
     * @param provider
     */
    public void setDocumentProvider(IDocumentProvider provider){
    	if(provider==null )
    		throw new IllegalArgumentException();      
    	dataGen = provider;
    }
    
    public void addDocument(com.ibm.semplore.xir.Document doc) { 
    	if(dataGen instanceof IFillableDocProvider)
    		((IFillableDocProvider)dataGen).addDocument( convert(doc) );
    	else
    		throw new RuntimeException("Cannot add document to a unfillable document stream.");
	}
    
    public void run()
    {  	
        Thread dsWriterThread = new DiskIndexWriterThread();
        Thread memWriterThread = new MemoryIndexWriterThread();
        dsWriterThread.start();
        memWriterThread.start();
        try
        {
            dsWriterThread.join();
            memWriterThread.join();
        } catch (InterruptedException e)
        {
             e.printStackTrace();
        }
    }

    /**
     * An object to keep the RAMDirectoy as drops of job.
     * 
     * @author zhangjie
     *
     */
    class JobDrop 
    {
        private Directory indexDir;
        private boolean available ;
        
        public JobDrop() { indexDir = null; available = false ; }
        
        public synchronized Directory get()
        {
            while ( ! available )
                try {
                    wait();
                } catch (InterruptedException e) {}
            return indexDir ;
        }
        
        public synchronized void done()
        {
            indexDir = null ;
            available = false ;
            notifyAll();
        }
        
        public synchronized void put( Directory dir )
        {
            while ( available )
                try {
                    wait();
                } catch (InterruptedException e ) {}
            indexDir = dir ;
            available = true ;
            notifyAll();
        }
    }
    
    /**
     * Consumer thread. Merge RAMDirectory to disk
     * 
     * @author zhangjie
     *
     */
    class DiskIndexWriterThread extends Thread
    {
        public void run()
        {
            try
            {
                Directory ramDir;
                Directory[] dirs = new Directory[1];
                while ( (ramDir = jobDrop.get()) != null )
                {
                    dirs[0] = ramDir ;
                    fsWriter.addIndexes(dirs);
                    ramDir.close();
                    ramDir = null ;
                    jobDrop.done();
                }
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Producer thread. Generate RAMDirectory
     * 
     * @author zhangjie
     *
     */
    class MemoryIndexWriterThread extends Thread 
    {
        public void run()
        {
            try
            {
                boolean hasMoreData = true ;
                Document doc = null;
                SemploreAnalyzer ana = new SemploreAnalyzer();
                do 
                {
                    RAMIndexWriter riw = new RAMIndexWriter(
                    		new RAMDirectory(), ana, true);
                    riw.setMergeFactor( mergeFactor );
                    riw.setMaxFieldLength( maxFieldLength );
                    boolean outOfMem = false ;
                    int count = 0 ;
                    while ( !outOfMem )
                    {                        
                        if ( (doc = dataGen.next()) == null ) 
                        {
                            hasMoreData = false ;
                            break ;
                        }
                        riw.addDocument(doc);
                        count++;
                        if ( (count & 1023) == 0 )
                            if ( riw.getRAMSize() > maxMemUsageLimit )
                                outOfMem = true ;
                    }
                    riw.optimize();
                    jobDrop.put( riw.getRAMDirectory() );                 
                } 
                while ( hasMoreData );                
                jobDrop.put( null );   // signal for end             
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
}
