package org.team.xxplore.core.service.search.datastructure;

import java.util.LinkedList;

/**
 * This class represents a list of keywords, that can be handled like a query.
 * @author tpenin
 */
public class Keywords implements Query {
   
	// The list of the keywords
	public LinkedList<String> wordList;
   
	/**
	 * Default constructor
	 */
	public Keywords() {
		this.wordList = new LinkedList<String>();
	}
	
	/**
	 * Constructor
	 * @param list The list of the keywords
	 */
	public Keywords(LinkedList<String> list) {
		this.wordList = list;
	}

	/**
	 * @return the wordList
	 */
	public LinkedList<String> getWordList() {
		return this.wordList;
	}

	/**
	 * @param wordList the wordList to set
	 */
	public void setWordList(LinkedList<String> wordList) {
		this.wordList = wordList;
	}
}
