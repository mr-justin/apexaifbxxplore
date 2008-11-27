package org.apexlab.service.q2semantic;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
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

import org.apexlab.service.q2semantic.QueryInterpretationService.Subgraph;
import org.apexlab.service.session.datastructure.Concept;
import org.apexlab.service.session.datastructure.ConceptSuggestion;
import org.apexlab.service.session.datastructure.Facet;
import org.apexlab.service.session.datastructure.GraphEdge;
import org.apexlab.service.session.datastructure.Litteral;
import org.apexlab.service.session.datastructure.QueryGraph;
import org.apexlab.service.session.datastructure.Relation;
import org.apexlab.service.session.datastructure.RelationSuggestion;
import org.apexlab.service.session.datastructure.Source;
import org.apexlab.service.session.datastructure.Suggestion;
import org.jgrapht.graph.Pseudograph;
import org.team.xxplore.core.service.impl.Literal;
import org.team.xxplore.core.service.impl.NamedConcept;
import org.team.xxplore.core.service.impl.Property;
import org.team.xxplore.core.service.mapping.Mapping;
import org.team.xxplore.core.service.mapping.MappingIndexService;


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
	public QueryInterpretationService inter;
	private MappingIndexService mis = new MappingIndexService();

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
	public Collection<Suggestion> getSuggestion(List<Concept> concept, String ds, int topK) throws Exception {	
		List<String> con = new ArrayList<String>();
		for(Concept c: concept)
			con.add(c.getURI());
		
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
		
		return ress;
	}
	
	public LinkedList<QueryGraph> getPossibleGraphs(LinkedList<String> keywordList, int topNbGraphs) {
	//	LinkedList<String> tmp = new LinkedList<String>();
		LinkedList<QueryGraph> ret = this.getPossibleGraphs(keywordList, 5, 0.95, 5, 0.5);
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
			Collection<Mapping> mappings;
			HashSet<String> delDupMappingEdge = new HashSet<String>();
			
			mappings = inter.factory.getMappings();
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
	
	public Map<String,Collection<SummaryGraphElement>> searchKeyword(LinkedList<String> queryList,double prune) {
		//search for elements
		Map<String,Collection<SummaryGraphElement>> elementsMap = new HashMap<String,Collection<SummaryGraphElement>>();

		for(String qt : queryList) {
			for(String ds : summaryObjSet.keySet()) {
				String keywordIndex = keywordIndexRoot + "/" + ds + "-keywordIndex";
				System.out.println("keywordIndex " + keywordIndex);
				Map<String, Collection<SummaryGraphElement>> hm = 
					new KeywordIndexServiceForBTFromNT(keywordIndex, false).searchKb(qt, prune);
	
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
		return elementsMap;
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
			double edge_score) {
		
		// TODO
		// Note: I will certainly have to find a way to serialize this list of graphs to XML... (tpenin)
		
		QueryInterpretationService.EDGE_SCORE = edge_score;		
		Map<String, Collection<SummaryGraphElement>> elementsMap = searchKeyword(keywordList,prune);
		
		if(elementsMap.size()<keywordList.size()) return null;
		
		LinkedList<Subgraph> graphs = inter.computeQueries(elementsMap, distance, topNbGraphs);
		if(graphs == null) return null;
		LinkedList<QueryGraph> result = this.getQueryGraphFromTopKResult(graphs);
		
		for(int i=0; i<result.size(); i++) {
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
	
	public void loadPara(String fn) throws Exception {
		Properties prop = new Properties();
		InputStream is = new FileInputStream(fn);
		prop.load(is);
		root = prop.getProperty("root")+File.separator;
		summaryObjsRoot = root+prop.getProperty("summaryObjsRoot")+File.separator;
		schemaObjsRoot = root+prop.getProperty("schemaObjsRoot")+File.separator;
		keywordIndexRoot = root+prop.getProperty("keywordIndexRoot")+File.separator;
		mappingIndexRoot = root+prop.getProperty("mappingIndexRoot")+File.separator;
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
			System.out.println("Please input:");
			String line = scanner.nextLine();
			LinkedList<String> ll = new LinkedList<String>();
			String tokens [] = line.split(" ");
			for(int i=0;i<tokens.length;i++) {
				ll.add(tokens[i]);
			}
			for(int i=0;i<ll.size();i++) {
				System.out.println(ll.get(i));
			}
			
			
			new SearchQ2SemanticService(args[0]).getPossibleGraphs(
					ll, Integer.valueOf(args[1]), Double.valueOf(args[2]), 
					Integer.valueOf(args[3]),Double.valueOf(args[4]));
		}
	}
}
