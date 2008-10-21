package org.ateam.xxplore.core.service.labelizer;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xmedia.oms.model.api.INamedConcept;

public class OntologyConcept extends OntologyTBoxElement {
	
	private String _label;
	private INamedConcept _concept;
	private int _numInstances;
	
	public OntologyConcept (INamedConcept concept) {
		super(concept.getUri());
		_concept = concept;
		_numInstances = 0;
		String uri = concept.getUri();
		_label = uri.substring(getNamespaceEnd(uri) + 1);
		//_label = findTBoxLabelFullString(uri.substring(getNamespaceEnd(uri) + 1));
	}
		
	private int getNamespaceEnd(String uri) {
		for (int i=uri.length()-1;i>=0;i--) {
			char c=uri.charAt(i);
			if ((c=='#') || (c==':')) {
				return i;
			}
			if (c=='/') {
				if ((i>0) && (uri.charAt(i-1)=='/')) {
					return -1;
				}
				return i;
			}
		}
		return -1;
	}
	
	@Override
	public String findTBoxLabelFullString(String indexName) {
		String labelFullString = new String();
		List<String> words = new ArrayList<String>();
		int wordStart = 0;
		int indexNameLength = indexName.length();

		for (int i = 0; i < indexNameLength; i++) {
			if (i == indexNameLength - 1) {
				words.add(indexName.substring(wordStart, i + 1));
			} else {
				char c = indexName.charAt(i);
				if ((c == ' ') || (c == '_') || (c == '-')) {
					if (wordStart < i) {
						words.add(indexName.substring(wordStart, i));
					}
					wordStart = i + 1;
				}
				if ((c >= 'A') && (c <= 'Z') && (wordStart < i)) {
					char nextChar = indexName.charAt(i + 1);
					if ((nextChar >= 'a') && (nextChar <= 'z')) {
						words.add(indexName.substring(wordStart, i));
						wordStart = i;
					}
				}
			}
		}

		for (String word : words) {
			labelFullString = labelFullString.concat(word + " ");
		}
		labelFullString = labelFullString.substring(0, labelFullString.length() - 1).toLowerCase();
		return pruneString(labelFullString);
	}
	
	private String pruneString(String str) {
		return str.replace("\"", "").toLowerCase();
	}
	
	public String getLabel() {
		return _label;
	}
	
	public INamedConcept getConcept() {
		return _concept;
	}
	
	public int getNumInstances() {
		return _numInstances;
	}
	
	public void setConcept(INamedConcept concept) {
		_concept = concept;
	}
	
	public int increaseNumInstances() {
		_numInstances++;
		return _numInstances;
	}
	
	@Override
	public void labelElement(IndexStructure indexStructure) {
		indexStructure.addLabel(getLabelFullString(), getLabel(), LabelizerEnvironment.C_NODE);
		for (String labelString : getLabelStrings()) {
			indexStructure.addLabel(labelString, getLabelFullString(), LabelizerEnvironment.SUBLABEL);
		}
	}
	
//	private List<String> _indexingDPs;
//	private Map<String,String> _objectProperties;
//	
//	
//	
//	/**
//	 * Constructor of the OntologyConcept class.
//	 * @param uri the URI of the ontology concept
//	 */
//	public OntologyConcept(String uri) {
//		super(uri);
//		_indexingDPs = new ArrayList<String>();
//		_objectProperties = new LinkedHashMap<String,String>();
//	}
//	
//	/**
//	 * Constructor of the OntologyConcept class.
//	 * @param uri the URI of the ontology concept
//	 * @param indexingDPs the list of indexing data properties known for the concept
//	 */
//	public OntologyConcept(String uri, ArrayList<String> indexingDPs) {
//		super(uri);
//		_indexingDPs = indexingDPs;
//		_objectProperties = new LinkedHashMap<String,String>();
//	}
//
//	/**
//	 * Get the list of indexing data properties known for the concept.
//	 * @return the list of indexing data properties
//	 */
//	public ArrayList<String> getIndexingDPs() {
//		return (ArrayList<String>)_indexingDPs;
//	}
//	
//	public LinkedHashMap<String,String> getObjectProperties() {
//		return (LinkedHashMap<String,String>)_objectProperties;
//	}
//	
//	
//	/**
//	 * Add a data property as a (possible) indexing data property of the concept.
//	 * @param uri the URI of the data property
//	 */
//	public void addIndexingDP(String uri) {
//		if (_indexingDPs.contains(uri) == false)
//			_indexingDPs.add(uri);
//	}
//	
//	public void addObjectProperty(String uri, String target) {
//		if (_objectProperties.containsKey(uri) == false)
//			_objectProperties.put(uri, target);
//	}
//	
//	/**
//	 * Remove the data property from the list of (possible) indexing data properties of the concept.
//	 * @param uri the URI of the data property to be removed
//	 */
//	public void removeIndexingDP(String uri) {
//		if (_indexingDPs.contains(uri) == true)
//			_indexingDPs.remove(uri);
//	}
//	
//	/**
//	 * Create the labels (label full-strings and label strings) for the concept in an indexing structure.
//	 */
//	public void labelElement(IndexStructure indexStructure) {
//		indexStructure.addLabel(_labelFullString, _uri, LabelizerEnvironment.CONCEPT);
//		for (String labelString : _labelStrings)
//			indexStructure.addLabel(labelString, _labelFullString, LabelizerEnvironment.SUBLABEL);
//	}
	
