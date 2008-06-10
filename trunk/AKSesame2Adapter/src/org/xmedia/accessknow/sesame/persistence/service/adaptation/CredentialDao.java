package org.xmedia.accessknow.sesame.persistence.service.adaptation;

import org.xmedia.accessknow.sesame.persistence.model.adaptation.Credential;
import org.xmedia.accessknow.sesame.persistence.model.adaptation.ICredential;
import org.aifb.xxplore.shared.exception.Emergency;

import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.xmedia.oms.model.api.IOntology;
import org.xmedia.accessknow.sesame.model.SesameOntology;


public class CredentialDao extends AbstractDao implements ICredentialDao {

	private static ICredentialDao m_instance = null;
	private final String CREDENTIAL = "Credential";
	
	private CredentialDao() {}
	
	public static ICredentialDao getInstance() {
		if (m_instance == null)m_instance = new CredentialDao();
		return m_instance;
	}
	
	public void insert(ICredential credential) {
		
		Emergency.checkPrecondition(credential instanceof Credential,"credential instanceof Credential");
		IOntology onto = getAdaptationOntology();
		
		if (onto instanceof SesameOntology)
		{
			ValueFactory factory = ((SesameOntology)onto).getRepository().getValueFactory();

			URI subject = factory.createURI(credential.getUri());
			URI object = factory.createURI(getEntityUri(CREDENTIAL));

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