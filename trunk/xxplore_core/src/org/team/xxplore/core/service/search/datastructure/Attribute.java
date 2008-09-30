package org.team.xxplore.core.service.search.datastructure;

/**
 * This class represents an RDF attribute
 * @author tpenin
 */
public class Attribute extends Facet {
	
	/**
	 * Default constructor
	 */
	public Attribute() { 
		super(); 
	}
	
	/**
	 * Constructor
	 * @param label The label of the attribute
	 * @param uri The URI of the attribute
	 * @param source The source of the attribute
	 */
	public Attribute(String label, String uri, Source source) {
		super(label, uri, source);
	}
}