	/**
	 * Write a representation (XML DOM element) of the ontology concept in the XML config and labels
	 * file.
	 */
	@Override
	public void writeElementInXMLFiles(Document configFileDocument, Document labelsFileDocument, Element configFileSuperElement, Element labelsFileSuperElement) {
		
		/* Create the config file DOM element representation for the concept (with URI and indexing data
		 * properties possible for the concept) */
		Element configFileConceptElement = configFileDocument.createElement(LabelizerEnvironment.CONCEPT);
		configFileConceptElement.setAttribute(LabelizerEnvironment.LABEL, _label);
		configFileConceptElement.setAttribute("NumInstances", new String("" + _numInstances));
		/*for (String index : _indexingDPs) {
			Element indexElement = configFileDocument.createElement(LabelizerEnvironment.INDEXING_DP);
			indexElement.setTextContent(index);
			configFileConceptElement.appendChild(indexElement);
		}*/
		configFileSuperElement.appendChild(configFileConceptElement);
		
		/* Create the labels file DOM element representation for the concept (with URI, label full-string and
		 * label strings) */
		/*Element labelsFileConceptElement = labelsFileDocument.createElement(LabelizerEnvironment.CONCEPT);
		labelsFileConceptElement.setAttribute(LabelizerEnvironment.URI, _uri);
		Element lfsElement = labelsFileDocument.createElement(LabelizerEnvironment.LABEL_FULL_STRING);
		lfsElement.setTextContent(_labelFullString);
		labelsFileConceptElement.appendChild(lfsElement);
		for (String labelString : _labelStrings) {
			Element lsElement = labelsFileDocument.createElement(LabelizerEnvironment.LABEL_STRING);
			lsElement.setTextContent(labelString);
			lsElement.setAttribute(LabelizerEnvironment.LABEL_FULL_STRING, _labelFullString);
			labelsFileConceptElement.appendChild(lsElement);
		}
		labelsFileSuperElement.appendChild(labelsFileConceptElement);*/
	}
	
	/**
	 * Print a friendly version of the ontology concept representation.
	 */
	@Override
	public void printElement() {
//		System.out.print(LabelizerEnvironment.CONCEPT + ": " + getIndexName() + " ");
//		if (_indexingDPs.isEmpty() == false) {
//			System.out.print("--> Indexed by: ");
//			for (String index : getIndexingDPs())
//				System.out.print(index.substring(index.indexOf("#") + 1) + "; ");
//		}
//		if (_objectProperties.isEmpty() == false) {
//			System.out.print(";; --> OPs: ");
//			for (String uri : _objectProperties.keySet()) {
//				System.out.print(uri.substring(uri.indexOf("#") + 1) + " (" + _objectProperties.get(uri).substring(uri.indexOf("#") + 1) + "); ");
//			}
//		}
//		System.out.println("(" + LabelizerEnvironment.URI + ": " + _uri + ")");
	}
}
