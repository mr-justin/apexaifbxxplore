package org.ateam.xxplore.core.service.sampling;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aifb.xxplore.shared.util.Pair;
import org.aifb.xxplore.shared.vocabulary.RDFS;
import org.apache.log4j.Logger;
import org.ateam.xxplore.core.ExploreEnvironment;
import org.ateam.xxplore.core.service.search.KbEdge;
import org.ateam.xxplore.core.service.search.KbElement;
import org.ateam.xxplore.core.service.search.KbVertex;
import org.jgrapht.graph.WeightedPseudograph;
import org.openrdf.repository.RepositoryException;
import org.xmedia.accessknow.sesame.persistence.ExtendedSesameDaoManager;
import org.xmedia.accessknow.sesame.persistence.SesameConnection;
import org.xmedia.accessknow.sesame.persistence.SesameRepositoryFactory;
import org.xmedia.accessknow.sesame.persistence.SesameSession;
import org.xmedia.accessknow.sesame.persistence.SesameSessionFactory;
import org.xmedia.oms.model.api.IConcept;
import org.xmedia.oms.model.api.IIndividual;
import org.xmedia.oms.model.api.INamedConcept;
import org.xmedia.oms.model.api.IObjectProperty;
import org.xmedia.oms.model.api.IOntology;
import org.xmedia.oms.model.api.IProperty;
import org.xmedia.oms.model.api.IPropertyMember;
import org.xmedia.oms.model.api.IResource;
import org.xmedia.oms.model.api.OntologyImportException;
import org.xmedia.oms.model.impl.DataProperty;
import org.xmedia.oms.model.impl.NamedConcept;
import org.xmedia.oms.model.impl.ObjectProperty;
import org.xmedia.oms.model.impl.Property;
import org.xmedia.oms.persistence.DatasourceException;
import org.xmedia.oms.persistence.IKbConnection;
import org.xmedia.oms.persistence.ISession;
import org.xmedia.oms.persistence.ISessionFactory;
import org.xmedia.oms.persistence.ITransaction;
import org.xmedia.oms.persistence.InvalidParameterException;
import org.xmedia.oms.persistence.KbEnvironment;
import org.xmedia.oms.persistence.MissingParameterException;
import org.xmedia.oms.persistence.OntologyCreationException;
import org.xmedia.oms.persistence.OpenSessionException;
import org.xmedia.oms.persistence.PersistenceUtil;
import org.xmedia.oms.persistence.SessionFactory;
import org.xmedia.oms.persistence.StatelessSession;
import org.xmedia.oms.persistence.dao.IConceptDao;
import org.xmedia.oms.persistence.dao.IIndividualDao;
import org.xmedia.oms.persistence.dao.ILiteralDao;
import org.xmedia.oms.persistence.dao.IPropertyDao;
import org.xmedia.oms.persistence.dao.IPropertyMemberAxiomDao;
import org.xmedia.uris.impl.XMURIFactoryInsulated;


public class TestGraphIndexWithSesame2 {
	
	private static Logger s_log = Logger.getLogger(TestGraphIndexWithSesame2.class);
	
	private static IOntology m_onto;
	
	private static ISession m_session;
	
	private static String repositoryDir = "D:\\Repository\\indexgraph";
	
	public static int TOTAL_NUMBER_OF_INDIVIDUAL = 1; 
	
	public static int TOTAL_NUMBER_OF_PROPERTYMEMBER = 1;
	
	private static WeightedPseudograph<KbVertex,KbEdge> resourceGraph;
	
