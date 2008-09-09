package org.ateam.xxplore.core.service.search;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aifb.xxplore.shared.exception.Emergency;
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
import org.jgrapht.graph.Pseudograph;
import org.xmedia.accessknow.sesame.persistence.model.adaptation.IEntity;
import org.xmedia.oms.model.api.IConcept;
import org.xmedia.oms.model.api.IDataProperty;
import org.xmedia.oms.model.api.IIndividual;
import org.xmedia.oms.model.api.ILiteral;
import org.xmedia.oms.model.api.INamedConcept;
import org.xmedia.oms.model.api.IObjectProperty;
import org.xmedia.oms.model.api.IProperty;
import org.xmedia.oms.model.api.IPropertyMember;
import org.xmedia.oms.model.impl.DataProperty;
import org.xmedia.oms.model.impl.Literal;
import org.xmedia.oms.model.impl.NamedConcept;
import org.xmedia.oms.model.impl.ObjectProperty;
import org.xmedia.oms.model.impl.PropertyMember;
import org.xmedia.oms.persistence.PersistenceUtil;
import org.xmedia.oms.persistence.dao.IConceptDao;
import org.xmedia.oms.persistence.dao.IIndividualDao;
import org.xmedia.oms.persistence.dao.ILiteralDao;
import org.xmedia.oms.persistence.dao.IPropertyDao;

public class KeywordIndexService implements IService{

	private static Logger s_log = Logger.getLogger(KeywordIndexService.class);

	private IndexWriter m_indexWriter;
	private StandardAnalyzer m_analyzer;
	private Searcher m_searcher;


	private static final String TYPE_FIELD =  "type";
	//entries use for typing:
	private static final String CONCEPT = "concept";
	private static final String OBJECTPROPERTY = "objectproperty";
	private static final String DATAPROPERTY = "dataproperty";
	private static final String LITERAL = "literal";

	private static final String LABEL_FIELD =  "label";
	private static final String URI_FIELD =  "uri";
	private static final String DS_FIELD =  "ds";
	private static final String CONCEPT_FIELD =  "concept";
	private static final String ATTRIBUTE_FIELD =  "attr";
	private static final String LITERAL_FIELD = "value";

	private static final double THRESHOLD_SCORE = 0.9;

