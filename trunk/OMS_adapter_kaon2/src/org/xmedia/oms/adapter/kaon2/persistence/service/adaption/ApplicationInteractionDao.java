package org.xmedia.oms.adapter.kaon2.persistence.service.adaption;

import java.util.ArrayList;
import java.util.List;
import org.xmedia.oms.adapter.kaon2.persistence.model.adaptation.ApplicationInteraction;
import org.xmedia.oms.adapter.kaon2.persistence.model.adaptation.IApplicationInteraction;
import org.xmedia.oms.adapter.kaon2.persistence.model.adaptation.IComputerAidedProcess;
import org.aifb.xxplore.shared.exception.Emergency;
import org.semanticweb.kaon2.api.Axiom;
import org.semanticweb.kaon2.api.KAON2Exception;
import org.semanticweb.kaon2.api.KAON2Manager;
import org.semanticweb.kaon2.api.OntologyChangeEvent;
import org.semanticweb.kaon2.api.owl.elements.Individual;
import org.xmedia.oms.adapter.kaon2.persistence.Kaon2Ontology;
import org.xmedia.oms.model.api.INamedIndividual;
import org.xmedia.oms.model.api.IOntology;


public class ApplicationInteractionDao extends AbstractDao implements IApplicationInteractionDao {
	
	private static IApplicationInteractionDao m_instance = null;
	
	private final String APPLICATION_INTERACTION = "Application_Interaction";

	private final String IS_PART_OF = "is_part_of";
	
	private ApplicationInteractionDao() {
		
	}
	
	public static IApplicationInteractionDao getInstance() {
		if(m_instance == null) {
			m_instance = new ApplicationInteractionDao();
		}
		return m_instance;
	}

	public void setComputerAidedProcess(IApplicationInteraction interaction, IComputerAidedProcess process) {
		INamedIndividual interactionInd = findIndividualByUri(interaction.getUri());
		if (interactionInd == null) {
			interaction.storeApplicationInteraction();
			interactionInd = findIndividualByUri(interaction.getUri());
		} 
		INamedIndividual processInd = findIndividualByUri(process.getUri());
		if (process == null) {
			process.storeComputerAidedProcess();
			processInd = findIndividualByUri(process.getUri());
		} 
		
		Axiom axiom = KAON2Manager.factory().objectPropertyMember(
				KAON2Manager.factory().objectProperty(getEntityUri(IS_PART_OF)),
				(Individual)interactionInd.getDelegate(),
				(Individual)processInd.getDelegate());
		
		List<OntologyChangeEvent> changes = new ArrayList<OntologyChangeEvent>();
		changes.add(new OntologyChangeEvent(axiom,OntologyChangeEvent.ChangeType.ADD));
		try {
			IOntology onto = getAdaptationOntology();
			if (onto instanceof Kaon2Ontology) {
				((Kaon2Ontology)onto).getDelegate().applyChanges(changes);
			}
		} catch (KAON2Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void insert(IApplicationInteraction interaction) {
		Emergency.checkPrecondition(interaction instanceof ApplicationInteraction,
				"interaction instanceof ApplicationInteraction");

		Axiom axiom = KAON2Manager.factory().classMember(
				KAON2Manager.factory().owlClass(getEntityUri(APPLICATION_INTERACTION)),
				KAON2Manager.factory().individual(interaction.getUri()));

		List<OntologyChangeEvent> changes = new ArrayList<OntologyChangeEvent>();
		changes.add(new OntologyChangeEvent(axiom,OntologyChangeEvent.ChangeType.ADD));
		try {
			IOntology onto = getAdaptationOntology();
			if (onto instanceof Kaon2Ontology) {
				((Kaon2Ontology)onto).getDelegate().applyChanges(changes);
			}
		} catch (KAON2Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
