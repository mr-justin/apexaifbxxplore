package org.xmedia.accessknow.sesame.examples;

import java.io.FileReader;
import java.net.URI;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.xmedia.accessknow.sesame.model.NamedConcept;
import org.xmedia.accessknow.sesame.persistence.ExtendedSesameDaoManager;
import org.xmedia.accessknow.sesame.persistence.SesameConnection;
import org.xmedia.accessknow.sesame.persistence.SesameRepositoryFactory;
import org.xmedia.accessknow.sesame.persistence.SesameSessionFactory;
import org.xmedia.businessobject.IBusinessObject;
import org.xmedia.oms.model.api.IDatatype;
import org.xmedia.oms.model.api.ILiteral;
import org.xmedia.oms.model.api.INamedConcept;
import org.xmedia.oms.model.api.IOntology;
import org.xmedia.oms.model.api.IProperty;
import org.xmedia.oms.model.impl.Datatype;
import org.xmedia.oms.model.impl.NamedIndividual;
import org.xmedia.oms.persistence.IKbConnection;
import org.xmedia.oms.persistence.ISession;
import org.xmedia.oms.persistence.ISessionFactory;
import org.xmedia.oms.persistence.KbEnvironment;
import org.xmedia.oms.persistence.dao.IDaoManager;
import org.xmedia.uris.impl.XMURIFactoryInsulated;

public class AKSesame2AdapterFindLabelTest {
	
	private static String ONTOLOGY_URI = "localhost";
	private static String LANGUAGE = IOntology.RDF_XML_LANGUAGE;
	private static String BASE_URI = "http://localhost/smw-devel";
	private static String ONTOLOGY_FILE_PATH = "c:/wikidaniel.owl";
	private static String ONTOLOGY_TYPE = SesameRepositoryFactory.RDFS_NATIVE;
	private static String REPOSITORY_PATH ="C:/Dokumente und Einstellungen/Daniel/Eigene Dateien/daniel/studium/Diplomarbeit/Code3/XXplore_core/res/test";
		
	/**
	 * Tests the findLabel methods in the responding daos.
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		
		System.out.println("AKSesame2AdapterTest.main startet!");
		
		// Create the Sesame Connection and the SessionFactory 
		IKbConnection connection = new SesameConnection(REPOSITORY_PATH);
		ISessionFactory sessionFactory = new SesameSessionFactory(new XMURIFactoryInsulated());


		// *****************************************************
		// Retrieve the ontology
		// *****************************************************
		Map<String, Object> parameters = new Hashtable<String, Object>();
		parameters.put(KbEnvironment.ONTOLOGY_URI, new URI(ONTOLOGY_URI));
		parameters.put(KbEnvironment.ONTOLOGY_TYPE, new URI(ONTOLOGY_TYPE));
		IOntology persistedOntology = connection.loadOntology(parameters);
		persistedOntology.importOntology(LANGUAGE, BASE_URI, new FileReader(ONTOLOGY_FILE_PATH));

		try {
			// *****************************************************
			// Insert statements in the ontology and list some of them
			// *****************************************************
			ISession theSession = sessionFactory.openSession(connection, persistedOntology);
			ExtendedSesameDaoManager theDao = (ExtendedSesameDaoManager) theSession.getDaoManager();

			try {			
				
				for(IBusinessObject con: theDao.getConceptDao().findAll()){
					System.out.println("Concept:"+con+" "+ theDao.getConceptDao().findLabel((NamedConcept)con));
				}
				for(IBusinessObject prop: theDao.getPropertyDao().findAll()){
					System.out.println("Property:"+prop+" "+ theDao.getPropertyDao().findLabel((IProperty)prop));
				}
				for(IBusinessObject indi: theDao.getIndividualDao().findAll()){
					System.out.println("Individual:"+theDao.getIndividualDao().findLabel((NamedIndividual)indi)+" "+indi);
				}
//				for(IBusinessObject literal: theDao.getLiteralDao().findAll()){
//					System.out.println("Literal:"+literal);
//				}
//				for(IBusinessObject entity: theDao.getEntityDao().findAll()){
//					System.out.println("Entity:"+entity);
//				}
//				for(IBusinessObject schema:theDao.getSchemaDao().findAll()){
//					System.out.println("Schema:"+schema);
//				}
//				for(IBusinessObject PropertyMember:theDao.getPropertyMemberDao().findAll()){
//					System.out.println("PropertyMember:"+PropertyMember);
//				}
				for(IBusinessObject Datatype:theDao.getDatatypeDao().findAll()){
					System.out.println("Datatype:"+ Datatype);
				}
				
				
			} finally {
				theSession.close();
			}
			
		} finally {
			connection.closeOntology(persistedOntology);
		}

//		
//		// *****************************************************
//		// Create a volatile ontology and import the serialization
//		// *****************************************************
//		Map<String, Object> tmpOntologyParameters = new Hashtable<String, Object>();
//		tmpOntologyParameters.put(KbEnvironment.ONTOLOGY_URI, new URI("http://www.x-media-project.com/tmp_1"));
//		tmpOntologyParameters.put(KbEnvironment.ONTOLOGY_TYPE, new URI(SesameRepositoryFactory.RDF_MEMORY_VOLATILE));
//
//		IOntology volatileOntology = connection.createOntology(tmpOntologyParameters);
//
//		try {
//			// Import
//			volatileOntology.importOntology(serializationLanguage, "http://www.x-media-project.com", new StringReader(serializedOntology));
//
//			// Get the query evaluator
//			ISession theSession = sessionFactory.openSession(connection, volatileOntology);
//			
//			try {
//				IQueryEvaluator sparqlEvaluator = theSession.getDaoManager().getAvailableEvaluator(IDaoManager.SPARQL_QUERYTYPE);
//
//				// *****************************************************
//				// Query via sparql the volatile ontology
//				// *****************************************************
//				Set<ITuple> results =
//					sparqlEvaluator.evaluate(new QueryWrapper("SELECT ?subject ?property ?object WHERE {?subject ?property ?object .}", null)).getResult();
//
//				// Print the results
//				System.out.println("Sparql results:");
//				for (ITuple aResult : results) {
//					for (int i = 0; i < aResult.getArity(); i++)
//						System.out.println("Variable: '" + aResult.getLabelAt(i) + "' :: Value: '" + aResult.getElementAt(i) + "'");
//				}
//			} finally {
//				theSession.close();
//			}
//			
//		} finally {
//			connection.closeOntology(volatileOntology);
//		}

	}

}
