package org.team.xxplore.core.service.q2semantic;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;

import org.team.xxplore.core.service.mapping.MappingIndexSearcher;


/**
 * This class is the Q2Semantic service API that can be called by the search engine interface
 * @author tpenin
 */

public class Q2SemanticService {
	private Parameters param;
	
	private QueryInterpretationService inter;

	public Q2SemanticService(String fn) {
		Parameters.setConfigFilePath(fn);
		param = Parameters.getParameters();
		inter = new QueryInterpretationService();
	}
		
	public Map<String,Collection<SummaryGraphElement>> searchKeyword(LinkedList<String> queryList,double prune) {
		//search for elements
		Map<String,Collection<SummaryGraphElement>> elementsMap = new HashMap<String,Collection<SummaryGraphElement>>();

		for(String ds : param.getDataSourceSet()) {
			String keywordIndex = param.keywordIndexRoot + "/" + ds + "-keywordIndex";
			Map<String, Collection<SummaryGraphElement>> hm = 
				new KeywordSearcher().searchKb(keywordIndex,queryList,prune);
			elementsMap.putAll(hm);
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
	public LinkedList<Subgraph> getPossibleGraphs(LinkedList<String> keywordList, int topNbGraphs, double prune, int distance,
			double edge_score) {
		
		// TODO
		// Note: I will certainly have to find a way to serialize this list of graphs to XML... (tpenin)
		
		Map<String, Collection<SummaryGraphElement>> elementsMap = searchKeyword(keywordList,prune);
		
		if(elementsMap.size()<keywordList.size()) return null;
		
		LinkedList<Subgraph> graphs = inter.computeQueries(elementsMap, distance, topNbGraphs);
		return graphs;
	}
	

	
	public static void main(String[] args) {
		Q2SemanticService qSemanticService = new Q2SemanticService("config/path.prop");
		LinkedList<String> keywordList = new LinkedList<String>();
		Scanner scanner = new Scanner(System.in);
		while(true) {
			System.out.println("Please input the keywords:");
			String line = scanner.nextLine();
			String tokens [] = line.split(" ");
			for(int i=0;i<tokens.length;i++) {
				keywordList.add(tokens[i]);
			}
			qSemanticService.getPossibleGraphs(keywordList, 10, 0.95, 5, 0.5);
		}
	}

}
