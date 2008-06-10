package org.xmedia.accessknow.sesame.examples;

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
import org.xmedia.oms.persistence.dao.IPropertyMemberAxiomDao;
import org.xmedia.uris.XMURIFactory;
import org.xmedia.uris.impl.XMURIFactoryInsulated;

public class AKSesame2AdapterRemoteExample {

	private static final String REPOSITORY_PATH = "/sesame/example";
	
	/**
	 * This example demonstrates the usage of the AccessKnow Sesame Adaptor 
	 * working with a remote Sesame server.
	 * 
	 * It is assumed that you are familiar with AccessKnow: otherwise, please check out the related Deliverables,
	 * and/or the comprehensive sample code in examples\org\xmedia\accessknow\sesame\examples\AKSesame2AdapterExample.java 
	 * 
	 * Dependencies:
	 * <ul>
	 * 	<li>AccessKnow: trunk/OMS, trunk/util</li>
	 * 	<li>openrdf-sesame-2.0-onejar.jar</li>
	 * 	<li>Sesame dependencies</li>
	 * </ul>
	 * @throws Exception 
	 * 
	 */
	public static void main(String[] args) throws Exception {
		
		XMURIFactory uriFactory = new XMURIFactoryInsulated();
		
		/**
		 * Firtsly, let's set the URL of your remote Sesame server
		 */
		String sesameServerUrl = "http://127.0.0.1:9080/openrdf-sesame/";
		
		/**
		 * Then, let's create the URI of the ontology we want to create.
		 * 
		 * The URI of an ontology backed by a remote Sesame server is supposed to by made by:
		 * * a URI - as e.g. one built by means of an XMURIFactory,
		 * * plus the Sesame server URL as sesame_url parameter.  
		 * 
		 */
		URI ontologyUri = new URI(uriFactory.getUri() + "?server_url=" + sesameServerUrl);
		
		/**
		 * Please, note that, for your conveninece, you may want to create the URI of a 
		 * remote ontology by means of the following static method:
		 * 
		 * URI ontologyUri = 
		 * 		SesameRemoteRepositoryHandle.buildRemoteOntologyUri(uriFactory.getUri(), sesameServerUrl);  
		 * 
		 */


		IKbConnection connection = new SesameConnection(REPOSITORY_PATH);
		ISessionFactory sessionFactory = new SesameSessionFactory(new XMURIFactoryInsulated());
		
		IOntology ontology = null;
		try {
			/**
			 * Given that you have built the ontology URI as described above, all the AccessKnow
			 * functionalities can be accessed as usual - as the following code demonstrates.  
			 * 
			 * WARNING: the only difference is that you CANNOT create a remote ontology 
			 * of type SesameRepositoryFactory.RDF_MEMORY_VOLATILE. 
			 * 
			 */
			Map<String, Object> ontologyParameters = new Hashtable<String, Object>();
			ontologyParameters.put(KbEnvironment.ONTOLOGY_URI, ontologyUri);
			ontologyParameters.put(KbEnvironment.ONTOLOGY_TYPE, new URI(SesameRepositoryFactory.RDFS_NATIVE));
			ontology = connection.loadOrCreateOntology(ontologyParameters);

			ISession session = sessionFactory.openSession(connection, ontology);
			IPropertyMemberAxiomDao pmDao = session.getDaoManager().getPropertyMemberDao();

			try {

				ITransaction aTransaction = session.beginTransaction();
				try {

					pmDao.insert(
							ontology.createNamedIndividual("http://www.x-media-project.com/example_1#r1"),
							ontology.createProperty("http://www.x-media-project.com/example_1#property_1"),
							ontology.createNamedIndividual("http://www.x-media-project.com/example_1#r2"));

					pmDao.insert(
							ontology.createNamedIndividual("http://www.x-media-project.com/example_1#r2"),
							ontology.createProperty(RDFS.LABEL),
							ontology.createStringLiteral("This is a test", "en"));

					pmDao.insert(
							ontology.createNamedIndividual("http://www.x-media-project.com/example_1#r1"),
							ontology.createProperty("http://www.x-media-project.com/example_1#property_2"),
							ontology.createNamedIndividual("http://www.x-media-project.com/example_1#r3"));

					aTransaction.commit();
				} catch (Exception e) {
					aTransaction.rollback();
					throw e;
				}

				// List statments with r1 as subject
				Set<IPropertyMember> someStatements =
					pmDao.findBySourceIndividual(ontology.createNamedIndividual("http://www.x-media-project.com/example_1#r1"));

				System.out.println("Statements with r1 as subject #" + someStatements.size() + " :");
				for (IPropertyMember aStatement : someStatements)
					System.out.println(aStatement);

			} finally {
				if (session != null)
					session.close();	
			}
		} finally {
			if (ontology != null)
				connection.closeOntology(ontology);
			
			// You _may_ also want to delete the ontology
			// connection.deleteOntology(ontologyUri.toString());
		}
		
	}

}
