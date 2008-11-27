package org.apexlab.service.session.datastructure;

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
		if (source != null && source.getName().equals("dbpedia")) {
			if (label.startsWith("wordnet ")) {
				label = label.substring(8);
				int pos = label.lastIndexOf(' ');
				label = label.substring(0, pos);
			} else if (label.startsWith("wikicategory ")) {
				label = label.substring(13);
			} else 	if (uri.indexOf("wn20/schema")>0) {
				int pos = label.lastIndexOf(" ");
				if (pos > 0) label = label.substring(0, pos); 
			}
		}
		this.label = label;
	}
	
	public static void main(String args[]) {
		Concept c = new Concept("wikicategory Living people", "", new Source("dbpedia",null,0));
		System.out.println(c.label);
	}
}
