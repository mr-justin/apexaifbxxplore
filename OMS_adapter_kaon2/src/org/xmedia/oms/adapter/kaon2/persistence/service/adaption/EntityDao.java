package org.xmedia.oms.adapter.kaon2.persistence.service.adaption;

import java.util.ArrayList;
import java.util.List;
import org.xmedia.oms.adapter.kaon2.persistence.model.adaptation.Entity;
import org.xmedia.oms.adapter.kaon2.persistence.model.adaptation.IApplicationInteraction;
import org.xmedia.oms.adapter.kaon2.persistence.model.adaptation.IComputerAidedProcess;
import org.xmedia.oms.adapter.kaon2.persistence.model.adaptation.IEntity;
import org.xmedia.oms.adapter.kaon2.persistence.model.adaptation.IProcess;
import org.aifb.xxplore.shared.exception.Emergency;
import org.semanticweb.kaon2.api.Axiom;
import org.semanticweb.kaon2.api.KAON2Exception;
import org.semanticweb.kaon2.api.KAON2Manager;
import org.semanticweb.kaon2.api.OntologyChangeEvent;
import org.semanticweb.kaon2.api.owl.elements.Individual;
import org.xmedia.oms.adapter.kaon2.persistence.Kaon2Ontology;
import org.xmedia.oms.model.api.INamedIndividual;
import org.xmedia.oms.model.api.IOntology;


public class EntityDao extends AbstractDao implements IEntityDao {

	private static IEntityDao m_instance = null;

	private final String ENTITY = "Entity";
	
	private final String IS_RELATED_TO = "refer";
	
	private final String INVOLVED_IN = "involved_in";

	private EntityDao() {
		
	}
	
	public static IEntityDao getInstance() {
		if(m_instance == null) {
			m_instance = new EntityDao();
		}
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
		
		Axiom axiom = KAON2Manager.factory().objectPropertyMember(
				KAON2Manager.factory().objectProperty(getEntityUri(IS_RELATED_TO)),
				(Individual)entityInd.getDelegate(),
				(Individual)relatedInd.getDelegate());
		
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
	
	public void setInvolvingProcess(IEntity entity, IProcess process) {
		INamedIndividual entityInd = findIndividualByUri(entity.getUri());
		if (entityInd == null) {
			entity.storeEntity();
			entityInd = findIndividualByUri(entity.getUri());
		}
		INamedIndividual processInd = findIndividualByUri(process.getUri());
		if (processInd == null) {
			if (process instanceof IApplicationInteraction) {
				((IApplicationInteraction)process).storeApplicationInteraction();
			} else if (process instanceof IComputerAidedProcess) {
				((IComputerAidedProcess)process).storeComputerAidedProcess();
			}
			processInd = findIndividualByUri(process.getUri());
		}
		
		Axiom axiom = KAON2Manager.factory().objectPropertyMember(
				KAON2Manager.factory().objectProperty(getEntityUri(INVOLVED_IN)),
				(Individual)entityInd.getDelegate(),
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
		
	public void insert(IEntity entity) {
		Emergency.checkPrecondition(entity instanceof Entity,
				"entity instanceof Entity");

		Axiom axiom = KAON2Manager.factory().classMember(
				KAON2Manager.factory().owlClass(getEntityUri(ENTITY)),
				KAON2Manager.factory().individual(entity.getUri()));

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
