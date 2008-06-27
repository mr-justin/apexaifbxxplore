package sjtu.apex.q2semantic.search;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
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
import org.xmedia.oms.model.api.IDataProperty;
import org.xmedia.oms.model.api.ILiteral;
import org.xmedia.oms.model.api.INamedConcept;
import org.xmedia.oms.model.api.IObjectProperty;
import org.xmedia.oms.model.impl.DataProperty;
import org.xmedia.oms.model.impl.Literal;
import org.xmedia.oms.model.impl.NamedConcept;
import org.xmedia.oms.model.impl.ObjectProperty;
import org.xmedia.oms.model.impl.Resource;

import sjtu.apex.q2semantic.index.IndexEnvironment;

public class LuceneQueryService {

	private static Logger s_log = Logger.getLogger(LuceneQueryService.class);

//	private boolean m_KbReader_isClosed = false;
	
//	private int numOfConcept;


	private Searcher m_kbSearcher;

	private StandardAnalyzer m_analyzer;
	
	private String indexPath;
	
	private String baseURI;
	private String ns;


	public LuceneQueryService(int flag){
		m_analyzer = new StandardAnalyzer();
		baseURI = "";
		ns = "";
		if (flag == IndexEnvironment.TAP_FLAG) {
			indexPath = IndexEnvironment.TAP_KB_INDEX;
			baseURI = IndexEnvironment.TAP_BASEURI;
			ns = IndexEnvironment.TAP_NS;
		} else if (flag == IndexEnvironment.LUBM_FLAG) {
			indexPath = IndexEnvironment.LUBM_KB_INDEX;
			baseURI = IndexEnvironment.LUBM_BASEURI;
			ns = IndexEnvironment.LUBM_NS;
		} else if (flag == IndexEnvironment.DBLP_FLAG) {
			indexPath = IndexEnvironment.DBLP_KB_INDEX;
		} else {
			System.exit(3);
		}
	}

	private String pruneString(String str) {
		return str.replace("\"", "");
	}
	
	
	public Map<String,Collection<KbElement>> searchKb(String query){
		Map<String,Collection<KbElement>> ress = new LinkedHashMap<String,Collection<KbElement>>();
		try {
			if (m_kbSearcher ==null){
				s_log.debug("Open index " + indexPath + " and init kb searcher!");
				m_kbSearcher = new IndexSearcher(indexPath);
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
//		System.out.println(ress);
		return ress;
	}

	private void searchWithClause(Query clausequery, Map<String,Collection<KbElement>> ress){
		try {
			Hits results = m_kbSearcher.search(clausequery);
			if ((results == null) || (results.length() == 0)){
				Set<Term> term = new HashSet<Term>();
				clausequery.extractTerms(term);
				//if clause query is a term query
				if(term.size() == 1){
					results = m_kbSearcher.search(new FuzzyQuery(term.iterator().next()));
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
		        				
		        				Term term = new Term("literal",lit.getLiteral());
		        		        TermQuery query = new TermQuery(term);
		        		        Hits hits = m_kbSearcher.search(query);
		        		        if((hits != null) && (hits.length() > 0)){
		        		        	for(int j = 0; j < hits.length(); j++){
		        		        		Document docu = hits.doc(j);
		        		        		if(docu != null){
		        		        			IDataProperty prop = new DataProperty(pruneString(docu.get("dataproperty")));
		        		        			INamedConcept con = new NamedConcept(pruneString(docu.get("concept")));
		        		        			KbVertex cvertex = new KbVertex(con,KbElement.CVERTEX,1);
		        		        			res.add(new KbEdge(cvertex, vvertex, prop, KbElement.AEDGE,1));
//		        		        			System.out.println(con.getUri());
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
		        				
		        				String str = dataProp.getUri().substring(ns.length());
		        				Term term = new Term("attribute",str);
		        		        TermQuery query = new TermQuery(term);
		        		        Hits hits = m_kbSearcher.search(query);
		        		        if((hits != null) && (hits.length() > 0)){
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
		        				String str = objProp.getUri().substring(ns.length());
		        				Term term = new Term("relation",str);
		        		        TermQuery query = new TermQuery(term);
		        		        Hits hits = m_kbSearcher.search(query);
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

	
	private static String CONCEPT = "concept";
	private static String OBJECTPROPERTY = "objectproperty";
	private static String DATAPROPERTY = "dataproperty";
//	private static String INDIVIDUAL = "indiviudal";
	private static String LITERAL = "literal";
	
}
