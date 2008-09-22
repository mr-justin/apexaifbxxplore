package org.team.xxplore.core.service.search.datastructure;

import java.util.LinkedList;

/**
 * This class represents a ResultPage. It is related to a Result object, contains all its lists but only a subset
 * of its results for a given page.
 * @author tpenin
 */
public class ResultPage extends Result {
	
	// The number of the current page
	public int pageNum;
   
	/**
	 * Default Constructor
	 */
	public ResultPage() {
		super();
	}
	
	/**
	 * Usual constructor
	 * @param resultItemList List of result items for this page
	 * @param source The source of the results
	 * @param pageNum Number of this page of result within results from its source
	 */
	public ResultPage(LinkedList<ResultItem> resultItemList, Source source, int pageNum) {
		// Call to the Result constructor
		super(resultItemList, source);
		this.pageNum = pageNum;
	}

	/**
	 * @return the pageNum
	 */
	public int getPageNum() {
		return this.pageNum;
	}

	/**
	 * @param pageNum the pageNum to set
	 */
	public void setPageNum(int pageNum) {
		this.pageNum = pageNum;
	}
}
