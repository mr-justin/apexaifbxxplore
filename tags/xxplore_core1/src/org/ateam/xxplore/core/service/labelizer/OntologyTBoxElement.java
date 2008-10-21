package org.ateam.xxplore.core.service.labelizer;

import java.util.ArrayList;
import java.util.List;

public abstract class OntologyTBoxElement extends OntologyElement {

	protected String _indexName;
	protected String _labelFullString;
	protected List<String> _labelStrings;

	/**
	 * Constructor of the OntologyTBoxElement class
	 * @param uri the URI of the ontology T-Box element
	 */
	public OntologyTBoxElement(String uri) {
		super(/*uri*/);
		_indexName = uri.substring(getNamespaceEnd(uri) + 1);
		_labelFullString = findTBoxLabelFullString(_indexName);
		_labelStrings = findLabelStrings(_labelFullString);
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

	/**
	 * Get the index name (name without the namespace) of the ontology T-Box element.
	 * @return the index name of the ontology T-Box element
	 */
	public String getIndexName() {
		return _indexName;
	}

	/**
	 * Get the label full-string (index name with the correct white spaces) of the ontology T-Box
	 * element.
	 * @return the label full-string of the ontology T-Box element
	 */
	public String getLabelFullString() {
		return _labelFullString;
	}

	/**
	 * Get the list of label strings (label full-string divided into words) of the ontology T-Box element.
	 * @return the label strings of the ontology T-Box element
	 */
	public List<String> getLabelStrings() {
		return _labelStrings;
	}
	
	/**
	 * Add a label string indexing the ontology T-Box element.
	 * @param labelString
	 */
	public void addLabelString(String labelString) {
		if (_labelStrings.contains(labelString) == false)
			_labelStrings.add(labelString);
	}

	/**
	 * Find the label full-string of the ontology T-Box element from its index name; in practice,
	 * this method places white spaces between a non-capitalized and a capitalized letter of the index
	 * name (e.g. ProjectReport --> Project Report) and between the ocurrence of a last and before-last
	 * capitalized letters in the index name (e.g. PhDStudent --> PhD Student).
	 * @param indexName the index name of the ontology T-Box element
	 * @return the label full-string of the T-Box element
	 */
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

}
