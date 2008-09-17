package org.team.xxplore.core.service.search.datastructure;

import org.dom4j.Document;

/**
 * This class represents a facet that may be used to specify queries. A facet represents an RDF element
 * @author tpenin
 */
public class Facet implements Query, XMLSerializable {
	
	// The label can be displayed by the interface
	protected String label;
	// The URI identifies the facet in a unique manner
	protected String URI;
	// The data source from which originates the facet
	protected Source source;
   
	/**
	 * Default constructor
	 */
	public Facet() {
		this.label = "";
		this.URI = "";
		this.source = new Source();
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

	/* (non-Javadoc)
	 * @see dataStructures.XMLSerializable#toXML()
	 */
	public Document toXML() {
		// TODO Auto-generated method stub
		return null;
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
}
