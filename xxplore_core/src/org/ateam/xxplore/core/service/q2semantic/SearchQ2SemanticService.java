package org.ateam.xxplore.core.service.q2semantic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;


import org.ateam.xxplore.core.service.mapping.Mapping;
import org.ateam.xxplore.core.service.mapping.MappingIndexService;
import org.ateam.xxplore.core.service.q2semantic.QueryInterpretationService.Subgraph;
import org.jgrapht.graph.Pseudograph;
import org.team.xxplore.core.service.search.datastructure.*;
import org.xmedia.oms.model.impl.Literal;
import org.xmedia.oms.model.impl.NamedConcept;
import org.xmedia.oms.model.impl.Property;

/**
 * This class is the Q2Semantic service API that can be called by the search engine interface
 * @author tpenin
 */

class evalTime {
	public long span_Time;
	public long compute_Time;
	
	public evalTime() {
		span_Time = 0;
		compute_Time = 0;
	}
}

class evalStruct {
	public LinkedList<String> ll;
	public long keyTime;
	public long topkTime;
	public long keyTime1;
	public long topkTime1;
	
	public evalStruct() {
		ll = new LinkedList<String>();
	}
}

public class SearchQ2SemanticService {

	public static String root;
	public static String summaryObjsRoot;
	public static String schemaObjsRoot;
	public static String keywordIndexRoot;
	public static String mappingIndexRoot;
	public static HashSet<String> keywordIndexSet;
	public static HashMap<String, String> summaryObjSet;
	public static HashMap<String, String> schemaObjSet;
	public static final String ConceptMark = "c", PredicateMark = "p";
	public QueryInterpretationService inter;
	public String configFilePath;
	public HashMap<String, String> key_database = new HashMap<String, String>();
	private MappingIndexService mis = new MappingIndexService();

	public HashMap<CacheKey,Collection<Suggestion>> cache_suggestion = 
		new HashMap<CacheKey, Collection<Suggestion>>();
	public static final int max_cache_num = 100;
	
	class CacheKey {
		public List<Concept> concept;
		public String ds;
		public CacheKey(List<Concept> concept, String ds) {
			super();
			this.concept = concept;
			this.ds = ds;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result
					+ ((concept == null) ? 0 : concept.hashCode());
			result = prime * result + ((ds == null) ? 0 : ds.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			CacheKey other = (CacheKey) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (concept == null) {
				if (other.concept != null)
					return false;
			} else if (!concept.equals(other.concept))
				return false;
			if (ds == null) {
				if (other.ds != null)
					return false;
			} else if (!ds.equals(other.ds))
				return false;
			return true;
		}
		private SearchQ2SemanticService getOuterType() {
			return SearchQ2SemanticService.this;
		}
				
	}	
	
	public Map<LinkedList<String>,LinkedList<QueryGraph>> cache = new HashMap<LinkedList<String>, LinkedList<QueryGraph>>();
	public static final int cache_max_count = 1000;
	
	public SearchQ2SemanticService(){}
	
