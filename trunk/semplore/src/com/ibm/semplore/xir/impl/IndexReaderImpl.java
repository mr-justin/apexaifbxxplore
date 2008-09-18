/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 *
 * $Id: IndexReaderImpl.java,v 1.6 2008/09/12 09:21:22 xrsun Exp $
 */
package com.ibm.semplore.xir.impl;

import java.io.IOException;
import java.io.StringReader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.store.FSDirectory;

import com.ibm.semplore.xir.CompoundTerm;
import com.ibm.semplore.xir.DocPositionStream;
import com.ibm.semplore.xir.DocStream;
import com.ibm.semplore.xir.FieldType;
import com.ibm.semplore.xir.IndexReader;
import com.ibm.semplore.xir.Term;
import com.ibm.semplore.xir.IndexService.IndexType;

/**
 * @author zhangjie
 *
 */
public class IndexReaderImpl implements IndexReader {

	protected final org.apache.lucene.index.IndexReader reader;
	protected final IndexType type;
	protected final FSDirectory directory;
	
	public org.apache.lucene.index.IndexReader getReader() {
		return reader;
	}

	public IndexReaderImpl(String indexPath, IndexType type) throws IOException {
		reader = org.apache.lucene.index.IndexReader.open(indexPath);
		this.type = type;
		directory = null;
	}
	
	public IndexReaderImpl(String indexPath, IndexType type, boolean doDisableLocks) throws IOException {
		directory = FSDirectory.getDirectory(indexPath);
		directory.setDisableLocks(doDisableLocks);
		reader = org.apache.lucene.index.IndexReader.open(directory);
		this.type = type;
	}
	
	/* (non-Javadoc)
	 * @see com.ibm.semplore.xir.IndexReader#close()
	 */
	public void close() throws IOException {
		reader.close();
		if (directory!=null)
			directory.close();
	}

	/* (non-Javadoc)
	 * @see com.ibm.semplore.xir.IndexReader#getDocClass()
	 */
	public Class getDocClass() {
		try{
			if(type.equals(IndexType.Instance))
				return Class.forName("com.ibm.semplore.model.Instance");
			if(type.equals(IndexType.Category))
				return Class.forName("com.ibm.semplore.model.Category");
			if(type.equals(IndexType.Relation))
				return Class.forName("com.ibm.semplore.model.Relation");
			if(type.equals(IndexType.Attribute))
				return Class.forName("com.ibm.semplore.model.Attribute");			
		}catch(ClassNotFoundException e){
			e.printStackTrace();
		}
		throw new IllegalArgumentException();
	}

	/* (non-Javadoc)
	 * @see com.ibm.semplore.xir.IndexReader#getDocPositionStream(com.ibm.semplore.xir.Term)
	 */
	public DocPositionStream getDocPositionStream(Term term) throws IOException {
		if(term instanceof CompoundTerm){
			throw new UnsupportedOperationException();
		}else{ // simple term
			return (DocPositionStream)reader.termPositions(
					new org.apache.lucene.index.Term(
							term.getFieldType().toString(), 
							term.getString() )
					);       
		}
	}

	/* (non-Javadoc)
	 * @see com.ibm.semplore.xir.IndexReader#getDocStream(com.ibm.semplore.xir.Term)
	 */
	public DocStream getDocStream(Term term) throws IOException {
		return getDocStreamWithSort(term, Sort.INDEXORDER);
	}
	
    /* (non-Javadoc)
     * @see com.ibm.semplore.xir.IndexReader#getDocStreamByRelevanceOrder(com.ibm.semplore.xir.Term)
     */
    public DocStream getDocStreamByRelevanceOrder(Term term) throws IOException
    {
        return getDocStreamWithSort(term, Sort.RELEVANCE);
    }

    /**
     * Get a document stream from the index with given term by specified sort order.
     * @param term
     * @param sort
     * @return
     * @throws IOException
     */
    protected DocStream getDocStreamWithSort(Term term, Sort sort) throws IOException {        
        if(term instanceof CompoundTerm || term.getFieldType()==FieldType.TEXT){
            Searcher searcher = new IndexSearcher(reader);
            MemCollector clt = new MemCollector();
            searcher.search(toLuceneQuery(term), clt);
            return new CltDocStream(clt);
           
        }else{ // simple term
            return (DocStream)reader.termDocs(
                    new org.apache.lucene.index.Term(
                            term.getFieldType().toString(), 
                            term.getString() )
                    );       
        }

    }

/////////////////////////////////old codes by jackee//////////////////
//    protected DocStream getDocStreamWithSort(Term term, Sort sort) throws IOException {        
//        if(term instanceof CompoundTerm){
//            Searcher searcher = new IndexSearcher(reader);
//            Hits hits = searcher.search( toLuceneQuery(term), sort );
//            return new HitsDocStream(hits);
//        }else{ // simple term
//            return (DocStream)reader.termDocs(
//                    new org.apache.lucene.index.Term(
//                            term.getFieldType().toString(), 
//                            term.getString() )
//                    );       
//        }        
//    }
/////////////////////////////////old codes by jackee//////////////////

    private Query toLuceneQuery(Term term){
		
		if(term instanceof CompoundTerm){
			BooleanQuery query = new BooleanQuery();
			CompoundTerm cTerm = (CompoundTerm)term;
			Occur o = cTerm.getCompoundType() == 
				CompoundTerm.TYPE_AND ? Occur.MUST : Occur.SHOULD;
			for(int i=0;i<cTerm.getSize();i++)
				query.add( toLuceneQuery(cTerm.getTerm(i)), o);
			return query;
		}else
			if (term.getFieldType()==FieldType.TEXT) {
				String field = FieldType.TEXT.toString();
				String text = term.getString();				
			      try {
					    Analyzer analyzer = new StandardAnalyzer();
					    TokenStream tokenstream = analyzer.tokenStream(field, new StringReader(text));
	  					  PhraseQuery query = new PhraseQuery();
					    for (Token t = tokenstream.next(); t!=null; t=tokenstream.next()) {
						    query.add(new org.apache.lucene.index.Term(field, t.termText()));
					    }
					    query.setSlop(Integer.MAX_VALUE);				
					  return query;
			      } catch (Exception e) {
						return new TermQuery( 
								new org.apache.lucene.index.Term(term.getFieldType().toString(), 
										term.getString()) 
								);
			      }					
			} else {
				return new TermQuery( 
						new org.apache.lucene.index.Term(term.getFieldType().toString(), 
								term.getString()) 
						);
    		}
	}

	/* (non-Javadoc)
	 * @see com.ibm.semplore.xir.IndexReader#getFieldValues(int, com.ibm.semplore.xir.FieldType[])
	 */
	public String[] getFieldValues(int docID, FieldType[] types) throws IOException {
        Document document = reader.document(docID);
        String[] res = new String[types.length];
        for (int i=0; i<types.length; i++)
            res[i] = document.get(types[i].toString());
        return res;
	}

}
