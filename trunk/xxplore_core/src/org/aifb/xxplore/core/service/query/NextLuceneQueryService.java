package org.aifb.xxplore.core.service.query;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aifb.xxplore.core.ExploreEnvironment;
import org.aifb.xxplore.core.service.IService;
import org.aifb.xxplore.core.service.IServiceListener;
import org.aifb.xxplore.core.service.query.htmlparser.HTMLDocument;
import org.aifb.xxplore.core.service.query.pdfparser.PDFDocumentParser;
import org.aifb.xxplore.shared.util.Pair;
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
import org.apache.lucene.search.TermQuery;
import org.xmedia.oms.model.api.IConcept;
import org.xmedia.oms.model.api.IDataProperty;
import org.xmedia.oms.model.api.ILiteral;
import org.xmedia.oms.model.api.INamedConcept;
import org.xmedia.oms.model.api.INamedIndividual;
import org.xmedia.oms.model.api.IObjectProperty;
import org.xmedia.oms.model.api.IProperty;
import org.xmedia.oms.model.api.IPropertyMember;
import org.xmedia.oms.model.impl.DataProperty;
import org.xmedia.oms.model.impl.Literal;
import org.xmedia.oms.model.impl.NamedConcept;
import org.xmedia.oms.model.impl.ObjectProperty;
import org.xmedia.oms.model.impl.PropertyMember;
import org.xmedia.oms.model.impl.Resource;
import org.xmedia.oms.persistence.PersistenceUtil;
import org.xmedia.oms.persistence.dao.IConceptDao;
import org.xmedia.oms.persistence.dao.IIndividualDao;
import org.xmedia.oms.persistence.dao.ILiteralDao;
import org.xmedia.oms.persistence.dao.IPropertyDao;

public class NextLuceneQueryService implements IService {

	private static Logger s_log = Logger.getLogger(NextLuceneQueryService.class);

	private boolean m_KbReader_isClosed = false;
	
	private int numOfConcept;

	//already indexed datasources
	private Set<String> m_indexedDS;

	private Searcher m_docSearcher;

	private Searcher m_kbSearcher;

	private StandardAnalyzer m_analyzer;

	private String[] m_docFields={"contents","path","summary","Author","title","Title"};

	private BooleanClause.Occur[] m_docFlags = {BooleanClause.Occur.MUST,BooleanClause.Occur.SHOULD,
			BooleanClause.Occur.SHOULD,BooleanClause.Occur.SHOULD,BooleanClause.Occur.SHOULD,
			BooleanClause.Occur.SHOULD};


	private NextLuceneQueryService(){
		m_indexedDS = new HashSet<String>();
		m_analyzer = new StandardAnalyzer();
	}

	private static class SingletonHolder {
		private static NextLuceneQueryService s_instance = new NextLuceneQueryService();
	} 

	public static NextLuceneQueryService getInstance() {
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


	private String pruneString(String str) {
		return str.replace("\"", "");
	}
	
	
	public Map<String,Collection<KbElement>> searchKb(String query){
		Map<String,Collection<KbElement>> ress = new LinkedHashMap<String,Collection<KbElement>>();
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
					System.out.println(clauseQ.getClass());
					System.out.println(clauseQ.toString("label"));
					searchWithClause(clauseQ,ress);
					System.out.println();
				}
			}
			//is variations of phrase or term query 
			else{
				searchWithClause(q,ress);
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(ress);
		return ress;
	}

