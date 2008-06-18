package org.xmedia.oms.adapter.kaon2.persistence.service.adaption;

import java.util.ArrayList;
import java.util.List;
import org.xmedia.oms.adapter.kaon2.persistence.model.adaptation.ComputerAidedProcess;
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


public class ComputerAidedProcessDao extends AbstractDao implements IComputerAidedProcessDao {

	private static IComputerAidedProcessDao m_instance = null;
	
	private final String COMPUTER_AIDED_PROCESS = "Computer-aided_Process";

	private final String HAS_PART = "has_part";

	private ComputerAidedProcessDao() {
		
	}

	public static IComputerAidedProcessDao getInstance() {
		if(m_instance == null) {
			m_instance = new ComputerAidedProcessDao();
		}
		return m_instance;
	}
	
	public void setApplicationInteraction(IComputerAidedProcess process,IApplicationInteraction interaction) {
		INamedIndividual processInd = findIndividualByUri(process.getUri());
		if (processInd == null) {
			process.storeComputerAidedProcess();
			processInd = findIndividualByUri(process.getUri());
		}
		INamedIndividual interactionInd = findIndividualByUri(interaction.getUri());
		if (interactionInd == null) {
			interaction.storeApplicationInteraction();
			interactionInd = findIndividualByUri(interaction.getUri());
		}
		
		Axiom axiom = KAON2Manager.factory().objectPropertyMember(
				KAON2Manager.factory().objectProperty(getEntityUri(HAS_PART)),
				(Individual)processInd.getDelegate(),
				(Individual)interactionInd.getDelegate());
		
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
	
	public void insert(IComputerAidedProcess process) {
		Emergency.checkPrecondition(process instanceof ComputerAidedProcess,
				"process instanceof ComputerAidedProcess");

		Axiom axiom = KAON2Manager.factory().classMember(
				KAON2Manager.factory().owlClass(getEntityUri(COMPUTER_AIDED_PROCESS)),
				KAON2Manager.factory().individual(process.getUri()));

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
