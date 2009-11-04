package test;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;

public class DocumentFreqTest {

	public static void main(String[] args) throws Exception {
		Analyzer analyzer = new WhitespaceAnalyzer();
		Directory dir = new RAMDirectory();
		IndexWriter iwriter = new IndexWriter(dir, analyzer, true, 
				IndexWriter.MaxFieldLength.UNLIMITED);
		Document doc = new Document();
		doc.add(new Field("text", "1 1 2 2 3 3 4 4 5 5", Field.Store.YES, 
				Field.Index.ANALYZED));
		iwriter.addDocument(doc);
		doc = new Document();
		doc.add(new Field("text", "1 3 5", Field.Store.YES, 
				Field.Index.ANALYZED));
		iwriter.addDocument(doc);
		iwriter.optimize();
		iwriter.close();
		IndexReader ireader = IndexReader.open(dir);
		TermEnum te = ireader.terms(new Term("text", "5"));
		System.out.println("5 : " + te.docFreq());
		ireader.close();
		dir.close();
	}
}