	private void searchWithClause(Query clausequery, Map<String,Collection<KbElement>> ress){
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
			
			if(results != null && results.length() > 0){
				System.out.println("results.length(): " + results.length());
				Collection<KbElement> res = new LinkedHashSet<KbElement>();
				ress.put(clausequery.toString("label"), res);
				for(int i = 0; i < results.length(); i++){
		        	Document doc = results.doc(i);
		        	float score = results.score(i);
		        	if(score >= 0.9){
		        		if(doc != null){
		        			String type = doc.get("type");
		        			if(type.equals(LITERAL)){
		        				System.out.println("type: " + type);
			        			System.out.println("score: " + score);
		        				System.out.println("label: " + doc.get("label"));
		        				ILiteral lit = new Literal(pruneString(doc.get("label")));
		        				KbVertex vvertex = new KbVertex(lit,KbElement.VVERTEX,score,1);
		        				res.add(vvertex);
		        				
		        				Term term = new Term("literal",lit.getLabel());
		        		        TermQuery query = new TermQuery(term);
		        		        Hits hits = m_kbSearcher.search(query);
		        		        if(hits != null && hits.length() > 0){
		        		        	for(int j = 0; j < hits.length(); j++){
		        		        		Document docu = hits.doc(j);
		        		        		if(docu != null){
		        		        			IDataProperty prop = new DataProperty(pruneString(docu.get("dataproperty")));
		        		        			INamedConcept con = new NamedConcept(pruneString(docu.get("concept")));
		        		        			KbVertex cvertex = new KbVertex(con,KbElement.CVERTEX,1);
		        		        			res.add(new KbEdge(cvertex, vvertex, prop, KbElement.AEDGE,1));
		        		        		}
		        		        	}
		        		        }
		        			}
		        			else if(type.equals(CONCEPT)){
		        				System.out.println("type: " + type);
			        			System.out.println("score: " + score);
		        				System.out.println("uri: " + doc.get("uri"));
		        				INamedConcept con = new NamedConcept(pruneString(doc.get("uri")));
		        				res.add(new KbVertex(con,KbElement.CVERTEX,score,1));
		        			}
		        			else if(type.equals(DATAPROPERTY)){
		        				System.out.println("type: " + type);
			        			System.out.println("score: " + score);
		        				System.out.println("uri: " + doc.get("uri"));
		        				DataProperty dataProp = new DataProperty(pruneString(doc.get("uri")));
		        				KbVertex vvertex = new KbVertex(new Resource("dummy"),KbElement.DUMMY,score,1);
		        				res.add(vvertex);
		        				
		        				String str = dataProp.getLabel();
		        				Term term = new Term("attribute",str);
		        		        TermQuery query = new TermQuery(term);
		        		        Hits hits = m_kbSearcher.search(query);
		        		        if(hits != null && hits.length() > 0){
		        		        	for(int j = 0; j < hits.length(); j++){
		        		        		Document docu = hits.doc(j);
		        		        		if(docu != null){
		        		        			INamedConcept con = new NamedConcept(pruneString(docu.get("domain")));
		        		        			KbVertex cvertex = new KbVertex(con,KbElement.CVERTEX,1);
		        		        			res.add(new KbEdge(cvertex, vvertex, dataProp, KbElement.AEDGE,1));
		        		        		}
		        		        	}
		        		        }
		        			}
		        			else if(type.equals(OBJECTPROPERTY)){
		        				System.out.println("type: " + type);
			        			System.out.println("score: " + score);
		        				System.out.println("uri: " + doc.get("uri"));
		        				IObjectProperty objProp = new ObjectProperty(pruneString(doc.get("uri")));
		        				String str = objProp.getLabel();
		        				Term term = new Term("relation",str);
		        		        TermQuery query = new TermQuery(term);
		        		        Hits hits = m_kbSearcher.search(query);
		        		        if(hits != null && hits.length() > 0){
		        		        	for(int j = 0; j < hits.length(); j++){
		        		        		Document docu = hits.doc(j);
		        		        		if(docu != null){
		        		        			INamedConcept dcon = new NamedConcept(pruneString(docu.get("domain")));
		        		        			INamedConcept rcon = new NamedConcept(pruneString(docu.get("range")));
		        		        			KbVertex dvertex = new KbVertex(dcon,KbElement.CVERTEX,score,1);
		        		        			KbVertex rvertex = new KbVertex(rcon,KbElement.CVERTEX,score,1);
		        		        			res.add(new KbEdge(dvertex, rvertex, objProp, KbElement.REDGE,0));
		        		        		}
		        		        	}
		        		        }
		        			}
		        		}
		        	}
		        }
			} 
				
		} 
		
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

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

	
	private static String CONCEPT = "concept";
	private static String OBJECTPROPERTY = "objectproperty";
	private static String DATAPROPERTY = "dataproperty";
	private static String INDIVIDUAL = "indiviudal";
	private static String LITERAL = "literal";
	
