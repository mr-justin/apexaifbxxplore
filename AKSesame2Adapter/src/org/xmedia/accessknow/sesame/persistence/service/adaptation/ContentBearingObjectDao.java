package org.xmedia.accessknow.sesame.persistence.service.adaptation;

import org.xmedia.accessknow.sesame.persistence.model.adaptation.ContentBearingObject;
import org.xmedia.accessknow.sesame.persistence.model.adaptation.IContent;
import org.xmedia.accessknow.sesame.persistence.model.adaptation.IContentBearingObject;
import org.xmedia.accessknow.sesame.persistence.model.adaptation.ICredential;
import org.aifb.xxplore.shared.exception.Emergency;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.xmedia.oms.model.api.INamedIndividual;
import org.xmedia.oms.model.api.IOntology;
import org.xmedia.accessknow.sesame.model.SesameOntology;


public class ContentBearingObjectDao extends AbstractDao implements IContentBearingObjectDao {

	private static IContentBearingObjectDao m_instance = null;	
	private final String CONTENT_BEARING_OBJECT = "Content_Bearing_Object";
	private final String CONTAINSINFORMATION = "containsinformation";
	private final String REQUIRED_CREDENTIAL = "required_credential";

	
	private ContentBearingObjectDao() {}
	
	
	public static IContentBearingObjectDao getInstance() {
		if(m_instance == null)
			m_instance = new ContentBearingObjectDao();
		return m_instance;
	}

	public void insert(IContentBearingObject resource) {
		Emergency.checkPrecondition(resource instanceof ContentBearingObject,"resources instanceof ContentBearingObject");

		IOntology onto = getAdaptationOntology();
		if (onto instanceof SesameOntology)
		{
			ValueFactory factory = ((SesameOntology)onto).getRepository().getValueFactory();

			URI subject = factory.createURI(resource.getUri());
			URI object = factory.createURI(getEntityUri(CONTENT_BEARING_OBJECT));

			try 
			{
				RepositoryConnection con = ((SesameOntology)onto).getRepository().getConnection();
				
				try
				{
					con.add(subject,RDF.TYPE,object);
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

	public void setContent(IContentBearingObject cbo, IContent content) {
		INamedIndividual cboInd = findIndividualByUri(cbo.getUri());
		if (cboInd == null) {
			cbo.storeCBO();
			cboInd = findIndividualByUri(cbo.getUri());
		} 
		INamedIndividual contentInd = findIndividualByUri(content.getUri());
		if (contentInd == null) {
			content.storeContent();
			contentInd = findIndividualByUri(content.getUri());
		} 
		
		IOntology onto = getAdaptationOntology();
		if (onto instanceof SesameOntology)
		{
			ValueFactory factory = ((SesameOntology)onto).getRepository().getValueFactory();

			URI subject = factory.createURI(cboInd.getUri());
			URI predicate = factory.createURI(getEntityUri(CONTAINSINFORMATION));
			URI object = factory.createURI(contentInd.getUri());

			try 
			{
				RepositoryConnection con = ((SesameOntology)onto).getRepository().getConnection();
				
				try
				{
					con.add(subject,predicate,object);
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

	public void setCredential(IContentBearingObject cbo, ICredential credential) {
		INamedIndividual cboInd = findIndividualByUri(cbo.getUri());
		if (cboInd == null) {
			cbo.storeCBO();
			cboInd = findIndividualByUri(cbo.getUri());
		} 
		INamedIndividual credentialInd = findIndividualByUri(credential.getUri());
		if (credentialInd == null) {
			credential.storeCredential();
			credentialInd = findIndividualByUri(credential.getUri());
		} 

		IOntology onto = getAdaptationOntology();
		
		if (onto instanceof SesameOntology)
		{
			ValueFactory factory = ((SesameOntology)onto).getRepository().getValueFactory();

			URI subject = factory.createURI(cboInd.getUri());
			URI predicate = factory.createURI(getEntityUri(REQUIRED_CREDENTIAL));
			URI object = factory.createURI(credentialInd.getUri());

			try 
			{
				RepositoryConnection con = ((SesameOntology)onto).getRepository().getConnection();
				
				try
				{
					con.add(subject,predicate,object);
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
