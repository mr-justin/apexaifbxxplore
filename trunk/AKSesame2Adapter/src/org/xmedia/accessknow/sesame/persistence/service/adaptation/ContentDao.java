package org.xmedia.accessknow.sesame.persistence.service.adaptation;

import java.util.Set;

import org.xmedia.accessknow.sesame.persistence.model.adaptation.Content;
import org.xmedia.accessknow.sesame.persistence.model.adaptation.IContent;
import org.xmedia.accessknow.sesame.persistence.model.adaptation.IContentBearingObject;
import org.xmedia.accessknow.sesame.persistence.model.adaptation.IEntity;
import org.aifb.xxplore.shared.exception.Emergency;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.xmedia.oms.model.api.INamedConcept;
import org.xmedia.oms.model.api.INamedIndividual;
import org.xmedia.oms.model.api.IOntology;
import org.xmedia.oms.model.api.IProperty;
import org.xmedia.accessknow.sesame.model.SesameOntology;

public class ContentDao extends AbstractDao implements IContentDao {

	private static IContentDao m_instance = null;
	private final String CONTENT = "Content";
	private final String HAS_SUBJECT = "has_subject";
	private final String IS_POST = "is_post";
	private final String EMBODIED_IN = "embodied_in";

	
	private ContentDao() {}
	
	
	public static IContentDao getInstance() {
		if(m_instance == null)
			m_instance = new ContentDao();
		return m_instance;
	}

	public void setCBO(IContent content, IContentBearingObject cbo) {
		
		INamedIndividual contentInd = findIndividualByUri(content.getUri());
		if (contentInd == null) {
			content.storeContent();
			contentInd = findIndividualByUri(content.getUri());
		}
		INamedIndividual cboInd = findIndividualByUri(cbo.getUri());
		if (cboInd == null) {
			cbo.storeCBO();
			cboInd = findIndividualByUri(cbo.getUri());
		}	
		
		INamedConcept concept = findConceptByUri(getEntityUri(CONTENT));
		Set<IProperty> props = findPropertyBySourceConcept(concept);
		
		IOntology onto = getAdaptationOntology();
		Emergency.checkPrecondition(onto instanceof SesameOntology,"onto instanceof Xmedia2Ontology");
		
		ValueFactory factory = ((SesameOntology)onto).getRepository().getValueFactory();
		RepositoryConnection con;
		try 
		{
			con = ((SesameOntology)onto).getRepository().getConnection();

			for (IProperty prop : props) 
			{
				if (prop.getUri().equals(getEntityUri(EMBODIED_IN)))
				{
					URI subject = factory.createURI(contentInd.getUri());
					URI predicate = factory.createURI(prop.getUri());
					URI object = factory.createURI(cboInd.getUri());

					con.add(subject,predicate, object);
				}
			}
			con.close();
		} 
		catch (RepositoryException e) 
		{
			e.printStackTrace();
		}
	}
	
	
	public void setPostContent(IContent content, IContent postContent) {
		INamedIndividual contentInd = findIndividualByUri(content.getUri());
		if (contentInd == null) {
			content.storeContent();
			contentInd = findIndividualByUri(content.getUri());
		}
		INamedIndividual postContentInd = findIndividualByUri(postContent.getUri());
		if (postContentInd == null) {
			postContent.storeContent();
			postContentInd = findIndividualByUri(content.getUri());
		}

		INamedConcept concept = findConceptByUri(getEntityUri(CONTENT));
		Set<IProperty> props = findPropertyBySourceConcept(concept);
		
		IOntology onto = getAdaptationOntology();
		Emergency.checkPrecondition(onto instanceof SesameOntology,"onto instanceof Xmedia2Ontology");
		
		ValueFactory factory = ((SesameOntology)onto).getRepository().getValueFactory();
		RepositoryConnection con;
		
		try 
		{
			con = ((SesameOntology)onto).getRepository().getConnection();
			
			for (IProperty prop : props) 
			{
				if (prop.getUri().equals(getEntityUri(IS_POST)))
				{
					URI subject = factory.createURI(contentInd.getUri());
					URI predicate = factory.createURI(prop.getUri());
					URI object = factory.createURI(postContentInd.getUri());

					con.add(subject,predicate, object);
				}
			}
			con.close();
		}
		catch (RepositoryException e) 
		{
			e.printStackTrace();
		}
	}
	
	public void setSubject(IContent content, IEntity entity) {
	
		INamedIndividual contentInd = findIndividualByUri(content.getUri());
		if (contentInd == null) {
			content.storeContent();
			contentInd = findIndividualByUri(content.getUri());
		}
		INamedIndividual entityInd = findIndividualByUri(entity.getUri());
		if (entityInd == null) {
			entity.storeEntity();
			entityInd = findIndividualByUri(entity.getUri());
		}
		

		INamedConcept concept = findConceptByUri(getEntityUri(CONTENT));
		Set<IProperty> props = findPropertyBySourceConcept(concept);
		
		IOntology onto = getAdaptationOntology();
		Emergency.checkPrecondition(onto instanceof SesameOntology,"onto instanceof Xmedia2Ontology");
		
		ValueFactory factory = ((SesameOntology)onto).getRepository().getValueFactory();
		RepositoryConnection con;
		try
		{
			con = ((SesameOntology)onto).getRepository().getConnection();
			
			for (IProperty prop : props) 
			{
				if (prop.getUri().equals(getEntityUri(HAS_SUBJECT)))
				{

					URI subject = factory.createURI(contentInd.getUri());
					URI predicate = factory.createURI(prop.getUri());
					URI object = factory.createURI(entityInd.getUri());

					con.add(subject,predicate, object);		
				}
			}
			con.close();
		}
		catch (RepositoryException e) 
		{
			e.printStackTrace();
		}
	}
	
	public void insert(IContent resource) {
		
		Emergency.checkPrecondition(resource instanceof Content,"resources instanceof Content");
		IOntology onto = getAdaptationOntology();
		
		if (onto instanceof SesameOntology)
		{
			ValueFactory factory = ((SesameOntology)onto).getRepository().getValueFactory();

			URI subject = factory.createURI(resource.getUri());
			URI object = factory.createURI(getEntityUri(CONTENT));

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
