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
	
	public Attribute(String label, String uri, Source source) {
		super(label, uri, source);
	}
}
