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
	
	public Litteral(String label, String uri, Source source) {
		super(label, uri, source);
	}

}
