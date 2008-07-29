package org.aifb.xxplore.core.service.keywordtrans;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.aifb.xxplore.core.ExploreEnvironment;
import org.aifb.xxplore.core.service.IService;
import org.aifb.xxplore.core.service.IServiceListener;
import org.aifb.xxplore.core.service.query.htmlparser.HTMLDocument;
import org.aifb.xxplore.core.service.query.pdfparser.PDFDocumentParser;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;
import org.xmedia.oms.model.api.IConcept;
import org.xmedia.oms.model.api.IIndividual;
import org.xmedia.oms.model.api.ILiteral;
import org.xmedia.oms.model.api.INamedConcept;
import org.xmedia.oms.model.api.IProperty;
import org.xmedia.oms.model.api.IResource;
import org.xmedia.oms.model.impl.DataProperty;
import org.xmedia.oms.model.impl.Literal;
import org.xmedia.oms.model.impl.NamedConcept;
import org.xmedia.oms.model.impl.NamedIndividual;
import org.xmedia.oms.model.impl.ObjectProperty;
import org.xmedia.oms.persistence.PersistenceUtil;
import org.xmedia.oms.persistence.dao.IConceptDao;
import org.xmedia.oms.persistence.dao.IIndividualDao;
import org.xmedia.oms.persistence.dao.ILiteralDao;
import org.xmedia.oms.persistence.dao.IPropertyDao;

public class LuceneQueryService implements IService {

	private static Logger s_log = Logger.getLogger(LuceneQueryService.class);

	private boolean m_KbReader_isClosed = false;

	//already indexed datasources
	private Set<String> m_indexedDS;

	private Searcher m_docSearcher;

	private Searcher m_kbSearcher;

	private StandardAnalyzer m_analyzer;

	private String[] m_docFields={"contents","path","summary","Author","title","Title"};

	private BooleanClause.Occur[] m_docFlags = {BooleanClause.Occur.MUST,BooleanClause.Occur.SHOULD,
			BooleanClause.Occur.SHOULD,BooleanClause.Occur.SHOULD,BooleanClause.Occur.SHOULD,
			BooleanClause.Occur.SHOULD};


	private LuceneQueryService(){
		m_indexedDS = new HashSet<String>();
		m_analyzer = new StandardAnalyzer();
	}

	private static class SingletonHolder {
		private static LuceneQueryService s_instance = new LuceneQueryService();
	} 

	public static LuceneQueryService getInstance() {
		return SingletonHolder.s_instance;
	}

	public void callService(IServiceListener listener, Object... params) {
		// TODO Auto-generated method stub

	}

