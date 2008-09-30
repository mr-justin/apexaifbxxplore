package org.ateam.xxplore.core.service.labelizer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class IndexStructure {

	private Map<String,OntologyConcept> _concepts;
	private Map<String,OntologyObjectProperty> _objectProperties;
	private Map<String,OntologyDataProperty> _dataProperties;
	private Map<String,OntologyIndividual> _individuals;
	private Map<String,OntologyLiteral> _literals;
	private Map<String,OntologyLabel> _labels;
	
	private int _numLiteralInstances;
	private int _numPropertyMemberInstances;

	/*private MorphologicalProcessor _mp = null;
	private List WordNet_POSs = null;*/

	/**
	 * The constructor of the indexing structure (initializes all the sub-structures associated with
	 * each ontology element type)
	 */
	public IndexStructure() {
		_concepts = new LinkedHashMap<String,OntologyConcept>();
		_objectProperties = new LinkedHashMap<String,OntologyObjectProperty>();
		_dataProperties = new LinkedHashMap<String,OntologyDataProperty>();
		_individuals = new LinkedHashMap<String,OntologyIndividual>();
		_literals = new LinkedHashMap<String,OntologyLiteral>();
		_labels = new LinkedHashMap<String,OntologyLabel>();
	}
	
	public int getNumLiteralInstances() {
		return _numLiteralInstances;
	}
	
	public int getNumPropertyMemberInstances() {
		return _numPropertyMemberInstances;
	}
	
	public int countNumLiteralInstances() {
		int numInstances = 0;
		for (String label : _concepts.keySet()) {
			OntologyConcept ontologyConcept = _concepts.get(label);
			numInstances = numInstances + ontologyConcept.getNumInstances();
		}
		_numLiteralInstances = numInstances;
		return numInstances;
	}
	
	public int countNumPropertyMemberInstances() {
		int numInstances = 0;
		for (String label : _objectProperties.keySet()) {
			OntologyObjectProperty objectProperty = _objectProperties.get(label);
			for (OntologyPropertyMember propertyMember : objectProperty.getPropertyMembers())
				numInstances = numInstances + propertyMember.getNumInstances();
		}
		_numPropertyMemberInstances = numInstances;
		return numInstances;
	}

	/**
	 * Get the list of all concepts stored in the indexing structure
	 * @return the list of concepts stored
	 */
	public Map<String,OntologyConcept> getConcepts() {
		return _concepts;
	}

	/**
	 * Get a concept stored in the indexing structure
	 * @param uri the uri of the concept
	 * @return the representation of the matching concept or null if it doesn't exist in the structure
	 */
	public OntologyConcept getConcept(String uri) {
		return _concepts.get(uri);
	}

	/**
	 * Get the list of all object properties stored in the indexing structure
	 * @return the list of object properties stored
	 */
	public Map<String,OntologyObjectProperty> getObjectProperties() {
		return _objectProperties;
	}
	
	public OntologyObjectProperty getObjectProperty(String uri) {
		return _objectProperties.get(uri);
	}

	/**
	 * Get the list of all data properties stored in the indexing structure
	 * @return the list of data properties stored
	 */
	public Map<String,OntologyDataProperty> getDataProperties() {
		return _dataProperties;
	}

	/**
	 * Get a data property stored in the indexing structure
	 * @param uri the uri of the data property
	 * @return the representation of the matching data property or null if it doesn't exist in the structure
	 */
	public OntologyDataProperty getDataProperty(String uri) {
		return _dataProperties.get(uri);
	}

	/**
	 * Get the list of all individuals stored in the indexing structure
	 * @return the list of individuals stored
	 */
	public Map<String,OntologyIndividual> getIndividuals() {
		return _individuals;
	}

	/**
	 * Get an individual stored in the indexing structure
	 * @param uri the uri of the individual
	 * @return the representation of the matching individual or null if it doesn't exist in the structure
	 */
	public OntologyIndividual getIndividual(String uri) {
		return _individuals.get(uri);
	}

	/**
	 * Get the structure mapping the ontology labels stored in the indexing structure with its
	 * corresponding string labels
	 * @return the structure mapping the string and ontology labels
	 */
	public Map<String,OntologyLabel> getLabels() {
		return _labels;
	}

	/**
	 * Add a representation of an ontology concept to the indexing structure
	 * @param concept the representation of the concept to add
	 */
	public void addConcept(OntologyConcept concept) {
		if (_concepts.containsKey(concept.getLabel()) == false)
			_concepts.put(concept.getConcept().getUri(), concept);
	}

	/**
	 * Add a representation of an ontology object property to the indexing structure
	 * @param objectProperty the representation of the object property to add
	 */
	public void addObjectProperty(OntologyObjectProperty objectProperty) {
		if (_objectProperties.containsKey(objectProperty.getObjectProperty().getUri()) == false)
			_objectProperties.put(objectProperty.getObjectProperty().getUri(), objectProperty);
	}

	/**
	 * Add a representation of an ontology data property to the indexing structure
	 * @param dataProperty the representation of the data property to add
	 */
	public void addDataProperty(OntologyDataProperty dataProperty) {
		if (_dataProperties.containsKey(dataProperty.getDataProperty().getUri()) == false)
			_dataProperties.put(dataProperty.getDataProperty().getUri(), dataProperty);
	}

	/**
	 * Add a representation of an individual to the indexing structure
	 * @param individual the representation of the individual to add
	 */
	public void addIndividual (OntologyIndividual individual) {
		if (_individuals.containsKey(individual.getURI()) == false)
			_individuals.put(individual.getURI(), individual);
	}
	
	public void addLiteral (OntologyLiteral literal) {
		if (_literals.containsKey(literal.getLabel()) == false)
			_literals.put(literal.getLabel(), literal);
	}

	/**
	 * Add an ontology label to the indexing structure
	 * @param label the label string of the ontology label
	 * @param sublabel the sub-label of the ontology label
	 * @param type the type of the sub-label (URI, sub-label string or alternative label)
	 */
	public void addLabel(String label, String sublabel, String type) {
		label = label.toLowerCase();
		OntologyLabel oLabel = _labels.get(label);
		if (oLabel == null) {
			oLabel = new OntologyLabel(label);
			_labels.put(label, oLabel);
		}
		oLabel.addSublabel(sublabel, type);
	}
	
	public void addLabel(String label, String sublabel, String type, String property) {
		label = label.toLowerCase();
		OntologyLabel oLabel = _labels.get(label);
		if (oLabel == null) {
			oLabel = new OntologyLabel(label);
			_labels.put(label, oLabel);
		}
		oLabel.addSublabel(sublabel, type, property);
	}

	/**
	 * Remove the data property as a possible label from all the concepts of the indexing structure and
	 * remove all the label strings already created matching this data property from all individuals
	 * @param dataPropertyURI the data property to be removed as a possible label
	 */
//	public void removeLabelDataProperty (String dataPropertyURI) {
//		for (String conceptURI : _concepts.keySet())
//			_concepts.get(conceptURI).removeIndexingDP(dataPropertyURI);
//		for (String individualURI : _individuals.keySet())
//			_individuals.get(individualURI).removeLabelFullString(dataPropertyURI);
//	}

	/**
	 * Create the labels for all the ontology elements currently existing in the indexing structure and
	 * store them in this structure; this method matches the T-Box elements label full-strings with
	 * possible synonyms in the WordNet database and adds these to the labeling structure as alternative
	 * labels.
	 * @return the mapping between the label string and the label structure (with the sublabels and its types)
	 */	
	public Map<String,OntologyLabel> labelize() {

		/* WordNetLFSs - stores the label full-string to be searched in the WordNet database */
		List<String> wordNetLFSs = new ArrayList<String>();

		/* OntologyElements - keeps all the ontology elements in one single structure for iteration */
		Map<String,Map<String,? extends OntologyElement>> ontologyElements = joinOntologyElementMaps();

		/* For each ontology element kept in the indexing structure ... */
		for (String type : ontologyElements.keySet()) {
			Map<String,? extends OntologyElement> elements = ontologyElements.get(type);
			for (String elementURI : elements.keySet()) {
				OntologyElement element = elements.get(elementURI);

				/* Add the labels of the element to the labeling structure */
				element.labelElement(this);

				/* If the element belongs to the T-Box, add the full label-string to WordNetLFSs to be
				 * searched in the WordNet database */
				if (element instanceof OntologyTBoxElement)
					wordNetLFSs.add(((OntologyTBoxElement)element).getLabelFullString());
			}
		}

		/* Add the labels (synonyms of the T-Box elements) found in WordNet database to the labeling structure */
		//addWordNetSynonyms(wordNetLFSs);

		return _labels;
	}

	/**
	 * Join all the ontology elements in one integrating mapping structure.
	 * @return the integrating mapping structure
	 */
	private Map<String, Map<String,? extends OntologyElement>> joinOntologyElementMaps() {
		Map<String,Map<String,? extends OntologyElement>> joinedMap = new LinkedHashMap<String,Map<String,? extends OntologyElement>>();
		joinedMap.put(LabelizerEnvironment.CONCEPT, _concepts);
		joinedMap.put(LabelizerEnvironment.OBJECT_PROPERTY, _objectProperties);
		joinedMap.put(LabelizerEnvironment.DATA_PROPERTY, _dataProperties);
		joinedMap.put(LabelizerEnvironment.INDIVIDUAL, _individuals);
		joinedMap.put(LabelizerEnvironment.LITERAL, _literals);
		return joinedMap;
	}

	/**
	 * Add to the labeling structure all the valid synonyms found in the WordNet database for all the
	 * found T-Box elements label full-strings.
	 * @param labels list of T-Box elements label full-strings
	 */
//	private void addWordNetSynonyms(List<String> labels) {
//
//		/* Initialize the API elements of the WordNet database */
//		try {
//			JWNL.initialize(new FileInputStream("c:/MasterThesis/file_properties.xml"));
//			_mp = Dictionary.getInstance().getMorphologicalProcessor();
//			WordNet_POSs = POS.getAllPOS();
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//			return;
//		} catch (JWNLException e) {
//			e.printStackTrace();
//			return;
//		}
//
//		/* For each label full-string ... */
//		for (String label : labels) {
//
//			/* Find the correct synonyms in the WordNet database */
//			Word[] synonymsFound = findWordNetSynonyms(label);
//
//			/* If WordNet database has a valid synonym for this label full-string, for each one found ... */
//			if (synonymsFound != null) {
//				for (Word synonym : synonymsFound) {
//
//					/* Replace the underscores of the synonym lemma by white spaces */
//					String synonymLemma = LabelizerEnvironment.replaceUnderscoresInString(synonym.getLemma());
//
//					/* If the labeling structure doesn't have a label corresponding to the synonym lemma,
//					 * add the label full-string and corresponding label-strings to it */
//					if (_labels.containsKey(synonymLemma) == false) {
//						System.out.println("Adding for synonym: " + synonymLemma + " the label " + label);
//						addLabel(synonymLemma, label, LabelizerEnvironment.ALT_LABEL);
//						for (String synonymLemmaWord : synonymLemma.split("[ ]"))
//							addLabel(synonymLemmaWord, synonymLemma, LabelizerEnvironment.ALT_LABEL);
//					}
//				}
//			}
//		}
//	}

	/**
	 * Find the valid synonyms (according to the labels existing in the ontology) of a label full-string
	 * (the synonyms with the biggest intersection ratio between the meaning words and the ontology labels
	 * words.
	 * @param label the label full-string for which the synonyms are to be found
	 * @return a list of WordNet Words corresponding to the valid found synonyms
	 * @throws JWNLException
	 */
//	private Word[] findWordNetSynonyms(String label) {
//
//		float intersectionRatio = 0;
//		Word[] synonyms = null;
//
//		try {
//			/* IntersectionRatio - intersection ratio between the words in a synonyms's gloss and the
//			 * labels of the ontology;
//			 * Synonyms - the found synonyms for this label (return value) */
//
//			/* Get the word morphological base form from WordNet database to allow the label to be searched */
//			IndexWord labelBaseForm = getWordBaseForm(label);
//
//			/* If there is a valid base form in the WordNet database, ... */
//			if (labelBaseForm != null) {
//
//				/* Get the synsets corresponding to the different meanings of the label in the database;
//				 * calculate the synset with the biggest intersection ratio between its gloss and the
//				 * ontology labels */
//				for (Synset synset : labelBaseForm.getSenses()) {
//
//					float intersection = calculateGlossLabelIntersection(synset.getGloss());
//					if (intersection > intersectionRatio) {
//						synonyms = synset.getWords();
//						intersectionRatio = intersection;
//					}
//				}
//			}
//		} catch (JWNLException e) {
//			e.printStackTrace();
//		}
//
//		/* If a synset with a positive intersection ratio was found, return the words of that synset as
//		 * the synonyms of the searched label; if not return null */
//		if (intersectionRatio > 0)
//			return synonyms;
//		return null;
//	}

	/**
	 * Get the morphological base form of a label (keyword), regarding all the possible POSs
	 * (taking into account the following POS order: noun, verb, adjective, adverb).
	 * @param keyword the label to be converted
	 * @return the IndexWord corresponding to the morphological base form found
	 * @throws JWNLException
	 */
//	private IndexWord getWordBaseForm(String keyword) throws JWNLException {
//		for (Object pos : WordNet_POSs) {
//			IndexWord keywordBaseForm = _mp.lookupBaseForm((POS)pos, keyword);
//			if (keywordBaseForm != null && LabelizerEnvironment.countNumWords(keywordBaseForm.getLemma()) == LabelizerEnvironment.countNumWords(keyword))
//				return keywordBaseForm;
//		}
//		return null;
//	}

	/**
	 * Calculate the intersection ratio between all the words (with more than 3 characters long) and the
	 * ontology label words, according to the following formula:
	 * IR = (S intersection L) / (#S + #L), where:
	 *    S --> the words of the synset gloss (being #S the number of words in the synset gloss);
	 *    L --> the ontology label words (being #L the number of label words of the ontology);
	 * @param gloss the synset gloss to be calculated
	 * @return the intersection ratio between the synset gloss and the ontology label words
	 */
//	private float calculateGlossLabelIntersection(String gloss) {
//		float labelWordsCount = _concepts.size();
//		String[] glossWords = gloss.split("(( )|(\")|(; ))");
//		float glossWordsCount = glossWords.length, intersectionsCount = 0;
//		for (String glossWord: glossWords) {
//			if (glossWord.length() > 3) {
//				OntologyLabel glossWordLabel = _labels.get(glossWord);
//				if (glossWordLabel != null)
//					intersectionsCount++;
//			}
//		}
//
//		if (glossWordsCount + labelWordsCount == 0)
//			return 0;
//
//		return intersectionsCount / (glossWordsCount + labelWordsCount);
//	}
	
	

	public void indexStructureInXMLFiles(Document configFileDocument, Document labelsFileDocument) {

		/* OntologyElements - keeps all the ontology elements in one single structure for iteration */
		Map<String,Map<String,? extends OntologyElement>> ontologyElements = joinOntologyElementMaps();

		/* Construct the root elements of the XML config file document */
		org.w3c.dom.Node configFileRootElement = configFileDocument.getDocumentElement();
		Element configFileConceptsElement = configFileDocument.createElement("Concepts");
		Element configFileObjectPropertiesElement = configFileDocument.createElement("ObjectProperties");
		Element configFileDataPropertiesElement = configFileDocument.createElement("DataProperties");
		Element configFileLiteralsElement = configFileDocument.createElement("Literals");

		/* Construct the root elements of the XML labels file document */
		org.w3c.dom.Node labelsFileRootElement = labelsFileDocument.getDocumentElement();
		Element labelsFileConceptsElement = labelsFileDocument.createElement("Concepts");
		Element labelsFileObjectPropertiesElement = labelsFileDocument.createElement("ObjectProperties");
		Element labelsFileDataPropertiesElement = labelsFileDocument.createElement("DataProperties");
		Element labelsFileIndividualsElement = labelsFileDocument.createElement("Individuals");
		Element labelsFileLiteralsElement = labelsFileDocument.createElement("Literals");

		/* Add a representation for all ontology element in the XML config and labels files, according
		 * to its type */
		for (String type : ontologyElements.keySet()) {
			Map<String,? extends OntologyElement> elements = ontologyElements.get(type);
			for (String elementURI : elements.keySet()) {
				OntologyElement element = elements.get(elementURI);
				if (element instanceof OntologyIndividual)
					element.writeElementInXMLFiles(configFileDocument, labelsFileDocument, null, labelsFileIndividualsElement);
				else if (element instanceof OntologyConcept)
					element.writeElementInXMLFiles(configFileDocument, labelsFileDocument, configFileConceptsElement, labelsFileConceptsElement);
				else if (element instanceof OntologyObjectProperty)
					element.writeElementInXMLFiles(configFileDocument, labelsFileDocument, configFileObjectPropertiesElement, labelsFileObjectPropertiesElement);
				else if (element instanceof OntologyDataProperty)
					element.writeElementInXMLFiles(configFileDocument, labelsFileDocument, configFileDataPropertiesElement, labelsFileDataPropertiesElement);
				else if (element instanceof OntologyLiteral)
					element.writeElementInXMLFiles(configFileDocument, labelsFileDocument, configFileLiteralsElement, labelsFileLiteralsElement);
			}
		}

		/* Append the child group elements for each ontology element type to the XML config file root
		 * element */
		configFileRootElement.appendChild(configFileConceptsElement);
		configFileRootElement.appendChild(configFileObjectPropertiesElement);
		configFileRootElement.appendChild(configFileDataPropertiesElement);

		/* Append the child group elements for each ontology element type to the XML labels file root
		 * element */
		labelsFileRootElement.appendChild(labelsFileConceptsElement);
		labelsFileRootElement.appendChild(labelsFileObjectPropertiesElement);
		labelsFileRootElement.appendChild(labelsFileDataPropertiesElement);
		labelsFileRootElement.appendChild(labelsFileIndividualsElement);
		labelsFileRootElement.appendChild(labelsFileLiteralsElement);
	}

	/**
	 * Print a friendly version of the list of concepts stored in the indexing structure.
	 */
	public void printAllConcepts() {
		System.out.println("--------------------------------");
		System.out.println("CONCEPTS:");
		for (String conceptURI : _concepts.keySet())
			_concepts.get(conceptURI).printElement();
	}

	/**
	 * Print a friendly version of the list of data properties stored in the indexing structure.
	 */
	public void printAllDataProperties() {
		System.out.println("--------------------------------");
		System.out.println("DATATYPE PROPERTIES:");
		for (String dataPropertyURI : _dataProperties.keySet())
			_dataProperties.get(dataPropertyURI).printElement();
	}

	/**
	 * Print a friendly version of the list of object properties stored in the indexing structure.
	 */
	public void printAllObjectProperties() {
		System.out.println("--------------------------------");
		System.out.println("OBJECT PROPERTIES:");
		for (String objectPropertyURI : _objectProperties.keySet())
			_objectProperties.get(objectPropertyURI).printElement();
	}

	/**
	 * Print a friendly version of the list of individuals stored in the indexing structure.
	 */
	public void printAllIndividuals() {
		System.out.println("--------------------------------");
		System.out.println("INDIVIDUALS:");
		for (String individualURI : _individuals.keySet())
			_individuals.get(individualURI).printElement();
	}

	/**
	 * Print a friendly version of the labels stored in the indexing structure.
	 */
	public void printAllLabels() {
		System.out.println("--------------------------------");
		System.out.println("LABELS:");
		for (String label : _labels.keySet()) {
			OntologyLabel oLabel = _labels.get(label);
			if (oLabel.getLabel().toLowerCase().contains("philipp") == true)
				oLabel.printOntologyLabel();
			/*if (oLabel.getIElements().keySet().size() > 2)
				oLabel.printOntologyLabel();*/
		}
	}

	/**
	 * Print a friendly version of all the ontology elements and labels stored in the indexing structure.
	 */
	public void printAll() {
		printAllConcepts();
		printAllDataProperties();
		printAllObjectProperties();
		printAllIndividuals();
		printAllLabels();
	}
}
