package org.team.xxplore.core.service.search.datastructure;

/**
 * This class represents an RDF concept class
 * @author tpenin
 */
public class Concept extends Facet {
	/**
	 * This is the letter identify a variable in the query disambiguation
	 * process (ex: 'x' in '?x relation ?y')
	 */
	public String variableLetter;
	
	/**
	 * Default constructor
	 */
	public Concept() {
		super();
	}
	
	/**
	 * Constructor
	 * @param label The label of the concept
	 * @param uri The URI of the concept, or "<TOP_Category>"
	 * @param source The source of the concept
	 */
	public Concept(String label, String uri, Source source) {
		super(label, uri, source);
	}
}
