package org.aifb.xxplore.core.service.labelizer;

import java.io.File;
import java.io.IOException;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

//import net.didion.jwnl.JWNLException;

import org.aifb.xxplore.core.ExploreEnvironment;
//import org.aifb.xxplore.core.service.query.QueryInterpretationServiceExtent.KbEdge;
import org.aifb.xxplore.shared.exception.Emergency;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.w3c.dom.DOMImplementation;
import org.xmedia.businessobject.IBusinessObject;
//import org.xmedia.oms.adapter.kaon2.persistence.Kaon2ConceptDao;
//import org.xmedia.oms.adapter.kaon2.persistence.Kaon2DaoManager;
//import org.xmedia.oms.adapter.kaon2.persistence.Kaon2DatatypeDao;
//import org.xmedia.oms.adapter.kaon2.persistence.Kaon2PropertyMemberDao;
import org.xmedia.oms.model.api.IConcept;
import org.xmedia.oms.model.api.INamedConcept;
import org.xmedia.oms.model.api.INamedIndividual;
import org.xmedia.oms.model.api.IProperty;
import org.xmedia.oms.model.api.IPropertyMember;
import org.xmedia.oms.model.api.IResource;
import org.xmedia.oms.model.impl.DataProperty;
import org.xmedia.oms.model.impl.NamedConcept;
import org.xmedia.oms.model.impl.NamedIndividual;
import org.xmedia.oms.model.impl.ObjectProperty;
import org.xmedia.oms.persistence.PersistenceUtil;
import org.xmedia.oms.persistence.dao.DaoUnavailableException;
import org.xmedia.oms.persistence.dao.IConceptDao;
import org.xmedia.oms.persistence.dao.IIndividualDao;
import org.xmedia.oms.persistence.dao.IPropertyDao;

public class Labelizer {

	private static Logger s_log = Logger.getLogger(Labelizer.class);

	private static IndexStructure _indexStructure;
	private static String _configFilePath;
	private static String _labelsFilePath;
	
	private WhitespaceAnalyzer m_analyzer;

	private SimpleGraph<CNode,REdge> _indexGraph;

	public Labelizer(String configFilePath, String labelsFilePath) {
		_indexStructure = new IndexStructure();
		_configFilePath = configFilePath;
		_labelsFilePath = labelsFilePath;
		
		m_analyzer = new WhitespaceAnalyzer();

		_indexGraph = new SimpleGraph<CNode,REdge>(REdge.class);
	}

	public void labelizeOntology() {
		s_log.debug("Step 1: Discovering all concepts and adding them to the indexing structure...");
		discoverConcepts();
		s_log.debug("Step 2: Discovering all properties and adding them to the indexing structure...");
		discoverProperties();
		s_log.debug("Step 3: Discovering all individuals (literals) and adding them to the indexing structure...");
		discoverIndividuals();
		
		int numLiteralInstances = _indexStructure.countNumLiteralInstances();
		int numPropertyMemberInstances = _indexStructure.countNumPropertyMemberInstances();
		
		s_log.debug("Step 4: Adding the found concepts and object properties to the index graph as C-Nodes and R-Edges...");
		addElementsToGraph(numLiteralInstances, numPropertyMemberInstances);

		buildXmlConfigFiles();
		ExploreEnvironment.INDEX_GRAPH = _indexGraph;
		
		s_log.debug("Step 5: Create the element labels in the indexing structure...");
		Map<String, OntologyLabel> labels = _indexStructure.labelize();
		
		_indexStructure.printAllLabels();
		
		s_log.debug("Step 6: Indexing the ontology in Lucene documents...");
		indexOntology(labels);
	}
	