	public SearchQ2SemanticService(String fn) {
		// == chenjunquan ==
		try {
			this.loadPara(fn);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * TO DO
	 * @param concept
	 * @param ds
	 * @return
	 * @throws Exception
	 */
	public Collection<Suggestion> getSuggestion(List<Concept> concept, String ds, int topK) throws Exception
	{
		Collection<Suggestion> ret = cache_suggestion.get(new CacheKey(concept,ds));
		if(ret != null) {
			return ret;
		}
		
		List<String> con = new ArrayList<String>();
		for(Concept c: concept)
			con.add(c.getURI());
		//QueryInterpretationService inter = new QueryInterpretationService();
		
		Set<String> sugg = inter.getSuggestion(con, ds, mis);
		PriorityQueue<Suggestion> res = new PriorityQueue<Suggestion>();
		for(String str: sugg)
		{
//			System.out.println(str);
			String[] part = str.split("\t");
			if(part.length!=4) continue;
			String label = SummaryGraphUtil.getLocalName(SummaryGraphUtil.removeNum(part[0]));
			if(part[3].equals(ConceptMark))
				res.add(new ConceptSuggestion(label, new Source(part[1],null, 0), "<"+part[0]+">", Double.parseDouble(part[2])));
			else if(part[3].equals(PredicateMark))
				res.add(new RelationSuggestion(label, new Source(part[1],null, 0), "<"+part[0]+">", Double.parseDouble(part[2])));
		}
//		System.out.println("Total Suggestion: "+res.size());
		int conceptK = topK, relationK = topK;
		LinkedList<Suggestion> ress = new LinkedList<Suggestion>();
		for(Suggestion sug: res)
		{
			if(sug instanceof ConceptSuggestion && conceptK>0)
			{
				System.out.println("ConceptSuggestion: "+sug.getURI());
				ress.add(sug);
				conceptK--;
			}
			else if(sug instanceof RelationSuggestion && relationK>0)
			{
				System.out.println("RelationSuggestion: "+sug.getURI());
				ress.add(sug);
				relationK--;
			}
		}
		
		if(cache_suggestion.size() < max_cache_num) {
			cache_suggestion.put(new CacheKey(concept,ds), ress);
		}
		
		return ress;
	}
	
	public LinkedList<QueryGraph> getPossibleGraphs(LinkedList<String> keywordList, int topNbGraphs) {
		LinkedList<String> tmp = new LinkedList<String>();
		for(String k : keywordList) {
			if( k.charAt(0) == '\"' ) {
				k = k.substring(0,k.length() - 1);
			}
			tmp.add(k.replaceAll("xx", ":"));
		}
//		LinkedList<QueryGraph> ret = cache.get(keywordList);
//		if(ret != null) return ret;
//		else {
//			ret = this.getPossibleGraphs(tmp, topNbGraphs, 0.95, 5, 0.5);
//			if(cache.size() < cache_max_count) {
//				cache.put(keywordList, ret);
//			}
//		}
		LinkedList<QueryGraph> ret = this.getPossibleGraphs(tmp, 5, 0.95, 5, 0.5,new evalTime());
		return ret;
	}
	
	
	public LinkedList<QueryGraph> getPossibleGraphs(LinkedList<String> keywordList, int topNbGraphs,evalTime time) {
		LinkedList<String> tmp = new LinkedList<String>();
		for(String k : keywordList) {
			if( k.charAt(0) == '\"' ) {
				k = k.substring(0,k.length() - 1);
			}
			tmp.add(k.replaceAll("xx", ":"));
		}
//		LinkedList<QueryGraph> ret = cache.get(keywordList);
//		if(ret != null) return ret;
//		else {
//			ret = this.getPossibleGraphs(tmp, topNbGraphs, 0.95, 5, 0.5);
//			if(cache.size() < cache_max_count) {
//				cache.put(keywordList, ret);
//			}
//		}
		LinkedList<QueryGraph> ret = this.getPossibleGraphs(tmp, topNbGraphs, 0.95, 5, 0.5,time);
		return ret;
	}
	
	private LinkedList<QueryGraph> getQueryGraphFromTopKResult(LinkedList<Subgraph> graphs) {
		LinkedList<QueryGraph> result = new LinkedList<QueryGraph>();

		for(Pseudograph<SummaryGraphElement, SummaryGraphEdge> qg: graphs)
		{
			Set<SummaryGraphEdge> edges = qg.edgeSet();
			SummaryGraphElement from, to;
			Map<Facet, Set<Facet>> con2rel = new HashMap<Facet, Set<Facet>>();
			Map<Facet, Set<Facet>> con2attr = new HashMap<Facet, Set<Facet>>();
			HashMap<Facet, Contain> attr2lit = new HashMap<Facet, Contain>();
			HashMap<Facet, Contain> rel2con = new HashMap<Facet, Contain>();
			for(SummaryGraphEdge edge: edges)
			{
				from = edge.getSource();
				to = edge.getTarget();
				collectEdge(from, to, con2rel, con2attr, rel2con, attr2lit);
			}
			
			LinkedList<GraphEdge> graphEdges = new LinkedList<GraphEdge>();
			LinkedList<Facet> graphVertexes = new LinkedList<Facet>();
			
			for(Facet f: con2rel.keySet()) {
				for(Facet r: con2rel.get(f)) {
					if(rel2con.get(r) != null) {
						rel2con.get(r).isVisited = true;
						for(Facet t: rel2con.get(r).sf) {
							GraphEdge edge = new GraphEdge(f, t, r);
							graphEdges.add(edge);
						}
					}
					else {
						Concept top_concept = new Concept("","<TOP_Category>",r.getSource());
						graphVertexes.add(top_concept);
						GraphEdge edge = new GraphEdge(f, top_concept, r);
						graphEdges.add(edge);
					}
				}
			}
			
			for(Facet f: con2attr.keySet()) {
				for(Facet a: con2attr.get(f)) {
					if(attr2lit.get(a) != null) {
						attr2lit.get(a).isVisited = true;
						for(Facet t: attr2lit.get(a).sf) {
							GraphEdge edge = new GraphEdge(f, t, a);
							graphEdges.add(edge);
						}
					}
					else {
						Concept top_concept = new Concept("","<TOP_Category>",a.getSource());
						graphVertexes.add(top_concept);
						GraphEdge edge = new GraphEdge(f, top_concept, a);
						graphEdges.add(edge);
					}
				}
			}
			
			
			// == chenjunquan == 
			// add null,(relation/attribute),(concept/literal) to the query graph.
			for(Facet fac : rel2con.keySet()) {
				if(!rel2con.get(fac).isVisited) {
					for(Facet con : rel2con.get(fac).sf) {
						Concept top_concept = new Concept("","<TOP_Category>",fac.getSource());
						graphVertexes.add(top_concept);
						graphEdges.add(new GraphEdge(top_concept,con,fac));
					}
				}
			}
			
			for(Facet fac : attr2lit.keySet()) {
				if(!attr2lit.get(fac).isVisited) {
					for(Facet lit : attr2lit.get(fac).sf) {
						Concept top_concept = new Concept("","<TOP_Category>",fac.getSource());
						graphVertexes.add(top_concept);
						graphEdges.add(new GraphEdge(top_concept,lit,fac));
					}
				}
			}
			
			
			for(SummaryGraphElement elem : qg.vertexSet()) {
				if( elem.getType() == SummaryGraphElement.VALUE ||
						elem.getType() == SummaryGraphElement.CONCEPT) {
					graphVertexes.add(getFacet(elem));
				}
			}
			
			for(GraphEdge edge : graphEdges) {
				edge.decorationElement.URI = SummaryGraphUtil.removeNum(edge.decorationElement.URI);
				edge.decorationElement.label = SummaryGraphUtil.removeNum(edge.decorationElement.label);
			}
//			================ by kaifengxu
			LinkedList<GraphEdge> mappingEdges = new LinkedList<GraphEdge>();
			HashMap<String, Facet> uri2facet = new HashMap<String, Facet>();
			for(GraphEdge edge: graphEdges)
			{
				uri2facet.put(edge.getFromElement().getURI()+edge.getFromElement().getSource().getName(), edge.getFromElement());
				uri2facet.put(edge.getDecorationElement().getURI()+edge.getDecorationElement().getSource().getName(), edge.getDecorationElement());
				uri2facet.put(edge.getToElement().getURI()+edge.getToElement().getSource().getName(), edge.getToElement());
			}
//			if ( == null || inter.factory.getM_datasource().size() == 0)
//				return null;
			Collection<Mapping> mappings;
			HashSet<String> delDupMappingEdge = new HashSet<String>();
//			for (String ds : inter.factory.getM_datasources().keySet()) {
//				mappings = inter.mis.searchMappingsForDS(ds,
//						MappingIndexService.SEARCH_TARGET_AND_SOURCE_DS);
			
			mappings = inter.factory.getMappings();
//			System.out.println("aaaa"+mappings.size());
			for(Mapping mapping: mappings)
			{
				String uriA = mapping.getSource()+mapping.getSourceDsURI();
				String uriB = mapping.getTarget()+mapping.getTargetDsURI();
				Facet mappingA = uri2facet.get(uriA);
				Facet mappingB = uri2facet.get(uriB);
				if(mappingA != null && mappingB != null && !delDupMappingEdge.contains(uriA+uriB))
				{
					delDupMappingEdge.add(uriA+uriB);
					mappingEdges.add(new GraphEdge(mappingA, mappingB, null));
					System.out.println("add mappingedge: "+mappingA.getURI()+"("+mappingA.getSource().getName()+") | "+mappingB.getURI()+"("+mappingB.getSource().getName()+")");
				}
			}
//			}
//			===============================
			result.add(new QueryGraph(null, graphVertexes, graphEdges, mappingEdges));
		}
		this.addVariable(result);
		return result;
	}
	
	private void addVariable(LinkedList<QueryGraph> result) {
		for (QueryGraph qg : result) {
			char current_char = 'a';
			HashMap<String, String> letter_hm = new HashMap<String, String>();
			for (GraphEdge mapping : qg.mappingList) {
				if ((mapping.fromElement instanceof Concept)
						&& mapping.toElement instanceof Concept) {
					Concept con1 = (Concept) mapping.fromElement;
					Concept con2 = (Concept) mapping.toElement;
					String l = letter_hm.get(con1.URI);
					if(l == null) l = letter_hm.get(con2.URI);
					if(l == null) l = String.valueOf(current_char++);
					con1.variableLetter = l;
					con2.variableLetter = l;
					letter_hm.put(con1.URI, l);
					letter_hm.put(con2.URI, l);
				}
			}
			for (GraphEdge ge : qg.edgeList) {
				if (ge.getFromElement() instanceof Concept) {
					Concept con = (Concept) ge.getFromElement();
					if(con.variableLetter == null)
					{
						if( letter_hm.get(con.URI) == null ) {
							con.variableLetter = String.valueOf(current_char++);
							letter_hm.put(con.URI, con.variableLetter);
						}					
						else {
							con.variableLetter = letter_hm.get(con.URI);
						}
					}
				}
				if (ge.getToElement() instanceof Concept) {
					Concept con = (Concept) ge.getToElement();
					if(con.variableLetter == null)
					{
						if( letter_hm.get(con.URI) == null ) {
							con.variableLetter = String.valueOf(current_char++);
							letter_hm.put(con.URI, con.variableLetter);
						}					
						else {
							con.variableLetter = letter_hm.get(con.URI);
						}
					}
				}
			}
			for(Facet con: qg.vertexList)
				if(con instanceof Concept)
					((Concept)con).variableLetter = letter_hm.get(con.URI);
		}
	}
	
	public Map<String,Collection<SummaryGraphElement>> searchKeyword(Set<String> ds_used,LinkedList<QueryToken> queryList,double prune) {
		//search for elements
		Map<String,Collection<SummaryGraphElement>> elementsMap = new HashMap<String,Collection<SummaryGraphElement>>();

		for(QueryToken qt : queryList) {
			String [] ds = qt.datasource.split(",");
			for(int i = 0; i < ds.length;i++) {
				ds_used.add(ds[i]);
				String keywordIndex = keywordIndexRoot + "/" + ds[i] + "-keywordIndex";
				System.out.println("keywordIndex " + keywordIndex);
				Map<String, Collection<SummaryGraphElement>> hm = 
					new KeywordIndexServiceForBTFromNT(keywordIndex, false).searchKb(qt.type,qt.key, prune);
	
				for(String key_str : hm.keySet()) {
					Collection<SummaryGraphElement> coll = elementsMap.get(key_str);
					if(coll == null) {
						elementsMap.put(key_str, hm.get(key_str));
					}
					else {
						coll.addAll(hm.get(key_str));
					}
				}
			}
		}
		
		for(String ds : ds_used) {
			System.out.print("ds_used:" + ds + " ");
		}
		System.out.println();
		
//		for(String ds : summaryObjSet.keySet()) {
//			String keywordIndex = this.keywordIndexRoot + "/" + ds + "-keywordIndex";
//			System.out.println("keywordIndex " + keywordIndex);
//			Map<String, Collection<SummaryGraphElement>> hm = 
//				new KeywordIndexServiceForBTFromNT(keywordIndex, false).searchKb(query, prune);
//
//			for(String key_str : hm.keySet()) {
//				Collection<SummaryGraphElement> coll = elementsMap.get(key_str);
//				if(coll == null) {
//					elementsMap.put(key_str, hm.get(key_str));
//				}
//				else {
//					coll.addAll(hm.get(key_str));
//				}
//			}
//		}
		
		for(String key : elementsMap.keySet()) {
			Collection<SummaryGraphElement> t = elementsMap.get(key);
			System.out.println("=================================");
			System.out.println(key + " : ");
			for(SummaryGraphElement ele : t) {
				System.out.println(SummaryGraphUtil.getResourceUri(ele) + "\t" + ele.getDatasource());
			}
			System.out.println();
			System.out.println();
			
		}
		
//		for(String keywordIndex: keywordIndexSet) {
//			System.out.println("keywordIndex " + keywordIndex);
//			Map<String, Collection<SummaryGraphElement>> hm = 
//				new KeywordIndexServiceForBTFromNT(keywordIndex, false).searchKb(query, prune);
//
//			for(String key_str : hm.keySet()) {
//				Collection<SummaryGraphElement> coll = elementsMap.get(key_str);
//				if(coll == null) {
//					elementsMap.put(key_str, hm.get(key_str));
//				}
//				else {
//					coll.addAll(hm.get(key_str));
//				}
//			}
//		}

//		Map<String,Collection<SummaryGraphElement>> tmpMap = elementsMap;
//		elementsMap = new HashMap<String, Collection<SummaryGraphElement>>();
//		for(String key : tmpMap.keySet()) {
//			
//			System.out.println("========================");
//			for(SummaryGraphElement ele : tmpMap.get(key)) {
//				boolean flag = false;
////				if(ele.getType() == SummaryGraphElement.VALUE) {
////					if(SummaryGraphUtil.getResourceUri(ele).toLowerCase().equals(key.toLowerCase())) {
////						flag = true;
////					}	
////				}
////				else {
////					if(SummaryGraphUtil.getResourceUri(ele).toLowerCase().endsWith(key.toLowerCase())) {
////						flag = true;
////					}
//					if(SummaryGraphUtil.getResourceUri(ele).toLowerCase().equals("http://www.freebase.com/property/produced_by")) {
//						flag = true;	
//					}
//					if(SummaryGraphUtil.getResourceUri(ele).toLowerCase().equals("http://www.freebase.com/property/name")) {
//						flag = true;
//					}
//					if(SummaryGraphUtil.getResourceUri(ele).toLowerCase().equals("http://www.freebase.com/class/publication")) {
//						flag = true;
//					}
//					if(SummaryGraphUtil.getResourceUri(ele).toLowerCase().equals("http://www.freebase.com/class/person")) {
//						flag = true;
//					}
//					if(SummaryGraphUtil.getResourceUri(ele).toLowerCase().equals("http://www.freebase.com/class/company")) {
//						flag = true;
//					}
//					if(SummaryGraphUtil.getResourceUri(ele).toLowerCase().equals("google") && ele.getDatasource().equals("freebase")) {
//						flag = true;
//					}
////				}
//				
//				
//				if( flag ) {
//					System.out.println(ele.getDatasource() + "\t" + SummaryGraphUtil.getResourceUri(ele));
//					Collection<SummaryGraphElement> coll = elementsMap.get(key);
//					if(coll == null) {
//						coll = new HashSet<SummaryGraphElement>();
//						elementsMap.put(key, coll);
//					}
//					coll.add(ele);
//				}
//			}
//			int size = elementsMap.get(key) == null ? 0 : elementsMap.get(key).size();
//			System.out.println(key + "\t" + size);
//			System.out.println("==========================");
//		}
		return elementsMap;
	}
	
	class QueryToken {
		public String key;
		public String datasource;
		public int type;
		
		public QueryToken(String key,String datasource,int type) {
			this.key = key;
			this.datasource = datasource;
			this.type = type;
		}
	}

	/**
	 * This method returns an ordered list of QueryGraph objects (most suitable at the head of the list) that 
	 * are possible semantic interpretations of the ordered list of keywords (first input word at the head of the 
	 * list) provided as a parameter. The most relevant graphs only are provided, respecting the maximum number of 
	 * graph condition.
	 * @param keywordList
	 * @param topNbGraphs
	 * @return
	 */
	public LinkedList<QueryGraph> getPossibleGraphs(LinkedList<String> keywordList, int topNbGraphs, double prune, int distance,
			double edge_score,evalTime time) {
		
		// TODO
		// Note: I will certainly have to find a way to serialize this list of graphs to XML... (tpenin)

//		LinkedList<String> keywordList = new LinkedList<String>();
//		int [] type = new int[keywordList1.size()];
		HashMap<Character,Integer> type_hm = new HashMap<Character, Integer>();
		type_hm.put('a', SummaryGraphElement.ATTRIBUTE);
		type_hm.put('c', SummaryGraphElement.CONCEPT);
		type_hm.put('l', SummaryGraphElement.VALUE);
		type_hm.put('r', SummaryGraphElement.RELATION);
		
//		int count = 0;
//		for(String item : keywordList1) {
//			if(item.charAt(1) == ':') {
//				type[count++] = type_hm.get(item.charAt(0));
//				keywordList.add(item.substring(2));
//			}
//			else {
//				type[count++] = -1;
//				keywordList.add(item);
//			}
//		}
		Set<String> ds_used = new HashSet<String>();
		LinkedList<QueryToken> queryList = new LinkedList<QueryToken>();
		for(String line : keywordList) {
			String tokens[] = line.split(":");
			
			QueryToken queryToken = null;
			if(tokens.length == 2 || (tokens.length == 3 && !tokens[1].equals("*"))) {
				queryToken = new QueryToken("\"" + tokens[0] + "\"",tokens[1],-1);
				System.out.println("token first:" + line);
			}
			else if(tokens.length == 1 || ( tokens.length == 3 && tokens[1].equals("*"))){
				String all_ds = "";
				for(String tmp : summaryObjSet.keySet()) {
					all_ds = all_ds + tmp + ",";
				}
				all_ds = all_ds.substring(0,all_ds.length() - 1);
				
				if(this.key_database.get(tokens[0]) != null) {
					 queryToken = new QueryToken("\"" + tokens[0] + "\"",this.key_database.get(tokens[0]),-1); 
				}
				else {
					queryToken = new QueryToken("\"" + tokens[0] + "\"",all_ds,-1);
				}
				System.out.println("token second:" + line);
			}
			
			if(tokens.length == 3) {
				queryToken.type = type_hm.get(tokens[2].charAt(0));
				System.out.println("token third:" + line);
			}
			queryList.add(queryToken);

		}
		
		long start_time = System.currentTimeMillis();
		
		QueryInterpretationService.EDGE_SCORE = edge_score;
		
//		String query = "";
//		//merge keywords
//		for(String str: keywordList) {
//			query += "\""+str+"\" ";
//		}
//		query = query.substring(0, query.length()-1);
		
		Map<String, Collection<SummaryGraphElement>> elementsMap = searchKeyword(ds_used,queryList,prune);
		long end_time = System.currentTimeMillis();
		time.span_Time = end_time - start_time;
		
		if(elementsMap.size()<keywordList.size()) return null;
//		Map<String,Collection<SummaryGraphElement>> elementsMap2 = new HashMap<String, Collection<SummaryGraphElement>>();
//		
//		count = 0;
//		for(String item : elementsMap.keySet()) {
//			if(elementsMap == null) {
//				System.out.println("elementsMap is null!");
//				
//			}
//			if(elementsMap.get(item) == null) {
//				System.out.println(elementsMap.size() + "\t" + item);
//				continue;
//			}
//			
//			for(SummaryGraphElement ele : elementsMap.get(item)) {
//				if( type[count] == -1 || ele.getType() == type[count] ) {
//					Collection<SummaryGraphElement> ele_set = elementsMap2.get(item);
//					if(ele_set == null) {
//						ele_set = new LinkedList<SummaryGraphElement>();
//						elementsMap2.put(item, ele_set);
//					}
//					ele_set.add(ele);
//				}
//			}
//			count ++;
//		}
//		
//		System.out.println("elementsMap size:" + elementsMap.size());
//		System.out.println("begin elementsMap2 =========");
//		for(String item : elementsMap2.keySet()) {
//			System.out.println(item + ":");
//			for(SummaryGraphElement ele : elementsMap2.get(item)) {
//				System.out.println("\t"+SummaryGraphUtil.getResourceUri(ele));
//			}
//		}
//		
//		System.out.println("size of elementsMap2:" + elementsMap2.size());
//		
//		for(String key : elementsMap2.keySet()) {
//			System.out.println(key + ":" + elementsMap2.get(key));
//			for(SummaryGraphElement ele : elementsMap2.get(key)) {
//				System.out.println("\t" + SummaryGraphUtil.getResourceUri(ele));
//			}
//		}
		
		
		start_time = System.currentTimeMillis();
		LinkedList<Subgraph> graphs = inter.computeQueries(ds_used,elementsMap, distance, topNbGraphs);
		if(graphs == null) return null;
		LinkedList<QueryGraph> result = this.getQueryGraphFromTopKResult(graphs);
		end_time = System.currentTimeMillis();
		time.compute_Time = end_time - start_time;
		
		for(int i=0; i<result.size(); i++) {
			System.out.println("=============== Top "+(i+1)+" QueryGraph ==============");
			result.get(i).print();
		}

//		System.out.println();
//		System.out.println("computeQueries time:" + computeTime/1000.0 + "s" );
//		System.out.println();
		return result;
	}

	/**
	 * use to add a member value isVistited.
	 * @author jqchen
	 *
	 */
	class Contain{
		Set<Facet> sf;
		boolean isVisited;
		
		public Contain() {
			this.sf = new HashSet<Facet>();
			isVisited = false;
		}
		
		public Contain(Set<Facet> sf) {
			this.sf = sf;
			isVisited = false;
		}
	}
	
	public void loadPara(String fn) throws Exception
	{
		
		
		
		Properties prop = new Properties();
		InputStream is = new FileInputStream(fn);
		prop.load(is);
		root = prop.getProperty("root")+File.separator;
		summaryObjsRoot = root+prop.getProperty("summaryObjsRoot")+File.separator;
		schemaObjsRoot = root+prop.getProperty("schemaObjsRoot")+File.separator;
		keywordIndexRoot = root+prop.getProperty("keywordIndexRoot")+File.separator;
		mappingIndexRoot = root+prop.getProperty("mappingIndexRoot")+File.separator;
		configFilePath = root + prop.getProperty("key2database");
		System.out.println("Root:"+root+"\r\nsummaryObjsRoot:"+summaryObjsRoot+"\r\nschemaObjsRoot:"+schemaObjsRoot+"\r\nkeywordIndexRoot:"+keywordIndexRoot+"\r\nmappingIndexRoot:"+mappingIndexRoot);
//		add keywordindexes
		keywordIndexSet = new HashSet<String>();
		File[] indexes = new File(keywordIndexRoot).listFiles();
		for(File index: indexes)
			keywordIndexSet.add(index.getAbsolutePath());
//		add graphs
		summaryObjSet = new HashMap<String, String>();
		File[] summaries = new File(summaryObjsRoot).listFiles();
		for(File summary: summaries)
			summaryObjSet.put(summary.getName().substring(0, summary.getName().lastIndexOf('-')), summary.getAbsolutePath());
		schemaObjSet = new HashMap<String, String>();
		File[] schemas = new File(schemaObjsRoot).listFiles();
		for(File schema: schemas)
			schemaObjSet.put(schema.getName().substring(0, schema.getName().lastIndexOf('-')), schema.getAbsolutePath());
		
		if(new File(this.configFilePath).exists()) {
			try {
				BufferedReader br = new BufferedReader(new FileReader(this.configFilePath));
				String line;
				while((line = br.readLine()) != null) {
					String tokens[] = line.split(":");
					
					this.key_database.put(tokens[0], tokens[1]);
				}
				br.close();
			}
			catch (Exception e) {
				// TODO: handle exception
			}
		}
		
		inter = new QueryInterpretationService(summaryObjSet.keySet());
		
		mis.init4Search(mappingIndexRoot);
		
	}
	
	
	private void collectEdge(SummaryGraphElement from, SummaryGraphElement to, Map<Facet, Set<Facet>> c2r, Map<Facet, Set<Facet>> c2a, HashMap<Facet, Contain> rel2con, HashMap<Facet, Contain> attr2lit)
	{
		Facet f = getFacet(from);
		Facet t = getFacet(to);
		// == chenjunquan ==
		if(from.getType() == SummaryGraphElement.ATTRIBUTE && to.getType() == SummaryGraphElement.VALUE)
		{
			
			Contain contain = attr2lit.get(f);
			if(contain == null) contain = new Contain();
			contain.sf.add(t);
			attr2lit.put(f, contain);
		}
		else if(from.getType() == SummaryGraphElement.CONCEPT && to.getType() == SummaryGraphElement.RELATION)
		{
			Set<Facet> set = c2r.get(f);
			if(set == null) set = new HashSet<Facet>();
			set.add(t);
			c2r.put(f, set);
		}
		else if(from.getType() == SummaryGraphElement.RELATION && to.getType() == SummaryGraphElement.CONCEPT)
		{
			Contain contain = rel2con.get(f);
			if(contain == null) contain = new Contain();
			contain.sf.add(t);
			rel2con.put(f, contain);
		}
		else if(from.getType() == SummaryGraphElement.CONCEPT && to.getType() == SummaryGraphElement.ATTRIBUTE)
		{
			Set<Facet> set = c2a.get(f);
			if(set == null) set = new HashSet<Facet>();
			set.add(t);
			c2a.put(f, set);
		}
	}
	
	private Facet getFacet(SummaryGraphElement elem)
	{
		if(elem.getType() == SummaryGraphElement.ATTRIBUTE)
		{
			String uri = ((Property)elem.getResource()).getUri();
			return new Relation(SummaryGraphUtil.getLocalName(uri), "<"+uri+">", new Source(elem.getDatasource(),null,0));
		}
		else if(elem.getType() == SummaryGraphElement.RELATION)
		{
			String uri = ((Property)elem.getResource()).getUri();
			return new Relation(SummaryGraphUtil.getLocalName(uri), "<"+uri+">", new Source(elem.getDatasource(),null,0));
		}
		else if(elem.getType() == SummaryGraphElement.CONCEPT)
		{
			String uri = ((NamedConcept)elem.getResource()).getUri();
			return new Concept(SummaryGraphUtil.getLocalName(uri), "<"+uri+">", new Source(elem.getDatasource(),null,0));
		}
		else if(elem.getType() == SummaryGraphElement.VALUE)
		{
			String uri = ((Literal)elem.getResource()).getLabel();
			return new Litteral(uri, uri, new Source(elem.getDatasource(),null,0));
		}
		System.out.println("Miss matching: "+elem.getType());
		return null;
	}
	
	public static void main(String[] args) throws Exception {
		Scanner scanner = new Scanner(System.in);
		while( scanner.hasNext() ) {
			String line = scanner.nextLine();
			LinkedList<String> ll = new LinkedList<String>();
			String tokens [] = line.split(" ");
			for(int i=0;i<tokens.length;i++) {
				ll.add(tokens[i]);
			}
			new SearchQ2SemanticService(args[0]).getPossibleGraphs(ll, Integer.valueOf(args[1]), Double.valueOf(args[2]), Integer.valueOf(args[3]),Double.valueOf(args[4]),new evalTime());
		}
	}

//	public static void main(String[] args) throws Exception {
//		SearchQ2SemanticService s = new SearchQ2SemanticService(args[0]);
//		LinkedList<LinkedList<String>> ll_set = new LinkedList<LinkedList<String>>();
//		
//		try {
//			BufferedReader br = new BufferedReader(new FileReader("input.txt"));
//			String line;
//			while((line = br.readLine()) != null) {
//				String tokens[] = line.split("\t");
//				LinkedList<String> ll = new LinkedList<String>();
//				for(int i=0;i<tokens.length;i++) {
//					ll.add(tokens[i]);
//				}
//				ll_set.add(ll);
//			}
//		}
//		catch (Exception e) {
//			e.printStackTrace();
//		}
//		
//		
//		try {
//			PrintWriter pw = new PrintWriter("output.txt");
//			for(LinkedList<String> ll : ll_set) {
//				pw.println(ll.toString());
//				for(int i=0;i<11;i++) {
//					evalTime time = new evalTime();
//					s.getPossibleGraphs(ll, Integer.valueOf(args[1]),time);
//					pw.println(time.span_Time + "\t" + time.compute_Time);
//				}
//				pw.println("\n\r\n\r");
//
//			}
//			pw.close();
//		}
//		catch (Exception e) {
//			e.printStackTrace();
//		}
//		
//	}

	
//	public static void main(String[] args) throws Exception {
//		SearchQ2SemanticService s = new SearchQ2SemanticService(args[0]);
//		LinkedList<String> ll = new LinkedList<String>();
//		Scanner scanner = new Scanner(System.in);
//		while(true) {
//			System.out.println("please input your parameter!");
//			String line = scanner.nextLine();
//			String[] tokens = line.split("\t");
//			
//			for(int t=0;t<5;t++) {
//				ll.clear();
//				for(int i=0;i<tokens.length;i++)  {
//					ll.add(tokens[i].replace('|', ' '));
//				}
//				evalTime time = new evalTime();
//				s.getPossibleGraphs(ll, Integer.valueOf(args[1]),time);
//				System.err.println("key:" + time.span_Time);
//				System.err.println("topk:" + time.compute_Time);
//				System.err.println();
//			}
//			//s.getPossibleGraphs(ll, Integer.valueOf(args[1]), Double.valueOf(args[2]), Integer.valueOf(args[3]), Double.valueOf(args[4]));
//		}
//	}
}