	public void disposeService() {
		try {
			m_docSearcher.close();
			m_kbSearcher.close();
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

	public Collection<IResource> searchKb(String query){
		Collection<IResource> ress = new ArrayList<IResource>();
		try {
			if (m_kbSearcher ==null){
				s_log.debug("Open index " + ExploreEnvironment.KB_INDEX_DIR + " and init kb searcher!");
				m_kbSearcher = new IndexSearcher(ExploreEnvironment.KB_INDEX_DIR);
			}
			QueryParser parser = new QueryParser("label", m_analyzer);
			Query q = parser.parse(query);
			if (q instanceof BooleanQuery){
				BooleanClause[] clauses = ((BooleanQuery)q).getClauses();
				for(int i = 0; i < clauses.length; i++){
					Query clauseQ = clauses[i].getQuery();
					IResource res = searchWithClause(clauseQ);
					if(res != null) ress.add(res);
				}
			}
			//is variations of phrase or term query 
			else{
				IResource res = searchWithClause(q);
				if(res != null) ress.add(res);
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return ress;
	}

	private IResource searchWithClause(Query clausequery){
		Document doc = null; 
		try {
			Hits results = m_kbSearcher.search(clausequery);
			if (results == null || results.length() == 0){
				Set<Term> term = new HashSet<Term>();
				clausequery.extractTerms(term);
				//if clause query is a term query
				if(term.size() == 1){
					results = m_kbSearcher.search(new FuzzyQuery(term.iterator().next()));
				}
			}
			//get the top result
			if(results != null && results.length() > 0) 
				doc = results.doc(0);
		} 

		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(doc != null){
			String type = doc.get("type");
			if(type.equals(LITERAL)){
				return new Literal(pruneString(doc.get("label")));
			}
			else if(type.equals(INDIVIDUAL)){
				return new NamedIndividual(pruneString(doc.get("uri")));
			}
			else if(type.equals(CONCEPT)){
				return new NamedConcept(pruneString(doc.get("uri")));
			}
			else if(type.equals(OBJECTPROPERTY)){
				return new ObjectProperty(pruneString(doc.get("uri")));
			}
			else if(type.equals(DATAPROPERTY)){
				return new DataProperty(pruneString(doc.get("uri")));
			}

		}

		return null;
	}

	private String pruneString(String str) {
		return str.replace("\"", "");
	}

	private static String CONCEPT = "concept";
	private static String OBJECTPROPERTY = "objectproperty";
	private static String DATAPROPERTY = "dataproperty";
	private static String INDIVIDUAL = "indiviudal";
	private static String LITERAL = "literal";
	public void indexDataSourceByEntity (String datasourceUri){
		if(isIndexingRequired(datasourceUri)){
			s_log.debug("Creating the index for " + " datasource " + datasourceUri + " .....!");

			IConceptDao conceptDao = (IConceptDao) PersistenceUtil.getDaoManager().getAvailableDao(IConceptDao.class);
			List concepts = conceptDao.findAll();
			IIndividualDao individualDao = (IIndividualDao) PersistenceUtil.getDaoManager().getAvailableDao(IIndividualDao.class);
			List individuals = individualDao.findAll();
			IPropertyDao propertyDao = (IPropertyDao) PersistenceUtil.getDaoManager().getAvailableDao(IPropertyDao.class);
			List properties = propertyDao.findAll(); 
			ILiteralDao literalDao = (ILiteralDao) PersistenceUtil.getDaoManager().getAvailableDao(ILiteralDao.class);
			List literals = literalDao.findAll();
			try {

				File file = new File(ExploreEnvironment.KB_INDEX_DIR);
				IndexWriter indexWriter = new IndexWriter(file, m_analyzer, true);
				String uri = "";

				for (Object concept:concepts){
					Document doc = new Document();
					doc.add(new Field("type", CONCEPT, Field.Store.YES, Field.Index.NO));
					doc.add(new Field("label", ((IConcept)concept).getLabel(), Field.Store.NO, Field.Index.TOKENIZED));
					if (concept instanceof NamedConcept) {
						uri = ((NamedConcept)concept).getUri();
						doc.add(new Field("uri", uri, Field.Store.YES, Field.Index.NO));
					}

					indexWriter.addDocument(doc);
				}
				for (Object property:properties){

					Document doc = new Document();
					doc.add(new Field("label", ((IProperty)property).getLabel(), Field.Store.NO,Field.Index.TOKENIZED));
					doc.add(new Field("uri", ((IProperty)property).getUri(), Field.Store.YES, Field.Index.NO));
					if(property instanceof ObjectProperty)
						doc.add(new Field("type", OBJECTPROPERTY, Field.Store.YES, Field.Index.NO));
					else
						doc.add(new Field("type", DATAPROPERTY, Field.Store.YES, Field.Index.NO));
					indexWriter.addDocument(doc);
				}
				for (Object individual:individuals){
					Document doc = new Document();
					doc.add(new Field("type", INDIVIDUAL, Field.Store.YES, Field.Index.NO));
					doc.add(new Field("label", ((IIndividual)individual).getLabel(), Field.Store.NO, Field.Index.TOKENIZED));
					if (individual instanceof NamedIndividual) {
						uri = ((NamedIndividual)individual).getUri();
						doc.add(new Field("uri", uri, Field.Store.YES, Field.Index.NO));
					}
					indexWriter.addDocument(doc);
				}
				//TODO have to index also the datatype and other infos for literal reconstruction
				for (Object literal:literals){
					Document doc = new Document();
					doc.add(new Field("type", LITERAL, Field.Store.YES, Field.Index.NO));
					doc.add(new Field("label", ((ILiteral)literal).getLabel(), Field.Store.YES, Field.Index.TOKENIZED));
					indexWriter.addDocument(doc);
				}

				indexWriter.optimize();
				indexWriter.close();
				m_indexedDS.add(datasourceUri);
			}

			catch (IOException e) {
				s_log.error("Exception occurred while making index: " + e);
				//TODO handle excpetion
				e.printStackTrace();
			} 
		}
	}

	private boolean isIndexingRequired(String datasourceUri){
		if (!m_indexedDS.contains(datasourceUri)){
			File file = new File(ExploreEnvironment.KB_INDEX_DIR);
			//TODO check of the knowledgebase has been changed instead 
			if (file.list() == null || file.list().length <= 2) return true;
		}

		return false;
	}

	public void indexDataSourceByConcept(String datasourceUri){
		if(isIndexingRequired(datasourceUri)){

			if(s_log.isDebugEnabled()) s_log.debug("Creating the index for " + " datasource " + datasourceUri + " .....!");
			//creare Analyzer for the Knowlege Base

			IConceptDao conceptDao = (IConceptDao) PersistenceUtil.getDaoManager().getAvailableDao(IConceptDao.class);
			List concepts = conceptDao.findAll();

			try {

				File file = new File(ExploreEnvironment.KB_INDEX_DIR);
				IndexWriter indexWriter = new IndexWriter(file, m_analyzer, true);

				for (Object concept : concepts)
				{	
					Document doc = new Document();
					String label = ((IConcept)concept).getLabel();
					String uri = "";
					String relProps = "";
					String relCons = ""; 
					String relInds = "";
					if (concept instanceof NamedConcept) uri = ((NamedConcept)concept).getUri();

					Set<IProperty> relatedProps = ((INamedConcept)concept).getProperties();
					Set<IConcept> relatedCons = new HashSet<IConcept>();
					Set<IConcept> subconcepts = ((INamedConcept)concept).getSubconcepts();
					Set<IConcept> superconcepts = ((INamedConcept)concept).getSuperconcepts();
					Set<IIndividual>   relatedInds = ((INamedConcept)concept).getMemberIndividuals();
					if(subconcepts != null) relatedCons.addAll(subconcepts);
					if(superconcepts != null) relatedCons.addAll(superconcepts);

					if(relatedProps != null && relatedProps.size() != 0) { 
						for (IProperty prop : relatedProps) 
							relProps = relProps + prop.getLabel() + " "; 
					} 

					if(relatedCons != null && relatedCons.size() != 0){ 
						for (IConcept con : relatedCons) 
							relCons = relCons + con.getLabel() + " "; 
					} 

					if(relatedInds != null && relatedInds.size() != 0){ 
						for (IIndividual ind : relatedInds) 
							relInds = relInds + ind.getLabel() + " "; 
					}

					doc.add(new Field("subject", label, Field.Store.YES, Field.Index.TOKENIZED));
					doc.add(new Field("concepts", relCons, Field.Store.YES, Field.Index.TOKENIZED));
					doc.add(new Field("properties", relProps, Field.Store.YES, Field.Index.TOKENIZED));
					doc.add(new Field("individuals", relInds, Field.Store.YES, Field.Index.TOKENIZED));
					doc.add(new Field("uri", uri, Field.Store.YES, Field.Index.NO));

					indexWriter.addDocument(doc);

				}
				indexWriter.optimize();
				indexWriter.close();

				//add datasource to indexed list
				m_indexedDS.add(datasourceUri);
			}

			catch (IOException e) {
				s_log.error("Exception occurred while making index: " + e);
				//TODO handle excpetion
				e.printStackTrace();
			} 
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
			for (int i = 0; i < files.length; i++)    // recursively index them
				indexDocs(new File(file, files[i]), writer);
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
			System.out.println( "Indexing HTML , Text document: " + file );
			doc = HTMLDocument.Document(file);
		}

		else if( path.endsWith(".PDF")){
			System.out.println( "Indexing PDF document: " + file );
			doc = PDFDocumentParser.getLuceneDocument(file);
		}

		else{
			System.out.println( "Indexing File document: " + file );
			doc = FileDocument.Document(file);
		}

		if( doc != null ){
			writer.addDocument(doc);
		}
		
	}
}
