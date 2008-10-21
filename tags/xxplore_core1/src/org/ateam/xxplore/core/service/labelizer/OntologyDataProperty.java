package org.ateam.xxplore.core.service.labelizer;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xmedia.oms.model.api.IProperty;

public class OntologyDataProperty extends OntologyTBoxElement {

	private String _label;
	private IProperty _dataProperty;
	private String _range;
	
	/**
	 * Constructor of the OntologyDataProperty class.
	 * @param uri the URI of the data property
	 */
	public OntologyDataProperty(IProperty dataProperty) {
		super(dataProperty.getUri());
		_dataProperty = dataProperty;
		_range = null;
		String uri = dataProperty.getUri();
		_label = findTBoxLabelFullString(uri.substring(getNamespaceEnd(uri) + 1));
	}
	
	/**
	 * Constructor of the OntologyDataProperty class.
	 * @param uri the URI of the data property
	 * @param range the range datatype of the data property
	 */
	public OntologyDataProperty(IProperty dataProperty, String range) {
		super(dataProperty.getUri());
		_dataProperty = dataProperty;
		_range = range;
		String uri = dataProperty.getUri();
		_label = findTBoxLabelFullString(uri.substring(getNamespaceEnd(uri) + 1));
	}
	
	private int getNamespaceEnd(String uri) {
		for (int i=uri.length()-1;i>=0;i--) {
			char c=uri.charAt(i);
			if (c=='#' || c==':')
				return i;
			if (c=='/') {
				if (i>0 && uri.charAt(i-1)=='/')
					return -1;
				return i;
			}
		}
		return -1;
	}
	
	public String findTBoxLabelFullString(String indexName) {
		String labelFullString = new String();
		List<String> words = new ArrayList<String>();
		int wordStart = 0;
		int indexNameLength = indexName.length();

		for (int i = 0; i < indexNameLength; i++) {
			if (i == indexNameLength - 1)
				words.add(indexName.substring(wordStart, i + 1));
			else {
				char c = indexName.charAt(i);
				if (c == ' ' || c == '_' || c == '-') {
					if (wordStart < i) 
						words.add(indexName.substring(wordStart, i));
					wordStart = i + 1;
				}
				if (c >= 'A' && c <= 'Z' && wordStart < i) {
					char nextChar = indexName.charAt(i + 1);
					if (nextChar >= 'a' && nextChar <= 'z') {
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
	
	public IProperty getDataProperty() {
		return _dataProperty;
	}
	
	/**
	 * Get the range datatype of the data property (or null if there is no range datatype defined).
	 * @return the range datatype of the data property
	 */
	public String getRange() {
		return _range;
	}

	/**
	 * Add a range datatype associated with the data property (if there is no range defined yet).
	 * @param range the range datatype to be associated
	 */
	public void addRange(String range) {
		if (hasRange() == false)
			_range = range;
	}

	/**
	 * Check if the data property already has a range datatype defined for it.
	 * @return true if the data property has a range datatype defined and false otherwise
	 */
	private boolean hasRange() {
		return (getRange() != null) ? true : false;
	}
	
	/**
	 * Create the labels (label full-strings and label strings) for the data property in an indexing
	 * structure.
	 */
	public void labelElement(IndexStructure indexStructure) {
//		indexStructure.addLabel(_labelFullString, _uri, LabelizerEnvironment.DATA_PROPERTY);
//		for (String labelString : _labelStrings)
//			indexStructure.addLabel(labelString, _labelFullString, LabelizerEnvironment.SUBLABEL);
	}
	
	/**
	 * Write a representation (XML DOM element) of the ontology data property in the XML config and
	 * labels file.
	 */
	public void writeElementInXMLFiles(Document configFileDocument, Document labelsFileDocument, Element configFileSuperElement, Element labelsFileSuperElement) {
		
		/* Create the labels file DOM element representation for the data property (with URI, label full-string
		 * and label strings) */
		Element labelsFileDataPropertyElement = labelsFileDocument.createElement(LabelizerEnvironment.DATA_PROPERTY);
		labelsFileDataPropertyElement.setAttribute(LabelizerEnvironment.LABEL, _label);
		Element lfsElement = labelsFileDocument.createElement(LabelizerEnvironment.LABEL_FULL_STRING);
		lfsElement.setTextContent(_labelFullString);
		labelsFileDataPropertyElement.appendChild(lfsElement);
		for (String labelString : _labelStrings) {
			Element lsElement = labelsFileDocument.createElement(LabelizerEnvironment.LABEL_STRING);
			lsElement.setTextContent(labelString);
			lsElement.setAttribute(LabelizerEnvironment.LABEL_FULL_STRING, _labelFullString);
			labelsFileDataPropertyElement.appendChild(lsElement);
		}
		labelsFileSuperElement.appendChild(labelsFileDataPropertyElement);
	}

	/**
	 * Print a friendly version of the ontology data property representation.
	 */
	public void printElement() {
		System.out.print("Data property: " + getIndexName() + " ");
		System.out.print("(" + LabelizerEnvironment.URI + ": " + getURI() + ")");
		System.out.println(" --> Range: " + getRange());
	}
}
