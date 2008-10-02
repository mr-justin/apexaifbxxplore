package org.team.xxplore.core.service.search.datastructure;

/**
 * This class represents a suggestion. That is to say, a facet from another source than the current source
 * @author tpenin
 */
public class Suggestion implements Comparable{
	
	// Label that will be displayed
	public String label;
	// Source of this facet
	public Source source;
	// URI of the element
	public String URI;
	// Confidence of the suggestion
	public double conf;
	
	/**
	 * Default constructor
	 */
	public Suggestion() {
		this.label = "";
		this.source = null;
		this.URI = "";
		this.conf = 0.0;
	}

	/**
	 * @param label
	 * @param source
	 * @param uri
	 * @param conf
	 */
	public Suggestion(String label, Source source, String uri, double conf) {
		this.label = label;
		this.source = source;
		URI = uri;
		this.conf = conf;
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

	public double getConf() {
		return conf;
	}

	public void setConf(double conf) {
		this.conf = conf;
	}
	
	public int compareTo(Object arg0) {
		// TODO Auto-generated method stub
		double confidence = ((Suggestion)arg0).getConf();
		if(confidence < conf)
			return -1;
		else if(confidence > conf)
			return 1;
		return 0;
	}
}