	private boolean isIndexingRequired(String datasourceUri){
		if (!m_indexedDS.contains(datasourceUri)){
			File file = new File(ExploreEnvironment.KB_INDEX_DIR);
			//TODO check of the knowledgebase has been changed instead 
			if (file.list() == null || file.list().length <= 2) return true;
		}

		return false;
	}

	private boolean isWordNetIndexingRequired(){
		File file = new File(ExploreEnvironment.SYN_INDEX_DIR);
		//TODO check of the knowledgebase has been changed instead 
		if (file.list() == null || file.list().length <= 2) return true;
		
		return false;
	}
	
	public void indexDataSource (String datasourceUri){
		if(isIndexingRequired(datasourceUri)){
			s_log.debug("Creating the index for " + " datasource " + datasourceUri + " .....!");

			try {
				if(isWordNetIndexingRequired())
					Syns2Index.indexWordNet();

				File file = new File(ExploreEnvironment.KB_INDEX_DIR);
				IndexWriter indexWriter = new IndexWriter(file, m_analyzer, true);
				
				IndexSearcher indexSearcher = new IndexSearcher(ExploreEnvironment.SYN_INDEX_DIR);

				indexDataSourceByConcept(indexWriter,indexSearcher);
				indexDataSourceByProperty(indexWriter,indexSearcher);
				
				indexSearcher.close();
				
				indexDataSourceByLiteral(indexWriter);
				
				indexGraphElement(indexWriter);
				
				indexWriter.optimize();
				indexWriter.close();
				m_indexedDS.add(datasourceUri);
			}

			catch (IOException e) {
				s_log.error("Exception occurred while making index: " + e);
				//TODO handle excpetion
				e.printStackTrace();
			}
			catch (Throwable e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		}
	}
	
	protected  void indexDataSourceByConcept(IndexWriter indexWriter,IndexSearcher searcher){
		IConceptDao conceptDao = (IConceptDao) PersistenceUtil.getDaoManager().getAvailableDao(IConceptDao.class);
		List concepts = conceptDao.findAll();
		int conSize = concepts.size();
		
		try{
			for (Object concept : concepts){
				String label = ((IConcept)concept).getLabel();
				Document doc = new Document();
				doc.add(new Field("type", CONCEPT, Field.Store.YES, Field.Index.NO));
				doc.add(new Field("label", label, Field.Store.NO, Field.Index.TOKENIZED));
				if (concept instanceof NamedConcept) {
					String uri = ((NamedConcept)concept).getUri();
					doc.add(new Field("uri", uri, Field.Store.YES, Field.Index.NO));
				}
				indexWriter.addDocument(doc);
				
				if(concept instanceof NamedConcept){
					NamedConcept ncon = (NamedConcept)concept;
//					Set<IProperty> propsFrom = ncon.getPropertiesFrom();
//					if(propsFrom != null){
//						for(IProperty prop : propsFrom){
//							if(prop instanceof DataProperty){
//								Document dpdoc = new Document();
//								System.out.println(prop.getLabel());
//								System.out.println(ncon.getUri());
//								dpdoc.add(new Field("attribute", prop.getLabel(), Field.Store.YES, Field.Index.UN_TOKENIZED));
//								dpdoc.add(new Field("domain", ncon.getUri(), Field.Store.YES, Field.Index.NO));
//								indexWriter.addDocument(dpdoc);
//						}
//					}
					Set<Pair> proAndRanges = ncon.getPropertiesAndRangesFrom(); 
					if(proAndRanges != null){
						for(Pair pair : proAndRanges){
							IProperty prop = (IProperty)pair.getHead();
							if(prop instanceof IObjectProperty){
								INamedConcept range = (INamedConcept)pair.getTail();
								Document opdoc = new Document();
								System.out.println(prop.getLabel());
								System.out.println(ncon.getUri());
								opdoc.add(new Field("relation", prop.getLabel(), Field.Store.YES, Field.Index.UN_TOKENIZED));
								opdoc.add(new Field("domain", ncon.getUri(), Field.Store.YES, Field.Index.NO));
								opdoc.add(new Field("range", range.getUri(), Field.Store.YES, Field.Index.NO));
								indexWriter.addDocument(opdoc);
							}
							else if (prop instanceof IDataProperty ){
								Document dpdoc = new Document();
								System.out.println(prop.getLabel());
								System.out.println(ncon.getUri());
								dpdoc.add(new Field("attribute", prop.getLabel(), Field.Store.YES, Field.Index.UN_TOKENIZED));
								dpdoc.add(new Field("domain", ncon.getUri(), Field.Store.YES, Field.Index.NO));
								indexWriter.addDocument(dpdoc);
							}
						}
					}
					
				}
				
				Set<String> values = new HashSet<String>();
				Term term = new Term("word", label.toLowerCase());
			    TermQuery termQuery = new TermQuery(term);
			    Hits results = searcher.search(termQuery);
			    if(results != null && results.length() > 0){
					for(int i = 0; i < results.length(); i++){
			        	Document docu = results.doc(i);
			        	values.addAll(Arrays.asList(docu.getValues("syn")));
					}
			    }	
			    System.out.println(concept + ": " + values);
			    for(String value : values){
			    	Document docu = new Document();
					docu.add(new Field("type", CONCEPT, Field.Store.YES, Field.Index.NO));
					docu.add(new Field("label", value, Field.Store.NO, Field.Index.TOKENIZED));
					if (concept instanceof NamedConcept) {
						String uri = ((NamedConcept)concept).getUri();
						docu.add(new Field("uri", uri, Field.Store.YES, Field.Index.NO));
					}
					indexWriter.addDocument(docu);
			    }
				
			}
		}
		catch (IOException e) {
			s_log.error("Exception occurred while making index: " + e);
			//TODO handle excpetion
			e.printStackTrace();
		} 
	}
	
	protected  void indexDataSourceByProperty(IndexWriter indexWriter,IndexSearcher searcher){
		IPropertyDao propertyDao = (IPropertyDao) PersistenceUtil.getDaoManager().getAvailableDao(IPropertyDao.class);
		List properties = propertyDao.findAll(); 
		
		try{
			for (Object property : properties){
				String label = ((IProperty)property).getLabel();
				Document doc = new Document();
				doc.add(new Field("label", label, Field.Store.NO,Field.Index.TOKENIZED));
				String uri = ((IProperty)property).getUri();
				doc.add(new Field("uri", uri, Field.Store.YES, Field.Index.NO));
				if(property instanceof IObjectProperty) {
					doc.add(new Field("type", OBJECTPROPERTY, Field.Store.YES, Field.Index.NO));
					indexWriter.addDocument(doc);
				}	
				else if(property instanceof IDataProperty) {
					doc.add(new Field("type", DATAPROPERTY, Field.Store.YES, Field.Index.NO));
					indexWriter.addDocument(doc);
				}	
				
				Set<String> values = new HashSet<String>();
				Term term = new Term("word", label.toLowerCase());
			    TermQuery termQuery = new TermQuery(term);
			    Hits results = searcher.search(termQuery);
			    if(results != null && results.length() > 0){
					for(int i = 0; i < results.length(); i++){
			        	Document docu = results.doc(i);
			        	values.addAll(Arrays.asList(docu.getValues("syn")));
					}
			    }	
			    for(String value : values){
			    	Document docu = new Document();
					docu.add(new Field("label", value, Field.Store.NO,Field.Index.TOKENIZED));
					docu.add(new Field("uri", uri, Field.Store.YES, Field.Index.NO));
					if(property instanceof IObjectProperty)
						docu.add(new Field("type", OBJECTPROPERTY, Field.Store.YES, Field.Index.NO));
					else
						docu.add(new Field("type", DATAPROPERTY, Field.Store.YES, Field.Index.NO));
					indexWriter.addDocument(docu);
			    }
			}
		}
		catch (IOException e) {
			s_log.error("Exception occurred while making index: " + e);
			//TODO handle excpetion
			e.printStackTrace();
		} 
	}
	
	protected  void indexDataSourceByLiteral(IndexWriter indexWriter){
		ILiteralDao literalDao = (ILiteralDao) PersistenceUtil.getDaoManager().getAvailableDao(ILiteralDao.class);
		List literals = literalDao.findAll();
		int i= 1;
		
		try{
			for (Object literal:literals){
				System.out.println(i + ": " + ((ILiteral)literal).getLabel());
				i++;
				
				Document doc = new Document();
				doc.add(new Field("type", LITERAL, Field.Store.YES, Field.Index.NO));
				doc.add(new Field("label", ((ILiteral)literal).getLabel(), Field.Store.YES, Field.Index.TOKENIZED));
				
				indexWriter.addDocument(doc);
			}
		}
		catch (IOException e) {
			s_log.error("Exception occurred while making index: " + e);
			//TODO handle excpetion
			e.printStackTrace();
		} 
	}
	
	public void indexGraphElement(IndexWriter indexWriter){
		IIndividualDao individualDao = (IIndividualDao) PersistenceUtil.getDaoManager().getAvailableDao(IIndividualDao.class);
		List individuals = individualDao.findAll();
		System.out.println("individuals.size(): " + individuals.size());
		int i = 0; 
		try{
			for (Object individual:individuals){
				System.out.println(i + ": " + ((INamedIndividual)individual).getLabel());
				if (individual instanceof INamedIndividual) {
//					INamedConcept concept = null;
//					Object[] cons =  ((INamedIndividual)individual).getTypes().toArray();
//					if(cons != null && cons.length > 0){
//						for (int j = cons.length - 1; j > 0; j--) {
//							if (isSubConcept(cons[j],cons[j-1])) {
//								Object o = cons[j];
//								cons[j] = cons[j-1];
//								cons[j-1] = o;
//							}
//						}
//						concept = (INamedConcept)cons[0];
//						System.out.println(i + ": " + concept.getLabel());
//					}	
					Set<IConcept> cons = ((INamedIndividual)individual).getTypes();
					Set<IPropertyMember> propmembers = ((INamedIndividual)individual).getPropertyFromValues();
					for(IPropertyMember propmember : propmembers){
						if(propmember.getTarget() instanceof ILiteral){
							for(IConcept con : cons){
								Document doc = new Document();
								doc.add(new Field("literal", propmember.getTarget().getLabel(), Field.Store.YES, Field.Index.UN_TOKENIZED));
								doc.add(new Field("dataproperty", propmember.getProperty().getUri(),Field.Store.YES,Field.Index.NO));
								doc.add(new Field("concept", ((INamedConcept)con).getUri(),Field.Store.YES, Field.Index.NO));
								
								indexWriter.addDocument(doc);
							}
						}	
						else if(propmember.getType() == PropertyMember.OBJECT_PROPERTY_MEMBER){
							
						}
					}
				}
				i++;
			}
		}
		catch (IOException e) {
			s_log.error("Exception occurred while making index: " + e);
			//TODO handle excpetion
			e.printStackTrace();
		} 
	}
	
	public boolean isSubConcept(Object subconcept, Object superconcept){
		if (subconcept instanceof INamedConcept && superconcept instanceof INamedConcept){
			INamedConcept sup = (INamedConcept)superconcept;
			INamedConcept sub = (INamedConcept)subconcept;
			if(sup.getSubconcepts() !=  null){
				if (sup.getSubconcepts().contains(sub)) 
					return true;
				else {
					for (Object obj : sup.getSubconcepts()) {
						if(isSubConcept(sub,obj)) return true;
					}
				}
			}
			else 
				return false;
		}
		return false;
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
