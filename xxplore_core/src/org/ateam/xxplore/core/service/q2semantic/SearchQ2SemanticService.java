package org.ateam.xxplore.core.service.q2semantic;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Properties;
import java.util.Set;

import org.ateam.xxplore.core.service.mapping.MappingIndexService;
import org.ateam.xxplore.core.service.q2semantic.QueryInterpretationService.Subgraph;
import org.jgrapht.graph.WeightedPseudograph;
import org.team.xxplore.core.service.search.datastructure.*;
import org.xmedia.oms.model.impl.Literal;
import org.xmedia.oms.model.impl.NamedConcept;
import org.xmedia.oms.model.impl.Property;

/**
 * This class is the Q2Semantic service API that can be called by the search engine interface
 * @author tpenin
 */
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
	public Collection<Suggestion> getSuggestion(List<Concept> concept, String ds) throws Exception
	{
		List<String> con = new ArrayList<String>();
		for(Concept c: concept)
			con.add(c.getURI());
		QueryInterpretationService inter = new QueryInterpretationService();
		MappingIndexService mis = new MappingIndexService();
		mis.init4Search(mappingIndexRoot);
		Set<String> sugg = inter.getSuggestion(con, ds, mis);
		Collection<Suggestion> res = new PriorityQueue<Suggestion>();
		for(String str: sugg)
		{
			String[] part = str.split("\t");
			if(part.length!=4) continue;
			String label = part[0].substring(part[0].lastIndexOf('/')+1);
			if(part[3].equals(ConceptMark))
				res.add(new ConceptSuggestion(label, new Source(part[1],null, 0), part[0], Double.parseDouble(part[2])));
			else if(part[3].equals(PredicateMark))
				res.add(new RelationSuggestion(label, new Source(part[1],null, 0), part[0], Double.parseDouble(part[2])));
		}
		return res;
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
	public LinkedList<QueryGraph> getPossibleGraphs(LinkedList<String> keywordList, int topNbGraphs) {
		// TODO
		// Note: I will certainly have to find a way to serialize this list of graphs to XML... (tpenin)
		
		double prune = 0.99;
		int distance = 1000;
		String query = "";
		//merge keywords
		for(String str: keywordList)
			query += "\""+str+"\" ";
		query = query.substring(0, query.length()-1);
		
		
//		System.out.println(query);
		//search for elements
		Map<String,Collection<SummaryGraphElement>> elementsMap = new HashMap<String,Collection<SummaryGraphElement>>();
		System.out.println("size " + keywordIndexSet.size());
		
		long start_time = System.currentTimeMillis();
		
		for(String keywordIndex: keywordIndexSet) {

			System.out.println("keywordIndex " + keywordIndex);
			elementsMap.putAll(new KeywordIndexServiceForBTFromNT(keywordIndex, false).searchKb(query, prune));
		}
		
		long end_time = System.currentTimeMillis();
		
		System.out.println("Time: " + (end_time - start_time));
		
		// == chenjunquan ==
		//if the query string number is one. This scenario will be tackled individually.
		if(keywordList.size() == 1) {
			ArrayList<SummaryGraphElement> elements = new ArrayList<SummaryGraphElement>();
			for(Collection<SummaryGraphElement> coll : elementsMap.values()) {
				for(SummaryGraphElement ele : coll) {
					elements.add(ele);
				}
			}
			
			Collections.sort(elements,new Comparator<SummaryGraphElement>() {
				@Override
				public int compare(SummaryGraphElement arg0,
						SummaryGraphElement arg1) {
					if(arg0.getMatchingScore() - arg1.getMatchingScore() > 0) {
						return -1;
					}
					else if(arg0.getMatchingScore() - arg1.getMatchingScore() < 0) {
						return 1;
					}
					else return 0;
				}
			});
			
			
			LinkedList<QueryGraph> result = new LinkedList<QueryGraph>();

			//ArrayList<SummaryGraphElement> elements_topk = new ArrayList<SummaryGraphElement>();
			for(int i=0;i<Math.min(100, elements.size());i++) {
				
				LinkedList<Facet> graphVertexes = new LinkedList<Facet>();
				graphVertexes.add(getFacet(elements.get(i)));
				QueryGraph queryGraph = new QueryGraph(null,graphVertexes,null);
				System.out.println("output1: " + elements.get(i).getMatchingScore());
				result.add(queryGraph);
			}
			
			for(int i=0; i<result.size(); i++)
			{
				System.out.println("=============== Top "+(i+1)+" QueryGraph ==============");
				result.get(i).print();
			}
			return result;
		}
		
	
		// == chenjunquan ==
		
//		KeywordIndexServiceForBT service = new KeywordIndexServiceForBT("d:/freebase-keywordIndex", false);
//		
//		Map<String, Collection<SummaryGraphElement>> tmp = service.searchKb(query, prune);
//		
//		
//		elementsMap.putAll(tmp);
		
		// == chenjunquan ==
//		for(String key : elementsMap.keySet()) {
//			System.out.println("key " + key);
//			Collection<SummaryGraphElement> tmp = elementsMap.get(key);
//			for(SummaryGraphElement ele : tmp) {
//				System.out.println(ele.getDatasource());
//			}
//		}
//		System.out.println(elementsMap.size());
		//search for topk querygraph
		QueryInterpretationService inter = new QueryInterpretationService();
		LinkedList<QueryGraph> result = new LinkedList<QueryGraph>();
		//package the querygraph(Class:WeightedPseudograph) with Class:QueryGraph
		MappingIndexService mis = new MappingIndexService();
		mis.init4Search(mappingIndexRoot);
		
		// == chenjunquan ==
		start_time = System.currentTimeMillis();
		LinkedList<Subgraph> graphs = inter.computeQueries(elementsMap, mis, distance, topNbGraphs);
		if(graphs == null) return null;
		end_time = System.currentTimeMillis();
		System.out.println("Time2:" + (end_time - start_time) );
		
		for(WeightedPseudograph<SummaryGraphElement, SummaryGraphEdge> qg: graphs)
		{
			Set<SummaryGraphEdge> edges = qg.edgeSet();
			SummaryGraphElement from, to;
			Map<Facet, Set<Facet>> con2rel = new HashMap<Facet, Set<Facet>>(), con2attr = new HashMap<Facet, Set<Facet>>();
			HashMap<Facet, Contain> attr2lit = new HashMap<Facet, Contain>();
			HashMap<Facet, Contain> rel2con = new HashMap<Facet, Contain>();
			for(SummaryGraphEdge edge: edges)
			{
				from = edge.getSource();
				to = edge.getTarget();
				collectEdge(from, to, con2rel, con2attr, rel2con, attr2lit);
			}
			
			LinkedList<GraphEdge> graphEdges = new LinkedList<GraphEdge>();
//			System.out.println(con2rel.size()+"\t"+rel2con.size()+"\t"+con2attr.size()+"\t"+attr2lit.size());
			for(Facet f: con2rel.keySet()) {
				for(Facet r: con2rel.get(f)) {
					if(rel2con.get(r) != null) {
						rel2con.get(r).isVisited = true;
						for(Facet t: rel2con.get(r).sf) {
							graphEdges.add(new GraphEdge(f, t, r));
						}
					}
					else graphEdges.add(new GraphEdge(f, null, r));
				}
			}
			
			for(Facet f: con2attr.keySet()) {
				for(Facet a: con2attr.get(f)) {
					if(attr2lit.get(a) != null) {
						attr2lit.get(a).isVisited = true;
						for(Facet t: attr2lit.get(a).sf) {
							graphEdges.add(new GraphEdge(f, t, a));
						}
					}
					else graphEdges.add(new GraphEdge(f, null, a));
				}
			}
			
			
			// == chenjunquan == 
			// add null,(relation/attribute),(concept/literal) to the query graph.
			for(Facet fac : rel2con.keySet()) {
				if(!rel2con.get(fac).isVisited) {
					for(Facet con : rel2con.get(fac).sf) {
						graphEdges.add(new GraphEdge(null,con,fac));
						System.out.println("output2: " + fac.URI + "\t" + con.URI);
					}
				}
			}
			
			for(Facet fac : attr2lit.keySet()) {
				if(!attr2lit.get(fac).isVisited) {
					for(Facet lit : attr2lit.get(fac).sf) {
						graphEdges.add(new GraphEdge(null,lit,fac));
						System.out.println("output2: " + fac.URI + "\t" + lit.URI);
					}
				}
			}
			
			LinkedList<Facet> graphVertexes = new LinkedList<Facet>();
			for(SummaryGraphElement elem : qg.vertexSet()) {
				if( elem.getType() == SummaryGraphElement.VALUE ||
						elem.getType() == SummaryGraphElement.CONCEPT) {
					graphVertexes.add(getFacet(elem));
				}
			}
				
			result.add(new QueryGraph(null, graphVertexes, graphEdges));
		}
		for(int i=0; i<result.size(); i++)
		{
			System.out.println("=============== Top "+(i+1)+" QueryGraph ==============");
			result.get(i).print();
		}
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
		System.out.println("Root:"+root+"\r\nsummaryObjsRoot:"+summaryObjsRoot+"\r\nschemaObjsRoot:"+schemaObjsRoot+"\r\nkeywordIndexRoot:"+keywordIndexRoot);
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
	}
	
	
	private void collectEdge(SummaryGraphElement from, SummaryGraphElement to, Map<Facet, Set<Facet>> c2r, Map<Facet, Set<Facet>> c2a, HashMap<Facet, Contain> rel2con, HashMap<Facet, Contain> attr2lit)
	{
		Facet f = getFacet(from);
		Facet t = getFacet(to);
		if(f instanceof Attribute && t instanceof Litteral)//from.getType() == SummaryGraphElement.ATTRIBUTE && to.getType() == SummaryGraphElement.VALUE)
		{
			
			Contain contain = attr2lit.get(f);
			if(contain == null) contain = new Contain();
			contain.sf.add(t);
			attr2lit.put(f, contain);
		}
		else if(f instanceof Concept && t instanceof Relation)//from.getType() == SummaryGraphElement.CONCEPT && to.getType() == SummaryGraphElement.RELATION)
		{
			Set<Facet> set = c2r.get(f);
			if(set == null) set = new HashSet<Facet>();
			set.add(t);
			c2r.put(f, set);
		}
		else if(f instanceof Relation && t instanceof Concept)//from.getType() == SummaryGraphElement.RELATION && to.getType() == SummaryGraphElement.CONCEPT)
		{
			Contain contain = rel2con.get(f);
			if(contain == null) contain = new Contain();
			contain.sf.add(t);
			rel2con.put(f, contain);
		}
		else if(f instanceof Concept && t instanceof Attribute)//from.getType() == SummaryGraphElement.CONCEPT && to.getType() == SummaryGraphElement.ATTRIBUTE)
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
			return new Attribute(uri.substring(uri.lastIndexOf('/')+1), uri, new Source(elem.getDatasource(),null,0));
		}
		else if(elem.getType() == SummaryGraphElement.RELATION)
		{
			String uri = ((Property)elem.getResource()).getUri();
			return new Relation(uri.substring(uri.lastIndexOf('/')+1), uri, new Source(elem.getDatasource(),null,0));
		}
		else if(elem.getType() == SummaryGraphElement.CONCEPT)
		{
			String uri = ((NamedConcept)elem.getResource()).getUri();
			return new Concept(uri.substring(uri.lastIndexOf('/')+1), uri, new Source(elem.getDatasource(),null,0));
		}
		else if(elem.getType() == SummaryGraphElement.VALUE)
		{
			String uri = ((Literal)elem.getResource()).getLabel();
			return new Litteral(uri, uri, new Source(elem.getDatasource(),null,0));
		}
		System.out.println("Miss matching: "+elem.getType());
		return null;
	}
	
	public static void main(String[] args) {
		LinkedList ll = new LinkedList();

		ll.add("yao ming");
//		ll.add("fish");
		ll.add("basketball");
//		ll.add("omasicRV98");

//		ll.add("ayGS85");
		new SearchQ2SemanticService(args[0]).getPossibleGraphs(ll, 10);
	}
}
