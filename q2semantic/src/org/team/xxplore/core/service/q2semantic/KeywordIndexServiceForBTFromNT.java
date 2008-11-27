package org.team.xxplore.core.service.q2semantic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
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
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.FSDirectory;
import org.jgrapht.graph.Pseudograph;
import org.team.xxplore.core.service.api.ILiteral;
import org.team.xxplore.core.service.api.INamedConcept;
import org.team.xxplore.core.service.impl.DataProperty;
import org.team.xxplore.core.service.impl.Literal;
import org.team.xxplore.core.service.impl.NamedConcept;
import org.team.xxplore.core.service.impl.ObjectProperty;
import org.team.xxplore.core.service.impl.Property;

/**
 * Building KeywordIndex from graph and nt File
 * @author kaifengxu
 *
 */
public class KeywordIndexServiceForBTFromNT{

	private IndexWriter m_indexWriter;
	private StandardAnalyzer m_analyzer;
	private IndexSearcher m_searcher;
	private BloomFilter bf;
	private  String m_IndexDir = null;

	/* Constant */
	private static final String TYPE_FIELD =  "type";
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
	
	/**
	 * index service
	 * @param keywordIndexDir
	 * @param create
	 */
	public KeywordIndexServiceForBTFromNT(String keywordIndexDir,boolean build) 
	{
		m_analyzer = new StandardAnalyzer();
		m_IndexDir = keywordIndexDir;
		
		File indexDir = new File(keywordIndexDir);
		if (!indexDir.exists())
			indexDir.mkdirs();
		try 
		{
			IndexReader.unlock(FSDirectory.getDirectory(indexDir)); //unlock the writing of index
			m_indexWriter = new IndexWriter(m_IndexDir, m_analyzer, build);
		} 
		catch (IOException e) 
		{
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
	public void indexKeywords(String ntFn, String datasourceURI, Pseudograph<SummaryGraphElement, SummaryGraphEdge> schemaGraph, boolean bigNT) 
	{
		try 
		{
			System.out.println("===========keyword index===========");
			/* index concept from graph */
			indexDataSourceByConcept(m_indexWriter, datasourceURI, schemaGraph);
			/* index property from graph */
			indexDataSourceByProperty(m_indexWriter, datasourceURI, schemaGraph);
			/* index literal & individual from nt file */
			indexDataSourceByLiteralandIndividual(m_indexWriter, ntFn, datasourceURI, bigNT);
			m_indexWriter.optimize();
			m_indexWriter.close();
		}
		catch (Exception e) 
		{
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
	protected  void indexDataSourceByConcept(IndexWriter indexWriter, String ds, Pseudograph<SummaryGraphElement, SummaryGraphEdge> graph) throws Exception
	{
		System.out.println("start indexing by concept");
		
		/* Scan the graphElement of schema graph */
		Set<SummaryGraphElement> nodes = graph.vertexSet();
		Iterator<SummaryGraphElement> nodeIter = nodes.iterator();
		while (nodeIter.hasNext()) {
			SummaryGraphElement node = nodeIter.next();
			
			if (node.getType() == SummaryGraphElement.CONCEPT) 
			{
				String uri = ((NamedConcept)node.getResource()).getUri();
				String label = SummaryGraphUtil.getLocalName(uri);
				/* Write Index */
				Document doc = new Document();
				doc.add(new Field(TYPE_FIELD, CONCEPT,
						Field.Store.YES, Field.Index.NO));
				doc.add(new Field(LABEL_FIELD, label, Field.Store.NO,
						Field.Index.TOKENIZED));
				doc.add(new Field(URI_FIELD, uri, Field.Store.YES, Field.Index.NO));
				doc.add(new Field(DS_FIELD, ds, Field.Store.YES, Field.Index.NO));
				indexWriter.addDocument(doc);
				
				/* get Concept's attribute or relation */
				Set<SummaryGraphEdge> edges = graph.edgesOf(node);
				for (SummaryGraphEdge edge:edges) {
					SummaryGraphElement toNode = edge.getSource().equals(node)?edge.getTarget():edge.getSource();
					
					if (toNode.getType() == SummaryGraphElement.ATTRIBUTE) 
					{
						Document dpdoc = new Document();
						String lab = ((DataProperty)toNode.getResource()).getUri();
						dpdoc.add(new Field(ATTRIBUTE_FIELD, SummaryGraphUtil.getLocalName(lab),
								Field.Store.YES, Field.Index.UN_TOKENIZED));
						dpdoc.add(new Field(DOMAIN_FIELD, uri, Field.Store.YES,
								Field.Index.NO));
						dpdoc.add(new Field(DS_FIELD, ds, Field.Store.YES, Field.Index.NO));
						indexWriter.addDocument(dpdoc);
					} 
					else if (toNode.getType() == SummaryGraphElement.RELATION)
					{
						Document opdoc = new Document();
						String lab = ((ObjectProperty)toNode.getResource()).getUri();
						opdoc.add(new Field(RELATION_FIELD, SummaryGraphUtil.getLocalName(lab), Field.Store.YES, Field.Index.UN_TOKENIZED));
						/* Domain or Range */
						if(edge.getEdgeLabel().equals(SummaryGraphEdge.DOMAIN_EDGE))
							opdoc.add(new Field(DOMAIN_FIELD, uri, Field.Store.YES, Field.Index.NO));
						else if(edge.getEdgeLabel().equals(SummaryGraphEdge.RANGE_EDGE))
							opdoc.add(new Field(RANGE_FIELD, uri, Field.Store.YES, Field.Index.NO));
						opdoc.add(new Field(DS_FIELD, ds, Field.Store.YES, Field.Index.NO));
						indexWriter.addDocument(opdoc);
					}
				}
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
	protected  void indexDataSourceByProperty(IndexWriter indexWriter, String ds, Pseudograph<SummaryGraphElement, SummaryGraphEdge> schemagraph) throws Exception
	{
		System.out.println("start indexing by property");
		
		/* Scan the graphEdge of schema graph */
		Set<SummaryGraphElement> edges = schemagraph.vertexSet();
		Set<String> relSet = new HashSet<String>();
		Set<String> attrSet = new HashSet<String>();
		for(SummaryGraphElement edge: edges) {
			if(edge.getType() == SummaryGraphElement.ATTRIBUTE || edge.getType() == SummaryGraphElement.RELATION)
			{
				String uri = ((Property)edge.getResource()).getUri();
				String label = SummaryGraphUtil.getLocalName(uri);
				Document doc = new Document();
				/* relation or attribute */
				if (edge.getType() == SummaryGraphElement.RELATION)
				{
					if (relSet.contains(label))//remove dup
						continue;
					relSet.add(label);
					doc.add(new Field(TYPE_FIELD, OBJECTPROPERTY, Field.Store.YES, Field.Index.NO));
					doc.add(new Field(DS_FIELD, ds, Field.Store.YES, Field.Index.NO));
					doc.add(new Field(LABEL_FIELD, label, Field.Store.NO, Field.Index.TOKENIZED));
					doc.add(new Field(URI_FIELD, uri, Field.Store.YES, Field.Index.NO));
					indexWriter.addDocument(doc);
				} 
				else if (edge.getType() == SummaryGraphElement.ATTRIBUTE)
				{
					if (attrSet.contains(label))//remove dup
						continue;
					attrSet.add(label);
					doc.add(new Field(TYPE_FIELD, DATAPROPERTY, Field.Store.YES, Field.Index.NO));
					doc.add(new Field(DS_FIELD, ds, Field.Store.YES, Field.Index.NO));
					doc.add(new Field(LABEL_FIELD, label, Field.Store.YES, Field.Index.TOKENIZED));
					doc.add(new Field(URI_FIELD, uri, Field.Store.YES, Field.Index.NO));
					indexWriter.addDocument(doc);
				}
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
	public void indexDataSourceByLiteralandIndividual(IndexWriter writer,  String ntFile, String ds, boolean bigNT) throws Exception
	{
		System.out.println("start indexing by lit and indiv");
		/* variable for indexing */
		bf = new BloomFilter();
		TreeSet<String> litSet = new TreeSet<String>();
		TreeSet<Integer> litSetForBigNT = new TreeSet<Integer>();//no use if bigNT = false
		TreeSet<String> indivSet = new TreeSet<String>();
		TreeSet<Integer> indivSetForBigNT = new TreeSet<Integer>();//no use if bigNT = false
		TreeSet<String> concept = new TreeSet<String>();
		TreeMap<String, String> attrlit = new TreeMap<String, String>();

		/* variable for scanning */
		String cur=null, pre=null;
		int count = 0;
		boolean isIndiv = true;
		
		BufferedReader br = new BufferedReader(new FileReader(ntFile));
		String line;
		while((line = br.readLine())!=null)
		{
			count++;
			if(count%10000==0)
				System.out.println(count);

			String[] part = Util4NT.processTripleLine(line);
			if(part==null || part[0].startsWith("_:node") || part[0].length()<2 || part[1].length()<2) continue;
			cur = part[0];

			/* meet with another block and index the previous block */
			if(pre!=null && !pre.equals(cur))
			{
				if(isIndiv)
					writeDocument(concept, attrlit, indivSet, indivSetForBigNT, writer,ds, bigNT);
				isIndiv = true;
				concept.clear();
				concept = new TreeSet<String>();
				attrlit.clear();
				attrlit = new TreeMap<String, String>();
			}
			/* scanning within a block */
			if(isIndiv)
			{
				if(part[1].equals("<http://www.w3.org/2002/07/owl#ObjectProperty>") || part[1].equals("<http://www.w3.org/2002/07/owl#Class>"))
					isIndiv = false;
				else if(part[1].equals("<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>") && !BuildQ2SemanticService.rdfsEdgeSet.contains(part[2].substring(1, part[2].length()-1)))
					concept.add(part[2]);
				else if(!part[2].startsWith("<") && (part[1].equals("<http://www.w3.org/2000/01/rdf-schema#label>") || !BuildQ2SemanticService.rdfsEdgeSet.contains(part[1].substring(1, part[1].length()-1))))
				{
					attrlit.put(part[1].substring(1, part[1].length()-1), part[2]);
					if(!bigNT)//index after scanning
						litSet.add(part[2]);
					else
					{
						int key = bf.hashRabin(part[2]);
						if(litSetForBigNT.contains(key)) continue;//dup
						else litSetForBigNT.add(key);
						Document doc = new Document();
						doc.add(new Field(DS_FIELD, ds, Field.Store.YES, Field.Index.NO));
						doc.add(new Field(TYPE_FIELD, LITERAL, Field.Store.YES, Field.Index.NO));
						doc.add(new Field(LABEL_FIELD, part[2], Field.Store.YES, Field.Index.TOKENIZED));
						writer.addDocument(doc);
					}
				}
			}
			pre = cur;
		}
		
		/* write the last block */
		if(isIndiv)
			writeDocument(concept, attrlit, indivSet,indivSetForBigNT, writer, ds, bigNT);
		indivSet.clear();
		indivSet = null;
		
		/* index literal when bigNT = false */
		if(!bigNT)
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
	 * Write concept+attribute+literal into document
	 * @param concept
	 * @param attrlit
	 * @param indivSet
	 * @param indivSetForBigNT
	 * @param writer
	 * @param ds
	 * @param bigNT
	 * @throws Exception
	 */
	public void writeDocument(TreeSet<String> concept, TreeMap<String, String> attrlit, TreeSet<String> indivSet, TreeSet<Integer> indivSetForBigNT, IndexWriter writer, String ds, boolean bigNT) throws Exception
	{
		for(String con: concept)
		{
			con = con.substring(1, con.length()-1);
			for(String attr: attrlit.keySet())
			{
				String lit = attrlit.get(attr);
				if(!bigNT)
				{
					if(indivSet.contains(lit+attr+con)) continue;
					else indivSet.add(lit+attr+con);
				}
				else
				{
					int key = bf.hashRabin(lit+attr+con);
					if(indivSetForBigNT.contains(key)) continue;
					else indivSetForBigNT.add(key);
				}
				Document doc = new Document();
				doc.add(new Field(LITERAL_FIELD, lit, Field.Store.YES, Field.Index.UN_TOKENIZED));
				doc.add(new Field(ATTRIBUTE_FIELD, attr,Field.Store.YES,Field.Index.UN_TOKENIZED));
				doc.add(new Field(CONCEPT_FIELD, con,Field.Store.YES, Field.Index.UN_TOKENIZED));
				doc.add(new Field(DS_FIELD, ds, Field.Store.YES, Field.Index.UN_TOKENIZED));
				writer.addDocument(doc);
			}
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
					System.out.println("aaa:" + clauseQ.toString());
					Map<String, Collection<SummaryGraphElement>> partialRes = searchWithClause(clauseQ, prune);
					if (partialRes != null && partialRes.size() > 0) ress.putAll(partialRes);
				}
			}
			//is a phrase or term query 
			else{
				ress = searchWithClause(q, prune);
			}

		} catch (Exception e) {
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
			
			if((hits != null) && (hits.length() > 0)){
				Collection<SummaryGraphElement> res = new LinkedHashSet<SummaryGraphElement>();	

				result.put(clausequery.toString("label"), res);
				int count = 0;
				for(int i = 0; i < hits.length(); i++){
					
					Document doc = hits.doc(i);
					float score = hits.score(i);
					
					if(score >= prune){

						String type = doc.get(TYPE_FIELD);

						count ++;
						if(count  > 5 ) break;
						if(type != null && type.equals(LITERAL)){
							ILiteral lit = new Literal(doc.get(LABEL_FIELD));
							SummaryGraphValueElement vvertex = new SummaryGraphValueElement(lit);
							vvertex.setMatchingScore(score);
							vvertex.setDatasource(doc.get(DS_FIELD));

							Map<DataProperty, Collection<INamedConcept>> neighbors = new HashMap<DataProperty, Collection<INamedConcept>>();
							Term term = new Term(LITERAL_FIELD,lit.getLabel());

							TermQuery query = new TermQuery(term);
							Hits results = m_searcher.search(query);

							Collection<INamedConcept> concepts;// = new HashSet<INamedConcept>();
							if((results != null) && (results.length() > 0)){
								for(int j = 0; j < results.length(); j++){
									Document docu = results.doc(j);
									if(docu != null){
//										by kaifengxu
										String property = docu.get(ATTRIBUTE_FIELD);
										if(property==null) continue;
										DataProperty prop = new DataProperty(property);
										String concept = docu.get(CONCEPT_FIELD);
										INamedConcept con = new NamedConcept(concept);
										concepts = neighbors.get(prop);
										if(concepts == null){
											concepts = new HashSet<INamedConcept>();
											neighbors.put(prop, concepts);
										}
										concepts.add(con);
										
									}
								}
							}
							System.out.println("================="+doc.get(LABEL_FIELD));
							for(DataProperty myP : neighbors.keySet()) {
								System.out.println("\t"+myP.getUri()+neighbors.get(myP).size());
								for(INamedConcept t : neighbors.get(myP)){
									System.out.println("\t\t"+t.getUri());
								}
							}
							System.out.println();
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
							DataProperty prop = new DataProperty(pruneString(doc.get(URI_FIELD)));
							SummaryGraphAttributeElement pVertex = new SummaryGraphAttributeElement(prop,SummaryGraphElement.ATTRIBUTE);
							pVertex.setMatchingScore(score);
							pVertex.setDatasource(doc.get(DS_FIELD));

							Collection<INamedConcept> neighborConcepts = new HashSet<INamedConcept>();
							
							Term term = new Term(ATTRIBUTE_FIELD,doc.get(LABEL_FIELD));
							
							TermQuery query = new TermQuery(term);
							
							Hits results = null;
							try{
								results = m_searcher.search(query);
							}
							catch(Exception e) {
								e.printStackTrace();
								System.out.println("guale, ni ge zt...");
							}
							
							if((results != null) && (results.length() > 0)){
								for(int j = 0; j < results.length(); j++){
									Document docu = results.doc(j);
									String concept = docu.get(DOMAIN_FIELD);
									neighborConcepts.add(new NamedConcept(concept));
									System.out.println(concept);
								}
							}
							pVertex.setNeighborConcepts(neighborConcepts);
							res.add(pVertex);
						}
						else if(type != null && type.equals(OBJECTPROPERTY)){
							ObjectProperty objProp = new ObjectProperty(pruneString(doc.get(URI_FIELD)));
							SummaryGraphElement pvertex = new SummaryGraphElement (objProp,SummaryGraphElement.RELATION);
							pvertex.setMatchingScore(score);
							pvertex.setDatasource(doc.get(DS_FIELD));
							res.add(pvertex);
						}
					}
					else break;
				}
				
				System.out.println("out count:" + count);
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
		return str.replaceAll("\"", "");
	}
	
	/**
	 * for test only
	 * @param args
	 * @throws Exception
	 * @throws IOException
	 */
	public static void main(String[] args) throws Exception, IOException {
		IndexSearcher searcher = new IndexSearcher("D:\\semplore\\10.15\\keywordIndexRoot\\swrc-keywordIndex");
		for(int i=0; i<searcher.maxDoc(); i++)
		{
			Document doc = searcher.doc(i);
			if(doc.get(TYPE_FIELD)==null || !doc.get(TYPE_FIELD).equals("concept"))
				continue;
			System.out.println(doc.get(URI_FIELD));

		}
	}
}
