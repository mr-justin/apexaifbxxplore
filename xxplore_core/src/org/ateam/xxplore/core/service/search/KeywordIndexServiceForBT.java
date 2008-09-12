package org.ateam.xxplore.core.service.search;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
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
import org.xmedia.oms.model.api.IDataProperty;
import org.xmedia.oms.model.api.ILiteral;
import org.xmedia.oms.model.api.INamedConcept;
import org.xmedia.oms.model.api.IObjectProperty;
import org.xmedia.oms.model.impl.DataProperty;
import org.xmedia.oms.model.impl.Literal;
import org.xmedia.oms.model.impl.NamedConcept;
import org.xmedia.oms.model.impl.ObjectProperty;
import org.xmedia.oms.model.impl.Property;
import org.xmedia.oms.persistence.PersistenceUtil;
import org.xmedia.oms.persistence.dao.IConceptDao;


public class KeywordIndexServiceForBT implements IService{

	private static Logger s_log = Logger.getLogger(KeywordIndexServiceForBT.class);

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
	private static final String RELATION_FIELD = "relation";
	private static final String LITERAL_FIELD = "value";
	private static final String DOMAIN_FIELD = "domain";
	private static final String RANGE_FIELD = "range";

	private static final double THRESHOLD_SCORE = 0.9;