	public KeywordIndexService(String keywordIndexDir, boolean create) {
		m_analyzer = new StandardAnalyzer();

		File indexDir = new File(keywordIndexDir);
		if (!indexDir.exists())
			indexDir.mkdirs();
		try {
			m_indexWriter = new IndexWriter(indexDir, m_analyzer, create);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public void indexKeywords(String datasourceURI, Pseudograph<SummaryGraphElement, SummaryGraphEdge> schemaGraph, String synIndexdir) {

		try {
			IndexSearcher indexSearcher = null;
			if (synIndexdir != null) indexSearcher = new IndexSearcher(synIndexdir);

			indexDataSourceByConcept(m_indexWriter, indexSearcher, datasourceURI);
			indexDataSourceByProperty(m_indexWriter, indexSearcher, datasourceURI, schemaGraph);

			indexSearcher.close();

			indexDataSourceByLiteral(m_indexWriter, datasourceURI);
			indexDataSourceByIndividual(m_indexWriter, datasourceURI);

			m_indexWriter.optimize();
			m_indexWriter.close();
		}

		catch (IOException e) {
			s_log.error("Exception occurred while making index: " + e);
			e.printStackTrace();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("deprecation")
	protected  void indexDataSourceByConcept(IndexWriter indexWriter,IndexSearcher searcher, String ds){
		IConceptDao conceptDao = (IConceptDao) PersistenceUtil.getDaoManager().getAvailableDao(IConceptDao.class);
		List concepts = conceptDao.findAll();

		try{
			for (Object concept : concepts){
				String label = ((IConcept)concept).getLabel();
				Document doc = new Document();
				doc.add(new Field(TYPE_FIELD, CONCEPT, Field.Store.YES, Field.Index.NO));
				doc.add(new Field(LABEL_FIELD, label, Field.Store.NO, Field.Index.TOKENIZED));
				if (concept instanceof NamedConcept) {
					String uri = ((NamedConcept)concept).getUri();
					doc.add(new Field(URI_FIELD, uri, Field.Store.YES, Field.Index.NO));
				}
				doc.add(new Field(DS_FIELD, ds, Field.Store.NO, Field.Index.NO));
				indexWriter.addDocument(doc);

				if(searcher != null){
					Set<String> values = new HashSet<String>();
					Term term = new Term(WordnetSynsIndexService.WORD_FIELD, label.toLowerCase());
					TermQuery termQuery = new TermQuery(term);

					Hits results = searcher.search(termQuery);
					if((results != null) && (results.length() > 0)){
						for(int i = 0; i < results.length(); i++){
							Document docu = results.doc(i);
							values.addAll(Arrays.asList(docu.getValues(WordnetSynsIndexService.SYN_FIELD)));
						}
					}					
					for(String value : values){
						Document docu = new Document();
						docu.add(new Field(TYPE_FIELD, CONCEPT, Field.Store.YES, Field.Index.NO));
						docu.add(new Field(LABEL_FIELD, value, Field.Store.NO, Field.Index.TOKENIZED));
						if (concept instanceof NamedConcept) {
							String uri = ((NamedConcept)concept).getUri();
							docu.add(new Field(URI_FIELD, uri, Field.Store.YES, Field.Index.NO));
						}
						doc.add(new Field(DS_FIELD, ds, Field.Store.NO, Field.Index.NO));
						indexWriter.addDocument(docu);
					}
				}

			}
		}
		catch (IOException e) {
			s_log.error("Exception occurred while making index: " + e);
			e.printStackTrace();
		} 
	}

	@SuppressWarnings("deprecation")
	protected  void indexDataSourceByProperty(IndexWriter indexWriter,IndexSearcher searcher, String ds, Pseudograph<SummaryGraphElement, SummaryGraphEdge> schemagraph){
		IPropertyDao propertyDao = (IPropertyDao) PersistenceUtil.getDaoManager().getAvailableDao(IPropertyDao.class);
		List properties = propertyDao.findAll(); 

		try{
			for (Object property : properties){
				String label = ((IProperty)property).getLabel();
				Document doc = new Document();
				doc.add(new Field(LABEL_FIELD, label, Field.Store.NO,Field.Index.TOKENIZED));
				String uri = ((IProperty)property).getUri();
				doc.add(new Field(URI_FIELD, uri, Field.Store.YES, Field.Index.NO));
				doc.add(new Field(DS_FIELD, ds, Field.Store.NO, Field.Index.NO));
				if(property instanceof IObjectProperty) {
					doc.add(new Field(TYPE_FIELD, OBJECTPROPERTY, Field.Store.YES, Field.Index.NO));
					indexWriter.addDocument(doc);
				}	
				else if(property instanceof IDataProperty) {
					doc.add(new Field(TYPE_FIELD, DATAPROPERTY, Field.Store.YES, Field.Index.NO));
					doc = addConceptsToDataPropertyDoc(doc, (IDataProperty) property, schemagraph);
					indexWriter.addDocument(doc);
				}	

				if(searcher != null){
					Set<String> values = new HashSet<String>();
					Term term = new Term(WordnetSynsIndexService.WORD_FIELD, label.toLowerCase());
					TermQuery termQuery = new TermQuery(term);
					Hits results = searcher.search(termQuery);
					if((results != null) && (results.length() > 0)){
						for(int i = 0; i < results.length(); i++){
							Document docu = results.doc(i);
							values.addAll(Arrays.asList(docu.getValues(WordnetSynsIndexService.SYN_FIELD)));
						}
					}	
					for(String value : values){
						Document docu = new Document();
						docu.add(new Field(LABEL_FIELD, value, Field.Store.NO,Field.Index.TOKENIZED));
						docu.add(new Field(URI_FIELD, uri, Field.Store.YES, Field.Index.NO));
						if(property instanceof IObjectProperty) {
							docu.add(new Field(TYPE_FIELD, OBJECTPROPERTY, Field.Store.YES, Field.Index.NO));
						} else {
							docu.add(new Field(TYPE_FIELD, DATAPROPERTY, Field.Store.YES, Field.Index.NO));
							docu = addConceptsToDataPropertyDoc(docu, (IDataProperty) property, schemagraph);
						}
						indexWriter.addDocument(docu);
					}
				}
			}
		}
		catch (IOException e) {
			s_log.error("Exception occurred while making index: " + e);
			e.printStackTrace();
		} 
	}

	protected  void indexDataSourceByLiteral(IndexWriter indexWriter, String ds){
		ILiteralDao literalDao = (ILiteralDao) PersistenceUtil.getDaoManager().getAvailableDao(ILiteralDao.class);
		List literals = literalDao.findAll();
		
		try{
			for (Object literal:literals){
				Document doc = new Document();
				doc.add(new Field(DS_FIELD, ds, Field.Store.NO, Field.Index.NO));
				doc.add(new Field(TYPE_FIELD, LITERAL, Field.Store.YES, Field.Index.NO));
				doc.add(new Field(LABEL_FIELD, ((ILiteral)literal).getLabel(), Field.Store.YES, Field.Index.TOKENIZED));
				
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
	public void indexDataSourceByIndividual (IndexWriter indexWriter, String ds){
		IIndividualDao individualDao = (IIndividualDao) PersistenceUtil.getDaoManager().getAvailableDao(IIndividualDao.class);
		List individuals = individualDao.findAll();
		try{
			for (Object souceIndividual:individuals){
				if (souceIndividual instanceof IIndividual) {
					Set<IConcept> sourceConcepts = ((IIndividual)souceIndividual).getTypes();
					Set<IPropertyMember> propmembers = ((IIndividual)souceIndividual).getPropertyFromValues();
					for(IPropertyMember propmember : propmembers){
						if(propmember.getType() == PropertyMember.DATA_PROPERTY_MEMBER && propmember.getTarget() instanceof ILiteral){
							Document attrdoc = new Document();
							attrdoc.add(new Field(LITERAL_FIELD, propmember.getTarget().getLabel(), Field.Store.YES, Field.Index.UN_TOKENIZED));
							attrdoc.add(new Field(ATTRIBUTE_FIELD, propmember.getProperty().getUri(),Field.Store.YES,Field.Index.UN_TOKENIZED));
							attrdoc.add(new Field(DS_FIELD, ds, Field.Store.NO, Field.Index.NO));
							for(IConcept scon : sourceConcepts){
								attrdoc.add(new Field(CONCEPT_FIELD, ((INamedConcept)scon).getUri(),Field.Store.YES, Field.Index.NO));
							}
							indexWriter.addDocument(attrdoc);
						}	
					}
				}
			}
		}
		catch (IOException e) {
			s_log.error("Exception occurred while making index: " + e);
			//TODO handle excpetion
			e.printStackTrace();
		} 
	}

	/**
	 * Search for keyword elements and augment the summary graph.
	 * @param query
	 * @param sumGraph
	 * @param prune
	 * @return
	 */
	public Map<String,Collection<ISummaryGraphElement>> searchKb(String query, Pseudograph<SummaryGraphElement, SummaryGraphEdge> sumGraph, double prune){
		Map<String,Collection<ISummaryGraphElement>> ress = new LinkedHashMap<String,Collection<ISummaryGraphElement>>();
		try {
			if (m_searcher ==null){
				s_log.debug("Open index " + ExploreEnvironment.KB_INDEX_DIR + " and init kb searcher!");
				m_searcher = new IndexSearcher(ExploreEnvironment.KB_INDEX_DIR);
			}
			QueryParser parser = new QueryParser("label", m_analyzer);
			Query q = parser.parse(query);
			if (q instanceof BooleanQuery){
				BooleanClause[] clauses = ((BooleanQuery)q).getClauses();
				for(int i = 0; i < clauses.length; i++){
					Query clauseQ = clauses[i].getQuery();
					Map<String, Collection<ISummaryGraphElement>> partialRes = searchWithClause(clauseQ, sumGraph, prune);
					if (partialRes != null && partialRes.size() > 0) ress.putAll(partialRes);
				}
			}
			//is a phrase or term query 
			else{
				ress = searchWithClause(q, sumGraph, prune);
			}

		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return ress;
	}

	private Map<String,Collection<ISummaryGraphElement>> searchWithClause(Query clausequery, Pseudograph<SummaryGraphElement, SummaryGraphEdge> sumGraph, double prune){
		Map<String,Collection<ISummaryGraphElement>> result = new LinkedHashMap<String,Collection<ISummaryGraphElement>>();
		try {
			Hits hits = m_searcher.search(clausequery);
			if ((hits == null) || (hits.length() == 0)){
				Set<Term> term = new HashSet<Term>();
				clausequery.extractTerms(term);
				//if clause query is a term query
				if(term.size() == 1){
					hits = m_searcher.search(new FuzzyQuery(term.iterator().next()));
				}
			}

			if((hits != null) && (hits.length() > 0)){
				s_log.debug("results.length(): " + hits.length());
				Collection<ISummaryGraphElement> res = new LinkedHashSet<ISummaryGraphElement>();	
				result.put(clausequery.toString("label"), res);
				for(int i = 0; i < hits.length(); i++){
					Document doc = hits.doc(i);
					float score = hits.score(i);
					if(score >= prune){
						String type = doc.get(TYPE_FIELD);
						if(type.equals(LITERAL)){
							ILiteral lit = new Literal(pruneString(doc.get(LABEL_FIELD)));
							SummaryGraphValueElement vvertex = new SummaryGraphValueElement(lit, score);
							vvertex.setDatasource(DS_FIELD);
							
							Map<IDataProperty, Collection<INamedConcept>> neighbors = new HashMap<IDataProperty, Collection<INamedConcept>>();
							Term term = new Term(LITERAL_FIELD,lit.getLabel());
	        		        TermQuery query = new TermQuery(term);
	        		        Hits results = m_searcher.search(query);
	        		        if((results != null) && (results.length() > 0)){
	        		        	for(int j = 0; j < results.length(); j++){
	        		        		Document docu = results.doc(j);
	        		        		if(docu != null){
	        		        			IDataProperty prop = new DataProperty(pruneString(docu.get(ATTRIBUTE_FIELD)));
	        		        			Collection<INamedConcept> concepts = new HashSet<INamedConcept>();
	        		        			String[] cons = docu.getValues(CONCEPT_FIELD);
	        		        			for (int k = 0; k < cons.length; k++){
	    									INamedConcept con = new NamedConcept(pruneString(cons[k]));
	    									concepts.add(con);
	    								}
	        		        			neighbors.put(prop, concepts);
	        		        		}
	        		        	}
	        		        }
	        		        vvertex.setNeighbors(neighbors);
	        		        res.add(vvertex);
							updateScore(sumGraph, vvertex, score);
						}
						else if(type.equals(CONCEPT)){
							INamedConcept con = new NamedConcept(pruneString(doc.get(URI_FIELD)));
							SummaryGraphElement cvertex = new SummaryGraphElement (con,SummaryGraphElement.CONCEPT, score);
							cvertex.setDatasource(doc.get(DS_FIELD));
							Emergency.checkPrecondition(sumGraph.containsVertex(cvertex), "Classvertex must be contained in summary graph:" + cvertex.toString());
							res.add(cvertex);
							updateScore(sumGraph, cvertex, score);
						}
						else if(type.equals(DATAPROPERTY)){
							IDataProperty prop = new DataProperty(pruneString(doc.get(URI_FIELD)));
							SummaryGraphAttributeElement pVertex = new SummaryGraphAttributeElement(prop,SummaryGraphElement.ATTRIBUTE, score);
							pVertex.setDatasource(doc.get(DS_FIELD));
							
							Collection<INamedConcept> neighborConcepts = new HashSet<INamedConcept>();
							String[] cons = doc.getValues(CONCEPT_FIELD);	
							for (int k = 0; k < cons.length; k++){
								INamedConcept con = new NamedConcept(pruneString(cons[k]));
								neighborConcepts.add(con);
							}
							pVertex.setNeighborConcepts(neighborConcepts);
							res.add(pVertex);
							updateScore(sumGraph, pVertex, score);
						}
						else if(type.equals(OBJECTPROPERTY)){
							IObjectProperty objProp = new ObjectProperty(pruneString(doc.get(URI_FIELD)));
							SummaryGraphElement pvertex = new SummaryGraphElement (objProp,SummaryGraphElement.RELATION, score);
							pvertex.setDatasource(doc.get(DS_FIELD));
							res.add(pvertex);
							updateScore(sumGraph, pvertex, score);
						}
					}
				}
			}
		}

		catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	//TODO this is not so efficient when graph is huge...
	private void updateScore(Pseudograph<SummaryGraphElement, SummaryGraphEdge> graph, SummaryGraphElement e, double score){
		Set<SummaryGraphElement> vertices = graph.vertexSet();
		if (vertices != null && vertices.size() > 0){
			for (SummaryGraphElement v : vertices){
				if(v.equals(e)){
					// score = 1 / (EF/IDF*matchingscore)
					v.setCost(1/(v.getCost() * score));
				}
			}
		}
	}

	private Document addConceptsToDataPropertyDoc(Document doc, IDataProperty prop, Pseudograph<SummaryGraphElement, SummaryGraphEdge> schemaGraph){
		if (schemaGraph == null){
			//use schema 
			IConceptDao conceptDao = (IConceptDao) PersistenceUtil.getDaoManager().getAvailableDao(IConceptDao.class);
			Set<INamedConcept> cons = conceptDao.findDomains(prop);
			if (cons != null && cons.size() > 0){
				for(INamedConcept con : cons)
					doc.add(new Field(CONCEPT_FIELD, con.getUri(), Field.Store.YES, Field.Index.NO));
			}
		}
		else{
			Set<SummaryGraphEdge> edges = schemaGraph.edgeSet();
			if (edges != null && edges.size() > 0){
				for (SummaryGraphEdge e : edges){
					if(e.getEdgeLabel() == SummaryGraphEdge.DOMAIN_EDGE){
						if(e.getTarget().getResource().equals(prop)){
							doc.add(new Field(CONCEPT_FIELD, ((IEntity)e.getSource().getResource()).getUri(), Field.Store.YES, Field.Index.NO));
						}
					}
				}
			}
		}

		return doc;
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
			e.printStackTrace();
		}
	}

	public void init(Object... params) {

	}

}
