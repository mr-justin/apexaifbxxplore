package org.xmedia.accessknow.sesame.persistence.model.interaction;

import java.util.HashSet;
import java.util.Set;

import org.aifb.xxplore.shared.exception.Emergency;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.xmedia.oms.model.api.IIndividual;
import org.xmedia.oms.model.api.ILiteral;
import org.xmedia.oms.model.api.INamedConcept;
import org.xmedia.oms.model.api.INamedIndividual;
import org.xmedia.oms.model.api.IOntology;
import org.xmedia.oms.model.api.IPropertyMember;
import org.xmedia.oms.persistence.PersistenceUtil;
import org.xmedia.oms.persistence.SessionFactory;
import org.xmedia.oms.persistence.StatelessSession;
import org.xmedia.oms.persistence.dao.DaoUnavailableException;
import org.xmedia.oms.persistence.dao.IConceptDao;
import org.xmedia.oms.persistence.dao.IIndividualDao;
import org.xmedia.oms.persistence.dao.IPropertyMemberAxiomDao;
import org.xmedia.accessknow.sesame.model.SesameOntology;


public class CognitiveAgentDao implements ICognitiveAgentDao {
	
	private static ICognitiveAgentDao m_instance = null;

	private final String POLICY_ONTOLOGY_URI = "http://www.applicationontologies/fiat/policy";

	private final String NAME = POLICY_ONTOLOGY_URI + "#name";

	private final String COGNITION_AGENT = POLICY_ONTOLOGY_URI
			+ "#cognition agent";

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


	public void saveCognitiveAgent(ICognitiveAgent agent) {
		
		Emergency.checkPrecondition(agent instanceof CognitiveAgent,"agent instanceof CognitiveAgent");
		StatelessSession session = (StatelessSession) SessionFactory.getInstance().getCurrentSession();
		IOntology onto = session.getOntology();
		
		if (onto instanceof SesameOntology)
		{
			ValueFactory factory = ((SesameOntology)onto).getRepository().getValueFactory();

			URI subject = factory.createURI(agent.getUri());
			URI object = factory.createURI(COGNITION_AGENT);

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

