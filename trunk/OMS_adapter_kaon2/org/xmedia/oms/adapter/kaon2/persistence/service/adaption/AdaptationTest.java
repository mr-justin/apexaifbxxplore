//package org.xmedia.oms.adapter.kaon2.persistence.service.adaption;
//
//import java.io.File;
//import java.net.URISyntaxException;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Properties;
//
//import org.xmedia.oms.adapter.kaon2.AdapterEnvironment;
//import org.xmedia.oms.adapter.kaon2.persistence.model.adaptation.ApplicationInteraction;
//import org.xmedia.oms.adapter.kaon2.persistence.model.adaptation.CognitiveAgent;
//import org.xmedia.oms.adapter.kaon2.persistence.model.adaptation.Content;
//import org.xmedia.oms.adapter.kaon2.persistence.model.adaptation.Entity;
//import org.xmedia.oms.adapter.kaon2.persistence.model.adaptation.IApplicationInteraction;
//import org.xmedia.oms.adapter.kaon2.persistence.model.adaptation.ICognitiveAgent;
//import org.xmedia.oms.adapter.kaon2.persistence.model.adaptation.IContent;
//import org.xmedia.oms.adapter.kaon2.persistence.model.adaptation.IEntity;
//import org.aifb.xxplore.shared.util.PropertyUtils;
//import org.semanticweb.kaon2.api.KAON2Exception;
//import org.semanticweb.kaon2.api.KAON2Manager;
//import org.semanticweb.kaon2.api.OntologyChangeEvent;
//import org.semanticweb.kaon2.api.formatting.OntologyFileFormat;
//import org.semanticweb.kaon2.api.logic.Literal;
//import org.semanticweb.kaon2.api.logic.Rule;
//import org.semanticweb.kaon2.api.logic.Term;
//import org.semanticweb.kaon2.api.logic.Variable;
//import org.semanticweb.kaon2.api.owl.elements.Individual;
//import org.semanticweb.kaon2.api.owl.elements.OWLClass;
//import org.semanticweb.kaon2.api.owl.elements.ObjectProperty;
//import org.xmedia.oms.adapter.kaon2.persistence.Kaon2ConnectionProvider;
//import org.xmedia.oms.adapter.kaon2.persistence.Kaon2DaoManager;
//import org.xmedia.oms.adapter.kaon2.persistence.Kaon2Ontology;
//import org.xmedia.oms.model.api.IOntology;
//import org.xmedia.oms.persistence.DatasourceException;
//import org.xmedia.oms.persistence.IConnectionProvider;
//import org.xmedia.oms.persistence.IKbConnection;
//import org.xmedia.oms.persistence.ISessionFactory;
//import org.xmedia.oms.persistence.InvalidParameterException;
//import org.xmedia.oms.persistence.KbEnvironment;
//import org.xmedia.oms.persistence.MissingParameterException;
//import org.xmedia.oms.persistence.OntologyLoadException;
//import org.xmedia.oms.persistence.OpenSessionException;
//import org.xmedia.oms.persistence.PersistenceUtil;
//import org.xmedia.oms.persistence.SessionFactory;
//import org.xmedia.oms.persistence.StatelessSession;
//import org.xmedia.oms.persistence.dao.DaoUnavailableException;
//import org.xmedia.oms.persistence.dao.IIndividualDao;
//import org.xmedia.oms.persistence.dao.IPropertyMemberAxiomDao;
//import org.semanticweb.kaon2.api.reasoner.*;
//
///**
// * @author lei
// * 
// */
//public class AdaptationTest {
//
//	private static IOntology m_onto;
//
//	private static IOntology m_adaptationOntology;
//
//	private static final String BASE_ONTOLOGY_URI = "file://PROJEKTE/Codes/Models/maasw.owl#";
//
//	private static IOntology loadOntology(IConnectionProvider provider,Properties props, String uri) throws DatasourceException,MissingParameterException, InvalidParameterException,OntologyLoadException {
//
//		/** ******** load ontology using the provider object *********** */
//		props.setProperty(KbEnvironment.PHYSICAL_ONTOLOGY_URI, uri);
//
//		return provider.getConnection().loadOntology(PropertyUtils.convertToMap(props));
//	}
//
//	private static void init() {
//
//		/** ******** create connection provider *********** */
//		// String providerClazz =
//		// parameters.getProperty(KbEnvironment.CONNECTION_PROVIDER_CLASS);
//		IConnectionProvider provider = new Kaon2ConnectionProvider();
//
//		/** ******** configure connection provider *********** */
//		Properties props = new Properties();
//		// set connection url, e.g. jdbc:hsqldb:hsql://localhost/
//		props.setProperty(KbEnvironment.CONNECTION_URL, "");
//		props.setProperty(AdapterEnvironment.BASE_ADAPTATION_ONTOLOGY_URI,"file://PROJEKTE/Codes/Models/maasw.owl");
//
//		// as kaon2 currently runs in memory only, no connection data to the db
//		// is required
//		// the product name of the database used by jena store, e.g. HSQL
//		props.setProperty(KbEnvironment.DB_PRODUCT_NAME, "");
//		// the driver class used for connection to the database, e.g.
//		// org.hsqldb.jdbcDriver
//		props.setProperty(KbEnvironment.DB_DRIVER_CLASS, "");
//
//		props.setProperty(KbEnvironment.TRANSACTION_CLASS,"org.xmedia.oms.adapter.kaon2.persistence.Kaon2Transaction");
//		props.setProperty(AdapterEnvironment.ADAPTATION_ONTOLOGY_URI,"file:../OMS/res/org/xmedia/oms/model/onto/metaknow/TaskPolicyFiat.owl");
//
//		provider.configure(props);
//
//		try {
//			m_onto = loadOntology(provider, props,"file:../OMS_adapter_kaon2/res/metaknow-example.xml");
//			m_adaptationOntology = loadOntology(provider, props,"file:../OMS/res/org/xmedia/oms/model/onto/maasw.owl");
//		} catch (DatasourceException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		} catch (MissingParameterException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		} catch (InvalidParameterException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		} catch (OntologyLoadException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//
//		/** ******** create a session *********** */
//		ISessionFactory factory = SessionFactory.getInstance();
//		// not much to configure now
//		factory.configure(PropertyUtils.convertToMap(props));
//		// set session factory
//		PersistenceUtil.setSessionFactory(factory);
//		// open a new session with the ontology
//		try {
//			factory.openSession(provider.getConnection(), m_onto);
//		} catch (DatasourceException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (OpenSessionException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//		/** ******** set a dao manager *********** */
//		// the dao manager provides the daos to be use to access the knowledge
//		// base
//		PersistenceUtil.setDaoManager(Kaon2DaoManager.getInstance());
//
//	}
//
//	private static IOntology getAdaptationOntology() {
//		IKbConnection conn = ((StatelessSession) SessionFactory.getInstance().getCurrentSession()).getConnection();
//		String ontoUri = (String) conn.getConfiguration().get(AdapterEnvironment.BASE_ADAPTATION_ONTOLOGY_URI);
//		return conn.findOntologyByUri(ontoUri);
//	}
//
//	private static String getBaseAdaptationOntologyUri() {
//		return (String)((StatelessSession)SessionFactory.getInstance().getCurrentSession()).getConnection().getConfiguration().get(AdapterEnvironment.BASE_ADAPTATION_ONTOLOGY_URI);
//	}
//	
//	private static void setRelatedContentAndProcessContext() {
//		StatelessSession session = (StatelessSession) PersistenceUtil.getSessionFactory().getCurrentSession();
//		IOntology activeOnto = session.getOntology();
//		try {
//			session.setOntology(m_adaptationOntology);
//			IIndividualDao indDao = PersistenceUtil.getDaoManager().getIndividualDao();
//			System.out.println("Exesting Individuals: " + indDao.findAll());
//			IPropertyMemberAxiomDao propMemDao = PersistenceUtil.getDaoManager().getPropertyMemberDao();
//			System.out.println("Exesting PropertyMembers: " + propMemDao.findAll() + "\n");
//			session.setOntology(activeOnto);
//		} catch (OntologyLoadException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (URISyntaxException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (DaoUnavailableException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		IContent content1 = new Content(BASE_ONTOLOGY_URI + "content1");
////		content1.storeContent();
//		IContent content2 = new Content(BASE_ONTOLOGY_URI + "content2");
//		content2.storeContent();
//		IContent content3 = new Content(BASE_ONTOLOGY_URI + "content3");
//		content3.storeContent();
//		IEntity entity1 = new Entity(BASE_ONTOLOGY_URI + "entity1");
//		entity1.storeEntity();
//		IEntity entity2 = new Entity(BASE_ONTOLOGY_URI + "entity2");
//		entity2.storeEntity();
//		ICognitiveAgent user = new CognitiveAgent(BASE_ONTOLOGY_URI + "user");
//		user.storeCognitiveAgent();
//		IApplicationInteraction interaction = new ApplicationInteraction(BASE_ONTOLOGY_URI + "interaction");
////		interaction.storeApplicationInteraction();
//		
//		try {
//			session.setOntology(m_adaptationOntology);
//			IIndividualDao indDao = PersistenceUtil.getDaoManager().getIndividualDao();
//			System.out.println("Exesting Individuals: " + indDao.findAll());
//			IPropertyMemberAxiomDao propMemDao = PersistenceUtil.getDaoManager().getPropertyMemberDao();
//			System.out.println("Exesting PropertyMembers: " + propMemDao.findAll() + "\n");
//			session.setOntology(activeOnto);
//		} catch (OntologyLoadException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (URISyntaxException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (DaoUnavailableException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		content1.setSubject(entity1);
//		content2.setSubject(entity1);
//		content3.setSubject(entity2);
//		interaction.setResource(content1);
//		interaction.setAgent(user);
//		
//		try {
//			session.setOntology(m_adaptationOntology);
//			IIndividualDao indDao = PersistenceUtil.getDaoManager().getIndividualDao();
//			System.out.println("Exesting Individuals: " + indDao.findAll());
//			IPropertyMemberAxiomDao propMemDao = PersistenceUtil.getDaoManager().getPropertyMemberDao();
//			System.out.println("Exesting PropertyMembers: " + propMemDao.findAll() + "\n");
//			session.setOntology(activeOnto);
//		} catch (OntologyLoadException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (URISyntaxException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (DaoUnavailableException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//	}
//
//	private static void storeRule() {
//		List<OntologyChangeEvent> changes = new ArrayList<OntologyChangeEvent>();
//		
//		Variable U = KAON2Manager.factory().variable("U");
//		Variable P = KAON2Manager.factory().variable("P");
//		Variable C1 = KAON2Manager.factory().variable("C1");
//		Variable C2 = KAON2Manager.factory().variable("C2");
//		Variable E = KAON2Manager.factory().variable("E");
//
//		OWLClass user = KAON2Manager.factory().owlClass(BASE_ONTOLOGY_URI + "Cognitive_Agent");
//		OWLClass content = KAON2Manager.factory().owlClass(BASE_ONTOLOGY_URI + "Content");
//		OWLClass interaction = KAON2Manager.factory().owlClass(BASE_ONTOLOGY_URI + "Application_Interaction");
//		OWLClass entity = KAON2Manager.factory().owlClass(BASE_ONTOLOGY_URI + "Entity");
//		
//		ObjectProperty resource = KAON2Manager.factory().objectProperty(BASE_ONTOLOGY_URI + "resource");
//		ObjectProperty experiencer = KAON2Manager.factory().objectProperty(BASE_ONTOLOGY_URI + "experiencer");
//		ObjectProperty has_subject = KAON2Manager.factory().objectProperty(BASE_ONTOLOGY_URI + "has_subject");
//		ObjectProperty is_recommended = KAON2Manager.factory().objectProperty(BASE_ONTOLOGY_URI + "is_recommended");
//		
//		Rule rule = KAON2Manager.factory().rule(
//				KAON2Manager.factory().literal(true, is_recommended,new Term[] { U, C2 }),
//				new Literal[] {
//						KAON2Manager.factory().literal(true, user,new Term[] { U }),
//						KAON2Manager.factory().literal(true, content,new Term[] { C1 }),
//						KAON2Manager.factory().literal(true, content,new Term[] { C2 }),
//						KAON2Manager.factory().literal(true, interaction,new Term[] { P }),
//						KAON2Manager.factory().literal(true, entity,new Term[] { E }),
//					
//						KAON2Manager.factory().literal(true, resource,new Term[] { P, C1 }),
//						KAON2Manager.factory().literal(true, experiencer,new Term[] { P, U }),
//						KAON2Manager.factory().literal(true, has_subject,new Term[] { C2, E }), 
//						KAON2Manager.factory().literal(true, has_subject,new Term[] { C1, E }), 
//				}
//				
//		);
//
//		changes.add(new OntologyChangeEvent(rule,OntologyChangeEvent.ChangeType.ADD));
//		
//		try {
//			IOntology onto = getAdaptationOntology();
//			if (onto instanceof Kaon2Ontology) {
//				((Kaon2Ontology) onto).getDelegate().applyChanges(changes);
//			}
//		} catch (KAON2Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		System.out.println("Rule has been stored.\n");
//	}
//	
//	private static void adapt() {
//		Variable U = KAON2Manager.factory().variable("U");
//		Variable C = KAON2Manager.factory().variable("C");
//		
//		ObjectProperty is_recommended = KAON2Manager.factory().objectProperty(BASE_ONTOLOGY_URI + "is_recommended");
//		ObjectProperty resource = KAON2Manager.factory().objectProperty(BASE_ONTOLOGY_URI + "resource");
//		OWLClass user = KAON2Manager.factory().owlClass(BASE_ONTOLOGY_URI + "Cognitive_Agent");
//		OWLClass content = KAON2Manager.factory().owlClass(BASE_ONTOLOGY_URI + "Content");
//		
//		IOntology onto = getAdaptationOntology();
//		try {
//			if (onto instanceof Kaon2Ontology) {
//				Reasoner reasoner = ((Kaon2Ontology) onto).getDelegate().createReasoner();
//
//				Query is_recomemnded_U_C = reasoner.createQuery(
//						new Literal[] {
//								KAON2Manager.factory().literal(true, user,new Term[] { U }),
//								KAON2Manager.factory().literal(true, content,new Term[] { C }),
//								KAON2Manager.factory().literal(true,is_recommended, new Term[] { U, C }) 
//						},
//						new Variable[] { U, C }
//				);
//				
//				is_recomemnded_U_C.open();
//				System.out.println("Query empty: " + is_recomemnded_U_C.afterLast());
//				
//				List<OntologyChangeEvent> changes = new ArrayList<OntologyChangeEvent>();
//				while (!is_recomemnded_U_C.afterLast()) {
//					Term[] tupleBuffer = is_recomemnded_U_C.tupleBuffer();
//					System.out.println("User '" + tupleBuffer[0].toString() + "' is_recommended  " + "Content '" + tupleBuffer[1].toString() + "'.");
//					changes.add(new OntologyChangeEvent(KAON2Manager.factory().objectPropertyMember(is_recommended,
//							(Individual) tupleBuffer[0],(Individual) tupleBuffer[1]),OntologyChangeEvent.ChangeType.ADD));
//					is_recomemnded_U_C.next();
//				}
//
//				if (onto instanceof Kaon2Ontology) {
//					((Kaon2Ontology) onto).getDelegate().applyChanges(changes);
//				}
//				
//				is_recomemnded_U_C.close();
//				is_recomemnded_U_C.dispose();
//				reasoner.dispose();
//			}
//		} catch (KAON2Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//	}
//
//	/**
//	 * @param args
//	 * @throws Exception
//	 */
//	public static void main(String[] args) throws Exception {
//
//		// init the connection
//		init();
//
//		// get the initialized session
//		StatelessSession session = (StatelessSession) PersistenceUtil.getSessionFactory().getCurrentSession();
//		IPropertyMemberAxiomDao propMemberDao = PersistenceUtil.getDaoManager().getPropertyMemberDao();
//		System.out.println(propMemberDao.findAll() + "\n");
//
//		setRelatedContentAndProcessContext();
//		
////		((Kaon2Ontology)m_adaptationOntology).getDelegate().saveOntology(OntologyFileFormat.OWL_RDF,new File("D:/zl/kaon2/src/maasw.owl"),"ISO-8859-1");
//		
//		storeRule();
//		
//		((Kaon2Ontology)m_adaptationOntology).getDelegate().saveOntology(OntologyFileFormat.OWL_RDF,new File("D:/zl/kaon2/src/maasw.owl"),"ISO-8859-1");
//		
//		adapt();
//		
//		((Kaon2Ontology)m_adaptationOntology).getDelegate().saveOntology(OntologyFileFormat.OWL_RDF,new File("D:/zl/kaon2/src/maasw.owl"),"ISO-8859-1");
//		
//	}
//
//}