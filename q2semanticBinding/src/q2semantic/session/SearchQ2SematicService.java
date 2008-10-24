package q2semantic.session;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;

import org.team.xxplore.core.service.search.datastructure.Concept;
import org.team.xxplore.core.service.search.datastructure.QueryGraph;
import org.team.xxplore.core.service.search.datastructure.Suggestion;
import org.ateam.xxplore.core.service.q2semantic.SearchQ2SemanticService;

/**
 * Binding class to use the Q2Semantic JAR with BlazeDS. For Flex, this is the true service interface.
 * @author tpenin
 */
public class SearchQ2SematicService {

	/**
	 * Get the possible query graphs given a list of keywords
	 * @param keywordList The list of keywords to consider
	 * @param topNbGraphs The maximum number of graphs to return
	 * @return The topNbGraphs first suggested graphs
	 */
	public LinkedList<QueryGraph> getPossibleGraphs(LinkedList<String> keywordList, int topNbGraphs) {
		SearchQ2SemanticService t = new SearchQ2SemanticService();
		try {
			t.loadPara("config/path.prop");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// Call the service
		LinkedList<QueryGraph> chen = t.getPossibleGraphs(keywordList, topNbGraphs);
		// Return the result
		return chen;
	}
	
	/**
	 * Get a list of suggestions knowing a list of concepts and the name of the current source
	 * @param conceptList The list of concepts associated with the current source
	 * @param sourceName The name of the current source
	 * @param topk The max number of suggestions to return per category (concept or relation)
	 * @return The suggestions from other sources
	 */
	public Collection<Suggestion> getSuggestion (LinkedList<Concept> conceptList, String sourceName, int topk) {
		// Real service
		SearchQ2SemanticService t = new SearchQ2SemanticService();
		try {
			t.loadPara("config/path.prop");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// Call the service
		Collection<Suggestion> chen = null;
		try {
			chen = t.getSuggestion(conceptList, sourceName, topk);
		} catch (Exception e) {
			e.printStackTrace();
		}
		// Return the result
		return chen;
	}
	
	public static void main(String[] args) {
		q2semantic.session.SearchQ2SematicService serv = new q2semantic.session.SearchQ2SematicService();
		LinkedList<String> keyword = new LinkedList<String>();
		keyword.add("yao ming");
		keyword.add("olympics");
		serv.getPossibleGraphs(keyword, 10);
	}
}
