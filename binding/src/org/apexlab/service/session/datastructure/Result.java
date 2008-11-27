package org.apexlab.service.session.datastructure;

import java.util.LinkedList;

/**
 * This class represents all the results returned by the search engine for a given query, independently from the page 
 * to display
 * @author tpenin
 */
public class Result {
	
	// The list of all the result items that were found by the search engine
	public LinkedList<ResultItem> resultItemList;
	// The list of all the sources that have contributed to the results
	public Source source;
	
	/**
	 * Default constructor
	 */
	public Result() {
		this.resultItemList = new LinkedList<ResultItem>();
		this.source = null;
	}
	
	/**
	 * Constructor
	 * @param resultItemList The result items that were found by the search engine
	 * @param source The current source
	 */
	public Result(LinkedList<ResultItem> resultItemList, Source source) {
		this.resultItemList = resultItemList;
		this.source = source;
	}
	
	/**
	 * @return the resultItemList
	 */
	public LinkedList<ResultItem> getResultItemList() {
		return resultItemList;
	}

	/**
	 * @param resultItemList the resultItemList to set
	 */
	public void setResultItemList(LinkedList<ResultItem> resultItemList) {
		this.resultItemList = resultItemList;
	}

	/**
	 * @return the source
	 */
	public Source getSource() {
		return source;
	}

	/**
	 * @param source the source to set
	 */
	public void setSource(Source source) {
		this.source = source;
	}
}
