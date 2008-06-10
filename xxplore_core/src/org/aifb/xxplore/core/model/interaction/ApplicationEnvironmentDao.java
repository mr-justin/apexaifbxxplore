package org.aifb.xxplore.core.model.interaction;

import java.util.HashSet;
import java.util.Set;

import org.xmedia.oms.model.api.IIndividual;
import org.xmedia.oms.model.api.ILiteral;
import org.xmedia.oms.model.api.INamedConcept;
import org.xmedia.oms.model.api.INamedIndividual;
import org.xmedia.oms.model.api.IPropertyMember;
import org.xmedia.oms.persistence.PersistenceUtil;
import org.xmedia.oms.persistence.dao.DaoUnavailableException;
import org.xmedia.oms.persistence.dao.IConceptDao;
import org.xmedia.oms.persistence.dao.IIndividualDao;
import org.xmedia.oms.persistence.dao.IPropertyMemberAxiomDao;

public class ApplicationEnvironmentDao implements IApplicationEnvironmentDao{
	private static IApplicationEnvironmentDao m_instance = null;
	
	private final String POLICY_ONTOLOGY_URI = "http://www.applicationontologies/fiat/policy";
	
	private final String DESCRIPTION = POLICY_ONTOLOGY_URI + "#description";
	
	private final String COGNITION_AGENT = POLICY_ONTOLOGY_URI + "#cognition agent";

	private final String DEVICE = POLICY_ONTOLOGY_URI + "#device";
	
	public static IApplicationEnvironmentDao getInstance() {
		if(m_instance == null)
			m_instance = new ApplicationEnvironmentDao();
		return m_instance;
	}
	
	public Set<IApplicationEnvironment> findAllApplicationEnvironment() {
	try {
			IConceptDao cDao = PersistenceUtil.getDaoManager().getConceptDao();

			Set<IApplicationEnvironment> envUris = new HashSet<IApplicationEnvironment>();
			
			INamedConcept envConcept = cDao.findByUri(POLICY_ONTOLOGY_URI + "#application environment");
			for (IIndividual processInd : envConcept.getMemberIndividuals(true)) {
				envUris.add( new ApplicationEnvironment(((INamedIndividual)processInd).getUri()));
			}
			return envUris;
			
		} catch (DaoUnavailableException e) {
			e.printStackTrace();
		}
		
		return null;
	}

	public IApplicationEnvironment findApplicationEnvironmentUri(String envUri) {
		IApplicationEnvironment env = null;
		
		try {
			IIndividualDao iDao = PersistenceUtil.getDaoManager().getIndividualDao();
			IPropertyMemberAxiomDao pDao = PersistenceUtil.getDaoManager().getPropertyMemberDao();
			INamedIndividual envIndividual = iDao.findByUri(envUri);
			
			env = new ApplicationEnvironment(envUri);
			
			Set<IPropertyMember> props = pDao.findBySourceIndividual(envIndividual);
			
			for (IPropertyMember prop : props) {
				if (prop.getProperty().getUri().equals(DESCRIPTION))
					env.setDescription(((ILiteral)prop.getTarget()).getLiteral());
			}
		} catch (DaoUnavailableException e) {
			e.printStackTrace();
		}
		
		return env;
	}

	public void saveIApplicationEnvironment(IApplicationEnvironment environment) {
		// TODO Auto-generated method stub
		
	}

	public Set<INamedIndividual> findAgentsForApplicationEnvironment(IApplicationEnvironment env) {
		try {
			IIndividualDao iDao = PersistenceUtil.getDaoManager().getIndividualDao();
			IPropertyMemberAxiomDao pDao = PersistenceUtil.getDaoManager().getPropertyMemberDao();
			INamedIndividual taskIndividual = iDao.findByUri(env.getUri());

			Set<INamedIndividual> agents = new HashSet<INamedIndividual>();
			
			Set<IPropertyMember> props = pDao.findBySourceIndividual(taskIndividual);
			for (IPropertyMember prop : props) {
				if (prop.getProperty().getUri().equals(COGNITION_AGENT)) {
					agents.add((INamedIndividual) prop.getTarget());
				}
			}
			return agents;
		} catch (DaoUnavailableException e) {
			e.printStackTrace();
		}
		
		return null;
	}

	public Set<INamedIndividual> findDevicesForApplicationEnvironment(IApplicationEnvironment env) {
		try {
			IIndividualDao iDao = PersistenceUtil.getDaoManager().getIndividualDao();
			IPropertyMemberAxiomDao pDao = PersistenceUtil.getDaoManager().getPropertyMemberDao();
			INamedIndividual taskIndividual = iDao.findByUri(env.getUri());

			Set<INamedIndividual> agents = new HashSet<INamedIndividual>();
			
			Set<IPropertyMember> props = pDao.findBySourceIndividual(taskIndividual);
			for (IPropertyMember prop : props) {
				if (prop.getProperty().getUri().equals(DEVICE)) {
					agents.add((INamedIndividual) prop.getTarget());
				}
			}
			return agents;
		} catch (DaoUnavailableException e) {
			e.printStackTrace();
		}
		
		return null;
	}

}
