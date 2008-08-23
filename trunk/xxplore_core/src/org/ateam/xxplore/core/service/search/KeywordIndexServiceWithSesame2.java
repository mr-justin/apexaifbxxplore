package org.ateam.xxplore.core.service.search;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.aifb.xxplore.shared.util.Pair;
import org.aifb.xxplore.shared.util.PropertyUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.ateam.xxplore.core.ExploreEnvironment;
import org.xmedia.accessknow.sesame.persistence.ExtendedSesameDaoManager;
import org.xmedia.accessknow.sesame.persistence.SesameConnection;
import org.xmedia.accessknow.sesame.persistence.SesameRepositoryFactory;
import org.xmedia.accessknow.sesame.persistence.SesameSession;
import org.xmedia.accessknow.sesame.persistence.SesameSessionFactory;
import org.xmedia.oms.model.api.IConcept;
import org.xmedia.oms.model.api.IDataProperty;
import org.xmedia.oms.model.api.IIndividual;
import org.xmedia.oms.model.api.ILiteral;
import org.xmedia.oms.model.api.INamedConcept;
import org.xmedia.oms.model.api.INamedIndividual;
import org.xmedia.oms.model.api.IObjectProperty;
import org.xmedia.oms.model.api.IOntology;
import org.xmedia.oms.model.api.IProperty;
import org.xmedia.oms.model.api.IPropertyMember;
import org.xmedia.oms.model.api.OntologyImportException;
import org.xmedia.oms.model.impl.NamedConcept;
import org.xmedia.oms.model.impl.PropertyMember;
import org.xmedia.oms.persistence.DatasourceException;
import org.xmedia.oms.persistence.ISession;
import org.xmedia.oms.persistence.ISessionFactory;
import org.xmedia.oms.persistence.InvalidParameterException;
import org.xmedia.oms.persistence.KbEnvironment;
import org.xmedia.oms.persistence.MissingParameterException;
import org.xmedia.oms.persistence.OntologyCreationException;
import org.xmedia.oms.persistence.OntologyLoadException;
import org.xmedia.oms.persistence.OpenSessionException;
import org.xmedia.oms.persistence.PersistenceUtil;
import org.xmedia.oms.persistence.SessionFactory;
import org.xmedia.oms.persistence.dao.IConceptDao;
import org.xmedia.oms.persistence.dao.IIndividualDao;
import org.xmedia.oms.persistence.dao.ILiteralDao;
import org.xmedia.oms.persistence.dao.IPropertyDao;
import org.xmedia.uris.impl.XMURIFactoryInsulated;

public class KeywordIndexServiceWithSesame2 {
	
	private static Logger s_log = Logger.getLogger(SummaryGraphIndexServiceWithSesame2.class);
	
	private static final String SYN_INDEX_DIR = "D:\\BTC\\sampling\\synIndex";
	
	private IOntology m_onto;
	private ISession m_session;
	
	private IndexWriter indexWriter;
	private StandardAnalyzer m_analyzer;
	
	private String repositoryDir;
	private Properties parameters;
	
	private String datasource;
	
	private static String ONTOLOGY_URI = "target"; // repository directory name
	private static String ONTOLOGY_FILE_PATH = "res/BTC/target.rdf";
	private static String ONTOLOGY_FILE_NAME = "target.rdf";
	private static String BASE_ONTOLOGY_URI = "http://www.example.org/example";
	private static String LANGUAGE = IOntology.RDF_XML_LANGUAGE;
	private static String ONTOLOGY_TYPE = SesameRepositoryFactory.RDFS_MEMORY_PERSISTENT;
	
	private static String REPOSITORY_DIR = "res/BTC/sampling/repository";
	private static String KEYWORD_INDEX_DIR = "res/BTC/sampling/keywordIndex";
	
	public static void main(String[] args) {
		Properties parameters = new Properties();
		parameters.setProperty(KbEnvironment.ONTOLOGY_URI, ONTOLOGY_URI);
		parameters.setProperty(KbEnvironment.ONTOLOGY_TYPE, ONTOLOGY_TYPE);
		
		parameters.setProperty(ExploreEnvironment.ONTOLOGY_FILE_PATH, ONTOLOGY_FILE_PATH);
		parameters.setProperty(ExploreEnvironment.BASE_ONTOLOGY_URI, BASE_ONTOLOGY_URI);
		parameters.setProperty(ExploreEnvironment.SERIALIZATION_FORMAT, LANGUAGE);
		
		KeywordIndexServiceWithSesame2 service = new KeywordIndexServiceWithSesame2(parameters, REPOSITORY_DIR, KEYWORD_INDEX_DIR, true);
	} 
	
