package org.ateam.xxplore.core.service.q2semantic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
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
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
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
import org.apache.lucene.store.FSDirectory;
import org.jgrapht.graph.Pseudograph;

import org.xmedia.oms.model.api.IDataProperty;
import org.xmedia.oms.model.api.ILiteral;
import org.xmedia.oms.model.api.INamedConcept;
import org.xmedia.oms.model.api.IObjectProperty;
import org.xmedia.oms.model.impl.DataProperty;
import org.xmedia.oms.model.impl.Literal;
import org.xmedia.oms.model.impl.NamedConcept;
import org.xmedia.oms.model.impl.ObjectProperty;
import org.xmedia.oms.model.impl.Property;

import com.ibm.semplore.imports.impl.data.load.Util4NT;

public class KeywordIndexServiceForBTFromNT{

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

	private  String m_IndexDir = null;
	
	/**
	 * index service
	 * @param keywordIndexDir
	 * @param create
	 */
	public KeywordIndexServiceForBTFromNT(String keywordIndexDir, boolean create) {
		m_analyzer = new StandardAnalyzer();
		m_IndexDir = keywordIndexDir;
		
		File indexDir = new File(keywordIndexDir);
		if (!indexDir.exists())
			indexDir.mkdirs();
		try {
			//unlock the writing of index
			IndexReader.unlock(FSDirectory.getDirectory(indexDir)); 
			m_indexWriter = new IndexWriter(indexDir, m_analyzer, create);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * index concept property literal individual
	 * @param ntFn
	 * @param datasourceURI
	 * @param schemaGraph
	 * @param synIndexdir
	 */
	public void indexKeywords(String ntFn, String datasourceURI, Pseudograph<SummaryGraphElement, SummaryGraphEdge> schemaGraph, String synIndexdir) {

		try {
			System.out.println("===========keyword index===========");
			IndexSearcher indexSearcher = null;
			if (synIndexdir != null) indexSearcher = new IndexSearcher(synIndexdir);
			//index concept
			indexDataSourceByConcept(m_indexWriter, indexSearcher, datasourceURI, schemaGraph);
			//index property
			indexDataSourceByProperty(m_indexWriter, indexSearcher, datasourceURI, schemaGraph);

			if(indexSearcher != null) indexSearcher.close();
			//index literal & individual
			indexDataSourceByLiteralandIndividual(m_indexWriter, ntFn, datasourceURI);

			m_indexWriter.optimize();
			m_indexWriter.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	/**
	 * index concept
	 * @param indexWriter
	 * @param searcher
	 * @param ds
	 * @param graph
	 * @throws Exception
	 */
	protected  void indexDataSourceByConcept(IndexWriter indexWriter,IndexSearcher searcher, String ds, Pseudograph<SummaryGraphElement, SummaryGraphEdge> graph) throws Exception{
		System.out.println("start indexing by concept");

		Set<SummaryGraphElement> nodes = graph.vertexSet();
		Iterator<SummaryGraphElement> nodeIter = nodes.iterator();
		while (nodeIter.hasNext()) {
			SummaryGraphElement node = nodeIter.next();
			if (node.getType() == SummaryGraphElement.CONCEPT) {
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
					if (toNode.getType() == SummaryGraphElement.ATTRIBUTE) {
						Document dpdoc = new Document();
//						System.out.println(edge.name + "\t" + uri);
						String lab = ((DataProperty)toNode.getResource()).getUri();
						dpdoc.add(new Field(ATTRIBUTE_FIELD, lab.substring(lab.lastIndexOf('/')+1),
								Field.Store.YES, Field.Index.UN_TOKENIZED));
						dpdoc.add(new Field(DOMAIN_FIELD, uri, Field.Store.YES,
								Field.Index.NO));
						dpdoc.add(new Field(DS_FIELD, ds, Field.Store.YES, Field.Index.NO));
						indexWriter.addDocument(dpdoc);
					} else if (toNode.getType() == SummaryGraphElement.RELATION) {
//						if (edge.originDirection) {
//						Node range = nodeMap.get(Integer
//						.valueOf(edge.to_id));
//						String range_uri = baseURI + range.name;
//						if (lastEdge.equals(edge.name) && lastURI.equals(uri) && lastRangeURI.equals(range_uri))
//						continue;
////						System.out.println(edge.name + "\t" + uri + "\t" + range_uri);
//						lastEdge = edge.name;
//						lastURI = uri;
//						lastRangeURI = range_uri;
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
//				if(searcher != null){
//					Set<String> values = new HashSet<String>();
//					Term term = new Term(WordnetSynsIndexService.WORD_FIELD, label.toLowerCase());
//					TermQuery termQuery = new TermQuery(term);
//					Hits results = searcher.search(termQuery);
//					if (results != null && results.length() > 0) {
//						for (int i = 0; i < results.length(); i++) {
//							Document docu = results.doc(i);
//							values.addAll(Arrays.asList(docu.getValues(WordnetSynsIndexService.SYN_FIELD)));
//						}
//					}
//
//					// System.out.println(uri + ": " + values);
//					for (String value : values) {
//						Document docu = new Document();
//						docu.add(new Field(TYPE_FIELD, CONCEPT,
//								Field.Store.YES, Field.Index.NO));
//						docu.add(new Field(LABEL_FIELD, value, Field.Store.NO,
//								Field.Index.TOKENIZED));
//						docu.add(new Field(URI_FIELD, uri, Field.Store.YES,
//								Field.Index.NO));
//						docu.add(new Field(DS_FIELD, ds, Field.Store.YES, Field.Index.NO));
//						indexWriter.addDocument(docu);
//					}
//					values.clear();
//				}
			}
		}
	}

	/**
	 * index property
	 * @param indexWriter
	 * @param searcher
	 * @param ds
	 * @param schemagraph
	 * @throws Exception
	 */
	protected  void indexDataSourceByProperty(IndexWriter indexWriter,IndexSearcher searcher, String ds, Pseudograph<SummaryGraphElement, SummaryGraphEdge> schemagraph) throws Exception{
//		Collection<Edge> edges = graph.edges.values();
//		Iterator<Edge> edgeIter = edges.iterator();
		System.out.println("start indexing by property");
		Set<SummaryGraphElement> edges = schemagraph.vertexSet();
		Set<String> relSet = new HashSet<String>();
		Set<String> attrSet = new HashSet<String>();
		for(SummaryGraphElement edge: edges) {
			if(edge.getType() == SummaryGraphElement.ATTRIBUTE || edge.getType() == SummaryGraphElement.RELATION){
				String uri = ((Property)edge.getResource()).getUri();
				String label = uri.substring(uri.lastIndexOf('/')+1);
				Document doc = new Document();
				doc.add(new Field(LABEL_FIELD, label, Field.Store.NO,
						Field.Index.TOKENIZED));

				doc.add(new Field(URI_FIELD, uri, Field.Store.YES, Field.Index.NO));
				if (edge.getType() == SummaryGraphElement.RELATION) {
					if (relSet.contains(label))
						continue;
					relSet.add(label);
					doc.add(new Field(TYPE_FIELD, OBJECTPROPERTY,
							Field.Store.YES, Field.Index.NO));
					doc.add(new Field(DS_FIELD, ds, Field.Store.YES, Field.Index.NO));
					indexWriter.addDocument(doc);
				} else if (edge.getType() == SummaryGraphElement.ATTRIBUTE) {
					if (attrSet.contains(label))
						continue;
					attrSet.add(label);
					doc.add(new Field(TYPE_FIELD, DATAPROPERTY,
							Field.Store.YES, Field.Index.NO));
					doc.add(new Field(DS_FIELD, ds, Field.Store.YES, Field.Index.NO));
					indexWriter.addDocument(doc);
				}

//				if(searcher != null){
//					//System.out.println(label);
//					Set<String> values = new HashSet<String>();
//					Term term = new Term(WordnetSynsIndexService.WORD_FIELD, label.toLowerCase());
//					TermQuery termQuery = new TermQuery(term);
//					Hits results = searcher.search(termQuery);
//					if (results != null && results.length() > 0) {
//						for (int i = 0; i < results.length(); i++) {
//							Document docu = results.doc(i);
//							values.addAll(Arrays.asList(docu.getValues(WordnetSynsIndexService.SYN_FIELD)));
//						}
//					}
//					for (String value : values) {
//						Document docu = new Document();
//						docu.add(new Field(LABEL_FIELD, value, Field.Store.NO,
//								Field.Index.TOKENIZED));
//						docu.add(new Field(URI_FIELD, uri, Field.Store.YES,
//								Field.Index.NO));
//						if (edge.getType() == SummaryGraphElement.RELATION)
//							docu.add(new Field(TYPE_FIELD, OBJECTPROPERTY,
//									Field.Store.YES, Field.Index.NO));
//						else if(edge.getType() == SummaryGraphElement.ATTRIBUTE)
//							docu.add(new Field(TYPE_FIELD, DATAPROPERTY,
//									Field.Store.YES, Field.Index.NO));
//						docu.add(new Field(DS_FIELD, ds, Field.Store.YES, Field.Index.NO));
//						indexWriter.addDocument(docu);
//					}
//					values.clear();
//				}
			}
		}
	}

	/**
	 * index literal individual
	 * @param ntFile
	 * @param writeLit
	 * @param ds
	 * @param writer
	 * @throws Exception
	 */
	public void indexDataSourceByLiteralandIndividual( IndexWriter writer, String ntFile, String ds) throws Exception
	{
		System.out.println("start indexing by lit and indiv");
		BufferedReader br = new BufferedReader(new FileReader(ntFile));
		TreeSet<String> litSet = new TreeSet<String>();
		TreeSet<String> concept = new TreeSet<String>();
		TreeSet<String> indivSet = new TreeSet<String>();
		TreeMap<String, String> attrlit = new TreeMap<String, String>();
		String cur=null, pre=null;
		String line;

		int count = 0;
		boolean isIndiv = true;
		while((line = br.readLine())!=null)
		{
			count++;
			if(count%10000==0)
			{
				System.out.println(count);
				System.gc();
			}

			String[] part = Util4NT.processTripleLine(line);
			if(part==null || part[0].startsWith("_:node") || part[0].length()<2 || part[1].length()<2)
				continue;
//			System.out.println();
			cur = part[0];
//			System.out.println(pre+" "+cur);

			if(pre!=null && !pre.equals(cur))
			{
				if(isIndiv)
					for(String con: concept)
					{
						con = con.substring(1, con.length()-1);
						for(String attr: attrlit.keySet())
						{
							String lit = attrlit.get(attr);
							{
								if(indivSet.contains(lit+attr+con))
									continue;
								indivSet.add(lit+attr+con);
								Document doc = new Document();
								doc.add(new Field(LITERAL_FIELD, lit, Field.Store.YES, Field.Index.UN_TOKENIZED));
								doc.add(new Field(ATTRIBUTE_FIELD, attr,Field.Store.YES,Field.Index.NO));
								doc.add(new Field(CONCEPT_FIELD, con,Field.Store.YES, Field.Index.NO));
								doc.add(new Field(DS_FIELD, ds, Field.Store.YES, Field.Index.NO));
								writer.addDocument(doc);
							}
						}
					}
				isIndiv = true;
				concept.clear();
				concept = new TreeSet<String>();
				attrlit.clear();
				attrlit = new TreeMap<String, String>();
			}
//			System.out.println("="+part[2]);
			if(isIndiv)
			{
				if(part[1].equals("<http://www.w3.org/2002/07/owl#ObjectProperty>") || part[1].equals("http://www.w3.org/2002/07/owl#Class"))
					isIndiv = false;
				else if(part[1].equals("<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>"))
					concept.add(part[2]);//<.,..>
				else if(!part[2].startsWith("<") && (part[1].equals("<http://www.w3.org/2000/01/rdf-schema#label>") || !BuildQ2SemanticService.rdfsEdgeSet.contains(part[1].substring(1, part[1].length()-1))))
				{
					attrlit.put(part[1].substring(1, part[1].length()-1), part[2]);//http...>...
					litSet.add(part[2]);//...
				}
			}
			pre = cur;
		}
//		write the rest
		for(String con: concept)
		{
			con = con.substring(1, con.length()-1);
			for(String attr: attrlit.keySet())
			{
				String lit = attrlit.get(attr);
				{
					if(indivSet.contains(lit+attr+con))
						continue;
					indivSet.add(lit+attr+con);
					Document doc = new Document();
					doc.add(new Field(LITERAL_FIELD, lit, Field.Store.YES, Field.Index.UN_TOKENIZED));
					doc.add(new Field(ATTRIBUTE_FIELD, attr,Field.Store.YES,Field.Index.NO));
					doc.add(new Field(CONCEPT_FIELD, con,Field.Store.YES, Field.Index.NO));
					doc.add(new Field(DS_FIELD, ds, Field.Store.YES, Field.Index.NO));
					writer.addDocument(doc);
				}
			}
		}
		indivSet.clear();
		indivSet = null;
//		index literal
		for(String lit: litSet)
		{
			Document doc = new Document();
			doc.add(new Field(DS_FIELD, ds, Field.Store.YES, Field.Index.NO));
			doc.add(new Field(TYPE_FIELD, LITERAL, Field.Store.YES, Field.Index.NO));
			doc.add(new Field(LABEL_FIELD, lit, Field.Store.YES, Field.Index.TOKENIZED));
			writer.addDocument(doc);
		}
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
				m_searcher = new IndexSearcher(m_IndexDir);
			}
			QueryParser parser = new QueryParser("label", m_analyzer);
			Query q = parser.parse(query);
			if (q instanceof BooleanQuery){
				BooleanClause[] clauses = ((BooleanQuery)q).getClauses();
				for(int i = 0; i < clauses.length; i++){
					Query clauseQ = clauses[i].getQuery();
					System.out.println(clauseQ.toString());
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
	/**
	 * sub query of searchKB
	 * @param clausequery
	 * @param prune
	 * @return
	 */
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
				Collection<SummaryGraphElement> res = new LinkedHashSet<SummaryGraphElement>();	

				result.put(clausequery.toString("label"), res);
				for(int i = 0; i < hits.length(); i++){
					Document doc = hits.doc(i);
					float score = hits.score(i);
					if(score >= prune){
						String type = doc.get(TYPE_FIELD);
						if(type != null && type.equals(LITERAL)){
//							by chenjunquan
							ILiteral lit = new Literal(doc.get(LABEL_FIELD));
							SummaryGraphValueElement vvertex = new SummaryGraphValueElement(lit);
							vvertex.setMatchingScore(score);
							vvertex.setDatasource(doc.get(DS_FIELD));

							Map<IDataProperty, Collection<INamedConcept>> neighbors = new HashMap<IDataProperty, Collection<INamedConcept>>();
							Term term = new Term(LITERAL_FIELD,lit.getLabel());

							TermQuery query = new TermQuery(term);
							Hits results = m_searcher.search(query);

							Collection<INamedConcept> concepts;// = new HashSet<INamedConcept>();
							if((results != null) && (results.length() > 0)){
								for(int j = 0; j < results.length(); j++){
									Document docu = results.doc(j);
									if(docu != null){
//										by kaifengxu
//										String property = pruneString(docu.get(ATTRIBUTE_FIELD));
										String property = docu.get(ATTRIBUTE_FIELD);
										if(property==null) continue;
										IDataProperty prop = new DataProperty(property);
//										Collection<INamedConcept> concepts = new HashSet<INamedConcept>();
//										by kaifengxu
//										String concept = pruneString(docu.get(CONCEPT_FIELD));
										String concept = docu.get(CONCEPT_FIELD);
										INamedConcept con = new NamedConcept(concept);
//										String[] cons = docu.getValues(CONCEPT_FIELD);
//										for (int k = 0; k < cons.length; k++){
//											INamedConcept con = new NamedConcept(pruneString(cons[k]));
//											System.out.println("\t" + k + ": " + cons[k]);
//											System.out.println(con.getUri());
//										System.out.println(prop==null);
//										System.out.println(property);
										concepts = neighbors.get(prop);
										if(concepts == null)
											concepts = new HashSet<INamedConcept>();
										concepts.add(con);
//										}
//										System.out.println(((HashSet)concepts).iterator().next());
										neighbors.put(prop, concepts);
									}
								}
							}
							vvertex.setNeighbors(neighbors);
							res.add(vvertex);
						}
						else if(type != null && type.equals(CONCEPT)){
							INamedConcept con = new NamedConcept(pruneString(doc.get(URI_FIELD)));
							SummaryGraphElement cvertex = new SummaryGraphElement (con,SummaryGraphElement.CONCEPT);
							cvertex.setMatchingScore(score);
							cvertex.setDatasource(doc.get(DS_FIELD));
							res.add(cvertex);
						}
						else if(type != null && type.equals(DATAPROPERTY)){
							IDataProperty prop = new DataProperty(pruneString(doc.get(URI_FIELD)));
							SummaryGraphAttributeElement pVertex = new SummaryGraphAttributeElement(prop,SummaryGraphElement.ATTRIBUTE);
							pVertex.setMatchingScore(score);
							pVertex.setDatasource(doc.get(DS_FIELD));

							Collection<INamedConcept> neighborConcepts = new HashSet<INamedConcept>();
							
							Term term = new Term(ATTRIBUTE_FIELD,doc.get(LABEL_FIELD));
							
							TermQuery query = new TermQuery(term);
							
//							System.out.println(query==null);
							Hits results = null;
							try{
							results = m_searcher.search(query);
							}catch(Exception e)
							{System.out.println("guale, ni ge zt...");}
//							System.out.println(doc.get("PPP: "+LABEL_FIELD));
							if((results != null) && (results.length() > 0)){
								for(int j = 0; j < results.length(); j++){
									Document docu = results.doc(j);
//									System.out.println("\t"+docu.get(DOMAIN_FIELD));
									String concept = docu.get(DOMAIN_FIELD);
									neighborConcepts.add(new NamedConcept(concept));
								}
							}
//							String[] cons = doc.getValues(CONCEPT_FIELD);	
//							for (int k = 0; k < cons.length; k++){
//								INamedConcept con = new NamedConcept(pruneString(cons[k]));
//								neighborConcepts.add(con);
//							}
							pVertex.setNeighborConcepts(neighborConcepts);
							res.add(pVertex);
						}
						else if(type != null && type.equals(OBJECTPROPERTY)){
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
	
	/**
	 * remove "
	 * @param str
	 * @return
	 */
	private String pruneString(String str) {
		return str.replace("\"", "");
	}


	public void disposeService() {
		// TODO Auto-generated method stub
		
	}

	public void init(Object... params) {
		// TODO Auto-generated method stub
		
	}
	
	public static void main(String[] args) throws Exception, IOException {
		IndexSearcher searcher = new IndexSearcher("D:\\semplore\\keywordIndexRoot\\dblp-keywordIndex");
		TermQuery query = new TermQuery(new Term(LABEL_FIELD, "net"));
		Hits hits = searcher.search(query);
		for(int i=0; i<hits.length(); i++)
		{
			Document doc = hits.doc(i);
			Enumeration<Field> fields = doc.fields();
			System.out.println("====="+i);
			while(fields.hasMoreElements())
			{
				Field field = fields.nextElement();
				System.out.println(field.name());
				System.out.println("\t"+field.stringValue());
			}
		}
	}
}
