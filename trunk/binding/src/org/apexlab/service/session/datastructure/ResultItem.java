package org.apexlab.service.session.datastructure;

/**
 * This class represent a result item, that is to say a single document found by the engine
 * @author tpenin
 */
public class ResultItem {

	// The URL that uniquely identify the result item
	public String URL;
	// The score of this result item in its source according to the ranking algorithm of the search engine
	public double score;
	// The type of this result item (text document, picture, etc.)
	public String type;
	// The title of this result item as it will be displayed
	public String title;
	// The text snippet associated with the result item
	public String snippet;
	
	/**
	 * Default constructor
	 */
	public ResultItem() {
		this.URL = "";
		this.score = 0.0;
		this.type = "";
		this.title = "";
		this.snippet = "";
	}
	
	/**
	 * Constructor
	 * @param url The URL that uniquely identify the result item
	 * @param score The score of this result item in its source according to the ranking algorithm of the search engine
	 * @param type The type of this result item (text document, picture, etc.)
	 * @param title The title of this result item as it will be displayed
	 * @param snippet The text snippet associated with the result item
	 */
	public ResultItem(String url, double score, String type, String title, String snippet) {
		this.URL = url;
		this.score = score;
		this.type = type;
		this.title = title;
		this.snippet = snippet;
	}

	/**
	 * @return the score
	 */
	public double getScore() {
		return this.score;
	}

	/**
	 * @param score the score to set
	 */
	public void setScore(double score) {
		this.score = score;
	}

	/**
	 * @return the snippet
	 */
	public String getSnippet() {
		return this.snippet;
	}

	/**
	 * @param snippet the snippet to set
	 */
	public void setSnippet(String snippet) {
		this.snippet = snippet;
	}

	/**
	 * @return the title
	 */
	public String getTitle() {
		return this.title;
	}

	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return this.type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @return the uRL
	 */
	public String getURL() {
		return this.URL;
	}

	/**
	 * @param url the uRL to set
	 */
	public void setURL(String url) {
		this.URL = url;
	}
}