	public void discoverConcepts() {
		try {

			IConceptDao conceptDao = PersistenceUtil.getDaoManager().getConceptDao();
			List<? extends IBusinessObject> concepts = conceptDao.findAll();

			for (IBusinessObject bo : concepts) {
				if (bo instanceof NamedConcept) {
					NamedConcept concept = (NamedConcept) bo;
					_indexStructure.addConcept(new OntologyConcept(concept));
					//_indexStructure.addLabel(concept.getLabel(), concept.getLabel(), LabelizerEnvironment.C_NODE);
				}
			}
		} catch (DaoUnavailableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void discoverProperties() {
		try {
			
			IPropertyDao propertyDao = PersistenceUtil.getDaoManager().getPropertyDao();
			List<? extends IBusinessObject> properties = propertyDao.findAll();
			
			for (IBusinessObject bo : properties) {
				if (bo instanceof ObjectProperty) {
					_indexStructure.addObjectProperty(new OntologyObjectProperty((ObjectProperty) bo));
				} else if (bo instanceof DataProperty) {
					_indexStructure.addDataProperty(new OntologyDataProperty((DataProperty) bo));
				}
			}
			
		} catch (DaoUnavailableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public void discoverIndividuals() {
		try {

			IIndividualDao individualDao = PersistenceUtil.getDaoManager().getIndividualDao();
			List<? extends IBusinessObject> individuals = individualDao.findAll();

			for (IBusinessObject bo : individuals) {
				if (bo instanceof NamedIndividual) {
					NamedIndividual namedIndividual = (NamedIndividual) bo;
					Set<IConcept> types = namedIndividual.getTypes();
					Set<IPropertyMember> propertyMembers = namedIndividual.getPropertyFromValues();
					for (IPropertyMember propertyMember : propertyMembers) {
						IProperty property = propertyMember.getProperty();
						if (property instanceof DataProperty) {
							String lfs = propertyMember.getTarget().getLabel();
							if ((lfs != null) && (lfs.length() > 1) && (LabelizerEnvironment.applyHeuristics(lfs) == true)) {
								OntologyLiteral newLiteral = new OntologyLiteral(lfs, property);
								for (IConcept concept : types) {
									if (concept instanceof NamedConcept) {
										newLiteral.addType((INamedConcept)concept);
										_indexStructure.getConcept(((NamedConcept)concept).getUri()).increaseNumInstances();
									}
								}
								_indexStructure.addLiteral(newLiteral);
							}
						}
						else if (property instanceof ObjectProperty) {
							IResource target = propertyMember.getTarget();
							if (target instanceof INamedIndividual) {
								INamedIndividual targetIndividual = (INamedIndividual) target;
								OntologyObjectProperty objectProperty = _indexStructure.getObjectProperty(property.getUri());
								for (IConcept sourceType : types) {
									for (IConcept targetType : targetIndividual.getTypes()) {
										if ((sourceType instanceof INamedConcept) && (targetType instanceof INamedConcept)) {
											objectProperty.addPropertyMember((INamedConcept) sourceType, (INamedConcept) targetType);
											objectProperty.increasePMNumInstances((INamedConcept) sourceType, (INamedConcept) targetType);
										}
									}
								}
							}
						}
					}
				}
			}
			
		} catch (DaoUnavailableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void addElementsToGraph(int numLiteralInstances, int numPropertyMemberInstances) {
		Map<String,OntologyConcept> concepts = _indexStructure.getConcepts();
		for (String label : concepts.keySet()) {
			OntologyConcept ontologyConcept = concepts.get(label);
			double costValue = 1.0 - ((double)ontologyConcept.getNumInstances() / (double)numLiteralInstances);
			_indexGraph.addVertex(new CNode(ontologyConcept.getConcept(), Math.rint(costValue*10000000000.0d) / 10000000000.0d));
		}
		
		Map<String, OntologyObjectProperty> objectProperties = _indexStructure.getObjectProperties();
		for (String label : objectProperties.keySet()) {
			OntologyObjectProperty ontologyObjectProperty = objectProperties.get(label);
			for (OntologyPropertyMember propertyMember : ontologyObjectProperty.getPropertyMembers()) {
				double costValue = 1.0 - ((double)propertyMember.getNumInstances() / (double)numPropertyMemberInstances);
				addEdgeToIndexGraph(getCNode(propertyMember.getSource()), getCNode(propertyMember.getTarget()), ontologyObjectProperty.getObjectProperty(), Math.rint(costValue*10000000000.0d) / 10000000000.0d);
			}
		}
	}
	
	private void addEdgeToIndexGraph(CNode vertex1, CNode vertex2, IProperty property, double costValue) {
		if (vertex1.equals(vertex2)) {
			return;
		}
		if (_indexGraph.containsVertex(vertex1) == false) {
			_indexGraph.addVertex(vertex1);
		}
		if (_indexGraph.containsVertex(vertex2) == false) {
			_indexGraph.addVertex(vertex2);
		}
		REdge edge = new REdge(vertex1, vertex2, property, costValue);
		if (_indexGraph.containsEdge(edge) == false) {
			s_log.debug("Adding edge: " + edge.toString() + "...");
			_indexGraph.addEdge(vertex1, vertex2, edge);
		}
	}
	
	public CNode getCNode(INamedConcept namedConcept) {
		Set<CNode> cnodes = _indexGraph.vertexSet();
		for (CNode cnode : cnodes) {
			if (cnode.getConceptLabel().equals(namedConcept)) {
				return cnode;
			}
		}
		return null;
	}

	public void indexOntology(Map<String,OntologyLabel> labels) {

		try {
			/* Create a new Lucene IndexWriter to write the documents, associated with the file with the
			 * path _kbIndexDir */
			File file = new File(ExploreEnvironment.KB_INDEX_DIR);
			IndexWriter indexWriter;
			indexWriter = new IndexWriter(file, m_analyzer, true);

			/* For all the labels of the ontology, add a new Lucene document with the correspondent label,
			 *  and sublabels (call of method addDocument) */
			for (String label : labels.keySet()) {
				OntologyLabel oLabel = labels.get(label);
				addLuceneDocument(indexWriter, oLabel.getLabel(), oLabel.getSubLabels());
			}

			indexWriter.optimize();
			indexWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void addLuceneDocument(IndexWriter indexWriter, String label, Set<OntologySubLabel> sublabels) {
		try {
			Document doc = new Document();
			doc.add(new Field(LABEL, label, Field.Store.YES, Field.Index.TOKENIZED));
			int i = 1;
			for (OntologySubLabel sublabel : sublabels) {
				doc.add(new Field(SUBLABEL + new String("" + i), sublabel.getSublabel(), Field.Store.YES, Field.Index.NO));
				doc.add(new Field(TYPE + new String("" + i), sublabel.getType(), Field.Store.YES, Field.Index.NO));
				if (sublabel.getProperty() != null) {
					doc.add(new Field(DATA_PROPERTY + new String("" + i), sublabel.getProperty(), Field.Store.YES, Field.Index.NO));
				}
				i++;
			}
			if (label.toLowerCase().equals("philipp")) {
				System.out.println("ADDING LABEL philipp WITH SUBLABEL " + sublabels.iterator().next().toString());
			}
			indexWriter.addDocument(doc);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}




	
	
	
	
	
	
	
	
	

	
	





	private static String LABEL = "Label";
	private static String SUBLABEL = "Sublabel";
	private static String ALT_LABEL = "AltLabel";
	private static String TYPE = "type";
	private final String CONCEPT = "Concept";
	private final String OBJECT_PROPERTY = "ObjectProperty";
	private final String DATA_PROPERTY = "DataProperty";
	private final String INDIVIDUAL = "Individual";

	/*private void addLuceneDocument(IndexWriter indexWriter, String label, Map<String,String> iElements) {
		try {
			org.apache.lucene.document.Document doc = new org.apache.lucene.document.Document();
			doc.add(new Field(LABEL, label, Field.Store.YES, Field.Index.TOKENIZED));
			Set<String> sublabels = iElements.keySet();
			int i = 1;
			for (String sublabel : sublabels) {
				Integer iInt = new Integer(i);
				doc.add(new Field(SUBLABEL + iInt.toString(), sublabel, Field.Store.YES, Field.Index.NO));
				doc.add(new Field(TYPE + iInt.toString(), iElements.get(sublabel), Field.Store.YES, Field.Index.NO));
				i++;
			}
			indexWriter.addDocument(doc);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}*/

	public class IndexGraphNode {
		
		private double m_costValue;
		
		public IndexGraphNode(double costValue) {
			m_costValue = costValue;
		}
		
		public double getCostValue() {
			return m_costValue;
		}
		
		public void setCostValue(double costValue) {
			m_costValue = costValue;
		}
	}
	
	public class CNode extends IndexGraphNode {
		
		private INamedConcept _conceptLabel;
		
		public CNode (INamedConcept conceptLabel, double costValue) {
			super(costValue);
			_conceptLabel = conceptLabel;
		}
		
		public INamedConcept getConceptLabel() {
			return _conceptLabel;
		}
		
		public void setConceptLabel(INamedConcept conceptLabel) {
			_conceptLabel = conceptLabel;
		}
		
		@Override
		public boolean equals(Object obj) {
			CNode cnode = (CNode) obj;
			if (_conceptLabel.equals(cnode.getConceptLabel())) {
				return true;
			}
			return false;
		}
		
		@Override
		public String toString() {
			return new String(_conceptLabel.getLabel() + "(" + getCostValue() + ")");
		}
	}
	
	public class REdge extends DefaultEdge {
		/**
		 * 
		 */
		private static final long serialVersionUID = 5584485745673658957L;

		private CNode m_vertex1;

		private CNode m_vertex2;

		private IProperty m_property;
		
		private double m_costValue;
		
		public REdge(CNode vertex1, CNode vertex2, IProperty prop, double costValue){
			m_vertex1 = vertex1;
			m_vertex2 = vertex2;
			m_property = prop;
			m_costValue = costValue;
			Emergency.checkPostcondition((m_vertex1 != null) && (m_vertex2 != null) && (m_property != null), "m_vertex1 != null && m_vertex2 != null && m_property != null"); 
		}

		public void setVertex1(CNode vertex1){
			m_vertex1 = vertex1;
		}

		public void setVertex2(CNode vertex2){
			m_vertex2 = vertex2;
		}

		public void setProperty(IProperty propertyName){
			m_property = propertyName;
		}
		
		public void setCostValue(double costValue) {
			m_costValue = costValue;
		}

		public CNode getVertex1(){
			return m_vertex1;
		}

		public CNode getVertex2(){
			return m_vertex2;
		}

		public IProperty getProperty(){
			return m_property;
		}
		
		public double getCostValue() {
			return m_costValue;
		}

//		public boolean equals(KbEdge edge){
//
//			if (!m_property.equals(edge.getProperty()))  return false;
//			if (!m_vertex1.equals(edge.getVertex1())) return false;
//			if (!m_vertex2.equals(edge.getVertex2())) return false;
//			return true;
//		}

		@Override
		public String toString(){
			if((m_vertex1 != null) && (m_vertex2 != null) && (m_property != null)) {
				return m_vertex1.toString() + " " + m_property + " "  + m_vertex2.toString() + " (" + m_costValue;
			} else {
				return super.toString();
			}
		}
	}



//	public Map<String,OntologyLabel> labelizeOntology() {
//
//		/* Steps 1 and 2: discover all T-Box and A-Box elements of the ontology and add them to the
//		 * indexing structure */
////		s_log.debug("Step 1: Discovering all T-Box elements...");
////		discoverAllProperties();
////		discoverAllConcepts();
////		s_log.debug("Step 2: Discovering all individuals and indexing according to their classes...");
////		discoverAllIndividuals();
//
////		/* Build the XML configuration files (result from steps 1 and 2) */
////		s_log.debug("Building XML config files...");
////		buildXmlConfigFiles();
//
////		/* Construct the labels for all the found ontology elements */
////		s_log.debug("Step 3: Labelizing the ontology elements...");
////		Map<String,OntologyLabel> labels = _indexStructure.labelize();
////		ExploreEnvironment.MIDDLE_LAYER = _indexStructure.getConcepts();
//
//		constructIndexGraph();
//		Map<String,OntologyLabel> labels = _indexStructure.getLabels();
//		return labels;
//	}

//	/**
//	*  Discover all object and data properties belonging to the schema ontology, and add them to the indexing
//	*  structure.
//	*/
//	private void discoverAllProperties() {
//	IPropertyDao propertyDao = (IPropertyDao) PersistenceUtil.getDaoManager().getAvailableDao(IPropertyDao.class);
//	List<? extends IBusinessObject> properties = propertyDao.findAll(); 

//	for (IBusinessObject bo : properties) {
//	IProperty property = (IProperty)bo;
//	if (property instanceof ObjectProperty) {
//	ObjectProperty objectProperty = (ObjectProperty)property;
//	_indexStructure.addObjectProperty(new OntologyObjectProperty(objectProperty.getUri()));
//	}
//	else if (property instanceof DataProperty) {
//	DataProperty dataProperty = (DataProperty)property;
//	_indexStructure.addDataProperty(new OntologyDataProperty(dataProperty.getUri()));
//	}
//	}
//	}

//	/**
//	* Discover all concepts belonging to the schema ontology, and add them to the indexing structure.
//	* In practice, this method transverses all subsumption hierarchy to associate the
//	* concepts with their possible labeling data properties.
//	*/
//	private void discoverAllConcepts() {

//	IConceptDao conceptDao = (IConceptDao) PersistenceUtil.getDaoManager().getAvailableDao(IConceptDao.class);
//	List<? extends IBusinessObject> concepts = conceptDao.findAll();

//	for (IBusinessObject bo : concepts) {
//	if (bo instanceof NamedConcept) {
//	NamedConcept concept = (NamedConcept) bo;
//	OntologyConcept newConcept = new OntologyConcept(concept.getUri());
//	IPropertyDao propertyMember = (IPropertyDao)PersistenceUtil.getDaoManager().getAvailableDao(IPropertyDao.class);
//	Set<Pair> conceptProperties = propertyMember.findPropertiesAndRangesFrom(concept);
//	for (Pair conceptProperty : conceptProperties) {
//	if (conceptProperty.getHead() instanceof DataProperty) {
//	DataProperty dataProperty = (DataProperty) conceptProperty.getHead();
//	OntologyDataProperty oDataProperty = _indexStructure.getDataProperty(dataProperty.getUri());
//	oDataProperty.addRange(conceptProperty.getTail().toString());
//	if (oDataProperty.getRange().equals(STRING_DATATYPE.toString()))
//	newConcept.addIndexingDP(dataProperty.getUri());
//	}
//	//Novo código
//	else if (conceptProperty.getHead() instanceof ObjectProperty) {
//	ObjectProperty objectProperty = (ObjectProperty) conceptProperty.getHead();
//	Object target = conceptProperty.getTail();
//	if (target instanceof NamedConcept)
//	newConcept.addObjectProperty(objectProperty.getUri(), ((NamedConcept)target).getUri());
//	}


//	//s_log.debug(conceptProperty.getHead() + " " + conceptProperty.getTail());
//	}
//	_indexStructure.addConcept(newConcept);
//	newConcept.printElement();
//	}
//	}
//	}

//	/**
//	* Discover all the individuals in the ontology, and construct a representation of them (and all
//	* their label strings) to add to the indexing structure (invocation of constructOntologyIndividual).
//	* @throws KAON2Exception
//	*/
//	private void discoverAllIndividuals() {
//	IIndividualDao individualDao = (IIndividualDao) PersistenceUtil.getDaoManager().getAvailableDao(IIndividualDao.class);
//	List<? extends IBusinessObject> individuals = individualDao.findAll(); 

//	/* For each found individual, construct and add it to the index structure */
//	for (IBusinessObject individual : individuals) {
//	if (individual instanceof NamedIndividual)
//	_indexStructure.addIndividual(constructOntologyIndividual((NamedIndividual)individual));
//	}
//	}

//	/**
//	* 
//	* @param individual the ontology individual to be added to the indexing structure
//	* @return the constructed representation of the ontology individual and its label strings
//	* @throws KAON2Exception
//	*/
//	private OntologyIndividual constructOntologyIndividual (NamedIndividual individual) {

//	/* BlackList - a list with all the data properties whose label strings didn't passed the heuristics test;
//	* OntologyIndividual - the representation of the individual in the indexing structure */
//	ArrayList<String> blackList = new ArrayList<String>();
//	OntologyIndividual ontologyIndividual = new OntologyIndividual(individual.getUri());

//	/* Get the concepts this individual belongs to */
//	Set<IConcept> classMembers = individual.getTypes();

//	/* Add the concepts found to the types list */
//	for (IConcept classMember : classMembers) {
//	if (classMember instanceof NamedConcept)
//	ontologyIndividual.addType((NamedConcept)classMember);
//	}

//	Set<IPropertyMember> propertyMembers = individual.getPropertyFromValues();

//	ArrayList<String> individualIndexes = new ArrayList<String>();
//	for (IConcept classMember : classMembers) {
//	if (classMember instanceof NamedConcept) {
//	NamedConcept typeConcept = (NamedConcept) classMember;
//	individualIndexes.addAll(_indexStructure.getConcept(typeConcept.getUri()).getIndexingDPs());
//	}
//	}

//	for (IPropertyMember propertyMember : propertyMembers) {
//	if (individualIndexes.contains(propertyMember.getProperty().getUri()) == true) {
//	String lfs = propertyMember.getTarget().getLabel();
//	if (lfs != null) {
//	if (LabelizerEnvironment.applyHeuristics(lfs) == true)
//	ontologyIndividual.addLabelFullString(propertyMember.getProperty().getUri(), lfs);
//	else
//	blackList.add(propertyMember.getProperty().getUri());
//	}	
//	}
//	}

//	/* Remove all data properties not indexable (after the heuristics) from the indexable DPs of all concepts */
//	for (String blackListElement : blackList)
//	_indexStructure.removeLabelDataProperty(blackListElement);

//	return ontologyIndividual;
//	}

	/**
	 * Build the XML config and labels files, according to the current ontology element stored in the
	 * index structure.
	 */
	private void buildXmlConfigFiles() {

		File configFile = new File(_configFilePath);
		File labelsFile = new File(_labelsFilePath);

		try {

			/* Get a DOM implementation to allow the construction of the config and labels XML file documents */
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			DOMImplementation impl = factory.newDocumentBuilder().getDOMImplementation();

			/* Create the XML DOM documents to represent the XML config and labels file */
			org.w3c.dom.Document configFileDocument = impl.createDocument(null, "ConfigFile", null);
			org.w3c.dom.Document labelsFileDocument = impl.createDocument(null, "LabelsFile", null);

			/* Create all XML DOM elements for all ontology elements stored in the indexing structure */
			_indexStructure.indexStructureInXMLFiles(configFileDocument, labelsFileDocument);

			/* Create the XML config and labels document from the resulting XML DOM config and labels
			 * document representation */ 
			Transformer idTransform = TransformerFactory.newInstance().newTransformer();
			idTransform.transform(new DOMSource(configFileDocument), new StreamResult(configFile));
			idTransform.transform(new DOMSource(labelsFileDocument), new StreamResult(labelsFile));

		}
		catch (ParserConfigurationException pce) {
			System.out.println("Error in building XMLConfigFiles - Could not locate a JAXP DocumentBuilder class!");
		}
		catch (TransformerConfigurationException tce) {
			System.out.println("Error in building XMLConfigFiles - Could not locate a JAXP factory class!");
		}
		catch (TransformerException te) {
			System.out.println("Error in building XMLConfigFiles - " + te);
		}
	}
}
