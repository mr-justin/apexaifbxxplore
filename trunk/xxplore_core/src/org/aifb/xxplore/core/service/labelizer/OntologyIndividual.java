package org.aifb.xxplore.core.service.labelizer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xmedia.oms.model.api.INamedConcept;
import org.xmedia.oms.model.impl.NamedConcept;


public class OntologyIndividual extends OntologyElement {

	private Map<String,String> _labelFullStrings;
	private Map<String,List<String>> _labelStrings;
	private List<INamedConcept> _types;

	/**
	 * Constructor of the OntologyIndividual class. In particular, this method finds all the appropriate
	 * label strings, given the label full-strings that label the individual (call of method findLabelStirngs).
	 * @param uri the URI of the ontology individual
	 * @param labelFullStrings the label full-strings that label the individual
	 */
	public OntologyIndividual(String uri, LinkedHashMap<String, String> labelFullStrings) {
		super(uri);
		_labelFullStrings = labelFullStrings;
		_labelStrings = new LinkedHashMap<String,List<String>>();
		Set<String> keys = _labelFullStrings.keySet();
		for (String key : keys) {
			ArrayList<String> labelStrings = (ArrayList<String>) findLabelStrings(_labelFullStrings.get(key));
			for (String labelString : labelStrings) {
				addLabelString(labelString, _labelFullStrings.get(key));
			}
		}
		
		_types = new ArrayList<INamedConcept>();
	}

	/**
	 * Constructor of the OntologyIndividual class.
	 * @param uri the URI of the ontology individual
	 */
	public OntologyIndividual(String uri) {
		super(uri);
		_labelFullStrings = new LinkedHashMap<String, String>();
		_labelStrings = new LinkedHashMap<String,List<String>>();
		
		_types = new ArrayList<INamedConcept>();
	}

	/**
	 * Get the all the label full-strings, mapped according to the URI of the property associated with
	 * each string.
	 * @return the list of label full-strings that label the individual
	 */
	public Map<String, String> getLabelFullStrings() {
		return _labelFullStrings;
	}

	/**
	 * Get all the label strings, mapped according to the label full-string from which it was found.
	 * @return the list of label strings that label the individual
	 */
	public Map<String,List<String>> getLabelStrings() {
		return _labelStrings;
	}
	
	public ArrayList<INamedConcept> getTypes() {
		return (ArrayList<INamedConcept>)_types;
	}

	/**
	 * Add a label full-string to label the individual; this method adds the label full-strings and the
	 * automatically found label strings from the first one (call of method findLabelStrings).
	 * @param property the data property associated with the label full-string
	 * @param labelFullString the label full-string to add to the individual
	 */
	public void addLabelFullString(String property, String labelFullString) {
		String lfsPruned = LabelizerEnvironment.pruneString(labelFullString);
		String lfsParsed = findABoxFullLabelString(lfsPruned);
		String lfsAfterParsed = afterFindingLabelStrings(lfsParsed);
		if (_labelFullStrings.containsKey(property) == false) {
			_labelFullStrings.put(property, lfsAfterParsed);
			ArrayList<String> labelStrings = (ArrayList<String>) findLabelStrings(lfsParsed);
			for (String labelString : labelStrings) {
				addLabelString(labelString, lfsAfterParsed);
			}
		}
	}

	/**
	 * Add a label string (found from a label full-string) to label the individual.
	 * @param labelString the label string to add to the individual
	 * @param labelFullString the label full-string from which the label string was found
	 */
	public void addLabelString(String labelString, String labelFullString) {
		List<String> labelStrings = _labelStrings.get(labelFullString);
		if (labelStrings == null) {
			labelStrings = new ArrayList<String>();
			_labelStrings.put(labelFullString, labelStrings);
		}
		labelStrings.add(labelString);
	}
	
	public void addType(INamedConcept concept) {
		if (_types.contains(concept) == false)
			_types.add(concept);
	}

