package org.apexlab.service.session.datastructure;

import java.util.LinkedList;

/**
 * Class representing a data source associated with results
 * @author tpenin
 */
public class Source {
	
	// The name of the source
	public String name;
	// The list of facets associated with this source
	public LinkedList<Facet> facetList;
	// The number of results associated with this source returned by the search engine
	public int resultCount;
   
	/**
	 * Default constructor
	 */
	public Source() {
		this.name = "";
		this.facetList = new LinkedList<Facet>();
		this.resultCount = 0;
	}

	/**
	 * Constructor
	 * @param name The name of the source
	 * @param facetList The list of facets associated with this source
	 * @param resultCount The number of results associated with this source returned by the search engine
	 */
	public Source(String name, LinkedList<Facet> facetList, int resultCount) {
		this.name = name;
		this.facetList = facetList;
		this.resultCount = resultCount;
	}
	
	/**
	 * @return the facetList
	 */
	public LinkedList<Facet> getFacetList() {
		return this.facetList;
	}

	/**
	 * @param facetList the facetList to set
	 */
	public void setFacetList(LinkedList<Facet> facetList) {
		this.facetList = facetList;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the resultCount
	 */
	public int getResultCount() {
		return this.resultCount;
	}

	/**
	 * @param resultCount the resultCount to set
	 */
	public void setResultCount(int resultCount) {
		this.resultCount = resultCount;
	}
	
	public boolean equals(Object b) {
		return name.equals(((Source)b).name);
	}
	
	public int hashCode() {
		return name.hashCode();
	}
}
