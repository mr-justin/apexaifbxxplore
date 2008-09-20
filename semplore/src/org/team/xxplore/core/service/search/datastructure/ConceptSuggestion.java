package org.team.xxplore.core.service.search.datastructure;

/**
 * This class represent a suggestion corresponding to an RDF relation
 * @author tpenin
 */
public class ConceptSuggestion extends Suggestion {

	/**
	 * Default constructor
	 */
	public ConceptSuggestion() {
		super();
	}

	/**
	 * @param label
	 * @param source
	 * @param uri
	 */
	public ConceptSuggestion(String label, Source source, String uri) {
		super(label, source, uri);
	}
}
