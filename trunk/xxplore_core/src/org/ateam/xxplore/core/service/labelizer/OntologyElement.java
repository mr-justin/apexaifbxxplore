package org.ateam.xxplore.core.service.labelizer;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public abstract class OntologyElement {

	protected String _label;
	protected String _uri;

	/**
	 * Constructor of the OntologyElement class.
	 * @param uri the URI of the ontology element
	 */
	protected OntologyElement(/*String uri*/) {
		_uri = new String();
		//_uri = uri;
	}

	/**
	 * Get the URI of the ontology element.
	 * @return the URI of the ontology element
	 */
	public String getURI() {
		return _uri;
	}

	/**
	 * Overriding method for Object.equals (compare the ontology element URI to the obj parameter, which
	 * must be a string.
	 */
	public boolean equals(Object obj) {
		return getURI().equals(obj.toString());
	}
	
	/**
	 * Get the label string that should be taken into account from a label full-string of an ontology
	 * element.
	 * @param labelFullString the label full-string to be explored
	 * @return the list of label string found for that label full-string
	 */
	public List<String> findLabelStrings(String labelFullString) {
		List<String> labelStrings = new ArrayList<String>();
		
		/* Split the label full-string in atomic words (possible label strings) */
		String[] words = labelFullString.split("[ ]");
		
		/* If the string is not big (number of words = 2 or 3), consider each word as a label string */
		if (words.length >= 2 && words.length <= 3) {
			for (String word : words) {
				if (word.length() > 0 && word.charAt(word.length() - 1) != '.')
					labelStrings.add(word);
			}
		}
		
		/* If the string is big (number of words > 3), ... */
		else if (words.length > 3) {
			
			/* Split the string consider the following word connections: " - " and ": " */
			String[] lfsSplitted = labelFullString.split("( \\- )|(: )");
			
			/* If there is a connection of the previous type, consider the first part of the label
			 * full-string and split the words in the first part in label strings */
			if (lfsSplitted.length > 1) {
				labelStrings.add(lfsSplitted[0]);
				labelStrings.addAll(findLabelStrings(lfsSplitted[0]));
			}
		}

		return labelStrings;
	}

	/**
	 * Create the labels (label full-strings and label strings) for the element in an indexing structure - 
	 * to be defined in the subclasses.
	 * @param indexStructure the indexing structure in which the labels are to be added
	 */
	public abstract void labelElement(IndexStructure indexStructure);

	/**
	 * Write a representation (XML DOM element) of the ontology element in the XML config and labels
	 * file - to be defined in the subclasses.
	 * @param configFileDocument the DOM document representation for the XML config file
	 * @param labelsDocument the DOM document representation for the XML labels file
	 * @param superElement the super element that the ontology element DOM representation is to be attached
	 */
	public abstract void writeElementInXMLFiles(Document configFileDocument, Document labelsFileDocument, Element configFileSuperElement, Element labelsFileSuperElement);
	
	/**
	 * Print a friendly version of the ontology element representation - to be defined in the subclasses.
	 */
	public abstract void printElement();
}
