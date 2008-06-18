//package org.xmedia.oms.adapter.kaon2.ex;
//
//import java.io.FileReader;
//import java.io.StringReader;
//import java.io.StringWriter;
//import java.net.URI;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.Hashtable;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//
////import org.aifb.xxplore.shared.vocabulary.RDFS;
//import org.semanticweb.kaon2.api.Axiom;
//import org.semanticweb.kaon2.api.DefaultOntologyResolver;
//import org.semanticweb.kaon2.api.KAON2Connection;
//import org.semanticweb.kaon2.api.KAON2Factory;
//import org.semanticweb.kaon2.api.KAON2Manager;
//import org.semanticweb.kaon2.api.Ontology;
//import org.semanticweb.kaon2.api.OntologyChangeEvent;
//import org.xmedia.accessknow.sesame.persistence.SesameConnection;
//import org.xmedia.accessknow.sesame.persistence.SesameRepositoryFactory;
//import org.xmedia.accessknow.sesame.persistence.SesameSessionFactory;
//import org.xmedia.oms.model.api.IConcept;
//import org.xmedia.oms.model.api.IIndividual;
//import org.xmedia.oms.model.api.INamedConcept;
//import org.xmedia.oms.model.api.INamedIndividual;
//import org.xmedia.oms.model.api.IOntology;
//import org.xmedia.oms.model.api.IPropertyMember;
//import org.xmedia.oms.model.impl.Individual;
//import org.xmedia.oms.model.impl.NamedIndividual;
//import org.xmedia.oms.model.impl.Property;
//import org.xmedia.oms.model.impl.PropertyMember;
//import org.xmedia.oms.persistence.IKbConnection;
//import org.xmedia.oms.persistence.ISession;
//import org.xmedia.oms.persistence.ISessionFactory;
//import org.xmedia.oms.persistence.ITransaction;
//import org.xmedia.oms.persistence.KbEnvironment;
//import org.xmedia.oms.persistence.dao.IConceptDao;
//import org.xmedia.oms.persistence.dao.IDaoManager;
//import org.xmedia.oms.persistence.dao.IIndividualDao;
//import org.xmedia.oms.persistence.dao.IPropertyMemberAxiomDao;
//import org.xmedia.oms.query.IQueryEvaluator;
//import org.xmedia.oms.query.ITuple;
//import org.xmedia.oms.query.QueryWrapper;
//import org.xmedia.uris.impl.XMURIFactoryInsulated;
//
//public class Kaon2SesameOntologyConverter {
//
//	private static final String REPOSITORY_PATH = "/sesame/example";
//
//	public static void main(String[] args) throws Exception {
//
//		// Create the Sesame Connection and the SessionFactory 
//		IKbConnection connection = new SesameConnection(REPOSITORY_PATH);
//		ISessionFactory sessionFactory = new SesameSessionFactory(new XMURIFactoryInsulated());
//
//		String serializationLanguage = IOntology.N3_LANGUAGE;
//		String serializedOntology = "";
//
//		// *****************************************************
//		// Retrieve the ontology
//		// *****************************************************
//		Map<String, Object> parameters = new Hashtable<String, Object>();
//		parameters.put(KbEnvironment.PHYSICAL_ONTOLOGY_URI, new URI("file:../OMS/res/org/xmedia/oms/model/onto/metaknow/export_fiat_prototype_071011-1034.trix"));
//		parameters.put(KbEnvironment.ONTOLOGY_TYPE, new URI(SesameRepositoryFactory.RDF_MEMORY_VOLATILE));
//		IOntology persistedOntology = connection.loadOrCreateOntology(parameters);
//
////		persistedOntology.importOntology(IOntology.TRIX_LANGUAGE, "", new FileReader("../OMS/res/org/xmedia/oms/model/onto/metaknow/export_fiat_prototype_071011-1034.trix"));
//		persistedOntology.importOntology(IOntology.TRIX_LANGUAGE, "", new FileReader("../OMS/res/org/xmedia/oms/model/onto/metaknow/metaknow-example.trix"));
//		
//    	DefaultOntologyResolver resolver=new DefaultOntologyResolver();
//        KAON2Connection k2conn = KAON2Manager.newConnection();
//        k2conn.setOntologyResolver(resolver);
//
//		Ontology k2onto = k2conn.createOntology(persistedOntology.getUri(), new HashMap<String,Object>());
//		
//		KAON2Factory factory = KAON2Manager.factory();
//		
//		try {
//			// *****************************************************
//			// Insert statements in the ontology and list some of them
//			// *****************************************************
//			ISession theSession = sessionFactory.openSession(connection, persistedOntology);
//			IPropertyMemberAxiomDao propMemberDao = theSession.getDaoManager().getPropertyMemberDao();
//
//			List<OntologyChangeEvent> changes = new ArrayList<OntologyChangeEvent>();
//			
//			try {
//				List<IPropertyMember> propertyMembers = propMemberDao.findAll();
//				for (IPropertyMember propertyMember : propertyMembers) {
//					System.out.println(propertyMember);
//					if (propertyMember.getType() == PropertyMember.OBJECT_PROPERTY_MEMBER) {
//						System.out.println(propertyMember.getProperty().getUri());
//						if (propertyMember.getProperty().getUri().equals(Property.IS_INSTANCE_OF.getUri())) {
//							changes.add(new OntologyChangeEvent(factory.classMember(
//									factory.owlClass(((NamedIndividual)propertyMember.getTarget()).getUri()), 
//									factory.individual(((NamedIndividual)propertyMember.getSource()).getUri())),
//									OntologyChangeEvent.ChangeType.ADD));
//						}
//						else {
//							changes.add(new OntologyChangeEvent(factory.objectPropertyMember(
//									factory.objectProperty(propertyMember.getProperty().getUri()), 
//									factory.individual(((NamedIndividual)propertyMember.getSource()).getUri()), 
//									factory.individual(((NamedIndividual)propertyMember.getTarget()).getUri())),
//									OntologyChangeEvent.ChangeType.ADD)
//							);
//						}
//					}
//					else {
//						changes.add(new OntologyChangeEvent(factory.dataPropertyMember(
//								factory.dataProperty(propertyMember.getProperty().getUri()), 
//								factory.individual(((NamedIndividual)propertyMember.getSource()).getUri()), 
//								factory.constant(propertyMember.getTarget())),
//								OntologyChangeEvent.ChangeType.ADD)
//						);
//					}
//				}
//
//			} finally {
//				theSession.close();
//			}
//			
//			k2onto.applyChanges(changes);
//			
//			Set<Axiom> axioms = k2onto.createAxiomRequest().getAll();
//			for (Axiom axiom : axioms) {
//				System.out.println(axiom);
//			}
//
//		} finally {
//			connection.closeOntology(persistedOntology);
//		}
//	}
//}
