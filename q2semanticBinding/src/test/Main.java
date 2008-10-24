package test;

import java.util.LinkedList;

import org.team.xxplore.core.service.search.datastructure.QueryGraph;
import org.ateam.xxplore.core.service.q2semantic.SearchQ2SemanticService;

public class Main {
	public static void main(String[] args) {
		SearchQ2SemanticService t = new SearchQ2SemanticService();
		try {
			t.loadPara("config/path.prop");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		LinkedList<String> keyword = new LinkedList<String>();
		keyword.add("word");
		keyword.add("net");
		
		LinkedList<QueryGraph> chen = t.getPossibleGraphs(keyword, 10);
		
		System.out.println(chen.size());
	}
}
