package org.team.xxplore.core.service.q2semantic;

import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.FSDirectory;

/**
 * Using Lucene to implement a disk-based HashTable
 * @author kaifengxu
 *
 */
public class LuceneMap {

	private IndexWriter writer;
	private IndexSearcher searcher;
	public static String KEY_FIELD = "key";
	public static String VALUE_FIELD = "val";
	
	/**
	 * open writer
	 * @param indexDir
	 * @param create
	 * @throws Exception
	 */
	public void openWriter(String indexDir, boolean create) throws Exception
	{
		IndexReader.unlock(FSDirectory.getDirectory(indexDir)); 
		writer = new IndexWriter(indexDir, new StandardAnalyzer(), create);
	}
	
	/**
	 * close writer
	 * @throws Exception
	 */
	public void closeWriter() throws Exception
	{
		if(writer != null)
		{
			writer.optimize();
			writer.close();
		}
	}
	
	/**
	 * open searcher
	 * @param indexDir
	 * @throws Exception
	 */
	public void openSearcher(String indexDir) throws Exception
	{
		IndexReader.unlock(FSDirectory.getDirectory(indexDir)); 
		searcher = new IndexSearcher(indexDir);
	}
	
	/**
	 * close searcher
	 * @throws Exception
	 */
	public void closeSearcher() throws Exception
	{
		searcher.close();
	}
	
	/**
	 * put key and data into index
	 * @param key
	 * @param data
	 * @throws Exception
	 */
	public void put(String key, String data) throws Exception
	{
		if(writer != null)
		{
			Document doc = new Document();
			doc.add(new Field(KEY_FIELD, key, Field.Store.NO, Field.Index.NO_NORMS));
			doc.add(new Field(VALUE_FIELD, data, Field.Store.YES, Field.Index.NO));
			writer.addDocument(doc);
		}
	}
	
	/**
	 * Search the data for the key
	 * @param key
	 * @return
	 * @throws Exception
	 */
	public TreeSet<String> search(String key) throws Exception
	{
		if(searcher != null)
		{
			Query query = new TermQuery(new Term(KEY_FIELD, key));
			Hits hits = searcher.search(query);
			TreeSet<String> res = null;
			if(hits != null && hits.length() != 0)
			{
				res = new TreeSet<String>();
				for(int i=0; i<hits.length(); i++)
					res.add(hits.doc(i).get(VALUE_FIELD));
			}
			return res;
		}
		return null;
	}
	
	/**
	 * Search the number of the triples whose key is input parameter
	 * @param key
	 * @return
	 * @throws Exception
	 */
	public int searchNum(String key) throws Exception
	{
		if(searcher != null)
		{
			Query query = new TermQuery(new Term(KEY_FIELD, key));
			Hits hits = searcher.search(query);
			if(hits != null)
				return hits.length();
		}
		return 0;
	}
	
	/**
	 * Print all triples
	 * @throws Exception
	 */
	public void printAllTriples() throws Exception
	{
		if(searcher != null)
		for(int i=0; i<searcher.maxDoc(); i++)
		{
			Document doc = searcher.doc(i);
			System.out.println(doc.get(KEY_FIELD)+" | "+doc.get(VALUE_FIELD));
		}
	}
	
	/**
	 * Print all triples without Dup
	 * @throws Exception
	 */
	public void printAllTriplesNoDuplicated()throws Exception
	{
		if(searcher != null)
		{
			TreeMap<String, String> output = new TreeMap<String, String>();
			for(int i=0; i<searcher.maxDoc(); i++)
			{
				Document doc = searcher.doc(i);
				output.put(doc.get(KEY_FIELD), doc.get(VALUE_FIELD));
			}
			for(String key: output.keySet())
				System.out.println(key+" | "+output.get(key));
		}
	}
	
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		LuceneMap li = new LuceneMap();
		li.openWriter("D:/semplore/lucenetest", true);
		li.put("key1", "data1");
		li.put("key2", "data2");
		li.put("key1", "data2");
		li.put("key2", "data2");
		li.put("Key1", "Data1");
		li.closeWriter();
		li.openSearcher("D:/semplore/lucenetest");
		
		li.printAllTriples();
		Set<String> res = li.search("key2");
		for(String r: res)
			System.out.println(r);
		li.closeSearcher();
	}

}
