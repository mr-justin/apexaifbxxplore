package org.team.xxplore.core.service.search.datastructure;

import java.util.LinkedList;

import org.dom4j.Document;

/**
 * This class represents a ResultPage. It is related to a Result object, contains all its lists but only a subset
 * of its results for a given page and a given source.
 * @author tpenin
 */
public class ResultPage extends Result {
	
	// The source for the current page
	private Source activeSource;
	// The number of the current page
	private int pageNum;
   
	/**
	 * Default Constructor
	 */
	public ResultPage() {
		super();
	}
	
	/**
	 * Usual constructor
	 * @param resultItemList List of result items for this page
	 * @param sourceList List of the sources of all results, not only from this page
	 * @param activeSource Source of the results of this page
	 * @param pageNum Number of this page of result within results from its source
	 */
	public ResultPage(LinkedList<ResultItem> resultItemList, LinkedList<Source> sourceList, Source activeSource, int pageNum) {
		// Call to the Result constructor
		super(resultItemList, sourceList);
		this.activeSource = activeSource;
		this.pageNum = pageNum;
	}
	
	/* (non-Javadoc)
	 * @see dataStructures.XMLSerializable#toXML()
	 */
	public Document toXML() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @return the activeSource
	 */
	public Source getActiveSource() {
		return this.activeSource;
	}

	/**
	 * @param activeSource the activeSource to set
	 */
	public void setActiveSource(Source activeSource) {
		this.activeSource = activeSource;
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
