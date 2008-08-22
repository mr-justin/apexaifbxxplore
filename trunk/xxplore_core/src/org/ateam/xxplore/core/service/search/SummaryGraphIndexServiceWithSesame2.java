package org.ateam.xxplore.core.service.search;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.aifb.xxplore.shared.util.PropertyUtils;
import org.apache.log4j.Logger;
import org.ateam.xxplore.core.ExploreEnvironment;
import org.ateam.xxplore.core.service.search.KbEdge;
import org.ateam.xxplore.core.service.search.KbElement;
import org.ateam.xxplore.core.service.search.KbVertex;
import org.jgrapht.graph.WeightedPseudograph;
import org.openrdf.model.URI;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.rdfxml.RDFXMLWriter;
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
import org.xmedia.oms.model.impl.NamedConcept;
import org.xmedia.oms.model.impl.ObjectProperty;
import org.xmedia.oms.model.impl.Property;
import org.xmedia.oms.persistence.DatasourceException;
import org.xmedia.oms.persistence.ISession;
import org.xmedia.oms.persistence.ISessionFactory;
import org.xmedia.oms.persistence.InvalidParameterException;
import org.xmedia.oms.persistence.KbEnvironment;
import org.xmedia.oms.persistence.MissingParameterException;
import org.xmedia.oms.persistence.OntologyCreationException;
import org.xmedia.oms.persistence.OntologyLoadException;
import org.xmedia.oms.persistence.OpenSessionException;
import org.xmedia.oms.persistence.PersistenceUtil;
import org.xmedia.oms.persistence.SessionFactory;
import org.xmedia.oms.persistence.StatelessSession;
import org.xmedia.oms.persistence.dao.IPropertyMemberAxiomDao;
import org.xmedia.uris.impl.XMURIFactoryInsulated;


public class SummaryGraphIndexServiceWithSesame2 {
	
	private static Logger s_log = Logger.getLogger(SummaryGraphIndexServiceWithSesame2.class);
	
	private IOntology m_onto;
	private ISession m_session;
	
	private String structureIndexDir; 
	private String repositoryDir;
	private Properties parameters;
	
	private BufferedWriter bw;
	private RDFXMLWriter writer;
	
	public int TOTAL_NUMBER_OF_INDIVIDUAL = 1; 
	public int TOTAL_NUMBER_OF_PROPERTYMEMBER = 1;
	private WeightedPseudograph<KbVertex,KbEdge> resourceGraph;
	
	
	private static String ONTOLOGY_URI = "target"; // repository directory name
	private static String ONTOLOGY_FILE_PATH = "res/target.rdf";
	private static String ONTOLOGY_FILE_NAME = "target.rdf";
	private static String BASE_ONTOLOGY_URI = "http://www.example.org/example";
	private static String LANGUAGE = IOntology.RDF_XML_LANGUAGE;
	private static String ONTOLOGY_TYPE = SesameRepositoryFactory.RDFS_MEMORY_PERSISTENT;
	
	private static String REPOSITORY_DIR = "res/BTC/sampling/repository";
	private static String STRUCTURE_INDEX_DIR = "res/BTC/sampling/structureIndex";
	private static String SCHEMA_FOR_MAPPING = "res/BTC/sampling/mapping/schema.rdf";
	
	public static void main(String[] args) {
		Properties parameters = new Properties();
		parameters.setProperty(KbEnvironment.ONTOLOGY_URI, ONTOLOGY_URI);
		parameters.setProperty(KbEnvironment.ONTOLOGY_TYPE, ONTOLOGY_TYPE);
		
		parameters.setProperty(ExploreEnvironment.ONTOLOGY_FILE_PATH, ONTOLOGY_FILE_PATH);
		parameters.setProperty(ExploreEnvironment.ONTOLOGY_FILE_NAME, ONTOLOGY_FILE_NAME);
		parameters.setProperty(ExploreEnvironment.BASE_ONTOLOGY_URI, BASE_ONTOLOGY_URI);
		parameters.setProperty(ExploreEnvironment.LANGUAGE, LANGUAGE);
		
		SummaryGraphIndexServiceWithSesame2 service = new SummaryGraphIndexServiceWithSesame2(parameters, REPOSITORY_DIR, STRUCTURE_INDEX_DIR, SCHEMA_FOR_MAPPING);
		service.indexSummaryGraph();
	} 
	
