package org.team.xxplore.core.service.q2semantic;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.team.xxplore.core.service.api.ILiteral;
import org.team.xxplore.core.service.api.INamedConcept;
import org.team.xxplore.core.service.impl.DataProperty;
import org.team.xxplore.core.service.impl.Literal;
import org.team.xxplore.core.service.impl.NamedConcept;
import org.team.xxplore.core.service.impl.ObjectProperty;

public class KeywordSearcher {
	private static final String TYPE_FIELD = "type";
	private static final String CONCEPT = "concept";
	private static final String OBJECTPROPERTY = "objectproperty";
	private static final String DATAPROPERTY = "dataproperty";
	private static final String LITERAL = "literal";
	
	private static final String LABEL_FIELD = "label";
	private static final String URI_FIELD = "uri";
	private static final String DS_FIELD = "ds";
	
	private static final String CONCEPT_FIELD = "concept_field";
	private static final String ATTRIBUTE_FIELD = "attribute_field";
	private static final String LITERAL_FIELD = "literal_field";

	/**
	 * Search for keyword elements and augment the summary graph.
	 * @param query
	 * @param sumGraph
	 * @param prune
	 * @return
	 */
	public Map<String,Collection<SummaryGraphElement>> searchKb(String indexDir,String query, double prune){
		Map<String,Collection<SummaryGraphElement>> ress = new LinkedHashMap<String,Collection<SummaryGraphElement>>();
		try {
			IndexSearcher searcher = new IndexSearcher(indexDir);
			StandardAnalyzer analyzer = new StandardAnalyzer();
			QueryParser parser = new QueryParser("label", analyzer);
			Query q = parser.parse(query);
			if (q instanceof BooleanQuery){
				BooleanClause[] clauses = ((BooleanQuery)q).getClauses();
				for(int i = 0; i < clauses.length; i++){
					Query clauseQ = clauses[i].getQuery();
					Map<String, Collection<SummaryGraphElement>> partialRes = searchWithClause(searcher,clauseQ, prune);
					if (partialRes != null && partialRes.size() > 0) ress.putAll(partialRes);
				}
			}
			//is a phrase or term query 
			else{
				ress = searchWithClause(searcher,q, prune);
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
	private Map<String,Collection<SummaryGraphElement>> searchWithClause(IndexSearcher searcher,Query clausequery, double prune){
		Map<String,Collection<SummaryGraphElement>> result = new LinkedHashMap<String,Collection<SummaryGraphElement>>();
		try {
			Hits hits = searcher.search(clausequery);
			if((hits != null) && (hits.length() > 0)){
				Collection<SummaryGraphElement> res = new LinkedHashSet<SummaryGraphElement>();	

				result.put(clausequery.toString("label"), res);
				for(int i = 0; i < hits.length(); i++){
					Document doc = hits.doc(i);
					float score = hits.score(i);
					
					if(score >= prune){
						System.out.println(doc);
						System.out.println(score);

						String type = doc.get(TYPE_FIELD);
						
						if(type == null) {
							System.err.println("type is null!");
							continue;
						}
						
						

						if(type.equals(LITERAL)){
							ILiteral lit = new Literal(doc.get(LABEL_FIELD));
							SummaryGraphValueElement vvertex = new SummaryGraphValueElement(lit);
							vvertex.setMatchingScore(score);
							vvertex.setDatasource(doc.get(DS_FIELD));

							Map<DataProperty, Collection<INamedConcept>> neighbors = new HashMap<DataProperty, Collection<INamedConcept>>();
							Term term = new Term(LITERAL_FIELD,lit.getLabel());

							TermQuery query = new TermQuery(term);
							Hits results = searcher.search(query);
							
							for(int j=0;j<results.length();j++) {
								System.out.println(results.doc(j));
							}

							Collection<INamedConcept> concepts;// = new HashSet<INamedConcept>();
							if((results != null) && (results.length() > 0)){
								for(int j = 0; j < results.length(); j++){
									Document docu = results.doc(j);
									if(docu != null){
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
						else if(type.equals(OBJECTPROPERTY)){
							ObjectProperty objProp = new ObjectProperty(pruneString(doc.get(URI_FIELD)));
							SummaryGraphElement pvertex = new SummaryGraphElement (objProp,SummaryGraphElement.RELATION);
							pvertex.setMatchingScore(score);
							pvertex.setDatasource(doc.get(DS_FIELD));
							res.add(pvertex);
						}
						else if(type.equals(DATAPROPERTY)){
							DataProperty prop = new DataProperty(pruneString(doc.get(URI_FIELD)));
							SummaryGraphAttributeElement pVertex = new SummaryGraphAttributeElement(prop,SummaryGraphElement.ATTRIBUTE);
							pVertex.setMatchingScore(score);
							pVertex.setDatasource(doc.get(DS_FIELD));

							Collection<INamedConcept> neighborConcepts = new HashSet<INamedConcept>();
							
							Term term = new Term(ATTRIBUTE_FIELD,doc.get(LABEL_FIELD));							
							TermQuery query = new TermQuery(term);

							try{
								Hits results = searcher.search(query);
								for(int j = 0; j < results.length(); j++){
									Document docu = results.doc(j);
									String concept = docu.get(CONCEPT_FIELD);
									neighborConcepts.add(new NamedConcept(concept));
									System.out.println(concept);
								}
								pVertex.setNeighborConcepts(neighborConcepts);
								res.add(pVertex);
							}
							catch(Exception e) {
								e.printStackTrace();
							}
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
		return str.replaceAll("\"", "");
	}
	
	public static void main(String[] args) {
		KeywordSearcher searcher = new KeywordSearcher();
		searcher.searchKb("d:/testbuilder/keywordIndex/", "Population", 0.0);
	}
	
}