	public static void main(String[] args) throws Exception {
		init();
		
		computeTotalNumber();
		
		resourceGraph = new WeightedPseudograph<KbVertex,KbEdge>(KbEdge.class);
		
//		IConceptDao conceptDao = (IConceptDao) PersistenceUtil.getDaoManager().getAvailableDao(IConceptDao.class);
//		List concepts = conceptDao.findAll() ;
//		int numConcept = concepts.size();
//		
//		IPropertyDao propertyDao = (IPropertyDao) PersistenceUtil.getDaoManager().getAvailableDao(IPropertyDao.class);
//		List properties = propertyDao.findAll(); 
//		int numProperty = properties.size();
//		
//		ILiteralDao literalDao = (ILiteralDao) PersistenceUtil.getDaoManager().getAvailableDao(ILiteralDao.class);
//		List literals = literalDao.findAll();
//		int numLiteral = literals.size();
//		
//		IIndividualDao individualDao = (IIndividualDao) PersistenceUtil.getDaoManager().getAvailableDao(IIndividualDao.class);
//		List individuals = individualDao.findAll();
//		int numIndividual = individuals.size();
//		
//		IPropertyMemberAxiomDao propertyMemberDao = (IPropertyMemberAxiomDao) PersistenceUtil.getDaoManager().getAvailableDao(IPropertyMemberAxiomDao.class);
//		List propertyMembers = propertyMemberDao.findAll();
//		int numPropertyMember = propertyMembers.size();
		
		IPropertyMemberAxiomDao propertyMemberDao = (IPropertyMemberAxiomDao)PersistenceUtil.getDaoManager().getAvailableDao(IPropertyMemberAxiomDao.class);
		List<IPropertyMember> propertyMembers = propertyMemberDao.findAll();
		int numPropertyMember = propertyMembers.size();
		
		for(IPropertyMember propMem : propertyMembers) {
			IResource source = propMem.getSource();
			IResource target = propMem.getTarget();
			IProperty property = propMem.getProperty();
			
			System.out.println(source);
			System.out.println(property);
			System.out.println(target);
			
			if(property instanceof IObjectProperty && !propMem.equals(Property.IS_INSTANCE_OF) ) {
				if(source instanceof IIndividual && target instanceof IIndividual) {
					Set<IConcept> sources = ((IIndividual)source).getTypes();
					Set<IConcept> targets = ((IIndividual)target).getTypes();
					Set<KbVertex> sourceVertices = new HashSet<KbVertex>();
					Set<KbVertex> targetVertices = new HashSet<KbVertex>();
					
					if(sources != null && sources.size() != 0 ) {
						for(IConcept scon : sources) {
							sourceVertices.add(new KbVertex(scon, KbElement.CVERTEX, computeWeight((INamedConcept)scon)));
						}
					} else {
						sourceVertices.add(new KbVertex(NamedConcept.TOP, KbElement.CVERTEX, 2));
					}
					
					if(targets != null && targets.size() != 0 ) {
						for(IConcept tcon : targets) {
							targetVertices.add(new KbVertex(tcon, KbElement.CVERTEX, computeWeight((INamedConcept)tcon)));
						}
					} else {
						targetVertices.add(new KbVertex(NamedConcept.TOP, KbElement.CVERTEX, 2));
					}
					
					for(KbVertex sourceVertex : sourceVertices) {
						for(KbVertex targetVertex : targetVertices) {
							addGraphElement(sourceVertex ,resourceGraph);
							addGraphElement(targetVertex ,resourceGraph);
							addGraphElement(sourceVertex, targetVertex, property, resourceGraph);
						}
					}
					
				}
			}
			
			System.out.println();
		}
		
		for(KbVertex vertex : resourceGraph.vertexSet()){
			System.out.println("vertex: " + vertex + "\n(" + vertex.getCost() + ")");
		}
		for(KbEdge edge : resourceGraph.edgeSet()){
			System.out.println("edge: " + edge + "\n(" + edge.getCost() + ")");
		}
		
	}
	
	public static void addGraphElement(KbVertex vertex1, KbVertex vertex2, IProperty property, WeightedPseudograph<KbVertex,KbEdge> graph){
		boolean addEdge = false; 
		KbEdge edge = null;
		if(vertex2.getType() == KbElement.CVERTEX){
			if(property.equals(Property.SUBCLASS_OF)) {
				edge = new KbEdge(vertex1, vertex2, property, KbElement.REDGE, 0);
			} else {
				edge = new KbEdge(vertex1, vertex2, property, KbElement.REDGE, computeWeight(property));
			}
		}
		else {
			edge = new KbEdge(vertex1, vertex2, property, KbElement.AEDGE, 1);
		} 
			
		if(!(graph.containsEdge(edge))){
			addEdge = graph.addEdge(vertex1, vertex2, edge);
			if(addEdge) {
				s_log.debug("Edge " + edge + " is added to the graph!");
			}
		} else {
			s_log.debug("Edge " + edge + " is already in the graph!");
		}
	}
	
	private static void addGraphElement(KbVertex vertex, WeightedPseudograph<KbVertex,KbEdge> graph){
		boolean addVertex = false;
		addVertex = graph.addVertex(vertex);
		if(addVertex) {
			s_log.debug("Vertex " + vertex + " is added to the graph!");
		} else {
			s_log.debug("Vertex " + vertex + " is already in the graph!");
		}
	}
	
	public static void addGraphElement(KbEdge edge, WeightedPseudograph<KbVertex,KbEdge> graph){
		boolean addEdge = false; 
		if(!(graph.containsEdge(edge))){
			KbVertex vertex1 = edge.getVertex1();
			if(!(graph.containsVertex(vertex1))) {
				graph.addVertex(vertex1);
			}
			KbVertex vertex2 = edge.getVertex2();
			if(!(graph.containsVertex(vertex2))) {
				graph.addVertex(vertex2);
			}
			addEdge = graph.addEdge(vertex1, vertex2, edge);
			if(addEdge) {
				s_log.debug("Edge " + edge + " is added to the graph!");
			}
		} else {
			s_log.debug("Edge " + edge + " is already in the graph!");
		}
	}
	
	private static void computeTotalNumber(){
		StatelessSession session = (StatelessSession)SessionFactory.getInstance().getCurrentSession();
		IOntology onto = session.getOntology();
		
		int numIndividual = onto.getNumberOfIndividual();
		System.out.println("number of Individual: " + numIndividual);
		
		int numoPropertyMember = onto.getNumberOfObjectPropertyMember();
		System.out.println("number of ObjectPropertyMember: " + numoPropertyMember);
		
		TOTAL_NUMBER_OF_INDIVIDUAL = numIndividual;
		
		TOTAL_NUMBER_OF_PROPERTYMEMBER = numoPropertyMember;
	}
	
