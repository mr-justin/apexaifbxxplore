package org.team.xxplore.core.service.q2semantic.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Scanner;

import org.team.xxplore.core.service.q2semantic.SummaryGraphElement;


/**
 * This class is the Q2Semantic service API that can be called by the search engine interface
 * @author tpenin
 */

public class Q2SemanticService {
	public Parameters param;
	
	private QueryInterpretationService inter;
	
	public QueryInterpretationService getInter() {
		return this.inter;
	}

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
			System.out.println("Begin keyword search in " + keywordIndex + " ...");
			Map<String, Collection<SummaryGraphElement>> hm = 
				new KeywordSearcher().searchKb(keywordIndex,queryList,prune);
			for(String key : hm.keySet()) {
				Collection<SummaryGraphElement> coll = elementsMap.get(key);
				if(coll == null) {
					coll = new ArrayList<SummaryGraphElement>();
					elementsMap.put(key, coll);
				}
				coll.addAll(hm.get(key));
			}
			System.out.println("OK!");
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
	public LinkedList<Subgraph> getPossibleGraphs(LinkedList<String> keywordList) {
		
		// TODO
		// Note: I will certainly have to find a way to serialize this list of graphs to XML... (tpenin)
	
		Map<String, Collection<SummaryGraphElement>> elementsMap = searchKeyword(keywordList,param.prune);
		
		for(String key : elementsMap.keySet()) {
			System.out.println(key);
			Collection<SummaryGraphElement> coll = elementsMap.get(key);
			for(SummaryGraphElement ele : coll) {
				System.out.println("\t" + ele.toString() + "\n\t" + ele.getMatchingScore() + "\t"+ ele.getDatasource() + "\t" + ele.getType());
				System.out.println();
			}
			System.out.println("====================");
		}
		
		if(elementsMap.size()<keywordList.size()) return null;
		
		LinkedList<Subgraph> graphs = inter.computeQueries(elementsMap, param.distance, param.topNbGraphs);
		return graphs;
	}
	

	
	public static void main(String[] args) {
		Q2SemanticService qSemanticService = new Q2SemanticService(args[0]);
		Scanner scanner = new Scanner(System.in);
		while(true) {
			if(args.length == 1){
				System.out.println("Please input the keywords:");
				String line = scanner.nextLine();
				String tokens [] = line.split(" ");
				LinkedList<String> keywordList = new LinkedList<String>();
				for(int i=0;i<tokens.length;i++) {
					keywordList.add(tokens[i]);
				}
				qSemanticService.getPossibleGraphs(keywordList);
			}
			else if(args[1].equals("suggestion")){
				System.out.println("Please input the concepturi and ds:");
				String line = scanner.nextLine();
				ArrayList<String> list = new ArrayList<String>();
				String[] tokens = line.split(" ");
				list.add(tokens[0]);
				qSemanticService.getInter().getSuggestion(list, tokens[1], qSemanticService.getInter().mis);
			}
		}
	}

}
