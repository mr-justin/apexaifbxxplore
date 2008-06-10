package org.xmedia.accessknow.sesame.persistence.service.adaptation;

import org.xmedia.accessknow.sesame.persistence.model.adaptation.Entity;
import org.xmedia.accessknow.sesame.persistence.model.adaptation.IApplicationInteraction;
import org.xmedia.accessknow.sesame.persistence.model.adaptation.IComputerAidedProcess;
import org.xmedia.accessknow.sesame.persistence.model.adaptation.IEntity;
import org.xmedia.accessknow.sesame.persistence.model.adaptation.IProcess;
import org.aifb.xxplore.shared.exception.Emergency;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.xmedia.oms.model.api.INamedIndividual;
import org.xmedia.oms.model.api.IOntology;
import org.xmedia.accessknow.sesame.model.SesameOntology;


public class EntityDao extends AbstractDao implements IEntityDao {

	
	private static IEntityDao m_instance = null;
	private final String ENTITY = "Entity";
	private final String IS_RELATED_TO = "refer";
	private final String INVOLVED_IN = "involved_in";
	

	private EntityDao() {}
	
	public static IEntityDao getInstance() {
		if(m_instance == null)m_instance = new EntityDao();
		return m_instance;
	}

	public void setRelatedEntity(IEntity entity, IEntity related) {
		
		INamedIndividual entityInd = findIndividualByUri(entity.getUri());
		if (entityInd == null) {
			entity.storeEntity();
			entityInd = findIndividualByUri(entity.getUri());
		}
		INamedIndividual relatedInd = findIndividualByUri(related.getUri());
		if (relatedInd == null) {
			related.storeEntity();
			relatedInd = findIndividualByUri(related.getUri());
		}
		
		IOntology onto = getAdaptationOntology();
		if (onto instanceof SesameOntology)
		{
			ValueFactory factory = ((SesameOntology)onto).getRepository().getValueFactory();

			URI subject = factory.createURI(entityInd.getUri());
			URI predicate = factory.createURI(getEntityUri(IS_RELATED_TO));
			URI object = factory.createURI(relatedInd.getUri());

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
	
	public void setInvolvingProcess(IEntity entity, IProcess process) {
		INamedIndividual entityInd = findIndividualByUri(entity.getUri());
		if (entityInd == null) {
			entity.storeEntity();
			entityInd = findIndividualByUri(entity.getUri());
		}
		INamedIndividual processInd = findIndividualByUri(process.getUri());
		if (processInd == null) {
			if (process instanceof IApplicationInteraction)
				((IApplicationInteraction)process).storeApplicationInteraction();
			else if (process instanceof IComputerAidedProcess)
				((IComputerAidedProcess)process).storeComputerAidedProcess();
			processInd = findIndividualByUri(process.getUri());
		}
		
		IOntology onto = getAdaptationOntology();
		if (onto instanceof SesameOntology)
		{
			ValueFactory factory = ((SesameOntology)onto).getRepository().getValueFactory();

			URI subject = factory.createURI(entityInd.getUri());
			URI predicate = factory.createURI(getEntityUri(INVOLVED_IN));
			URI object = factory.createURI(processInd.getUri());

			try 
			{
				RepositoryConnection con = ((SesameOntology)onto).getRepository().getConnection();
				
				try
				{
					con.add(subject,predicate, object);
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
		
	public void insert(IEntity entity) {
		
		Emergency.checkPrecondition(entity instanceof Entity,"entity instanceof Entity");

		IOntology onto = getAdaptationOntology();
		if (onto instanceof SesameOntology)
		{
			ValueFactory factory = ((SesameOntology)onto).getRepository().getValueFactory();

			URI subject = factory.createURI(entity.getUri());
			URI object = factory.createURI(getEntityUri(ENTITY));

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
