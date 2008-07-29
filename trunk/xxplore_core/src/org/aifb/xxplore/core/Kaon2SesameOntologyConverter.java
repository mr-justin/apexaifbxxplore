package org.aifb.xxplore.core;
//package org.aifb.xxplore.core.converter;
//
//import java.io.File;
//import java.io.FileReader;
//import java.io.IOException;
//import java.net.URI;
//import java.net.URISyntaxException;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.Hashtable;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//
//import org.xmedia.accessknow.sesame.persistence.SesameConnection;
//import org.xmedia.accessknow.sesame.persistence.SesameRepositoryFactory;
//import org.xmedia.accessknow.sesame.persistence.SesameSessionFactory;
//import org.xmedia.oms.model.api.IOntology;
//import org.xmedia.oms.model.api.IPropertyMember;
//import org.xmedia.oms.model.api.OntologyImportException;
//import org.xmedia.oms.model.impl.Axiom;
//import org.xmedia.oms.model.impl.NamedIndividual;
//import org.xmedia.oms.model.impl.Property;
//import org.xmedia.oms.model.impl.PropertyMember;
//import org.xmedia.oms.persistence.IKbConnection;
//import org.xmedia.oms.persistence.ISession;
//import org.xmedia.oms.persistence.ISessionFactory;
//import org.xmedia.oms.persistence.InvalidParameterException;
//import org.xmedia.oms.persistence.KbEnvironment;
//import org.xmedia.oms.persistence.MissingParameterException;
//import org.xmedia.oms.persistence.OntologyCreationException;
//import org.xmedia.oms.persistence.OpenSessionException;
//import org.xmedia.oms.persistence.dao.DaoUnavailableException;
//import org.xmedia.oms.persistence.dao.IPropertyMemberAxiomDao;
//
//public class Kaon2SesameOntologyConverter {
//
//	private static final String REPOSITORY_PATH = "/sesame/example";
//
//	public static void main(String[] args) {
//		
//		String importFrom = "../OMS/res/org/xmedia/oms/model/onto/metaknow/metaknow-example.trix";
//		String exportTo = "../OMS/res/org/xmedia/oms/model/onto/metaknow/metaknow-example.exported.owl";
//		
//		convert(new File(importFrom), new File(exportTo));
//		
//	}
//	
//	public static void convert(File sesameFile, File kaon2Output) {
//
//		try {
//			// Create the Sesame Connection and the SessionFactory 
//			IKbConnection connection = new SesameConnection(REPOSITORY_PATH);
//			ISessionFactory sessionFactory = new SesameSessionFactory(new XMURIFactoryInsulated());
//	
//			String serializationLanguage = IOntology.N3_LANGUAGE;
//			String serializedOntology = "";
//	
//			// *****************************************************
//			// Retrieve the ontology
//			// *****************************************************
//			Map<String, Object> parameters = new Hashtable<String, Object>();
//			parameters.put(KbEnvironment.ONTOLOGY_URI, new URI("file:../OMS/res/org/xmedia/oms/model/onto/metaknow/export_fiat_prototype_071011-1034.trix"));
//			parameters.put(KbEnvironment.ONTOLOGY_TYPE, new URI(SesameRepositoryFactory.RDF_MEMORY_VOLATILE));
//			IOntology persistedOntology = connection.loadOrCreateOntology(parameters);
//	
//	//		persistedOntology.importOntology(IOntology.TRIX_LANGUAGE, "", new FileReader("../OMS/res/org/xmedia/oms/model/onto/metaknow/export_fiat_prototype_071011-1034.trix"));
//			persistedOntology.importOntology(IOntology.TRIX_LANGUAGE, "", new FileReader(sesameFile));
//			
//	    	DefaultOntologyResolver resolver=new DefaultOntologyResolver();
//	        KAON2Connection k2conn = KAON2Manager.newConnection();
//	        k2conn.setOntologyResolver(resolver);
//	
//			Ontology k2onto = k2conn.createOntology(persistedOntology.getUri(), new HashMap<String,Object>());
//			
//			KAON2Factory factory = KAON2Manager.factory();
//			
//			// *****************************************************
//			// Insert statements in the ontology and list some of them
//			// *****************************************************
//			ISession theSession = sessionFactory.openSession(connection, persistedOntology);
//			IPropertyMemberAxiomDao propMemberDao = theSession.getDaoManager().getPropertyMemberDao();
//
//			List<OntologyChangeEvent> changes = new ArrayList<OntologyChangeEvent>();
//			
//			List<IPropertyMember> propertyMembers = propMemberDao.findAll();
//			for (IPropertyMember propertyMember : propertyMembers) {
//				System.out.println(propertyMember);
//				if (propertyMember.getType() == PropertyMember.OBJECT_PROPERTY_MEMBER) {
//					System.out.println(propertyMember.getProperty().getUri());
//					if (propertyMember.getProperty().getUri().equals(Property.IS_INSTANCE_OF.getUri())) {
//						changes.add(new OntologyChangeEvent(factory.classMember(
//								factory.owlClass(((NamedIndividual)propertyMember.getTarget()).getUri()), 
//								factory.individual(((NamedIndividual)propertyMember.getSource()).getUri())),
//								OntologyChangeEvent.ChangeType.ADD));
//					}
//					else {
//						changes.add(new OntologyChangeEvent(factory.objectPropertyMember(
//								factory.objectProperty(propertyMember.getProperty().getUri()), 
//								factory.individual(((NamedIndividual)propertyMember.getSource()).getUri()), 
//								factory.individual(((NamedIndividual)propertyMember.getTarget()).getUri())),
//								OntologyChangeEvent.ChangeType.ADD)
//						);
//					}
//				}
//				else {
//					changes.add(new OntologyChangeEvent(factory.dataPropertyMember(
//							factory.dataProperty(propertyMember.getProperty().getUri()), 
//							factory.individual(((NamedIndividual)propertyMember.getSource()).getUri()), 
//							factory.constant(propertyMember.getTarget())),
//							OntologyChangeEvent.ChangeType.ADD)
//					);
//				}
//			}
//
//			k2onto.applyChanges(changes);
//			
//			Set<Axiom> axioms = k2onto.createAxiomRequest().getAll();
//			for (Axiom axiom : axioms) {
//				System.out.println(axiom);
//			}
//			
//			k2onto.saveOntology(OntologyFileFormat.OWL_XML, kaon2Output, "UTF-8");
//
//		} catch (RepositoryException e) {
//			e.printStackTrace();
//		} catch (KAON2Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (OpenSessionException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (DaoUnavailableException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (OntologyImportException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (URISyntaxException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (MissingParameterException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (InvalidParameterException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (OntologyCreationException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
//}
