package org.ateam.xxplore.core.service.sampling;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;


import org.aifb.xxplore.shared.util.Pair;
import org.aifb.xxplore.shared.util.PropertyUtils;
import org.apache.log4j.Logger;
import org.ateam.xxplore.core.service.search.KbEdge;
import org.ateam.xxplore.core.service.search.KbElement;
import org.ateam.xxplore.core.service.search.KbVertex;
import org.jgrapht.graph.WeightedPseudograph;
import org.xmedia.oms.adapter.kaon2.persistence.Kaon2ConnectionProvider;
import org.xmedia.oms.adapter.kaon2.persistence.Kaon2DaoManager;
import org.xmedia.oms.model.api.IConcept;
import org.xmedia.oms.model.api.IIndividual;
import org.xmedia.oms.model.api.INamedConcept;
import org.xmedia.oms.model.api.IObjectProperty;
import org.xmedia.oms.model.api.IOntology;
import org.xmedia.oms.model.api.IProperty;
import org.xmedia.oms.model.api.IPropertyMember;
import org.xmedia.oms.model.api.IResource;
import org.xmedia.oms.model.impl.DataProperty;
import org.xmedia.oms.model.impl.NamedConcept;
import org.xmedia.oms.model.impl.ObjectProperty;
import org.xmedia.oms.model.impl.Property;
import org.xmedia.oms.persistence.DatasourceException;
import org.xmedia.oms.persistence.IConnectionProvider;
import org.xmedia.oms.persistence.ISession;
import org.xmedia.oms.persistence.ISessionFactory;
import org.xmedia.oms.persistence.InvalidParameterException;
import org.xmedia.oms.persistence.KbEnvironment;
import org.xmedia.oms.persistence.MissingParameterException;
import org.xmedia.oms.persistence.OntologyLoadException;
import org.xmedia.oms.persistence.OpenSessionException;
import org.xmedia.oms.persistence.PersistenceUtil;
import org.xmedia.oms.persistence.SessionFactory;
import org.xmedia.oms.persistence.StatelessSession;
import org.xmedia.oms.persistence.dao.IConceptDao;
import org.xmedia.oms.persistence.dao.IPropertyDao;
import org.xmedia.oms.persistence.dao.IPropertyMemberAxiomDao;


public class TestGraphIndexWithKaon2 {
	
	private static Logger s_log = Logger.getLogger(TestGraphIndexWithSesame2.class);
	
	private static IOntology m_onto;
	
	private static ISession m_session;
	
	private static String structureIndexDir = "D:\\BTC\\sampling\\structureIndex"; 
	
	public static int TOTAL_NUMBER_OF_INDIVIDUAL = 1; 
	
	public static int TOTAL_NUMBER_OF_PROPERTYMEMBER = 1;
	
	private static WeightedPseudograph<KbVertex,KbEdge> resourceGraph;
	
	private static String ONTOLOGY_FILE_PATH = "file:///D:/BTC/viewAIFB_OWL.owl";
	
	private static String ONTOLOGY_URI = "viewAIFB_OWL";
	
	public static void main(String[] args) throws Exception {
		init();
		
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

			// save graphIndex to the file *.graph
			String path = (structureIndexDir.endsWith(File.separator) ? structureIndexDir + ONTOLOGY_URI + ".graph" : 
				structureIndexDir + File.separator + ONTOLOGY_URI + ".graph");
			File graphIndex = new File(path);
			if(!graphIndex.exists()){
				graphIndex.getParentFile().mkdirs();
				graphIndex.createNewFile();
			}
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(graphIndex));
			out.writeObject(resourceGraph);
			out.close();

			
		}
		
		for(KbVertex vertex : resourceGraph.vertexSet()){
			System.out.println("vertex: " + vertex + "\n(" + vertex.getCost() + ")");
		}
		for(KbEdge edge : resourceGraph.edgeSet()){
			System.out.println("edge: " + edge + "\n(" + edge.getCost() + ")");
		}
		
		// retrieve graphIndex
		String path = (structureIndexDir.endsWith(File.separator) ? structureIndexDir + ONTOLOGY_URI + ".graph" : 
			structureIndexDir + File.separator + ONTOLOGY_URI + ".graph");
		ObjectInputStream in = new ObjectInputStream(new FileInputStream(path));
		WeightedPseudograph<KbVertex,KbEdge> newResourceGraph = (WeightedPseudograph<KbVertex,KbEdge>)in.readObject(); 
		in.close();
		
		System.out.println("\n" + "new graph:");
		for(KbVertex vertex : newResourceGraph.vertexSet()){
			System.out.println("vertex: " + vertex + "\n(" + vertex.getCost() + ")");
		}
		for(KbEdge edge : newResourceGraph.edgeSet()){
			System.out.println("edge: " + edge + "\n(" + edge.getCost() + ")");
		}
		
	}
	
	public static void addGraphElement(KbVertex vertex1, KbVertex vertex2, IProperty property, WeightedPseudograph<KbVertex,KbEdge> graph){
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
	
	private static IOntology loadOntology(IConnectionProvider provider,Properties props, String uri) throws DatasourceException,MissingParameterException, InvalidParameterException,OntologyLoadException {

		/** ******** load ontology using the provider object *********** */
		props.setProperty(KbEnvironment.PHYSICAL_ONTOLOGY_URI, uri);

		return provider.getConnection().loadOntology(PropertyUtils.convertToMap(props));
	}

	private static void init() {

		/** ******** create connection provider *********** */
		// String providerClazz =
		// parameters.getProperty(KbEnvironment.CONNECTION_PROVIDER_CLASS);
		IConnectionProvider provider = new Kaon2ConnectionProvider();

		/** ******** configure connection provider *********** */
		Properties props = new Properties();
		// set connection url, e.g. jdbc:hsqldb:hsql://localhost/
		props.setProperty(KbEnvironment.CONNECTION_URL, "");

		// as kaon2 currently runs in memory only, no connection data to the db
		// is required
		// the product name of the database used by jena store, e.g. HSQL
		props.setProperty(KbEnvironment.DB_PRODUCT_NAME, "");
		// the driver class used for connection to the database, e.g.
		// org.hsqldb.jdbcDriver
		props.setProperty(KbEnvironment.DB_DRIVER_CLASS, "");

		props.setProperty(KbEnvironment.TRANSACTION_CLASS,"org.xmedia.oms.adapter.kaon2.persistence.Kaon2Transaction");

		provider.configure(props);

		try {
			m_onto = loadOntology(provider, props, ONTOLOGY_FILE_PATH);
		} catch (DatasourceException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (MissingParameterException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InvalidParameterException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (OntologyLoadException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		/** ******** create a session *********** */
		ISessionFactory factory = SessionFactory.getInstance();
		// not much to configure now
		factory.configure(PropertyUtils.convertToMap(props));
		// set session factory
		PersistenceUtil.setSessionFactory(factory);
		// open a new session with the ontology
		try {
			factory.openSession(provider.getConnection(), m_onto);
		} catch (DatasourceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OpenSessionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		/** ******** set a dao manager *********** */
		// the dao manager provides the daos to be use to access the knowledge
		// base
		PersistenceUtil.setDaoManager(Kaon2DaoManager.getInstance());

	}

}
