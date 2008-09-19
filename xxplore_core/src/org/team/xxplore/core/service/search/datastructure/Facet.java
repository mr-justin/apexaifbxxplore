package org.team.xxplore.core.service.search.datastructure;

/**
 * This class represents a facet that may be used to specify queries. A facet represents an RDF element
 * @author tpenin
 */
public class Facet implements Query {
	
	// The label can be displayed by the interface
	public String label;
	// The URI identifies the facet in a unique manner
	public String URI;
	// The data source from which originates the facet
	public Source source;
	// Number of results associated with a facet
	public int resultNb;
   
	/**
	 * Default constructor
	 */
	public Facet() {
		this.label = "";
		this.URI = "";
		this.source = new Source();
		this.resultNb = 0;
	}
	
	/**
	 * @param label The label can be displayed by the interface
	 * @param uri The URI identifies the facet in a unique manner
	 * @param source The data source from which originates the facet
	 */
	public Facet(String label, String uri, Source source) {
		this.label = label;
		this.URI = uri;
		this.source = source;
	}

	/**
	 * @return the label
	 */
	public String getLabel() {
		return this.label;
	}

	/**
	 * @param label the label to set
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * @return the source
	 */
	public Source getSource() {
		return this.source;
	}

	/**
	 * @param source the source to set
	 */
	public void setSource(Source source) {
		this.source = source;
	}

	/**
	 * @return the URI
	 */
	public String getURI() {
		return this.URI;
	}

	/**
	 * @param uri the URI to set
	 */
	public void setURI(String URI) {
		this.URI = URI;
	}

	/**
	 * @return the resultNb
	 */
	public int getResultNb() {
		return resultNb;
	}

	/**
	 * @param resultNb the resultNb to set
	 */
	public void setResultNb(int resultNb) {
		this.resultNb = resultNb;
	}
}