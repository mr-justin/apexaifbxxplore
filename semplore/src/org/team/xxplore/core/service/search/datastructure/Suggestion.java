package org.team.xxplore.core.service.search.datastructure;

/**
 * This class represents a suggestion. That is to say, a facet from another source than the current source
 * @author tpenin
 */
public class Suggestion {
	
	// Label that will be displayed
	public String label;
	// Source of this facet
	public Source source;
	// URI of the element
	public String URI;
	
	/**
	 * Default constructor
	 */
	public Suggestion() {
		this.label = "";
		this.source = null;
		this.URI = "";
	}

	/**
	 * @param label
	 * @param source
	 * @param uri
	 */
	public Suggestion(String label, Source source, String uri) {
		this.label = label;
		this.source = source;
		URI = uri;
	}

	/**
	 * @return the label
	 */
	public String getLabel() {
		return label;
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
		return source;
	}

	/**
	 * @param source the source to set
	 */
	public void setSource(Source source) {
		this.source = source;
	}

	/**
	 * @return the uRI
	 */
	public String getURI() {
		return URI;
	}

	/**
	 * @param uri the uRI to set
	 */
	public void setURI(String uri) {
		URI = uri;
	}
}
