package org.ateam.xxplore.core.service.labelizer;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xmedia.oms.model.api.INamedConcept;
import org.xmedia.oms.model.api.IProperty;

public class OntologyObjectProperty extends OntologyTBoxElement {

	private String _label;
	private IProperty _objectProperty;
	private List<OntologyPropertyMember> _propertyMembers;

	/**
	 * The constructor of the OntologyObjectProperty class.
	 * @param uri the URI of the object property
	 */
	public OntologyObjectProperty(IProperty objectProperty) {
		super(objectProperty.getUri());
		_objectProperty = objectProperty;
		_propertyMembers = new ArrayList<OntologyPropertyMember>();
		String uri = objectProperty.getUri();
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

	public IProperty getObjectProperty() {
		return _objectProperty;
	}

	public List<OntologyPropertyMember> getPropertyMembers() {
		return _propertyMembers;
	}
	
	public OntologyPropertyMember getPropertyMember(INamedConcept source, INamedConcept target) {
		OntologyPropertyMember propertyMember = new OntologyPropertyMember(source, target);
		int index = getPropertyMembers().indexOf(propertyMember);
		if (index > -1)
			return getPropertyMembers().get(index);
		return null;
	}

	public void addPropertyMember(INamedConcept source, INamedConcept target) {
		if (getPropertyMember(source, target) == null)
			_propertyMembers.add(new OntologyPropertyMember(source, target));
	}
	
	public void increasePMNumInstances(INamedConcept source, INamedConcept target) {
		OntologyPropertyMember propertyMember = getPropertyMember(source, target);
		if (propertyMember != null)
			propertyMember.increaseNumInstances();
	}

	/**
	 * Create the labels (label full-strings and label strings) for the object property in an indexing
	 * structure.
	 */
	public void labelElement(IndexStructure indexStructure) {
		indexStructure.addLabel(getLabel(), getLabel(), LabelizerEnvironment.R_EDGE);
		for (String labelString : getLabelStrings())
			indexStructure.addLabel(labelString, getLabel(), LabelizerEnvironment.SUBLABEL);
	}

	/**
	 * Write a representation (XML DOM element) of the ontology object property in the XML config and
	 * labels file.
	 */
	public void writeElementInXMLFiles(Document configFileDocument, Document labelsFileDocument, Element configFileSuperElement, Element labelsFileSuperElement) {

		/* Create the labels file DOM element representation for the object property (with URI, label
		 * full-string and label strings) */
		Element labelsFileObjectPropertyElement = labelsFileDocument.createElement(LabelizerEnvironment.OBJECT_PROPERTY);
		labelsFileObjectPropertyElement.setAttribute(LabelizerEnvironment.LABEL, _label);
		Element lfsElement = labelsFileDocument.createElement(LabelizerEnvironment.LABEL_FULL_STRING);
		lfsElement.setTextContent(_labelFullString);
		labelsFileObjectPropertyElement.appendChild(lfsElement);
		for (String labelString : _labelStrings) {
			Element lsElement = labelsFileDocument.createElement(LabelizerEnvironment.LABEL_STRING);
			lsElement.setTextContent(labelString);
			lsElement.setAttribute(LabelizerEnvironment.LABEL_FULL_STRING, _labelFullString);
			labelsFileObjectPropertyElement.appendChild(lsElement);
		}
		for (OntologyPropertyMember propertyMember : _propertyMembers) {
			Element propertyMemberElement = labelsFileDocument.createElement("PropertyMembers");
			propertyMemberElement.setAttribute("Source", propertyMember.getSource().getLabel());
			propertyMemberElement.setAttribute("Target", propertyMember.getTarget().getLabel());
			propertyMemberElement.setAttribute("NumInstances", new String("" + propertyMember.getNumInstances()));
			labelsFileObjectPropertyElement.appendChild(propertyMemberElement);
		}
		labelsFileSuperElement.appendChild(labelsFileObjectPropertyElement);
	}

	/**
	 * Print a friendly version of the ontology object property representation.
	 */
	public void printElement() {
		System.out.print("Object property: " + getIndexName() + " ");
		System.out.println("(" + LabelizerEnvironment.URI + ": " + getURI() + ")");
	}
}