	private static double computeEdgeWeight(double num, double totalNum){
		if(num == 0) {
			return Double.POSITIVE_INFINITY;
		}
		return 2-Math.log(1+num/totalNum)/Math.log(2);
	}
	
	private static double computeVertexWeight(double num, double totalNum){
		if(num == 0) {
			return Double.POSITIVE_INFINITY;
		}
		return 2-Math.log(1+num/totalNum)/Math.log(2); 
	}
	
	private static double computeWeight(INamedConcept concept){
		int numIndividual = concept.getNumberOfIndividuals();
		return computeVertexWeight(numIndividual,TOTAL_NUMBER_OF_INDIVIDUAL);
	}
	
	private static double computeWeight(IProperty property){
		int numProMem = property.getNumberOfPropertyMember();
		return computeEdgeWeight(numProMem,TOTAL_NUMBER_OF_PROPERTYMEMBER);
	}  

	private static void init() {
		
//		Create the Sesame Connection and the SessionFactory 
		IKbConnection connection = null;
		try {
			connection = new SesameConnection(repositoryDir);
		} catch (RepositoryException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		ISessionFactory sessionFactory = new SesameSessionFactory(new XMURIFactoryInsulated());

		Map<String, Object> parameters = new Hashtable<String, Object>();
		try {
			parameters.put(KbEnvironment.ONTOLOGY_URI, new URI("target"));
			parameters.put(KbEnvironment.ONTOLOGY_TYPE, new URI(SesameRepositoryFactory.RDFS_MEMORY_PERSISTENT));
		} catch (URISyntaxException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		parameters.put(ExploreEnvironment.ONTOLOGY_FILE_PATH, "D:\\target.rdf");
		parameters.put(ExploreEnvironment.ONTOLOGY_FILE_NAME, "target.rdf");
		parameters.put(ExploreEnvironment.BASE_ONTOLOGY_URI, "http://www.example.org/example");
		parameters.put(ExploreEnvironment.LANGUAGE, IOntology.RDF_XML_LANGUAGE);
		
		
		IOntology onto = null;
		try {
			onto = connection.loadOrCreateOntology(parameters);
			addFileToRepository(onto, parameters);
		} catch (MissingParameterException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (InvalidParameterException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (OntologyCreationException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
		SesameSessionFactory sesame_factory = new SesameSessionFactory(new XMURIFactoryInsulated());
		ISession session = null;

		try {
			session = sesame_factory.openSession(connection, onto);
		} catch (DatasourceException e) {
			e.printStackTrace();
		} catch (OpenSessionException e) {
			e.printStackTrace();
		}
		
		//set dao manager
		PersistenceUtil.setDaoManager(ExtendedSesameDaoManager.getInstance((SesameSession)session));
		session.close();		
		
		ISessionFactory factory = SessionFactory.getInstance();
		PersistenceUtil.setSessionFactory(factory); 
		//open a new session with the ontology
		try {
			m_session = factory.openSession(connection, onto);
		} catch (DatasourceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OpenSessionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		m_onto = onto;
		
	}

	private static void addFileToRepository(IOntology onto, Map<String, Object> parameters)throws MissingParameterException{
		
		if(!parameters.containsKey(ExploreEnvironment.ONTOLOGY_FILE_PATH)) {
			throw new MissingParameterException("Ontology file path is missing.");
		}
		
		if(!parameters.containsKey(ExploreEnvironment.ONTOLOGY_FILE_NAME)) {
			throw new MissingParameterException("Ontology filename is missing.");
		}
		
		if(!parameters.containsKey(ExploreEnvironment.BASE_ONTOLOGY_URI)) {
			throw new MissingParameterException("Ontology base-uri is missing.");
		}
		
		if(!parameters.containsKey(ExploreEnvironment.LANGUAGE)) {
			throw new MissingParameterException("Ontology language is missing.");
		}
		
//		Check whether file has already been added to repository 
		
		File repositoryDir = new File(SesameRepositoryFactory.REPO_PATH_DEFAULT);
		String path = repositoryDir.getAbsolutePath()+"/"+parameters.get(ExploreEnvironment.ONTOLOGY_FILE_NAME)+"_added";
		boolean already_added = Arrays.asList(repositoryDir.list()).contains(parameters.get(ExploreEnvironment.ONTOLOGY_FILE_NAME)+"_added");
		
		if(already_added) {
			return;
		} else{
			try {
				(new File(path)).createNewFile();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		
		String filePath = (String)parameters.get(ExploreEnvironment.ONTOLOGY_FILE_PATH);
		String baseUri = (String)parameters.get(ExploreEnvironment.BASE_ONTOLOGY_URI);
		String language = (String)parameters.get(ExploreEnvironment.LANGUAGE);
					
//		File has not been added -> add it now
		
		try {
			onto.importOntology(language, baseUri, new FileReader(filePath));
		} 
		catch (FileNotFoundException e) {
			e.printStackTrace();
		} 
		catch (OntologyImportException e) {
			e.printStackTrace();
		}
	}
	
}
