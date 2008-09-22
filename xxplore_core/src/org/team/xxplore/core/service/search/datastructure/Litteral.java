package org.team.xxplore.core.service.search.datastructure;

/**
 * This class represents an RDF litteral
 * @author tpenin
 */
public class Litteral extends Facet {
   
	/**
	 * Default constructor
	 */
	public Litteral() {
		super();
	}
	
	/**
	 * Constructor
	 * @param label The label of the litteral
	 * @param uri The URI of the litteral
	 * @param source The source of the litteral
	 */
	public Litteral(String label, String uri, Source source) {
		super(label, uri, source);
	}

}
