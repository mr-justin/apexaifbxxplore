package org.team.xxplore.core.service.search.datastructure;

/**
 * This class represents an RDF relation
 * @author tpenin
 */
public class Relation extends Facet {
   
	/**
	 * Default constructor
	 */
	public Relation() {
		super();
	}
	
	public Relation(String label, String uri, Source source) {
		super(label, uri, source);
	}

}
