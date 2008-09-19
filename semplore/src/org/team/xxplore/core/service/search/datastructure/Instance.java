package org.team.xxplore.core.service.search.datastructure;

/**
 * This class represents an RDF instance
 * @author tpenin
 */
public class Instance extends Facet {
	
	/**
	 * Default constructor
	 */
	public Instance() {
	   super();
   	}
	
	public Instance(String label, String uri, Source source) {
		super(label, uri, source);
	}

}
