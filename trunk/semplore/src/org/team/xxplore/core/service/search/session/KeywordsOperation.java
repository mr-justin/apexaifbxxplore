package org.team.xxplore.core.service.search.session;

import java.util.Iterator;
import java.util.LinkedList;

import org.team.xxplore.core.service.search.datastructure.Keywords;

import com.ibm.semplore.btc.Graph;
import com.ibm.semplore.model.CompoundCategory;
import com.ibm.semplore.model.GeneralCategory;
import com.ibm.semplore.model.KeywordCategory;
import com.ibm.semplore.model.impl.SchemaFactoryImpl;

public class KeywordsOperation implements Operation {

	private Keywords keywords;
	
	public KeywordsOperation(Keywords k) {
		keywords = k;
	}
	
	@Override
	public Graph applyTo(Graph graph) {
		LinkedList<String> wordList = keywords.getWordList();
		if (wordList.isEmpty()) return graph;
		Iterator<String> it = wordList.iterator();
		String str = it.next();
		for (; it.hasNext(); ) str += " " + it.next();
		CompoundCategory cc = (CompoundCategory)graph.getNode(graph.getTargetVariable());
		cc.addComponentCategory(SchemaFactoryImpl.getInstance().createKeywordCategory(str));
		return graph;

	}

	@Override
	public Graph undo(Graph graph) {
		LinkedList<String> wordList = keywords.getWordList();
		if (wordList.isEmpty()) return graph;
		Iterator<String> it = wordList.iterator();
		String str = it.next();
		for (; it.hasNext(); ) str += " " + it.next();
		CompoundCategory cc = (CompoundCategory)graph.getNode(graph.getTargetVariable());
		GeneralCategory[] gc = cc.getComponentCategories();
		for (GeneralCategory c : gc) {
			if (c instanceof KeywordCategory && ((KeywordCategory)c).getKeyword().equals(str)) {
				//TODO cc.remove(c);
				break;
			}
		}
		return graph;
	}

}
