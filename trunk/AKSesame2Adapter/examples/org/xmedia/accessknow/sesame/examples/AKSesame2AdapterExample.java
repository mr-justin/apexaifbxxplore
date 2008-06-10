package org.xmedia.accessknow.sesame.examples;

import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import org.aifb.xxplore.shared.vocabulary.RDFS;
import org.xmedia.accessknow.sesame.persistence.SesameConnection;
import org.xmedia.accessknow.sesame.persistence.SesameRepositoryFactory;
import org.xmedia.accessknow.sesame.persistence.SesameSessionFactory;
import org.xmedia.oms.model.api.IOntology;
import org.xmedia.oms.model.api.IPropertyMember;
import org.xmedia.oms.persistence.IKbConnection;
import org.xmedia.oms.persistence.ISession;
import org.xmedia.oms.persistence.ISessionFactory;
import org.xmedia.oms.persistence.ITransaction;
import org.xmedia.oms.persistence.KbEnvironment;
import org.xmedia.oms.persistence.dao.IDaoManager;
import org.xmedia.oms.persistence.dao.IPropertyMemberAxiomDao;
import org.xmedia.oms.query.IQueryEvaluator;
import org.xmedia.oms.query.ITuple;
import org.xmedia.oms.query.QueryWrapper;
import org.xmedia.uris.impl.XMURIFactoryInsulated;

public class AKSesame2AdapterExample {

	private static final String REPOSITORY_PATH = "/sesame/example";

	/***
	 * Dependencies:
	 * <ul>
	 * 	<li>AccessKnow: trunk/OMS, trunk/util</li>
	 * 	<li>openrdf-sesame-2.0-onejar.jar</li>
	 * 	<li>Sesame dependencies</li>
	 * </ul>
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		// Create the Sesame Connection and the SessionFactory 
		IKbConnection connection = new SesameConnection(REPOSITORY_PATH);
		ISessionFactory sessionFactory = new SesameSessionFactory(new XMURIFactoryInsulated());

		String serializationLanguage = IOntology.N3_LANGUAGE;
		String serializedOntology = "";

		// *****************************************************
		// Retrieve the ontology
		// *****************************************************
		Map<String, Object> parameters = new Hashtable<String, Object>();
		parameters.put(KbEnvironment.ONTOLOGY_URI, new URI("http://www.x-media-project.com/example_1"));
		parameters.put(KbEnvironment.ONTOLOGY_TYPE, new URI(SesameRepositoryFactory.RDFS_MEMORY_PERSISTENT));
		IOntology persistedOntology = connection.loadOrCreateOntology(parameters);

		try {
			// *****************************************************
			// Insert statements in the ontology and list some of them
			// *****************************************************
			ISession theSession = sessionFactory.openSession(connection, persistedOntology);
			IPropertyMemberAxiomDao theDao = theSession.getDaoManager().getPropertyMemberDao();

			try {

				// Transactions are OPTIONAL
				ITransaction aTransaction = theSession.beginTransaction();
				try {

					theDao.insert(
							persistedOntology.createNamedIndividual("http://www.x-media-project.com/example_1#r1"),
							persistedOntology.createProperty("http://www.x-media-project.com/example_1#property_1"),
							persistedOntology.createNamedIndividual("http://www.x-media-project.com/example_1#r2"));

					theDao.insert(
							persistedOntology.createNamedIndividual("http://www.x-media-project.com/example_1#r2"),
							persistedOntology.createProperty(RDFS.LABEL),
							persistedOntology.createStringLiteral("This is a test", "en"));

					theDao.insert(
							persistedOntology.createNamedIndividual("http://www.x-media-project.com/example_1#r1"),
							persistedOntology.createProperty("http://www.x-media-project.com/example_1#property_2"),
							persistedOntology.createNamedIndividual("http://www.x-media-project.com/example_1#r3"));

					aTransaction.commit();
				} catch (Exception e) {
					aTransaction.rollback();
					throw e;
				}

				// List statments with r1 as subject
				Set<IPropertyMember> someStatements =
					theDao.findBySourceIndividual(persistedOntology.createNamedIndividual("http://www.x-media-project.com/example_1#r1"));

				System.out.println("Statements with r1 as subject #" + someStatements.size() + " :");
				for (IPropertyMember aStatement : someStatements)
					System.out.println(aStatement);

			} finally {
				theSession.close();
			}


			// *****************************************************
			// Export a serialization of the ontology
			// *****************************************************
			StringWriter sw = new StringWriter();
			persistedOntology.export(serializationLanguage, sw);
			serializedOntology = sw.toString();
			System.out.println("Ontology exported:\n" + serializedOntology);
			
		} finally {
			connection.closeOntology(persistedOntology);
		}

		
		// *****************************************************
		// Create a volatile ontology and import the serialization
		// *****************************************************
		Map<String, Object> tmpOntologyParameters = new Hashtable<String, Object>();
		tmpOntologyParameters.put(KbEnvironment.ONTOLOGY_URI, new URI("http://www.x-media-project.com/tmp_1"));
		tmpOntologyParameters.put(KbEnvironment.ONTOLOGY_TYPE, new URI(SesameRepositoryFactory.RDF_MEMORY_VOLATILE));

		IOntology volatileOntology = connection.createOntology(tmpOntologyParameters);

		try {
			// Import
			volatileOntology.importOntology(serializationLanguage, "http://www.x-media-project.com", new StringReader(serializedOntology));

			// Get the query evaluator
			ISession theSession = sessionFactory.openSession(connection, volatileOntology);
			
			try {
				IQueryEvaluator sparqlEvaluator = theSession.getDaoManager().getAvailableEvaluator(IDaoManager.SPARQL_QUERYTYPE);

				// *****************************************************
				// Query via sparql the volatile ontology
				// *****************************************************
				Set<ITuple> results =
					sparqlEvaluator.evaluate(new QueryWrapper("SELECT ?subject ?property ?object WHERE {?subject ?property ?object .}", null)).getResult();

				// Print the results
				System.out.println("Sparql results:");
				for (ITuple aResult : results) {
					for (int i = 0; i < aResult.getArity(); i++)
						System.out.println("Variable: '" + aResult.getLabelAt(i) + "' :: Value: '" + aResult.getElementAt(i) + "'");
				}
			} finally {
				theSession.close();
			}
			
		} finally {
			connection.closeOntology(volatileOntology);
		}

	}

}