	public KeywordIndexServiceWithSesame2(Properties parameters, String repositoryDir, String keywordIndexDir, String datasource, boolean create) {
		this.parameters = parameters;
		this.repositoryDir = repositoryDir;
		this.datasource = datasource;
		m_analyzer = new StandardAnalyzer();
		File indexDir = new File(keywordIndexDir);
		if (!indexDir.exists())
			indexDir.mkdirs();
		try {
			indexWriter = new IndexWriter(indexDir, m_analyzer, create);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		init();	
	}
	
	public KeywordIndexServiceWithSesame2(Properties parameters, String repositoryDir, String keywordIndexDir, boolean create) {
		this(parameters,repositoryDir,keywordIndexDir, parameters.getProperty(KbEnvironment.ONTOLOGY_URI),create);
	}
	
	public static String CONCEPT = "concept";
	public static String OBJECTPROPERTY = "objectproperty";
	public static String DATAPROPERTY = "dataproperty";
	public static String INDIVIDUAL = "indiviudal";
	public static String LITERAL = "literal";
	
	
	public void indexKeywords() {

		try {
			IndexSearcher indexSearcher = new IndexSearcher(SYN_INDEX_DIR);

			indexDataSourceByConcept(indexWriter, indexSearcher);
			indexDataSourceByProperty(indexWriter, indexSearcher);

			indexSearcher.close();

			indexDataSourceByLiteral(indexWriter);
			indexDataSourceByIndividual(indexWriter);

			indexWriter.optimize();
			indexWriter.close();
		}

		catch (IOException e) {
			s_log.error("Exception occurred while making index: " + e);
			// TODO handle excpetion
			e.printStackTrace();
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("deprecation")
	protected  void indexDataSourceByConcept(IndexWriter indexWriter,IndexSearcher searcher){
		IConceptDao conceptDao = (IConceptDao) PersistenceUtil.getDaoManager().getAvailableDao(IConceptDao.class);
		List concepts = conceptDao.findAll();
//		int conSize = concepts.size();
		
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
				
				Set<String> values = new HashSet<String>();
				Term term = new Term("word", label.toLowerCase());
			    TermQuery termQuery = new TermQuery(term);
			    Hits results = searcher.search(termQuery);
			    if((results != null) && (results.length() > 0)){
					for(int i = 0; i < results.length(); i++){
			        	Document docu = results.doc(i);
			        	values.addAll(Arrays.asList(docu.getValues("syn")));
					}
			    }	
//			    System.out.println(concept + ": " + values);
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
	
	@SuppressWarnings("deprecation")
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
			    if((results != null) && (results.length() > 0)){
					for(int i = 0; i < results.length(); i++){
			        	Document docu = results.doc(i);
			        	values.addAll(Arrays.asList(docu.getValues("syn")));
					}
			    }	
			    for(String value : values){
			    	Document docu = new Document();
					docu.add(new Field("label", value, Field.Store.NO,Field.Index.TOKENIZED));
					docu.add(new Field("uri", uri, Field.Store.YES, Field.Index.NO));
					if(property instanceof IObjectProperty) {
						docu.add(new Field("type", OBJECTPROPERTY, Field.Store.YES, Field.Index.NO));
					} else {
						docu.add(new Field("type", DATAPROPERTY, Field.Store.YES, Field.Index.NO));
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
	
	@SuppressWarnings("deprecation")
	protected  void indexDataSourceByLiteral(IndexWriter indexWriter){
		ILiteralDao literalDao = (ILiteralDao) PersistenceUtil.getDaoManager().getAvailableDao(ILiteralDao.class);
		List literals = literalDao.findAll();
		int i= 1;
		
		try{
			for (Object literal:literals){
//				System.out.println(i + ": " + ((ILiteral)literal).getLabel());
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
	
	@SuppressWarnings("deprecation")
	public void indexDataSourceByIndividual (IndexWriter indexWriter){
		IIndividualDao individualDao = (IIndividualDao) PersistenceUtil.getDaoManager().getAvailableDao(IIndividualDao.class);
		List individuals = individualDao.findAll();
//		System.out.println("individuals.size(): " + individuals.size());
		int i = 0; 
		try{
			for (Object souceIndividual:individuals){
//				System.out.println(i + ": " + ((INamedIndividual)individual).getLabel());
				if (souceIndividual instanceof IIndividual) {
					Set<IConcept> sourceConcepts = ((IIndividual)souceIndividual).getTypes();
					Document doc = new Document();
					doc.add(new Field("type", INDIVIDUAL, Field.Store.YES, Field.Index.NO));
					if(souceIndividual instanceof INamedIndividual)
						doc.add(new Field("label", ((INamedIndividual)souceIndividual).getUri(), Field.Store.NO, Field.Index.UN_TOKENIZED));
					else
						doc.add(new Field("label", ((IIndividual)souceIndividual).getLabel(), Field.Store.NO, Field.Index.UN_TOKENIZED));
					for(IConcept con : sourceConcepts) {
						doc.add(new Field("concept", ((INamedConcept)con).getUri(),Field.Store.YES, Field.Index.NO));
					}
					indexWriter.addDocument(doc);
					
					Set<IPropertyMember> propmembers = ((IIndividual)souceIndividual).getPropertyFromValues();
					for(IPropertyMember propmember : propmembers){
						if(propmember.getType() == PropertyMember.DATA_PROPERTY_MEMBER && propmember.getTarget() instanceof ILiteral){
							Document attrdoc = new Document();
							attrdoc.add(new Field("literal", propmember.getTarget().getLabel(), Field.Store.YES, Field.Index.UN_TOKENIZED));
							attrdoc.add(new Field("attribute", propmember.getProperty().getUri(),Field.Store.YES,Field.Index.UN_TOKENIZED));
							for(IConcept scon : sourceConcepts){
								attrdoc.add(new Field("concept", ((INamedConcept)scon).getUri(),Field.Store.YES, Field.Index.NO));
							}
							indexWriter.addDocument(attrdoc);
						}	
						else if(propmember.getType() == PropertyMember.OBJECT_PROPERTY_MEMBER && propmember.getTarget() instanceof IIndividual){
							IIndividual targetIndividual = (IIndividual)propmember.getTarget();
							Set<IConcept> targetConcepts = ((IIndividual)targetIndividual).getTypes();
							for(IConcept scon : sourceConcepts){
								for(IConcept tcon : targetConcepts) {
									Document reldoc = new Document();
									reldoc.add(new Field("relation", propmember.getProperty().getUri(),Field.Store.YES,Field.Index.UN_TOKENIZED));
									reldoc.add(new Field("domain", ((INamedConcept)scon).getUri(),Field.Store.YES, Field.Index.NO));
									reldoc.add(new Field("range", ((INamedConcept)tcon).getUri(),Field.Store.YES, Field.Index.NO));
								
									indexWriter.addDocument(reldoc);
								}
							}
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
	
	private void init() {
		
//		load ontology	
		m_onto = null;
		SesameConnection ses_con = null;
		try {
			try {
				ses_con = new SesameConnection(repositoryDir);
			} catch (Exception e2) {
				e2.printStackTrace();
			}

			try {
				m_onto = ses_con.loadOntology(PropertyUtils.convertToMap(parameters));
			} catch (OntologyLoadException e) {

				m_onto = ses_con.createOntology(PropertyUtils.convertToMap(parameters));

				try {
					addFileToRepository(m_onto, PropertyUtils.convertToMap(parameters));
				} catch (MissingParameterException e1) {
					e1.printStackTrace();
				}
			}
		} catch (DatasourceException e) {
			e.printStackTrace();
		} catch (MissingParameterException e) {
			e.printStackTrace();
		} catch (InvalidParameterException e) {
			e.printStackTrace();
		} catch (OntologyCreationException e) {
			e.printStackTrace();
		} 
		
		SesameSessionFactory sesame_factory = new SesameSessionFactory(new XMURIFactoryInsulated());
		ISession session = null;
		
		try {
			session = sesame_factory.openSession(ses_con, m_onto);
		} catch (DatasourceException e) {
			e.printStackTrace();
		} catch (OpenSessionException e) {
			e.printStackTrace();
		}
		//set dao manager
		PersistenceUtil.setDaoManager(ExtendedSesameDaoManager.getInstance((SesameSession)session));
		
		session.close();	
		
		ISessionFactory factory = SessionFactory.getInstance();
		PersistenceUtil.setSessionFactory(factory); 
		//open a new session with the ontology
		try {
			m_session = factory.openSession(ses_con,m_onto);
		} catch (DatasourceException e) {
			e.printStackTrace();
		} catch (OpenSessionException e) {
			e.printStackTrace();
		}
								
	}

	private static void addFileToRepository(IOntology onto, Map<String, Object> parameters)throws MissingParameterException{
		
		if(!parameters.containsKey(ExploreEnvironment.ONTOLOGY_FILE_PATH)) {
			throw new MissingParameterException(ExploreEnvironment.ONTOLOGY_FILE_PATH+" missing!");
		}
				
		if(!parameters.containsKey(ExploreEnvironment.BASE_ONTOLOGY_URI)) {
			throw new MissingParameterException(ExploreEnvironment.BASE_ONTOLOGY_URI+" missing!");
		}
		
		if(!parameters.containsKey(ExploreEnvironment.SERIALIZATION_FORMAT)) {
			throw new MissingParameterException(ExploreEnvironment.SERIALIZATION_FORMAT+" missing!");
		}
		
		String filePath = (String)parameters.get(ExploreEnvironment.ONTOLOGY_FILE_PATH);
		String baseUri = (String)parameters.get(ExploreEnvironment.BASE_ONTOLOGY_URI);
		String language = (String)parameters.get(ExploreEnvironment.SERIALIZATION_FORMAT);
				
		try {
			onto.importOntology(language, baseUri, new FileReader(filePath));
		} 
		catch (FileNotFoundException e) {
			e.printStackTrace();
		} 
		catch (OntologyImportException e) {
			e.printStackTrace();
		}
	}

}
