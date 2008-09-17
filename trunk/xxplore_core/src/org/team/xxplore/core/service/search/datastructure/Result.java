package org.team.xxplore.core.service.search.datastructure;

import java.util.LinkedList;

import org.dom4j.Document;

/**
 * This class represents all the results returned by the search engine for a given query, independently from the data
 * source or the page to display
 * @author tpenin
 */
public class Result implements XMLSerializable {
	
	// The list of all the result items that were found by the search engine
	protected LinkedList<ResultItem> resultItemList;
	// The list of all the sources that have contributed to the results
	protected LinkedList<Source> sourceList;
	
	/**
	 * Default constructor
	 */
	public Result() {
		this.resultItemList = new LinkedList<ResultItem>();
		this.sourceList = new LinkedList<Source>();
	}
	
	/**
	 * Constructor
	 * @param resultItemList The result items that were found by the search engine
	 * @param sourceList The list of the sources that have contributed to the results
	 */
	public Result(LinkedList<ResultItem> resultItemList, LinkedList<Source> sourceList) {
		this.resultItemList = resultItemList;
		this.sourceList = sourceList;
	}
	
	/* (non-Javadoc)
	 * @see dataStructures.XMLSerializable#toXML()
	 */
	public Document toXML() {
		// TODO Auto-generated method stub
		return null;
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
	 * @return the sourceList
	 */
	public LinkedList<Source> getSourceList() {
		return sourceList;
	}

	/**
	 * @param sourceList the sourceList to set
	 */
	public void setSourceList(LinkedList<Source> sourceList) {
		this.sourceList = sourceList;
	}
}