	public KeywordIndexServiceForBT(String keywordIndexDir, boolean create) {
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


	public void indexKeywords(String dsPath, String datasourceURI, Pseudograph<SummaryGraphElement, SummaryGraphEdge> schemaGraph, String synIndexdir) {

		try {
			IndexSearcher indexSearcher = null;
			if (synIndexdir != null) indexSearcher = new IndexSearcher(synIndexdir);

			indexDataSourceByConcept(m_indexWriter, indexSearcher, datasourceURI, schemaGraph);
			indexDataSourceByProperty(m_indexWriter, indexSearcher, datasourceURI, schemaGraph);

			indexSearcher.close();
			SesameDao sd = new SesameDao(dsPath);
			indexDataSourceByLiteral(m_indexWriter, datasourceURI, sd);
			indexDataSourceByIndividual(m_indexWriter, datasourceURI, sd);

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
	protected  void indexDataSourceByConcept(IndexWriter indexWriter,IndexSearcher searcher, String ds, Pseudograph graph) throws Exception{
		System.out.println("start indexing by concept");
	
		Set<SummaryGraphElement> nodes = graph.vertexSet();
		Iterator<SummaryGraphElement> nodeIter = nodes.iterator();
		String lastURI = "", lastEdge = "", lastRangeURI = "";
		while (nodeIter.hasNext()) {
			SummaryGraphElement node = nodeIter.next();
			if (node.type == SummaryGraphElement.CONCEPT) {
				String uri = ((NamedConcept)node.getResource()).getUri();
				String label = uri.substring(uri.lastIndexOf('/')+1);
//				System.out.println(uri);
				Document doc = new Document();
				doc.add(new Field(TYPE_FIELD, CONCEPT,
						Field.Store.YES, Field.Index.NO));
				doc.add(new Field(LABEL_FIELD, label, Field.Store.NO,
						Field.Index.TOKENIZED));
				doc.add(new Field(URI_FIELD, uri, Field.Store.YES, Field.Index.NO));
				doc.add(new Field(DS_FIELD, ds, Field.Store.YES, Field.Index.NO));
				indexWriter.addDocument(doc);

				Set<SummaryGraphEdge> edges = graph.edgesOf(node);
				for (SummaryGraphEdge edge:edges) {
					SummaryGraphElement toNode = edge.getSource().equals(node)?edge.getTarget():edge.getSource();
					if (toNode.type == SummaryGraphElement.ATTRIBUTE) {
						Document dpdoc = new Document();
//						System.out.println(edge.name + "\t" + uri);
						String lab = ((DataProperty)toNode.getResource()).getUri();
						dpdoc.add(new Field(ATTRIBUTE_FIELD, lab.substring(lab.lastIndexOf('/')+1),
								Field.Store.YES, Field.Index.UN_TOKENIZED));
						dpdoc.add(new Field(DOMAIN_FIELD, uri, Field.Store.YES,
								Field.Index.NO));
						dpdoc.add(new Field(DS_FIELD, ds, Field.Store.YES, Field.Index.NO));
						indexWriter.addDocument(dpdoc);
					} else if (toNode.type == SummaryGraphElement.RELATION) {
//						if (edge.originDirection) {
//							Node range = nodeMap.get(Integer
//									.valueOf(edge.to_id));
//							String range_uri = baseURI + range.name;
//							if (lastEdge.equals(edge.name) && lastURI.equals(uri) && lastRangeURI.equals(range_uri))
//								continue;
////							System.out.println(edge.name + "\t" + uri + "\t" + range_uri);
//							lastEdge = edge.name;
//							lastURI = uri;
//							lastRangeURI = range_uri;
							Document opdoc = new Document();
							String lab = ((ObjectProperty)toNode.getResource()).getUri();
							opdoc.add(new Field(RELATION_FIELD, lab.substring(lab.lastIndexOf('/')+1),
									Field.Store.YES, Field.Index.UN_TOKENIZED));
							if(edge.getEdgeLabel().equals(SummaryGraphEdge.DOMAIN_EDGE))
								opdoc.add(new Field(DOMAIN_FIELD, uri, Field.Store.YES,
									Field.Index.NO));
							else if(edge.getEdgeLabel().equals(SummaryGraphEdge.RANGE_EDGE))
								opdoc.add(new Field(RANGE_FIELD, uri,
									Field.Store.YES, Field.Index.NO));
							opdoc.add(new Field(DS_FIELD, ds, Field.Store.YES, Field.Index.NO));
							indexWriter.addDocument(opdoc);
						
					}
				}
				Set<String> values = new HashSet<String>();
				Term term = new Term(WordnetSynsIndexService.WORD_FIELD, label.toLowerCase());
				TermQuery termQuery = new TermQuery(term);
				Hits results = searcher.search(termQuery);
				if (results != null && results.length() > 0) {
					for (int i = 0; i < results.length(); i++) {
						Document docu = results.doc(i);
						values.addAll(Arrays.asList(docu.getValues(WordnetSynsIndexService.SYN_FIELD)));
					}
				}
				// System.out.println(uri + ": " + values);
				for (String value : values) {
					Document docu = new Document();
					docu.add(new Field(TYPE_FIELD, CONCEPT,
							Field.Store.YES, Field.Index.NO));
					docu.add(new Field(LABEL_FIELD, value, Field.Store.NO,
							Field.Index.TOKENIZED));
					docu.add(new Field(URI_FIELD, uri, Field.Store.YES,
							Field.Index.NO));
					docu.add(new Field(DS_FIELD, ds, Field.Store.YES, Field.Index.NO));
					indexWriter.addDocument(docu);
				}
				values.clear();
			}
		}
	}

	@SuppressWarnings("deprecation")
	protected  void indexDataSourceByProperty(IndexWriter indexWriter,IndexSearcher searcher, String ds, Pseudograph<SummaryGraphElement, SummaryGraphEdge> schemagraph) throws Exception{
//		Collection<Edge> edges = graph.edges.values();
//		Iterator<Edge> edgeIter = edges.iterator();
		Set<SummaryGraphElement> edges = schemagraph.vertexSet();
		Set<String> relSet = new HashSet<String>();
		Set<String> attrSet = new HashSet<String>();
		for(SummaryGraphElement edge: edges) {
			if(edge.type == SummaryGraphElement.ATTRIBUTE || edge.type == SummaryGraphElement.RELATION){
			String uri = ((Property)edge.getResource()).getUri();
			String label = uri.substring(uri.lastIndexOf('/')+1);
			Document doc = new Document();
			doc.add(new Field(LABEL_FIELD, label, Field.Store.NO,
					Field.Index.TOKENIZED));

			doc.add(new Field(URI_FIELD, uri, Field.Store.YES, Field.Index.NO));
			if (edge.type == SummaryGraphElement.RELATION) {
				if (relSet.contains(label))
					continue;
				relSet.add(label);
				doc.add(new Field(TYPE_FIELD, OBJECTPROPERTY,
						Field.Store.YES, Field.Index.NO));
				doc.add(new Field(DS_FIELD, ds, Field.Store.YES, Field.Index.NO));
				indexWriter.addDocument(doc);
			} else if (edge.type == SummaryGraphElement.ATTRIBUTE) {
				if (attrSet.contains(label))
					continue;
				attrSet.add(label);
				doc.add(new Field(TYPE_FIELD, DATAPROPERTY,
						Field.Store.YES, Field.Index.NO));
				doc.add(new Field(DS_FIELD, ds, Field.Store.YES, Field.Index.NO));
				indexWriter.addDocument(doc);
			}
			//System.out.println(label);
			Set<String> values = new HashSet<String>();
			Term term = new Term(WordnetSynsIndexService.WORD_FIELD, label.toLowerCase());
			TermQuery termQuery = new TermQuery(term);
			Hits results = searcher.search(termQuery);
			if (results != null && results.length() > 0) {
				for (int i = 0; i < results.length(); i++) {
					Document docu = results.doc(i);
					values.addAll(Arrays.asList(docu.getValues(WordnetSynsIndexService.SYN_FIELD)));
				}
			}
			for (String value : values) {
				Document docu = new Document();
				docu.add(new Field(LABEL_FIELD, value, Field.Store.NO,
						Field.Index.TOKENIZED));
				docu.add(new Field(URI_FIELD, uri, Field.Store.YES,
								Field.Index.NO));
				if (edge.type == SummaryGraphElement.RELATION)
					docu.add(new Field(TYPE_FIELD, OBJECTPROPERTY,
							Field.Store.YES, Field.Index.NO));
				else if(edge.type == SummaryGraphElement.ATTRIBUTE)
					docu.add(new Field(TYPE_FIELD, DATAPROPERTY,
							Field.Store.YES, Field.Index.NO));
				docu.add(new Field(DS_FIELD, ds, Field.Store.YES, Field.Index.NO));
				indexWriter.addDocument(docu);
			}
			values.clear();
		}
		}
	}

	protected  void indexDataSourceByLiteral(IndexWriter indexWriter, String ds, SesameDao sd) throws Exception{
//		ILiteralDao literalDao = (ILiteralDao) PersistenceUtil.getDaoManager().getAvailableDao(ILiteralDao.class);
//		List literals = literalDao.findAll();
		System.out.println("start indexing by literal");
		try{
			sd.findAllTriples();
			int count = 0;
			HashSet<String> litSet = new HashSet<String>();
			//for (Object literal:literals){
			while(sd.hasNext()){
				sd.next();
				count++;
				if(count%10000==0)
					System.out.println(count);
				if(!sd.getObjectType().equals(SesameDao.LITERAL))continue;
				String literal = sd.getObject();
				if(litSet.contains(literal))
					continue;
				litSet.add(literal);
				Document doc = new Document();
				doc.add(new Field(DS_FIELD, ds, Field.Store.YES, Field.Index.NO));
				doc.add(new Field(TYPE_FIELD, LITERAL, Field.Store.YES, Field.Index.NO));
				doc.add(new Field(LABEL_FIELD, literal.substring(1, literal.lastIndexOf('"')), Field.Store.YES, Field.Index.TOKENIZED));
				
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
	public void indexDataSourceByIndividual (IndexWriter indexWriter, String ds, SesameDao sd) throws Exception{
		HashSet<String> indSet = new HashSet<String>();
		sd.findAllTriples();
		int count = 0;
		while (sd.hasNext()) {
			sd.next();
			count++;
			if(count%10000==0)
				System.out.println(count);
			if(!sd.getObjectType().equals(SesameDao.LITERAL))
				continue;
			indexDataSourcePerIndividual(indexWriter, sd.getSubject(), ds, sd);
		}
		indSet.clear();
	}
	private void indexDataSourcePerIndividual(IndexWriter indexWriter, String ind, String ds, SesameDao sd) throws Exception {

//			RepositoryConnection conn = repository.getConnection();
			sd.findPropertyAndIndividual(ind);
			Set<String> concepts = new HashSet<String>();
			Map<String, String> av_map = new HashMap<String, String>();
			while (sd.hasNext()) {
				sd.next();
				if (sd.getObjectType().equals(SesameDao.CONCEPT)) {
					concepts.add(sd.getObject());
//					System.out.println(stmt.getObject().stringValue());
				} else if (sd.getObjectType().equals(SesameDao.LITERAL)) {
					av_map.put(sd.getPredicate(), sd.getObject().substring(1,sd.getObject().lastIndexOf('"')));
				}
			}
			Iterator<String> av_iter = av_map.keySet().iterator();
			
			while (av_iter.hasNext()) {
				String attr = av_iter.next();
				String lit = av_map.get(attr);
//				if (lit.equals("The Hungry Stones And Other Stories"))
//					System.out.println(lit); 
				Iterator<String> con_iter = concepts.iterator();
				while (con_iter.hasNext()) {
					String concept = con_iter.next();
					Document doc = new Document();
					doc.add(new Field(LITERAL_FIELD, lit, Field.Store.YES, Field.Index.UN_TOKENIZED));
//					if (lit.equals("The Hungry Stones And Other Stories"))
//						System.out.println(lit); 
					doc.add(new Field(ATTRIBUTE_FIELD, attr,Field.Store.YES,Field.Index.NO));
					doc.add(new Field(CONCEPT_FIELD, concept,Field.Store.YES, Field.Index.NO));
					doc.add(new Field(DS_FIELD, ds, Field.Store.YES, Field.Index.NO));
					indexWriter.addDocument(doc);
				}
			}
			concepts.clear();
			concepts = null;
			av_map.clear();
			av_map = null;
			
		
	}
	/**
	 * Search for keyword elements and augment the summary graph.
	 * @param query
	 * @param sumGraph
	 * @param prune
	 * @return
	 */
	public Map<String,Collection<SummaryGraphElement>> searchKb(String query, double prune){
		Map<String,Collection<SummaryGraphElement>> ress = new LinkedHashMap<String,Collection<SummaryGraphElement>>();
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
					Map<String, Collection<SummaryGraphElement>> partialRes = searchWithClause(clauseQ, prune);
					if (partialRes != null && partialRes.size() > 0) ress.putAll(partialRes);
				}
			}
			//is a phrase or term query 
			else{
				ress = searchWithClause(q, prune);
			}

		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return ress;
	}

	private Map<String,Collection<SummaryGraphElement>> searchWithClause(Query clausequery, double prune){
		Map<String,Collection<SummaryGraphElement>> result = new LinkedHashMap<String,Collection<SummaryGraphElement>>();
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
				Collection<SummaryGraphElement> res = new LinkedHashSet<SummaryGraphElement>();	
				result.put(clausequery.toString("label"), res);
				for(int i = 0; i < hits.length(); i++){
					Document doc = hits.doc(i);
					float score = hits.score(i);
					if(score >= prune){
						String type = doc.get(TYPE_FIELD);
						if(type.equals(LITERAL)){
							ILiteral lit = new Literal(pruneString(doc.get(LABEL_FIELD)));
							SummaryGraphValueElement vvertex = new SummaryGraphValueElement(lit);
							vvertex.setMatchingScore(score);
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
						}
						else if(type.equals(CONCEPT)){
							INamedConcept con = new NamedConcept(pruneString(doc.get(URI_FIELD)));
							SummaryGraphElement cvertex = new SummaryGraphElement (con,SummaryGraphElement.CONCEPT);
							cvertex.setMatchingScore(score);
							cvertex.setDatasource(doc.get(DS_FIELD));
							res.add(cvertex);
						}
						else if(type.equals(DATAPROPERTY)){
							IDataProperty prop = new DataProperty(pruneString(doc.get(URI_FIELD)));
							SummaryGraphAttributeElement pVertex = new SummaryGraphAttributeElement(prop,SummaryGraphElement.ATTRIBUTE);
							pVertex.setMatchingScore(score);
							pVertex.setDatasource(doc.get(DS_FIELD));
							
							Collection<INamedConcept> neighborConcepts = new HashSet<INamedConcept>();
							String[] cons = doc.getValues(CONCEPT_FIELD);	
							for (int k = 0; k < cons.length; k++){
								INamedConcept con = new NamedConcept(pruneString(cons[k]));
								neighborConcepts.add(con);
							}
							pVertex.setNeighborConcepts(neighborConcepts);
							res.add(pVertex);
						}
						else if(type.equals(OBJECTPROPERTY)){
							IObjectProperty objProp = new ObjectProperty(pruneString(doc.get(URI_FIELD)));
							SummaryGraphElement pvertex = new SummaryGraphElement (objProp,SummaryGraphElement.RELATION);
							pvertex.setMatchingScore(score);
							pvertex.setDatasource(doc.get(DS_FIELD));
							res.add(pvertex);
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

	public static void main(String[] args) throws Exception
	{
		Pseudograph graph = new SummaryGraphIndexServiceForBT().readGraphIndexFromFile(SesameDao.root+"schema-dblp.obj");
		String path  = null;
		new KeywordIndexServiceForBT(SesameDao.root+"keywordIndex", true).indexKeywords(path, "dblp", graph,SesameDao.root+"apexaifbxxplore\\keywordsearch\\syn_index");
//		concept & property test
		IndexSearcher searcher = new IndexSearcher(SesameDao.root+"keywordIndex");
		//System.out.println(hits.length());
		for(int i=0; i<searcher.maxDoc(); i++)
		{
			System.out.println("Doc:"+searcher.doc(i).toString());
			Enumeration flist = searcher.doc(i).fields();
			while(flist.hasMoreElements())
			{
				Field field = (Field)flist.nextElement();
				System.out.println("\t"+field.name()+": "+field.stringValue());
			}
		}
//		//literal test
//		TermQuery query = new TermQuery(new Term(LABEL_FIELD,"world"));
//		Hits hits = searcher.search(query);
//		for(int i=0; i<hits.length(); i++)
//			if(hits.doc(i).get(TYPE_FIELD).equals(LITERAL))
//			{
//				System.out.println(hits.doc(i).get(LABEL_FIELD));
//			}
//		//individual test
		
		
	}
}
