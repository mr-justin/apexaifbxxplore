package org.xmedia.oms.adapter.kaon2.persistence.model.interaction;

import java.util.HashSet;
import java.util.Set;

import org.aifb.xxplore.shared.exception.Emergency;
import org.xmedia.oms.adapter.kaon2.persistence.Kaon2Transaction;
import org.xmedia.oms.model.api.IIndividual;
import org.xmedia.oms.model.api.ILiteral;
import org.xmedia.oms.model.api.INamedConcept;
import org.xmedia.oms.model.api.INamedIndividual;
import org.xmedia.oms.model.api.IPropertyMember;
import org.semanticweb.kaon2.api.Axiom;
import org.semanticweb.kaon2.api.KAON2Factory;
import org.semanticweb.kaon2.api.KAON2Manager;
import org.xmedia.oms.persistence.PersistenceUtil;
import org.xmedia.oms.persistence.SessionFactory;
import org.xmedia.oms.persistence.StatelessSession;
import org.xmedia.oms.persistence.dao.DaoUnavailableException;
import org.xmedia.oms.persistence.dao.IConceptDao;
import org.xmedia.oms.persistence.dao.IIndividualDao;
import org.xmedia.oms.persistence.dao.IPropertyMemberAxiomDao;
import org.semanticweb.kaon2.api.OntologyChangeEvent;


public class CognitiveAgentDao implements ICognitiveAgentDao {
	
	private static ICognitiveAgentDao m_instance = null;

	private final String POLICY_ONTOLOGY_URI = "http://www.applicationontologies/fiat/policy";

	private final String NAME = POLICY_ONTOLOGY_URI + "#name";

	private final String COGNITION_AGENT = POLICY_ONTOLOGY_URI + "#cognition agent";

	public static ICognitiveAgentDao getInstance() {
		if (m_instance == null)
			m_instance = new CognitiveAgentDao();
		return m_instance;
	}

	public Set<ICognitiveAgent> findAllCognitiveAgents() {
		try {
			IConceptDao cDao = PersistenceUtil.getDaoManager().getConceptDao();

			Set<ICognitiveAgent> agentUris = new HashSet<ICognitiveAgent>();

			INamedConcept envConcept = cDao.findByUri(POLICY_ONTOLOGY_URI
					+ "#cognitive agent");
			for (IIndividual processInd : envConcept.getMemberIndividuals(true)) {
				agentUris.add(new CognitiveAgent(
						((INamedIndividual) processInd).getUri()));
			}
			return agentUris;

		} catch (DaoUnavailableException e) {
			e.printStackTrace();
		}

		return null;
	}

	public ICognitiveAgent findCognitiveAgentByUri(String agentUri) {

		ICognitiveAgent agent = null;

		try {
			IIndividualDao iDao = PersistenceUtil.getDaoManager()
					.getIndividualDao();
			IPropertyMemberAxiomDao pDao = PersistenceUtil.getDaoManager()
					.getPropertyMemberDao();
			INamedIndividual envIndividual = iDao.findByUri(agentUri);

			agent = new CognitiveAgent(agentUri);

			Set<IPropertyMember> props = pDao
					.findBySourceIndividual(envIndividual);

			for (IPropertyMember prop : props) {
				if (prop.getProperty().getUri().equals(NAME))
					agent.setName(((ILiteral) prop.getTarget()).getLiteral());
			}
		} catch (DaoUnavailableException e) {
			e.printStackTrace();
		}

		return agent;
	}

	protected Kaon2Transaction getTransaction() {
		StatelessSession session = (StatelessSession) SessionFactory
				.getInstance().getCurrentSession();
		return (Kaon2Transaction) session.getTransaction();
	}

	public void saveCognitiveAgent(ICognitiveAgent agent) {
		Emergency.checkPrecondition(agent instanceof CognitiveAgent,
				"agent instanceof CognitiveAgent");

		Kaon2Transaction trans = getTransaction();

		Set<Axiom> axioms = new HashSet<Axiom>();

		KAON2Factory factory = KAON2Manager.factory();
		Axiom agentAxiom = factory.classMember(factory
				.owlClass(COGNITION_AGENT), factory.individual(agent.getUri()));

		axioms.add(agentAxiom);

		for (Axiom axiom : axioms) {
			OntologyChangeEvent event = new OntologyChangeEvent(axiom,
					OntologyChangeEvent.ChangeType.ADD);
			trans.addChanges(event);
		}
	}
}
