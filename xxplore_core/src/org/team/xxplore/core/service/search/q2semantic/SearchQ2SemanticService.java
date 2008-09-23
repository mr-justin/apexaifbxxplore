package org.team.xxplore.core.service.search.q2semantic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import org.ateam.xxplore.core.service.mapping.MappingIndexService;
import org.ateam.xxplore.core.service.search.KeywordIndexServiceForBT;
import org.ateam.xxplore.core.service.search.QueryInterpretationService;
import org.ateam.xxplore.core.service.search.SummaryGraphEdge;
import org.ateam.xxplore.core.service.search.SummaryGraphElement;
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
		mis.init4Search(MappingIndexService.MAPPING_INDEX_DIR);
		Set<String> sugg = inter.getSuggestion(con, ds, mis);
		Collection<Suggestion> res = new PriorityQueue<Suggestion>();
		for(String str: sugg)
		{
			String[] part = str.split("\t");
			if(part.length!=4) continue;
			String label = part[0].substring(part[0].lastIndexOf('/')+1);
			if(part[3].equals("c"))
				res.add(new ConceptSuggestion(label, new Source(part[1],null, 0), part[0], Double.parseDouble(part[2])));
			else if(part[3].equals("p"))
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
		double prune = 0;
		int distance = 10;
		String query = "", mappingIndex="";
		HashSet<String> keywordIndexes = new HashSet<String>();
		//merge keywords
		for(String str: keywordList)
			query += "\""+str+"\" ";
		query = query.substring(0, query.length()-1);
		//search for elements
		Map<String,Collection<SummaryGraphElement>> elementsMap = new HashMap<String,Collection<SummaryGraphElement>>();
		for(String keywordIndex: keywordIndexes)
			elementsMap.putAll(new KeywordIndexServiceForBT(keywordIndex, false).searchKb(query, prune));
		//search for topk querygraph
		QueryInterpretationService inter = new QueryInterpretationService();
		LinkedList<QueryGraph> result = new LinkedList<QueryGraph>();
		//package the querygraph(Class:WeightedPseudograph) with Class:QueryGraph
		MappingIndexService mis = new MappingIndexService();
		mis.init4Search(MappingIndexService.MAPPING_INDEX_DIR);
		for(WeightedPseudograph qg: inter.computeQueries(elementsMap, mis, distance, topNbGraphs))
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
			
			for(Facet f: con2rel.keySet())
				for(Facet r: con2rel.get(f))
					for(Facet t: rel2con.get(r))
						graphEdges.add(new GraphEdge(f, r, t));
			for(Facet f: con2attr.keySet())
				for(Facet a: con2attr.get(f))
					for(Facet t: attr2lit.get(a))
						graphEdges.add(new GraphEdge(f, a, t));
			
			result.add(new QueryGraph(graphEdges));
		}
		return result;
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
}
