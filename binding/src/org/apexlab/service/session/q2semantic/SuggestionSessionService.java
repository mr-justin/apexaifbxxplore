package org.apexlab.service.session.q2semantic;

import java.util.Collection;
import java.util.LinkedList;

import org.apexlab.service.session.datastructure.Concept;
import org.apexlab.service.session.datastructure.QueryGraph;
import org.apexlab.service.session.datastructure.Suggestion;

/**
 * Binding class to use the Q2Semantic JAR with BlazeDS. For Flex, this is the true service interface.
 * @author tpenin
 */
public class SuggestionSessionService {

	/**
	 * Get the possible query graphs given a list of keywords
	 * @param keywordList The list of keywords to consider
	 * @param topNbGraphs The maximum number of graphs to return
	 * @return The topNbGraphs first suggested graphs
	 */
	public LinkedList<QueryGraph> getPossibleGraphs(LinkedList<String> keywordList, int topNbGraphs) throws Exception {
		int id = Q2SemanticPool.acquire();
		SearchQ2SemanticService t = Q2SemanticPool.getEvaluator(id);
		LinkedList<QueryGraph> chen = t.getPossibleGraphs(keywordList, topNbGraphs);
		Q2SemanticPool.release(id);
		return chen;
	}
	
	/**
	 * Get a list of suggestions knowing a list of concepts and the name of the current source
	 * @param conceptList The list of concepts associated with the current source
	 * @param sourceName The name of the current source
	 * @param topk The max number of suggestions to return per category (concept or relation)
	 * @return The suggestions from other sources
	 */
	public Collection<Suggestion> getSuggestion (LinkedList<Concept> conceptList, String sourceName, int topk) throws Exception {
		int id = Q2SemanticPool.acquire();
		SearchQ2SemanticService t = Q2SemanticPool.getEvaluator(id);
		
//		for(Concept con : conceptList) {
//			System.out.println(con.getURI());
//		}
		
		Collection<Suggestion> chen = t.getSuggestion(conceptList, sourceName, topk);
		Q2SemanticPool.release(id);
		return chen;
	}
	
	public static void main(String[] args) throws Exception {
		SearchQ2SemanticService serv = new SearchQ2SemanticService();
		LinkedList<String> keyword = new LinkedList<String>();
		keyword.add("yao ming");
		keyword.add("olympics");
		serv.getPossibleGraphs(keyword, 10);
	}
}
