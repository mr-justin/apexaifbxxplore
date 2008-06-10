package org.xmedia.accessknow.sesame.examples;

import java.io.StringWriter;
import java.net.URI;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.xmedia.accessknow.sesame.persistence.SesameConnection;
import org.xmedia.accessknow.sesame.persistence.SesameRepositoryFactory;
import org.xmedia.accessknow.sesame.persistence.SesameSessionFactory;
import org.xmedia.oms.metaknow.IProvenance;
import org.xmedia.oms.model.api.INamedIndividual;
import org.xmedia.oms.model.api.IOntology;
import org.xmedia.oms.model.api.IProperty;
import org.xmedia.oms.model.api.IPropertyMember;
import org.xmedia.oms.persistence.IKbConnection;
import org.xmedia.oms.persistence.ISession;
import org.xmedia.oms.persistence.ISessionFactory;
import org.xmedia.oms.persistence.KbEnvironment;
import org.xmedia.oms.persistence.dao.BODeletionException;
import org.xmedia.oms.persistence.dao.IPropertyMemberAxiomDao;
import org.xmedia.uris.impl.XMURIFactoryInsulated;

public class AKSesame2AdapterMetaknowledgeExample {

	private static final String REPOSITORY_PATH = "/sesame/example";

	private static void clearOntology(IPropertyMemberAxiomDao theDao) {
		
		List<IPropertyMember> all = theDao.findAll();
		
		for (IPropertyMember propertyMember : all)
			try {theDao.delete(propertyMember);} catch (BODeletionException e) {}
			
	}
	
	/***
	 * Dependencies:
	 * <ul>
	 * 	<li>AccessKnow: trunk/OMS, trunk/util</li>
	 * 	<li>openrdf-sesame-2.0-beta5-onejar.jar</li>
	 * 	<li>aduna-io-1.2.jar</li>
	 * 	<li>slf4j-api-1.3.0.jar</li>
	 * 	<li>slf4j-jdk14-1.3.0.jar</li>
	 * </ul>
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		// Create the Sesame Connection, the SessionFactory, and load (or create) the ontology 
		IKbConnection connection = new SesameConnection(REPOSITORY_PATH);
		ISessionFactory sessionFactory = new SesameSessionFactory(new XMURIFactoryInsulated());

		Map<String, Object> parameters = new Hashtable<String, Object>();
		parameters.put(KbEnvironment.ONTOLOGY_URI, new URI("http://www.x-media-project.com/example_1"));
		parameters.put(KbEnvironment.ONTOLOGY_TYPE, new URI(SesameRepositoryFactory.RDFS_MEMORY_PERSISTENT));
		IOntology ontology = connection.loadOrCreateOntology(parameters);
		
		try {
			ISession theSession = sessionFactory.openSession(connection, ontology);
			IPropertyMemberAxiomDao theDao = theSession.getDaoManager().getPropertyMemberDao();
			// (Clear ontology just for sake of demonstration simplicity)
			clearOntology(theDao);

			try {
				
				// *****************************************************
				// Each inserted statement is reified: use getUri to refer its "reification".
				// *****************************************************
				IPropertyMember aStatement = theDao.insert(
						ontology.createNamedIndividual("http://www.x-media-project.com/example_1#r1"),
						ontology.createProperty("http://www.x-media-project.com/example_1#property_1"),
						ontology.createNamedIndividual("http://www.x-media-project.com/example_1#r2"));
				String reificationUri = aStatement.getUri();

				System.out.println("Uri of statement:\n" + aStatement + "\nis: " + reificationUri);

				// *****************************************************
				// Use findByUri in order to get the statement given its uri.
				// This is useful, for example, when you have uris referred in COMM annotations.
				// *****************************************************			 	
				IPropertyMember referredStatement = theDao.findByUri(reificationUri); 

				assert (referredStatement == aStatement);
				
				// *****************************************************
				// Add provenance (i.e. metaknowledge) to new statements, 
				// and retrieve it later.
				// *****************************************************
				INamedIndividual subject = ontology.createNamedIndividual("http://www.x-media-project.com/example_1#r3");
				IProperty property = ontology.createProperty("http://www.x-media-project.com/example_1#property_2");
				INamedIndividual object = ontology.createNamedIndividual("http://www.x-media-project.com/example_1#r4"); 
				String agent = "http://www.x-media-project.com/agents#agent_1";
				String source = "http://www.x-media-project.com/sources#source_1";
				
				// Add with provenance
				theDao.insert(subject, property, object,
						theDao.createProvenance(
								ontology.createNamedIndividual(agent), 
								0.7, 
								new Date(), 
								ontology.createNamedIndividual(source)));
				
				// Retrieve.
				// Note that a statement may have multiple provenances associated
				// (as, for example, the same statement may have been extracted by more agents).
				
				// Firstly, find it out.
				IPropertyMember statementWithProvenance = theDao.find(subject, property, object);
				// Then, retrieve its provenance.
				Set<IProvenance> itsProveance = theDao.getProvenances(statementWithProvenance);
			 	
				System.out.println("Provenance of statement: " + statementWithProvenance + " is:");
				for (IProvenance provenance : itsProveance)
					System.out.println(provenance);
				
				// *****************************************************
				// Add provenance (i.e. metaknowledge) to _existing_ statements.
				// *****************************************************
				
				theDao.insert(
						(INamedIndividual)aStatement.getSource(), 
						aStatement.getProperty(), 
						aStatement.getTarget(),
						theDao.createProvenance(
								ontology.createNamedIndividual(agent), 
								0.8, 
								null, 
								ontology.createNamedIndividual("http://www.x-media-project.com/sources#source_2")));
				
				// Then, retrieve this other provenance.
				itsProveance = theDao.getProvenances(aStatement);
			 	
				System.out.println("Provenance of statement: " + aStatement + " is:");
				for (IProvenance provenance : itsProveance)
					System.out.println(provenance);
				
				// *****************************************************
				// Export preserving metaknowledge: you MUST use TRIX. 
				// *****************************************************
				StringWriter writer = new StringWriter();
				ontology.export(IOntology.TRIX_LANGUAGE, writer);
				
				System.out.println("Export with metaknowledge:");
				System.out.println(writer.toString());
				
			} finally {
				theSession.close();
			}
			
		} finally {
			connection.closeOntology(ontology);
		}
		

	}

}