	public SummaryGraphIndexServiceWithSesame2(Properties parameters, String repositoryDir, String structureIndexDir) {
		this.parameters = parameters;
		this.repositoryDir = repositoryDir;
		this.structureIndexDir = structureIndexDir;
		init();	
	}
	
	public SummaryGraphIndexServiceWithSesame2(Properties parameters, String repositoryDir, String structureIndexDir, String schema) {
		this.parameters = parameters;
		this.repositoryDir = repositoryDir;
		this.structureIndexDir = structureIndexDir;
		init();	
		new File(schema).getParentFile().mkdirs();
		try {
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(schema),"UTF-8"));
			writer = new RDFXMLWriter(bw);
			writer.startRDF();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	public void indexSummaryGraph() {
		
		computeTotalNumber();
		
		resourceGraph = new WeightedPseudograph<KbVertex,KbEdge>(KbEdge.class);
		
		IPropertyMemberAxiomDao propertyMemberDao = (IPropertyMemberAxiomDao)PersistenceUtil.getDaoManager().getAvailableDao(IPropertyMemberAxiomDao.class);
		List<IPropertyMember> propertyMembers = propertyMemberDao.findAll();
		int numPropertyMember = propertyMembers.size();
		
		for(IPropertyMember propMem : propertyMembers) {
			IResource source = propMem.getSource();
			IResource target = propMem.getTarget();
			IProperty property = propMem.getProperty();
			
			if(property instanceof IObjectProperty && !propMem.equals(Property.IS_INSTANCE_OF) ) {
				if(source instanceof IIndividual && target instanceof IIndividual) {
					Set<IConcept> sources = ((IIndividual)source).getTypes();
					Set<IConcept> targets = ((IIndividual)target).getTypes();
					Set<KbVertex> sourceVertices = new HashSet<KbVertex>();
					Set<KbVertex> targetVertices = new HashSet<KbVertex>();
					
					// you should set "SET_DELEGATES = false" in DelegatesManager. 
//					if (property.getDelegate() instanceof URI) {
//						try {
//							URI predicate = (URI) property.getDelegate();
//							writer.handleStatement(new StatementImpl(predicate, RDF.TYPE, OWL.OBJECTPROPERTY));
//							for (IConcept src : sources) {
//								if (src.getDelegate() instanceof URI) {
//									URI domain = (URI) src.getDelegate();
//									writer.handleStatement(new StatementImpl(domain, RDF.TYPE, OWL.CLASS));	
//									writer.handleStatement(new StatementImpl(predicate, RDFS.DOMAIN, domain));	
//								}
//							}
//							for (IConcept tar : targets) {
//								if (tar.getDelegate() instanceof URI) {
//									URI range = (URI) tar.getDelegate();
//									writer.handleStatement(new StatementImpl(range, RDF.TYPE, OWL.CLASS));	
//									writer.handleStatement(new StatementImpl(predicate, RDFS.RANGE, range));	
//								}
//							}
//						} catch (RDFHandlerException e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
//					}
					
					try {
						URI predicate = new URIImpl(property.getUri());
						writer.handleStatement(new StatementImpl(predicate,	RDF.TYPE, OWL.OBJECTPROPERTY));

						for (IConcept src : sources) {
							URI domain = new URIImpl(((INamedConcept)src).getUri());
							writer.handleStatement(new StatementImpl(domain, RDF.TYPE, OWL.CLASS));
							writer.handleStatement(new StatementImpl(predicate, RDFS.DOMAIN, domain));
						}
						for (IConcept tar : targets) {
							URI range = new URIImpl(((INamedConcept)tar).getUri());
							writer.handleStatement(new StatementImpl(range, RDF.TYPE, OWL.CLASS));
							writer.handleStatement(new StatementImpl(predicate, RDFS.RANGE, range));
						}

					} catch (RDFHandlerException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					if(sources != null && sources.size() != 0 ) {
						for(IConcept scon : sources) {
								sourceVertices.add(new KbVertex(new NamedConcept(((INamedConcept)scon).getUri()), KbElement.CVERTEX, computeWeight((INamedConcept)scon)));
						}
					} else {
						sourceVertices.add(new KbVertex(NamedConcept.TOP, KbElement.CVERTEX, 2));
					}
					
					if(targets != null && targets.size() != 0 ) {
						for(IConcept tcon : targets) {
							targetVertices.add(new KbVertex(new NamedConcept(((INamedConcept)tcon).getUri()), KbElement.CVERTEX, computeWeight((INamedConcept)tcon)));
						}
					} else {
						targetVertices.add(new KbVertex(NamedConcept.TOP, KbElement.CVERTEX, 2));
					}
					
					if (sourceVertices != null && sourceVertices.size() != 0 && targetVertices != null && targetVertices.size() != 0) {
						for (KbVertex sourceVertex : sourceVertices) {
							for (KbVertex targetVertex : targetVertices) {
								addGraphElement(sourceVertex, resourceGraph);
								addGraphElement(targetVertex, resourceGraph);
								addGraphElement(sourceVertex, targetVertex, property, resourceGraph);
							}
						}
					}
					
				}
			}
			
		}
		
		try {
			writer.endRDF();
			if(bw != null)
				bw.close();
		} catch (RDFHandlerException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//save graphIndex into the file *.graph
		String path = (structureIndexDir.endsWith(File.separator) ? structureIndexDir + parameters.getProperty(KbEnvironment.ONTOLOGY_URI) + ".graph" : 
			structureIndexDir + File.separator + parameters.getProperty(KbEnvironment.ONTOLOGY_URI) + ".graph");
		File graphIndex = new File(path);
		if(!graphIndex.exists()){
			graphIndex.getParentFile().mkdirs();
			try {
				graphIndex.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		ObjectOutputStream out;
		try {
			out = new ObjectOutputStream(new FileOutputStream(graphIndex));
			out.writeObject(resourceGraph);
			out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		for(KbVertex vertex : resourceGraph.vertexSet()){
			System.out.println("vertex: " + vertex + "\n(" + vertex.getCost() + ")");
		}
		for(KbEdge edge : resourceGraph.edgeSet()){
			System.out.println("edge: " + edge + "\n(" + edge.getCost() + ")");
		}
		
		// retrieve graphIndex
//		String path = (structureIndexDir.endsWith(File.separator) ? structureIndexDir + ONTOLOGY_URI + ".graph" : 
//			structureIndexDir + File.separator + ONTOLOGY_URI + ".graph");
//		ObjectInputStream in;
//		WeightedPseudograph<KbVertex,KbEdge> newResourceGraph = null;
//		try {
//			in = new ObjectInputStream(new FileInputStream(path));
//			newResourceGraph = (WeightedPseudograph<KbVertex,KbEdge>)in.readObject(); 
//			in.close();
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (ClassNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//		System.out.println("\n" + "new graph:");
//		for(KbVertex vertex : newResourceGraph.vertexSet()){
//			System.out.println("vertex: " + vertex + "\n(" + vertex.getCost() + ")");
//		}
//		for(KbEdge edge : newResourceGraph.edgeSet()){
//			System.out.println("edge: " + edge + "\n(" + edge.getCost() + ")");
//		}
		
	}
	
	public boolean addGraphElement(KbVertex vertex1, KbVertex vertex2, IProperty property, WeightedPseudograph<KbVertex,KbEdge> graph){
		boolean addEdge = false; 
		KbEdge edge = null;
		IObjectProperty objectProperty = new ObjectProperty(property.getUri());
		if(vertex2.getType() == KbElement.CVERTEX){
			if(objectProperty.equals(Property.SUBCLASS_OF)) {
				edge = new KbEdge(vertex1, vertex2, objectProperty, KbElement.REDGE, 0);
			} else {
				edge = new KbEdge(vertex1, vertex2, objectProperty, KbElement.REDGE, computeWeight(property));
			}
		}
		else {
			edge = new KbEdge(vertex1, vertex2, objectProperty, KbElement.AEDGE, 1);
		} 
			
		if(!(graph.containsEdge(edge))){
			addEdge = graph.addEdge(vertex1, vertex2, edge);
			if(addEdge) {
				s_log.debug("Edge " + edge + " is added to the graph!");
			}
		} else {
			s_log.debug("Edge " + edge + " is already in the graph!");
		}
		
		return addEdge;
	}
	
	private boolean addGraphElement(KbVertex vertex, WeightedPseudograph<KbVertex,KbEdge> graph){
		boolean addVertex = false;
		addVertex = graph.addVertex(vertex);
		if(addVertex) {
			s_log.debug("Vertex " + vertex + " is added to the graph!");
		} else {
			s_log.debug("Vertex " + vertex + " is already in the graph!");
		}
		
		return addVertex;
	}
	
	public boolean addGraphElement(KbEdge edge, WeightedPseudograph<KbVertex,KbEdge> graph){
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
		
		return addEdge;
	}
	
	private void computeTotalNumber(){
		StatelessSession session = (StatelessSession)SessionFactory.getInstance().getCurrentSession();
		IOntology onto = session.getOntology();
		
		int numIndividual = onto.getNumberOfIndividual();
		System.out.println("number of Individual: " + numIndividual);
		
		int numoPropertyMember = onto.getNumberOfObjectPropertyMember();
		System.out.println("number of ObjectPropertyMember: " + numoPropertyMember);
		
		TOTAL_NUMBER_OF_INDIVIDUAL = numIndividual;
		
		TOTAL_NUMBER_OF_PROPERTYMEMBER = numoPropertyMember;
	}
	
	private double computeEdgeWeight(double num, double totalNum){
		if(num == 0) {
			return Double.POSITIVE_INFINITY;
		}
		return 2-Math.log(1+num/totalNum)/Math.log(2);
	}
	
	private double computeVertexWeight(double num, double totalNum){
		if(num == 0) {
			return Double.POSITIVE_INFINITY;
		}
		return 2-Math.log(1+num/totalNum)/Math.log(2); 
	}
	
	private double computeWeight(INamedConcept concept){
		int numIndividual = concept.getNumberOfIndividuals();
		return computeVertexWeight(numIndividual,TOTAL_NUMBER_OF_INDIVIDUAL);
	}
	
	private double computeWeight(IProperty property){
		int numProMem = property.getNumberOfPropertyMember();
		return computeEdgeWeight(numProMem,TOTAL_NUMBER_OF_PROPERTYMEMBER);
	}  

	private void init() {
		
//		load ontology	
		m_onto = null;
		SesameConnection ses_con = null;
		try {
			try {
				ses_con = new SesameConnection(repositoryDir);
			} catch (Exception e2) {
				e2.printStackTrace();
			}

			try {
				m_onto = ses_con.loadOntology(PropertyUtils.convertToMap(parameters));
			} catch (OntologyLoadException e) {

				m_onto = ses_con.createOntology(PropertyUtils.convertToMap(parameters));

				try {
					addFileToRepository(m_onto, PropertyUtils.convertToMap(parameters));
				} catch (MissingParameterException e1) {
					e1.printStackTrace();
				}
			}
		} catch (DatasourceException e) {
			e.printStackTrace();
		} catch (MissingParameterException e) {
			e.printStackTrace();
		} catch (InvalidParameterException e) {
			e.printStackTrace();
		} catch (OntologyCreationException e) {
			e.printStackTrace();
		} 
		
		SesameSessionFactory sesame_factory = new SesameSessionFactory(new XMURIFactoryInsulated());
		ISession session = null;
		
		try {
			session = sesame_factory.openSession(ses_con, m_onto);
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
			m_session = factory.openSession(ses_con,m_onto);
		} catch (DatasourceException e) {
			e.printStackTrace();
		} catch (OpenSessionException e) {
			e.printStackTrace();
		}
								
	}

	private static void addFileToRepository(IOntology onto, Map<String, Object> parameters)throws MissingParameterException{
		
		if(!parameters.containsKey(ExploreEnvironment.ONTOLOGY_FILE_PATH)) {
			throw new MissingParameterException(ExploreEnvironment.ONTOLOGY_FILE_PATH+" missing!");
		}
		
		if(!parameters.containsKey(ExploreEnvironment.ONTOLOGY_FILE_NAME)) {
			throw new MissingParameterException(ExploreEnvironment.ONTOLOGY_FILE_NAME+" missing!");
		}
		
		if(!parameters.containsKey(ExploreEnvironment.BASE_ONTOLOGY_URI)) {
			throw new MissingParameterException(ExploreEnvironment.BASE_ONTOLOGY_URI+" missing!");
		}
		
		if(!parameters.containsKey(ExploreEnvironment.LANGUAGE)) {
			throw new MissingParameterException(ExploreEnvironment.LANGUAGE+" missing!");
		}
		
		String filePath = (String)parameters.get(ExploreEnvironment.ONTOLOGY_FILE_PATH);
		String baseUri = (String)parameters.get(ExploreEnvironment.BASE_ONTOLOGY_URI);
		String language = (String)parameters.get(ExploreEnvironment.LANGUAGE);
				
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
