/**
 * 
 */
package com.ibm.semplore.xir.impl;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.store.Directory;

import com.ibm.semplore.xir.Document;

/**
 * @author zhangjie
 *
 */
public class MultiThreadIndexWriter extends LuceneIndexWriter {
	
	protected ProduceConsumeIndexWriter[] writers;
	protected Thread[] threads;
	protected IFillableDocProvider dataGen ;
	protected int maxMemSize;
	protected int numThreads;

	/**
	 * @param indexPath The physical index path
	 * @param maxMemSize Max memory size can be used
	 * @param numThreads Number of threads that will be launched
	 * @throws IOException
	 */
	protected MultiThreadIndexWriter(String indexPath, 
			int maxMemSize, int numThreads) throws IOException {
		super(indexPath);
		if( maxMemSize<(1<<10) )
			throw new IllegalArgumentException("Too small memory size.");
		if( numThreads<1 )
			throw new IllegalArgumentException("Number of threads should be larger than 0");
		
		this.maxMemSize = maxMemSize;
		this.numThreads = numThreads;
		int maxMemSizeForEach = maxMemSize / numThreads;
		dataGen = new FillableDocProvider();
		
		writers = new ProduceConsumeIndexWriter[numThreads];
		threads = new Thread[numThreads];
		for( int i=0;i<numThreads;i++ ){
			writers[i] = new ProduceConsumeIndexWriter(
					indexPath+File.separator+"temp"+i, maxMemSizeForEach);
			writers[i].setDocumentProvider(dataGen);
			threads[i] = new Thread( writers[i] );
			threads[i].start();
		}
	}
	
//    protected MultiThreadIndexWriter(String indexMethod, String indexPath, 
//            int maxMemSize, int numThreads) throws IOException {
//        super(indexMethod, indexPath);
//        if( maxMemSize<(1<<10) )
//            throw new IllegalArgumentException("Too small memory size.");
//        if( numThreads<1 )
//            throw new IllegalArgumentException("Number of threads should be larger than 0");
//        
//        this.maxMemSize = maxMemSize;
//        this.numThreads = numThreads;
//        int maxMemSizeForEach = maxMemSize / numThreads;
//        dataGen = new FillableDocProvider();
//        
//        writers = new ProduceConsumeIndexWriter[numThreads];
//        threads = new Thread[numThreads];
//        for( int i=0;i<numThreads;i++ ){
//            writers[i] = new ProduceConsumeIndexWriter(
//                    indexPath+File.separator+"temp"+i, maxMemSizeForEach);
//            writers[i].setDocumentProvider(dataGen);
//            threads[i] = new Thread( writers[i] );
//            threads[i].start();
//        }
//    }
    
	@Override
	public void close() throws IOException {
        addDocument(null);
		try{
			for( int i=0;i<numThreads;i++ )
				threads[i].join();
		} catch (InterruptedException e) {
             e.printStackTrace();
        }
		Directory[] dirs = new Directory[numThreads];
		for( int i=0;i<numThreads;i++ )
			dirs[i] = writers[i].getIndexDirectory();
		this.addIndexes(dirs);
		super.close();
		
		// delete temp files
		for( int i=0;i<numThreads;i++ ){
			String[] fnames = writers[i].getIndexDirectory().list();
            for (int j=0; j<fnames.length; j++)
            	writers[i].getIndexDirectory().deleteFile( fnames[j] );
            File partDir = new File( writers[i].getIndexPath() );
            partDir.delete();
		}
	}

	@Override
	public void setMaxFieldLength(int maxFieldLength) {
		super.setMaxFieldLength(maxFieldLength);
		for( int i=0;i<numThreads;i++ )
			writers[i].setMaxFieldLength(maxFieldLength);
	}

	@Override
	public void setMergeFactor(int mergeFactor) {
		super.setMergeFactor(mergeFactor);
		for( int i=0;i<numThreads;i++ )
			writers[i].setMergeFactor(mergeFactor);
	}

	/* (non-Javadoc)
	 * @see com.ibm.semplore.xir.IndexWriter#addDocument(com.ibm.semplore.xir.Document)
	 */
	public void addDocument(Document doc) {
		if( doc != null )
			dataGen.addDocument( convert(doc) );
		else{
			// the end signal
			for( int i=0;i<numThreads;i++ )
				dataGen.addDocument( null );
		}
	}
}
