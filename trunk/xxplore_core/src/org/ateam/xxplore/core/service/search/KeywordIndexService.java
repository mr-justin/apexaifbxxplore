package org.ateam.xxplore.core.service.search;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
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
import org.ateam.xxplore.core.ExploreEnvironment;
import org.ateam.xxplore.core.service.IService;
import org.ateam.xxplore.core.service.IServiceListener;
import org.xmedia.oms.model.api.IConcept;
import org.xmedia.oms.model.api.IDataProperty;
import org.xmedia.oms.model.api.IIndividual;
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
import org.xmedia.oms.persistence.KbEnvironment;
import org.xmedia.oms.persistence.PersistenceUtil;
import org.xmedia.oms.persistence.dao.IConceptDao;
import org.xmedia.oms.persistence.dao.IIndividualDao;
import org.xmedia.oms.persistence.dao.ILiteralDao;
import org.xmedia.oms.persistence.dao.IPropertyDao;

public class KeywordIndexService implements IService{
	
	private static Logger s_log = Logger.getLogger(KeywordIndexService.class);
	
	private static final String SYN_INDEX_DIR = "D:\\BTC\\sampling\\synIndex";
		
	private IndexWriter m_indexWriter;
	private StandardAnalyzer m_analyzer;
	private Searcher m_searcher;
	
	public KeywordIndexService(String keywordIndexDir, boolean create) {
		m_analyzer = new StandardAnalyzer();
		
		File indexDir = new File(keywordIndexDir);
		if (!indexDir.exists())
			indexDir.mkdirs();
		try {
			m_indexWriter = new IndexWriter(indexDir, m_analyzer, create);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static String CONCEPT = "concept";
	public static String OBJECTPROPERTY = "objectproperty";
	public static String DATAPROPERTY = "dataproperty";
	public static String INDIVIDUAL = "indiviudal";
	public static String LITERAL = "literal";
	
	
	public void indexKeywords() {

		try {
			IndexSearcher indexSearcher = new IndexSearcher(SYN_INDEX_DIR);

			indexDataSourceByConcept(m_indexWriter, indexSearcher);
			indexDataSourceByProperty(m_indexWriter, indexSearcher);

			indexSearcher.close();

			indexDataSourceByLiteral(m_indexWriter);
			indexDataSourceByIndividual(m_indexWriter);

			m_indexWriter.optimize();
			m_indexWriter.close();
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
//				i++;
				
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
							for(IConcept scon : sourceConcepts){
								Document attrdoc = new Document();
								attrdoc.add(new Field("literal", propmember.getTarget().getLabel(), Field.Store.YES, Field.Index.UN_TOKENIZED));
								attrdoc.add(new Field("attribute", propmember.getProperty().getUri(),Field.Store.YES,Field.Index.UN_TOKENIZED));
								attrdoc.add(new Field("concept", ((INamedConcept)scon).getUri(),Field.Store.YES, Field.Index.NO));
								indexWriter.addDocument(attrdoc);
							}
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
	
	public Map<String,Collection<KbElement>> searchKb(String query, String datasource){
		Map<String,Collection<KbElement>> ress = new LinkedHashMap<String,Collection<KbElement>>();
		try {
			if (m_searcher ==null){
				s_log.debug("Open index " + ExploreEnvironment.KB_INDEX_DIR + " and init kb searcher!");
				m_searcher = new IndexSearcher(ExploreEnvironment.KB_INDEX_DIR + "/" + datasource);
			}
			QueryParser parser = new QueryParser("label", m_analyzer);
			Query q = parser.parse(query);
			if (q instanceof BooleanQuery){
				BooleanClause[] clauses = ((BooleanQuery)q).getClauses();
				for(int i = 0; i < clauses.length; i++){
					Query clauseQ = clauses[i].getQuery();
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
//		System.out.println(ress);
		return ress;
	}

	private void searchWithClause(Query clausequery, Map<String,Collection<KbElement>> ress){
		try {
			Hits results = m_searcher.search(clausequery);
			if ((results == null) || (results.length() == 0)){
				Set<Term> term = new HashSet<Term>();
				clausequery.extractTerms(term);
				//if clause query is a term query
				if(term.size() == 1){
					results = m_searcher.search(new FuzzyQuery(term.iterator().next()));
				}
			}
			
			if((results != null) && (results.length() > 0)){
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
		        		        Hits hits = m_searcher.search(query);
		        		        if((hits != null) && (hits.length() > 0)){
		        		        	for(int j = 0; j < hits.length(); j++){
		        		        		Document docu = hits.doc(j);
		        		        		if(docu != null){
		        		        			IDataProperty prop = new DataProperty(pruneString(docu.get("attribute")));
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
		        				KbVertex vvertex = new KbVertex(new Resource("dummy"),KbElement.DUMMY,score,2);
		        				res.add(vvertex);
		        				
		        				String str = dataProp.getLabel();
		        				Term term = new Term("attribute",str);
		        		        TermQuery query = new TermQuery(term);
		        		        Hits hits = m_searcher.search(query);
		        		        if((hits != null) && (hits.length() > 0)){
		        		        	for(int j = 0; j < hits.length(); j++){
		        		        		Document docu = hits.doc(j);
		        		        		if(docu != null){
		        		        			INamedConcept con = new NamedConcept(pruneString(docu.get("concept")));
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
		        		        Hits hits = m_searcher.search(query);
		        		        if((hits != null) && (hits.length() > 0)){
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


	private String pruneString(String str) {
		return str.replace("\"", "");
	}
	
	public void callService(IServiceListener listener, Object... params) {
		// TODO Auto-generated method stub
		
	}

	public void disposeService() {
		try {
			m_indexWriter.close();
			m_searcher.close();
			m_analyzer = null;

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void init(Object... params) {
		// TODO Auto-generated method stub
		
	}

}
