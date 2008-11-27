package org.apexlab.service.session.datastructure;

/**
 * This class represent a suggestion corresponding to an RDF relation
 * @author tpenin
 */
public class RelationSuggestion extends Suggestion {

	/**
	 * Default constructor
	 */
	public RelationSuggestion() {
		super();
	}

	/**
	 * @param label
	 * @param source
	 * @param uri
	 */
	public RelationSuggestion(String label, Source source, String uri, double score) {
		super(label, source, uri, score);
	}
}
