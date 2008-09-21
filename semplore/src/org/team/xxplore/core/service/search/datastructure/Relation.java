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
	
	/**
	 * Constructor
	 * @param label The label of the relation
	 * @param uri The URI of the relation
	 * @param source The source of the relation
	 */
	public Relation(String label, String uri, Source source) {
		super(label, uri, source);
	}

}