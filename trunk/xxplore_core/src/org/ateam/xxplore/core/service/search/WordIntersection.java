package org.ateam.xxplore.core.service.search;

import java.util.LinkedHashSet;
import java.util.Set;

import org.ateam.xxplore.core.service.labelizer.OntologySubLabel;

public class WordIntersection {

	private String _keyword;
	private int _numWords;
	private Set<OntologySubLabel> _results;
	
	public WordIntersection(String keyword, int numWords) {
		_keyword = keyword;
		_numWords = numWords;
		_results = new LinkedHashSet<OntologySubLabel>();
	}
	
	public String getKeyword() {
		return _keyword;
	}
	
	public int getNumWords() {
		return _numWords;
	}
	
	public Set<OntologySubLabel> getResults() {
		return _results;
	}
	
	public void setKeyword(String keyword) {
		_keyword = keyword;
	}
	
	public void setNumWords(int numWords) {
		_numWords = numWords;
	}
	
	public void addResult(OntologySubLabel subLabel) {
		_results.add(subLabel);
	}
	
	public void addResult(String sublabel, String type) {
		_results.add(new OntologySubLabel(sublabel, type));
	}
	
	public void addResult(String sublabel, String type, String property) {
		_results.add(new OntologySubLabel(sublabel, type, property));
	}
	
	public void addMultipleResults(Set<OntologySubLabel> results) {
		_results.addAll(results);
	}
	
	public void removeResult(String sublabel, String type) {
		_results.remove(sublabel);
	}
	
	public boolean hasNoIntersections() {
		return _results.isEmpty();
	}
}
