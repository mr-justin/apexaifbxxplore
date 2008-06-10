package org.xmedia.accessknow.sesame.persistence.service.adaptation;

import java.util.HashSet;
import java.util.Set;

import org.xmedia.accessknow.sesame.persistence.model.adaptation.CognitiveAgent;
import org.xmedia.accessknow.sesame.persistence.model.adaptation.IApplicationInteraction;
import org.xmedia.accessknow.sesame.persistence.model.adaptation.ICognitiveAgent;
import org.xmedia.accessknow.sesame.persistence.model.adaptation.ICredential;
import org.xmedia.accessknow.sesame.persistence.model.adaptation.IEntity;
import org.xmedia.accessknow.sesame.persistence.model.adaptation.IResource;
import org.xmedia.accessknow.sesame.persistence.model.adaptation.Resource;
import org.aifb.xxplore.shared.exception.Emergency;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.xmedia.oms.model.api.INamedIndividual;
import org.xmedia.oms.model.api.IOntology;
import org.xmedia.oms.model.api.IPropertyMember;
import org.xmedia.accessknow.sesame.model.SesameOntology;

public class CognitiveAgentDao extends AbstractDao implements ICognitiveAgentDao {

	private static ICognitiveAgentDao m_instance = null;

	private final String COGNITIVE_AGENT = "Cognitive_Agent";
	private final String INVOLVED_IN = "involved_in";
	private final String HAS_INTEREST = "has_interest";
	private final String IS_RECOMMENDED = "is_recommended";
	private final String HAS_CREDENTIAL = "has_credential";

	
	private CognitiveAgentDao() {}

	public static ICognitiveAgentDao getInstance() {
		if (m_instance == null)
			m_instance = new CognitiveAgentDao();
		return m_instance;
	}
	
	public void setApplicationInteraction(ICognitiveAgent agent, IApplicationInteraction interaction) {
		INamedIndividual agentInd = findIndividualByUri(agent.getUri());
		if (agentInd == null) {
			agent.storeCognitiveAgent();
			agentInd = findIndividualByUri(agent.getUri());
		} 
		INamedIndividual interactionInd = findIndividualByUri(interaction.getUri());
		if (interactionInd == null) {
			interaction.storeApplicationInteraction();
			interactionInd = findIndividualByUri(interaction.getUri());
		} 
		
		IOntology onto = getAdaptationOntology();
		if (onto instanceof SesameOntology)
		{
			ValueFactory factory = ((SesameOntology)onto).getRepository().getValueFactory();

			URI subject = factory.createURI(agentInd.getUri());
			URI predicate = factory.createURI(getEntityUri(INVOLVED_IN));
			URI object = factory.createURI(interactionInd.getUri());

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
	
	public void setCredentail(ICognitiveAgent agent, ICredential credential) {
		INamedIndividual agentInd = findIndividualByUri(agent.getUri());
		if (agentInd == null) {
			agent.storeCognitiveAgent();
			agentInd = findIndividualByUri(agent.getUri());
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

			URI subject = factory.createURI(agentInd.getUri());
			URI predicate = factory.createURI(getEntityUri(HAS_CREDENTIAL));
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

	public void setInterestingEntity(ICognitiveAgent agent, IEntity entity) {
		INamedIndividual agentInd = findIndividualByUri(agent.getUri());
		if (agentInd == null) {
			agent.storeCognitiveAgent();
			agentInd = findIndividualByUri(agent.getUri());
		} 
		INamedIndividual entityInd = findIndividualByUri(entity.getUri());
		if (entityInd == null) {
			entity.storeEntity();
			entityInd = findIndividualByUri(entity.getUri());
		} 
		
		IOntology onto = getAdaptationOntology();
		if (onto instanceof SesameOntology)
		{
			ValueFactory factory = ((SesameOntology)onto).getRepository().getValueFactory();

			URI subject = factory.createURI(agentInd.getUri());
			URI predicate = factory.createURI(getEntityUri(HAS_INTEREST));
			URI object = factory.createURI(entityInd.getUri());

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
	
	public Set<IResource> getRecommendation(ICognitiveAgent agent) {
		INamedIndividual agentInd = findIndividualByUri(agent.getUri());
		Set<IPropertyMember> props = findPropertyMemberBySourceIndividual(agentInd);
		Set<IResource> ress = new HashSet<IResource>();;
		for (IPropertyMember prop : props) {
			if (prop.getProperty().getUri().equals(getEntityUri(IS_RECOMMENDED))) {
				INamedIndividual ind = (INamedIndividual) prop.getTarget();
				if (ind.getTypes().contains(findConceptByUri(getEntityUri("Content"))))
					 ress.add(new Resource(ind.getUri(),0));
				else if(ind.getTypes().contains(findConceptByUri(getEntityUri("Content_Bearing_Object"))))
					 ress.add(new Resource(ind.getUri(),1));
			}
		}
		return ress;
	}
	
	public void insert(ICognitiveAgent agent) {
		
		Emergency.checkPrecondition(agent instanceof CognitiveAgent,"agent instanceof CognitiveAgent");

		IOntology onto = getAdaptationOntology();
		if (onto instanceof SesameOntology)
		{
			ValueFactory factory = ((SesameOntology)onto).getRepository().getValueFactory();

			URI subject = factory.createURI(agent.getUri());
			URI object = factory.createURI(getEntityUri(COGNITIVE_AGENT));

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
}
