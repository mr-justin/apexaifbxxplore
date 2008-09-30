package org.ateam.xxplore.core.service.search;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Searcher;
import org.ateam.xxplore.core.ExploreEnvironment;
import org.ateam.xxplore.core.service.IService;
import org.ateam.xxplore.core.service.IServiceListener;
import org.ateam.xxplore.core.service.search.htmlparser.HTMLDocument;
import org.ateam.xxplore.core.service.search.pdfparser.PDFDocumentParser;
public class DocumentIndexService implements IService {

	private static Logger s_log = Logger.getLogger(DocumentIndexService.class);

	private Searcher m_docSearcher;

	private StandardAnalyzer m_analyzer;

	private String[] m_docFields={"contents","path","summary","Author","title","Title"};

	private BooleanClause.Occur[] m_docFlags = {BooleanClause.Occur.MUST,BooleanClause.Occur.SHOULD,
			BooleanClause.Occur.SHOULD,BooleanClause.Occur.SHOULD,BooleanClause.Occur.SHOULD,
			BooleanClause.Occur.SHOULD};


	private DocumentIndexService(){
		m_analyzer = new StandardAnalyzer();
	}

	private static class SingletonHolder {
		private static DocumentIndexService s_instance = new DocumentIndexService();
	} 

	public static DocumentIndexService getInstance() {
		return SingletonHolder.s_instance;
	}

	public void callService(IServiceListener listener, Object... params) {
		// TODO Auto-generated method stub

	}

	public void disposeService() {
		try {
			m_docSearcher.close();
			m_analyzer = null;

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void init(Object... params) {}

	public Hits searchDocuments(String query){
		try {
			if (m_docSearcher == null) {
				s_log.debug("Open index " + ExploreEnvironment.DOC_INDEX_DIR + " and init doc searcher!");
				m_docSearcher = new IndexSearcher(ExploreEnvironment.DOC_INDEX_DIR);
			}
			return m_docSearcher.search(MultiFieldQueryParser.parse(query, m_docFields, m_docFlags, m_analyzer));

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	public void indexFiles(File root, boolean create, String index){
		
		IndexWriter writer = null;
		
		try
		{		
			writer = new IndexWriter(index, m_analyzer, create);
		}
		catch(FileNotFoundException e)
		{
			try
			{
				s_log.debug("FileNotFoundException: "+e.getMessage());
				s_log.debug("Try creating new index.");
				
				writer = new IndexWriter(index, m_analyzer, true);
				
			}
			catch(FileNotFoundException s)
			{
				s.printStackTrace();
			}
			catch(IOException s)
			{
				s.printStackTrace();
			}
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}		
		try
		{
			if(writer == null)
			{
				s_log.debug("IndexWriter is null - something went wrong. Please check stack trace.");
				return;
			}
			
			indexDocs(root, index, create, writer); // add new docs
	
			s_log.info("Optimizing index...");
			writer.optimize();
			writer.close();
	
			//refresh searcher
			m_docSearcher = new IndexSearcher(ExploreEnvironment.DOC_INDEX_DIR);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Walk directory hierarchy in uid order, while keeping uid iterator from
	 * existing index in sync.  Mismatches indicate one of: (a) old documents to
	 * be deleted; (b) unchanged documents, to be left alone; or (c) new
	 * documents, to be indexed.
	 *
	 * @param file The directory to index.
	 * @param index The index to add the file to.
	 * @param create A flag telling if we should create the index.
	 *
	 * @throws Exception If there is any error indexing the directory.
	 */
	private void indexDocs(File file, String index, boolean create, IndexWriter writer) throws Exception{
		//TODO index only new doucements! 
		indexDocs(file, writer);
	}

	private void indexDocs(File file, IndexWriter writer) throws Exception {
		if (file.isDirectory()){             
			// if a directory
			String[] files = file.list();         // list its files
			Arrays.sort(files);           // sort the files
			for (int i = 0; i < files.length; i++) {
				indexDocs(new File(file, files[i]), writer);
			}
		}

		else {
			try {
				addDocument( file, writer );
			}
			catch( IOException e ){

			}

		}
	}

	private void addDocument( File file, IndexWriter writer ) throws IOException, InterruptedException {
		String path = file.getName().toUpperCase();
		Document doc = null;
		//Gee, this would be a great place for a command pattern
		if( path.endsWith(".HTML") || // index .html files
				path.endsWith(".HTM") || // index .htm files
				path.endsWith(".TXT")) {
//			System.out.println( "Indexing HTML , Text document: " + file );
			doc = HTMLDocument.Document(file);
		}

		else if( path.endsWith(".PDF")){
//			System.out.println( "Indexing PDF document: " + file );
			doc = PDFDocumentParser.getLuceneDocument(file);
		}

		else{
//			System.out.println( "Indexing File document: " + file );
			doc = FileDocument.Document(file);
		}

		if( doc != null ){
			writer.addDocument(doc);
		}
		
	}
}
