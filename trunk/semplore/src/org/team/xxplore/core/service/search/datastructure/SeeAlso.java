package org.team.xxplore.core.service.search.datastructure;

import java.util.LinkedList;

/**
 * This class contains the elements to implement the "see also" fonctionality
 * @author tpenin
 */
public class SeeAlso {
	
	// The result item this SeeAlso object is attached to
	public ResultItem resultItem;
	// The list of instances attaached to the result item
	public LinkedList<Instance> facetList;
   
	/**
	 * Default constructor
	 */
	public SeeAlso() {
		this.resultItem = null;
		this.facetList = new LinkedList<Instance>();
	}

	/**
	 * Constructor
	 * @param resultItem The result item this SeeAlso object is attached to
	 * @param facetList The list of instances attaached to the result item
	 */
	public SeeAlso(ResultItem resultItem, LinkedList<Instance> facetList) {
		this.resultItem = resultItem;
		this.facetList = facetList;
	}

	/**
	 * @return the facetList
	 */
	public LinkedList<Instance> getFacetList() {
		return this.facetList;
	}

	/**
	 * @param facetList the facetList to set
	 */
	public void setFacetList(LinkedList<Instance> facetList) {
		this.facetList = facetList;
	}

	/**
	 * @return the resultItem
	 */
	public ResultItem getResultItem() {
		return this.resultItem;
	}

	/**
	 * @param resultItem the resultItem to set
	 */
	public void setResultItem(ResultItem resultItem) {
		this.resultItem = resultItem;
	}
}
