package org.team.xxplore.core.service.search.q2semantic;

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
import java.util.Set;

import org.ateam.xxplore.core.service.mapping.MappingIndexService;
import org.ateam.xxplore.core.service.q2semantic.KeywordIndexServiceForBT;
import org.ateam.xxplore.core.service.q2semantic.SesameDao;
import org.ateam.xxplore.core.service.search.QueryInterpretationService;
import org.ateam.xxplore.core.service.search.SummaryGraphEdge;
import org.ateam.xxplore.core.service.search.SummaryGraphElement;
import org.ateam.xxplore.core.service.search.QueryInterpretationService.Subgraph;
import org.jgrapht.graph.WeightedPseudograph;
import org.team.xxplore.core.service.search.datastructure.Attribute;
import org.team.xxplore.core.service.search.datastructure.Concept;
import org.team.xxplore.core.service.search.datastructure.ConceptSuggestion;
import org.team.xxplore.core.service.search.datastructure.Facet;
import org.team.xxplore.core.service.search.datastructure.GraphEdge;
import org.team.xxplore.core.service.search.datastructure.Litteral;
import org.team.xxplore.core.service.search.datastructure.QueryGraph;
import org.team.xxplore.core.service.search.datastructure.Relation;
import org.team.xxplore.core.service.search.datastructure.RelationSuggestion;
import org.team.xxplore.core.service.search.datastructure.Source;
import org.team.xxplore.core.service.search.datastructure.Suggestion;
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
	
	public SearchQ2SemanticService() {
		// == chenjunquan ==
		try {
			this.loadPara("config/path.prop");
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
		
		double prune = 0.9;
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
		
		for(String keywordIndex: keywordIndexSet) {
			System.out.println("keywordIndex " + keywordIndex);
			elementsMap.putAll(new KeywordIndexServiceForBT(keywordIndex, false).searchKb(query, prune));
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
		LinkedList<Subgraph> graphs = inter.computeQueries(elementsMap, mis, distance, topNbGraphs);
		if(graphs == null) return null;
		
		for(WeightedPseudograph qg: graphs)
		{
			Set<SummaryGraphEdge> edges = qg.edgeSet();
			SummaryGraphElement from, to;
			Map<Facet, Set<Facet>> con2rel = new HashMap<Facet, Set<Facet>>(), con2attr = new HashMap<Facet, Set<Facet>>(),
				rel2con = new HashMap<Facet, Set<Facet>>(), attr2lit = new HashMap<Facet, Set<Facet>>();
			for(SummaryGraphEdge edge: edges)
			{
				from = edge.getSource();
				to = edge.getTarget();
				gether(from, to, con2rel, con2attr, rel2con, attr2lit);
			}
			
			LinkedList<GraphEdge> graphEdges = new LinkedList<GraphEdge>();
//			System.out.println(con2rel.size()+"\t"+rel2con.size()+"\t"+con2attr.size()+"\t"+attr2lit.size());
			for(Facet f: con2rel.keySet())
				for(Facet r: con2rel.get(f))
					if(rel2con.get(r) != null)
					for(Facet t: rel2con.get(r))
						graphEdges.add(new GraphEdge(f, t, r));
			
			for(Facet f: con2attr.keySet())
				for(Facet a: con2attr.get(f))
					if(attr2lit.get(a) != null)
					for(Facet t: attr2lit.get(a))
						graphEdges.add(new GraphEdge(f, t, a));
			
			result.add(new QueryGraph(graphEdges));
		}
		for(QueryGraph graph: result)
		{
			System.out.println("===============");
			graph.print();
		}
		return result;
	}
	
	public void loadPara(String fn) throws Exception
	{
		Properties prop = new Properties();
		InputStream is = new FileInputStream(fn);
		prop.load(is);
		root = prop.getProperty("root")+File.separator;
		SesameDao.root = root;
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
	
	private void gether(SummaryGraphElement from, SummaryGraphElement to, Map<Facet, Set<Facet>> c2r, Map<Facet, Set<Facet>> c2a, Map<Facet, Set<Facet>> r2c, Map<Facet, Set<Facet>> a2l)
	{
		if(from.getType() == SummaryGraphElement.ATTRIBUTE && to.getType() == SummaryGraphElement.VALUE)
		{
			String uri = ((Property)from.getResource()).getUri();
			Facet f = new Attribute(uri.substring(uri.lastIndexOf('/')+1), uri, new Source(from.getDatasource(),null,0));
			uri = ((Literal)to.getResource()).getLabel();
			Facet t = new Litteral(uri, uri, new Source(to.getDatasource(),null,0));
			Set set = a2l.get(f);
			if(set == null) set = new HashSet<Facet>();
			set.add(t);
			a2l.put(f, set);
		}
		else if(from.getType() == SummaryGraphElement.CONCEPT && to.getType() == SummaryGraphElement.RELATION)
		{
			String uri = ((NamedConcept)from.getResource()).getUri();
			Facet f = new Concept(uri.substring(uri.lastIndexOf('/')+1), uri, new Source(from.getDatasource(),null,0));
			uri = ((Property)to.getResource()).getUri();
			Facet t = new Relation(uri.substring(uri.lastIndexOf('/')+1), uri, new Source(to.getDatasource(),null,0));
			Set set = c2r.get(f);
			if(set == null) set = new HashSet<Facet>();
			set.add(t);
			c2r.put(f, set);
		}
		else if(from.getType() == SummaryGraphElement.RELATION && to.getType() == SummaryGraphElement.CONCEPT)
		{
			String uri = ((Property)from.getResource()).getUri();
			Facet f = new Relation(uri.substring(uri.lastIndexOf('/')+1), uri, new Source(from.getDatasource(),null,0));
			uri = ((NamedConcept)to.getResource()).getUri();
			Facet t = new Concept(uri.substring(uri.lastIndexOf('/')+1), uri, new Source(to.getDatasource(),null,0));
			Set set = r2c.get(f);
			if(set == null) set = new HashSet<Facet>();
			set.add(t);
			r2c.put(f, set);
		}
		else if(from.getType() == SummaryGraphElement.CONCEPT && to.getType() == SummaryGraphElement.ATTRIBUTE)
		{
			String uri = ((NamedConcept)from.getResource()).getUri();
			Facet f = new Concept(uri.substring(uri.lastIndexOf('/')+1), uri, new Source(from.getDatasource(),null,0));
			uri = ((Property)to.getResource()).getUri();
			Facet t = new Relation(uri.substring(uri.lastIndexOf('/')+1), uri, new Source(to.getDatasource(),null,0));
			Set set = c2a.get(f);
			if(set == null) set = new HashSet<Facet>();
			set.add(t);
			c2a.put(f, set);
		}
	}
	
	public static void main(String[] args) {
		LinkedList ll = new LinkedList();
		ll.add("bill gates");
		ll.add("windows");
//		ll.add("omasicRV98");
//		ll.add("ayGS85");
		new SearchQ2SemanticService().getPossibleGraphs(ll, 10);
	}
}
