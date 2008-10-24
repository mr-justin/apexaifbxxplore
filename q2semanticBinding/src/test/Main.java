package test;

import java.util.LinkedList;

import org.team.xxplore.core.service.search.datastructure.QueryGraph;
import org.ateam.xxplore.core.service.q2semantic.SearchQ2SemanticService;

import q2semantic.session.Q2SemanticPool;

public class Main {
	public static void main(String[] args) throws Exception {
		LinkedList<String> keyword = new LinkedList<String>();
		keyword.add("word");
		keyword.add("net");
		int id = Q2SemanticPool.acquire();
		SearchQ2SemanticService t = Q2SemanticPool.getEvaluator(id);
		LinkedList<QueryGraph> chen = t.getPossibleGraphs(keyword, 10);
		Q2SemanticPool.release(id);
		System.out.println(chen.size());
	}
}
