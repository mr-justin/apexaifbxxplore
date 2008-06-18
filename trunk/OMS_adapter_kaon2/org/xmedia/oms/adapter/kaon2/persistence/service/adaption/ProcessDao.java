package org.xmedia.oms.adapter.kaon2.persistence.service.adaption;

import java.util.ArrayList;
import java.util.List;
import org.xmedia.oms.adapter.kaon2.persistence.model.adaptation.IApplicationInteraction;
import org.xmedia.oms.adapter.kaon2.persistence.model.adaptation.ICognitiveAgent;
import org.xmedia.oms.adapter.kaon2.persistence.model.adaptation.IComputerAidedProcess;
import org.xmedia.oms.adapter.kaon2.persistence.model.adaptation.IContent;
import org.xmedia.oms.adapter.kaon2.persistence.model.adaptation.IContentBearingObject;
import org.xmedia.oms.adapter.kaon2.persistence.model.adaptation.IDevice;
import org.xmedia.oms.adapter.kaon2.persistence.model.adaptation.IProcess;
import org.semanticweb.kaon2.api.Axiom;
import org.semanticweb.kaon2.api.KAON2Exception;
import org.semanticweb.kaon2.api.KAON2Manager;
import org.semanticweb.kaon2.api.OntologyChangeEvent;
import org.semanticweb.kaon2.api.owl.elements.Individual;
import org.xmedia.oms.adapter.kaon2.persistence.Kaon2Ontology;
import org.xmedia.oms.model.api.INamedIndividual;
import org.xmedia.oms.model.api.IOntology;


public class ProcessDao extends AbstractDao implements IProcessDao {

	private static IProcessDao m_instance = null;
	
	private final String IS_POST = "is_post";
	private final String RESOURCE = "resource";
	private final String EXPERIENCER = "experiencer";
	private final String INSTRUMENT = "instrument";

	private ProcessDao() {
		
	}
	
	public static IProcessDao getInstance() {
		if(m_instance == null) {
			m_instance = new ProcessDao();
		}
		return m_instance;
	}
	
	public void setAgent(IProcess process, ICognitiveAgent agent) {
		INamedIndividual processInd = findIndividualByUri(process.getUri());
		if (processInd == null) {
			if (process instanceof IApplicationInteraction) {
				((IApplicationInteraction)process).storeApplicationInteraction();
			} else if (process instanceof IComputerAidedProcess) {
				((IComputerAidedProcess)process).storeComputerAidedProcess();
			}
			processInd = findIndividualByUri(process.getUri());
		}
		INamedIndividual agentInd = findIndividualByUri(agent.getUri());
		if (agentInd == null) {
			agent.storeCognitiveAgent();
			agentInd = findIndividualByUri(agent.getUri());
		}
		
		Axiom axiom = KAON2Manager.factory().objectPropertyMember(
				KAON2Manager.factory().objectProperty(getEntityUri(EXPERIENCER)),
				(Individual)processInd.getDelegate(),
				(Individual)agentInd.getDelegate());
		
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
	
	public void setPostProcess(IProcess process, IProcess postProcess) {
		INamedIndividual processInd = findIndividualByUri(process.getUri());
		if (processInd == null) {
			if (process instanceof IApplicationInteraction) {
				((IApplicationInteraction)process).storeApplicationInteraction();
			} else if (process instanceof IComputerAidedProcess) {
				((IComputerAidedProcess)process).storeComputerAidedProcess();
			}
			processInd = findIndividualByUri(process.getUri());
		}
		INamedIndividual postProcessInd = findIndividualByUri(postProcess.getUri());
		if (postProcessInd == null) {
			if (postProcess instanceof IApplicationInteraction) {
				((IApplicationInteraction)postProcess).storeApplicationInteraction();
			} else if (postProcess instanceof IComputerAidedProcess) {
				((IComputerAidedProcess)postProcess).storeComputerAidedProcess();
			}
			postProcessInd = findIndividualByUri(postProcess.getUri());
		}
		
		Axiom axiom = KAON2Manager.factory().objectPropertyMember(
				KAON2Manager.factory().objectProperty(getEntityUri(IS_POST)),
				(Individual)processInd.getDelegate(),
				(Individual)postProcessInd.getDelegate());
		
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
	
	public void setResource(IProcess process, IContent content) {
		INamedIndividual processInd = findIndividualByUri(process.getUri());
		if (processInd == null) {
			if (process instanceof IApplicationInteraction) {
				((IApplicationInteraction)process).storeApplicationInteraction();
			} else if (process instanceof IComputerAidedProcess) {
				((IComputerAidedProcess)process).storeComputerAidedProcess();
			}
			processInd = findIndividualByUri(process.getUri());
		}
		INamedIndividual contentInd = findIndividualByUri(content.getUri());
		if (contentInd == null) {
			content.storeContent();
			contentInd = findIndividualByUri(content.getUri());
		}
		
		Axiom axiom = KAON2Manager.factory().objectPropertyMember(
				KAON2Manager.factory().objectProperty(getEntityUri(RESOURCE)),
				(Individual)processInd.getDelegate(),
				(Individual)contentInd.getDelegate());
		
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
	
	public void setResource(IProcess process, IContentBearingObject cbo) {
		INamedIndividual processInd = findIndividualByUri(process.getUri());
		if (processInd == null) {
			if (process instanceof IApplicationInteraction) {
				((IApplicationInteraction)process).storeApplicationInteraction();
			} else if (process instanceof IComputerAidedProcess) {
				((IComputerAidedProcess)process).storeComputerAidedProcess();
			}
			processInd = findIndividualByUri(process.getUri());
		}
		INamedIndividual cboInd = findIndividualByUri(cbo.getUri());
		if (cboInd == null) {
			cbo.storeCBO();
			cboInd = findIndividualByUri(cbo.getUri());
		}
		
		Axiom axiom = KAON2Manager.factory().objectPropertyMember(
				KAON2Manager.factory().objectProperty(getEntityUri(RESOURCE)),
				(Individual)processInd.getDelegate(),
				(Individual)cboInd.getDelegate());
		
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
	
	public void setInstrument(IProcess process, IDevice instrument) {
		INamedIndividual processInd = findIndividualByUri(process.getUri());
		if (processInd == null) {
			if (process instanceof IApplicationInteraction) {
				((IApplicationInteraction)process).storeApplicationInteraction();
			} else if (process instanceof IComputerAidedProcess) {
				((IComputerAidedProcess)process).storeComputerAidedProcess();
			}
			processInd = findIndividualByUri(process.getUri());
		}
		INamedIndividual instrumentInd = findIndividualByUri(instrument.getUri());
		if (instrumentInd == null) {
			instrument.storeDevice();
			instrumentInd = findIndividualByUri(instrument.getUri());
		}
		
		Axiom axiom = KAON2Manager.factory().objectPropertyMember(
				KAON2Manager.factory().objectProperty(getEntityUri(INSTRUMENT)),
				(Individual)processInd.getDelegate(),
				(Individual)instrumentInd.getDelegate());
		
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
