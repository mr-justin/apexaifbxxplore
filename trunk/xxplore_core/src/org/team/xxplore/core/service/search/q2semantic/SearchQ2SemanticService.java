package org.team.xxplore.core.service.search.q2semantic;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;

import org.ateam.xxplore.core.service.mapping.MappingIndexService;
import org.ateam.xxplore.core.service.search.KeywordIndexServiceForBT;
import org.ateam.xxplore.core.service.search.QueryInterpretationService;
import org.ateam.xxplore.core.service.search.SummaryGraphElement;
import org.jgrapht.graph.WeightedPseudograph;
import org.team.xxplore.core.service.search.datastructure.QueryGraph;

/**
 * This class is the Q2Semantic service API that can be called by the search engine interface
 * @author tpenin
 */
public class SearchQ2SemanticService {

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
		for(WeightedPseudograph qg: inter.computeQueries(elementsMap, new MappingIndexService(mappingIndex), distance, topNbGraphs))
		{
			QueryGraph graph = new QueryGraph();
			graph.setQueryGraph(qg);
			result.add(graph);
		}
		return result;
	}
}