	/**
	 * Remove a label full-string so it doesn't label the individual anymore; this method removes all the
	 * label strings found from the label full-string (call of method removeLabelStrings).
	 * @param property the data property associated to the label full-string to remove
	 */
	public void removeLabelFullString(String property) {
		if (_labelFullStrings.containsKey(property) == true) {
			String lfsToRemove = _labelFullStrings.get(property);
			_labelFullStrings.remove(property);
			removeLabelStrings(lfsToRemove);
		}
	}

	/**
	 * Remove all the label strings found from a label full-string.
	 * @param labelFullString the label full-string associated with all the label strings to remove
	 */
	private void removeLabelStrings(String labelFullString) {
		if (_labelStrings.containsKey(labelFullString) == true)
			_labelStrings.remove(labelFullString);
	}

	public String findABoxFullLabelString(String indexString) {
		String lfsTreated = indexString.replace("(", " ").replace(")", " ").replace(" / ", " ").replace(",", " ");
		String[] lfsSplitted = lfsTreated.split("[ ]+");
		lfsTreated = "";
		for (String lfsWord : lfsSplitted)
			lfsTreated = lfsTreated + lfsWord + " ";
		lfsTreated = lfsTreated.substring(0, lfsTreated.length() - 1);

		return LabelizerEnvironment.pruneString(lfsTreated);
	}

	public String afterFindingLabelStrings(String labelFullString) {
		return labelFullString.replace(" - ", " ").replace(": ", " ");
	}

	/**
	 * Create the labels (label full-strings and label strings) for the individual in an indexing
	 * structure.
	 */
	public void labelElement(IndexStructure indexStructure) {
		for (String dp : _labelFullStrings.keySet()) {
			String labelFullString = _labelFullStrings.get(dp);
			indexStructure.addLabel(labelFullString, _uri, LabelizerEnvironment.INDIVIDUAL);
			if (_labelStrings.containsKey(labelFullString)) {
				for (String labelString : _labelStrings.get(labelFullString))
					indexStructure.addLabel(labelString, labelFullString, LabelizerEnvironment.SUBLABEL);
			}
		}
	}
	
	/**
	 * Write a representation (XML DOM element) of the ontology individual in the XML config and labels
	 * file. 
	 */
	public void writeElementInXMLFiles(Document configFileDocument, Document labelsDocument, Element configFileSuperElement, Element labelsFileSuperElement) {
		Element individualElement = labelsDocument.createElement(LabelizerEnvironment.INDIVIDUAL);
		individualElement.setAttribute(LabelizerEnvironment.URI, _uri);
		for (String key : _labelFullStrings.keySet()) {
			Element lfsElement = labelsDocument.createElement(LabelizerEnvironment.LABEL_FULL_STRING);
			lfsElement.setTextContent(_labelFullStrings.get(key));
			lfsElement.setAttribute(LabelizerEnvironment.DATA_PROPERTY, key);
			individualElement.appendChild(lfsElement);
			if (_labelStrings.containsKey(_labelFullStrings.get(key))) {
				for (String labelString : _labelStrings.get(_labelFullStrings.get(key))) {
					Element lsElement = labelsDocument.createElement(LabelizerEnvironment.LABEL_STRING);
					lsElement.setTextContent(labelString);
					lsElement.setAttribute(LabelizerEnvironment.LABEL_FULL_STRING, _labelFullStrings.get(key));
					individualElement.appendChild(lsElement);
				}
			}
		}
		labelsFileSuperElement.appendChild(individualElement);
	}

	/**
	 * Print a friendly version of the ontology individual representation.
	 */
	public void printElement() {
		System.out.print("Individual - IFS: ");
		for (String key : _labelFullStrings.keySet()) {
			String indexFullString = _labelFullStrings.get(key);
			System.out.print(indexFullString + "(" + key.substring(key.indexOf("#") + 1) +  "); ");
		}
		System.out.println("(" + LabelizerEnvironment.URI + ": " + getURI() + ")");
	}
}
