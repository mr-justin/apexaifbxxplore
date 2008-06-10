package org.xmedia.accessknow.sesame.persistence.service.adaptation;

import org.aifb.xxplore.shared.exception.Emergency;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.xmedia.oms.model.api.IOntology;
import org.xmedia.accessknow.sesame.model.SesameOntology;
import org.xmedia.accessknow.sesame.persistence.model.adaptation.ApplicationEnvironment;
import org.xmedia.accessknow.sesame.persistence.model.adaptation.IApplicationEnvironment;


public class ApplicationEnvironmentDao extends AbstractDao implements IApplicationEnvironmentDao {

	
	private static IApplicationEnvironmentDao m_instance = null;
	private final String APPLICATION_ENVIRONMENT = "Application_Environment";

	
	private ApplicationEnvironmentDao() {}
	
	public static IApplicationEnvironmentDao getInstance() {
		
		if(m_instance == null)m_instance = new ApplicationEnvironmentDao();
		return m_instance;
		
	}
	
	public void insert(IApplicationEnvironment environment) {
		
		Emergency.checkPrecondition(environment instanceof ApplicationEnvironment,"environment instanceof ApplicationEnvironment");
		
		IOntology onto = getAdaptationOntology();
		
		if (onto instanceof SesameOntology)
		{
			ValueFactory factory = ((SesameOntology)onto).getRepository().getValueFactory();

			URI subject = factory.createURI(environment.getUri());
			URI object = factory.createURI(getEntityUri(APPLICATION_ENVIRONMENT));

			try 
			{
				RepositoryConnection con = ((SesameOntology)onto).getRepository().getConnection();
				
				try
				{
					con.add(subject, RDF.TYPE, object);
				}
				finally
				{
					con.close();
				}
			} 
			catch (RepositoryException e) 
			{

				e.printStackTrace();
			}
		}
		
	}
}
