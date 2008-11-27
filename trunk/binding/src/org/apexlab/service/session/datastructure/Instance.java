package org.apexlab.service.session.datastructure;

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
	
	/**
	 * Constructor
	 * @param label The label of the instance
	 * @param uri The URI of the instance
	 * @param source The source of the instance
	 */
	public Instance(String label, String uri, Source source) {
		super(label, uri, source);
	}

}
