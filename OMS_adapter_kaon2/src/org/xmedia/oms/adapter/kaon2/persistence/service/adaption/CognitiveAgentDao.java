package org.xmedia.oms.adapter.kaon2.persistence.service.adaption;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.xmedia.oms.adapter.kaon2.persistence.model.adaptation.CognitiveAgent;
import org.xmedia.oms.adapter.kaon2.persistence.model.adaptation.IApplicationInteraction;
import org.xmedia.oms.adapter.kaon2.persistence.model.adaptation.ICognitiveAgent;
import org.xmedia.oms.adapter.kaon2.persistence.model.adaptation.ICredential;
import org.xmedia.oms.adapter.kaon2.persistence.model.adaptation.IEntity;
import org.xmedia.oms.adapter.kaon2.persistence.model.adaptation.IResource;
import org.xmedia.oms.adapter.kaon2.persistence.model.adaptation.Resource;
import org.aifb.xxplore.shared.exception.Emergency;
import org.semanticweb.kaon2.api.Axiom;
import org.semanticweb.kaon2.api.KAON2Exception;
import org.semanticweb.kaon2.api.KAON2Manager;
import org.semanticweb.kaon2.api.OntologyChangeEvent;
import org.semanticweb.kaon2.api.owl.elements.Individual;
import org.xmedia.oms.adapter.kaon2.persistence.Kaon2Ontology;
import org.xmedia.oms.model.api.INamedIndividual;
import org.xmedia.oms.model.api.IOntology;
import org.xmedia.oms.model.api.IPropertyMember;


public class CognitiveAgentDao extends AbstractDao implements ICognitiveAgentDao {

	private static ICognitiveAgentDao m_instance = null;

	private final String COGNITIVE_AGENT = "Cognitive_Agent";

	private final String INVOLVED_IN = "involved_in";

	private final String HAS_INTEREST = "has_interest";

	private final String IS_RECOMMENDED = "is_recommended";

	private final String HAS_CREDENTIAL = "has_credential";

	private CognitiveAgentDao() {

	}

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
		
		Axiom axiom = KAON2Manager.factory().objectPropertyMember(
				KAON2Manager.factory().objectProperty(getEntityUri(INVOLVED_IN)),
				(Individual)agentInd.getDelegate(),
				(Individual)interactionInd.getDelegate());
		
		List<OntologyChangeEvent> changes = new ArrayList<OntologyChangeEvent>();
		changes.add(new OntologyChangeEvent(axiom,OntologyChangeEvent.ChangeType.ADD));
		try {
			IOntology onto = getAdaptationOntology();
			if (onto instanceof Kaon2Ontology)
				((Kaon2Ontology)onto).getDelegate().applyChanges(changes);
		} catch (KAON2Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
		
		Axiom axiom = KAON2Manager.factory().objectPropertyMember(
				KAON2Manager.factory().objectProperty(getEntityUri(HAS_CREDENTIAL)),
				(Individual)agentInd.getDelegate(),
				(Individual)credentialInd.getDelegate());
		
		List<OntologyChangeEvent> changes = new ArrayList<OntologyChangeEvent>();
		changes.add(new OntologyChangeEvent(axiom,OntologyChangeEvent.ChangeType.ADD));
		try {
			IOntology onto = getAdaptationOntology();
			if (onto instanceof Kaon2Ontology)
				((Kaon2Ontology)onto).getDelegate().applyChanges(changes);
		} catch (KAON2Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
		
		Axiom axiom = KAON2Manager.factory().objectPropertyMember(
				KAON2Manager.factory().objectProperty(getEntityUri(HAS_INTEREST)),
				(Individual)agentInd.getDelegate(),
				(Individual)entityInd.getDelegate());
		
		List<OntologyChangeEvent> changes = new ArrayList<OntologyChangeEvent>();
		changes.add(new OntologyChangeEvent(axiom,OntologyChangeEvent.ChangeType.ADD));
		try {
			IOntology onto = getAdaptationOntology();
			if (onto instanceof Kaon2Ontology)
				((Kaon2Ontology)onto).getDelegate().applyChanges(changes);
		} catch (KAON2Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
		Emergency.checkPrecondition(agent instanceof CognitiveAgent,
				"agent instanceof CognitiveAgent");

		Axiom axiom = KAON2Manager.factory().classMember(
				KAON2Manager.factory().owlClass(getEntityUri(COGNITIVE_AGENT)),
				KAON2Manager.factory().individual(agent.getUri()));

		List<OntologyChangeEvent> changes = new ArrayList<OntologyChangeEvent>();
		changes.add(new OntologyChangeEvent(axiom,OntologyChangeEvent.ChangeType.ADD));
		try {
			IOntology onto = getAdaptationOntology();
			if (onto instanceof Kaon2Ontology)
				((Kaon2Ontology)onto).getDelegate().applyChanges(changes);
		} catch (KAON2Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
