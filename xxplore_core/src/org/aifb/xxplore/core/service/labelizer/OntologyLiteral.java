package org.aifb.xxplore.core.service.labelizer;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xmedia.oms.model.api.INamedConcept;
import org.xmedia.oms.model.api.IProperty;

public class OntologyLiteral extends OntologyElement {
	
	private String _label;
	private String _labelFullString;
	private IProperty _property;
	private Set<String> _labelStrings;
	private Set<INamedConcept> _types;
	
	public OntologyLiteral(String label, IProperty property) {
		_label = label;
		_property = property;
		_labelFullString = findLabelFullString(label);
		_labelStrings = new LinkedHashSet<String>();
		List<String> labelStrings = (ArrayList<String>) findLabelStrings(_labelFullString);
		for (String labelString : labelStrings) {
			addLabelString(labelString);
		}
		_types = new LinkedHashSet<INamedConcept>();
	}
	
	public OntologyLiteral(String label, IProperty property, Set<INamedConcept> types) {
		_label = label;
		_labelFullString = findLabelFullString(label);
		_property = property;
		List<String> labelStrings = (ArrayList<String>) findLabelStrings(_labelFullString);
		for (String labelString : labelStrings) {
			addLabelString(labelString);
		}
		_types = types;
	}
	
	public String getLabel() {
		return _label;
	}
	
	public String getLabelFullString() {
		return _labelFullString;
	}
	
	public Set<String> getLabelStrings() {
		return _labelStrings;
	}
	
	public Set<INamedConcept> getTypes() {
		return _types;
	}
	
	public IProperty getProperty() {
		return _property;
	}
	
	public void addType(INamedConcept type) {
		_types.add(type);
	}
	
	public String findLabelFullString(String label) {
		String labelTreated = label.replace("(", " ").replace(")", " ").replace(" / ", " ").replace(",", " ");
		String[] labelSplitted = labelTreated.split("[ ]+");
		labelTreated = "";
		for (String lfsWord : labelSplitted)
			labelTreated = labelTreated + lfsWord + " ";
		labelTreated = labelTreated.substring(0, labelTreated.length() - 1);

		return LabelizerEnvironment.pruneString(labelTreated);
	}
	
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

	public void addLabelString(String labelString) {
		_labelStrings.add(labelString);
	}
	
	public void labelElement(IndexStructure indexStructure) {
		for (INamedConcept type : getTypes())
			indexStructure.addLabel(getLabel(), type.getLabel(), LabelizerEnvironment.K_NODE, getProperty().getLabel());
		
		for (String labelString : getLabelStrings())
			indexStructure.addLabel(labelString, getLabel(), LabelizerEnvironment.SUBLABEL);
	}

	public void printElement() {
/*		System.out.print("Individual - IFS: ");
		for (String key : _labelFullStrings.keySet()) {
			String indexFullString = _labelFullStrings.get(key);
			System.out.print(indexFullString + "(" + key.substring(key.indexOf("#") + 1) +  "); ");
		}
		System.out.println("(" + LabelizerEnvironment.URI + ": " + getURI() + ")");*/
	}

	public void writeElementInXMLFiles(Document configFileDocument, Document labelsFileDocument, Element configFileSuperElement, Element labelsFileSuperElement) {
		Element individualElement = labelsFileDocument.createElement(LabelizerEnvironment.LITERAL);
		individualElement.setAttribute(LabelizerEnvironment.LABEL, _label);
		individualElement.setAttribute("Property", _property.getLabel());
		for (INamedConcept type : _types) {
			Element typeElement = labelsFileDocument.createElement("Type");
			typeElement.setAttribute("Type", type.getLabel());
			individualElement.appendChild(typeElement);
		}
		for (String labelString : _labelStrings) {
			Element labelStringElement = labelsFileDocument.createElement(LabelizerEnvironment.LABEL_STRING);
			labelStringElement.setTextContent(labelString);
			individualElement.appendChild(labelStringElement);
		}
		/*for (String key : _labelFullStrings.keySet()) {
			Element lfsElement = labelsFileDocument.createElement(LabelizerEnvironment.LABEL_FULL_STRING);
			lfsElement.setTextContent(_labelFullStrings.get(key));
			lfsElement.setAttribute(LabelizerEnvironment.DATA_PROPERTY, key);
			individualElement.appendChild(lfsElement);
			if (_labelStrings.containsKey(_labelFullStrings.get(key))) {
				for (String labelString : _labelStrings.get(_labelFullStrings.get(key))) {
					Element lsElement = labelsFileDocument.createElement(LabelizerEnvironment.LABEL_STRING);
					lsElement.setTextContent(labelString);
					lsElement.setAttribute(LabelizerEnvironment.LABEL_FULL_STRING, _labelFullStrings.get(key));
					individualElement.appendChild(lsElement);
				}
			}
		}*/
		labelsFileSuperElement.appendChild(individualElement);
	}
}
