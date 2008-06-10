package org.aifb.xxplore.core.service.datafiltering;

import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

import org.aifb.xxplore.core.ExploreEnvironment;
import org.aifb.xxplore.core.service.datafiltering.ITaskPolicyDao;
import org.xmedia.oms.model.api.IIndividual;
import org.xmedia.oms.model.api.ILiteral;
import org.xmedia.oms.model.api.INamedConcept;
import org.xmedia.oms.model.api.INamedIndividual;
import org.xmedia.oms.model.api.IOntology;
import org.xmedia.oms.model.api.IPropertyMember;
import org.xmedia.oms.persistence.IKbConnection;
import org.xmedia.oms.persistence.OntologyLoadException;
import org.xmedia.oms.persistence.PersistenceUtil;
import org.xmedia.oms.persistence.SessionFactory;
import org.xmedia.oms.persistence.StatelessSession;
import org.xmedia.oms.persistence.dao.DaoUnavailableException;
import org.xmedia.oms.persistence.dao.IConceptDao;
import org.xmedia.oms.persistence.dao.IIndividualDao;
import org.xmedia.oms.persistence.dao.IPropertyMemberAxiomDao;

public class TaskPolicyDao implements ITaskPolicyDao {
	
	private static ITaskPolicyDao m_instance = null;

	private final String NAME = "name";
	private final String DESCRIPTION = "description";
	private final String AGENT = "agent";
	private final String HAS_RESOURCE_PROVIDER = "has_resoure_provider";
	private final String INSTRUMENT = "instrument";
	private final String NOTES = "notes";
	private final String CREATION_DATE = "creation_date";
	private final String END_DATE = "end_date";

	private TaskPolicyDao() {
		
	}
	
	public static ITaskPolicyDao getInstance() {
		if(m_instance == null) {
			m_instance = new TaskPolicyDao();
		}
		return m_instance;
	}
	
	public IOntology findOrganizationalPolicybyUri(String organizationUri) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private IOntology getPolicyOntology() {
		IKbConnection conn = ((StatelessSession)SessionFactory.getInstance().getCurrentSession()).getConnection();
		return conn.findOntologyByUri((String)conn.getConfiguration().get(ExploreEnvironment.POLICY_ONTOLOGY_URI));
	}

	private String getBasePolicyOntologyUri() {
		return (String)((StatelessSession)SessionFactory.getInstance().getCurrentSession()).getConnection().getConfiguration().get(ExploreEnvironment.BASE_POLICY_ONTOLOGY_URI);
	}
	
	private String getEntityUri(String entity) {
		return getBasePolicyOntologyUri() + "#" + entity;
	}

	public ITask findTaskPolicyByUri(String taskUri) {
		StatelessSession session = (StatelessSession) SessionFactory.getInstance().getCurrentSession();
//		IOntology oldOnto = session.getOntology();
		
		ITask task = null;
		
		IOntology activeOnto = session.getOntology();
		
		try {
			session.setOntology(getPolicyOntology());
			
			IIndividualDao iDao = PersistenceUtil.getDaoManager().getIndividualDao();
			IPropertyMemberAxiomDao pDao = PersistenceUtil.getDaoManager().getPropertyMemberDao();
			INamedIndividual taskIndividual = iDao.findByUri(taskUri);
			
			task = new Task(taskUri);
			
			Set<IPropertyMember> props = pDao.findBySourceIndividual(taskIndividual);
			for (IPropertyMember prop : props) {
				if (prop.getProperty().getUri().equals(getEntityUri(NAME))) {
					task.setName(((ILiteral)prop.getTarget()).getLiteral());
				}
				
				if (prop.getProperty().getUri().equals(getEntityUri(DESCRIPTION))) {
					task.setDescription(((ILiteral)prop.getTarget()).getLiteral());
				}

				if (prop.getProperty().getUri().equals(getEntityUri(NOTES))) {
					task.setNotes(((ILiteral)prop.getTarget()).getLiteral());
				}

				if (prop.getProperty().getUri().equals(getEntityUri(CREATION_DATE))) {
					task.setCreationDate(((ILiteral)prop.getTarget()).getLiteral());
				}
				
				if (prop.getProperty().getUri().equals(getEntityUri(END_DATE))) {
					task.setEndDate(((ILiteral)prop.getTarget()).getLiteral());
				}
			}
			
			session.setOntology(activeOnto);
		} catch (DaoUnavailableException e) {
			e.printStackTrace();
		} catch (OntologyLoadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return task;
	}

	public Set<INamedIndividual> findAgentsForTask(ITask task) {
		StatelessSession session = (StatelessSession) SessionFactory.getInstance().getCurrentSession();
//		IOntology onto = session.getOntology();
		
		IOntology activeOnto = session.getOntology();
		
		try {
			session.setOntology(getPolicyOntology());
			IIndividualDao iDao = PersistenceUtil.getDaoManager().getIndividualDao();
			IPropertyMemberAxiomDao pDao = PersistenceUtil.getDaoManager().getPropertyMemberDao();
			INamedIndividual taskIndividual = iDao.findByUri(task.getUri());

			Set<INamedIndividual> agents = new HashSet<INamedIndividual>();
			
			Set<IPropertyMember> props = pDao.findBySourceIndividual(taskIndividual);
			for (IPropertyMember prop : props) {
				if (prop.getProperty().getUri().equals(getEntityUri(AGENT))) {
					agents.add((INamedIndividual) prop.getTarget());
				}
			}
			
			session.setOntology(activeOnto);
			
			return agents;
		} catch (DaoUnavailableException e) {
			e.printStackTrace();
		} catch (OntologyLoadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}

	public Set<String> findAllTaskPolicies() {
		StatelessSession session = (StatelessSession) SessionFactory.getInstance().getCurrentSession();
//		IOntology onto = session.getOntology();
		
		IOntology activeOnto = session.getOntology();
		
		try {
			session.setOntology(getPolicyOntology());
			
			IConceptDao cDao = PersistenceUtil.getDaoManager().getConceptDao();

			Set<String> taskUris = new HashSet<String>();
			
			INamedConcept processConcept = cDao.findByUri(getEntityUri("Process"));
			for (IIndividual processInd : processConcept.getMemberIndividuals(true)) {
				taskUris.add(((INamedIndividual)processInd).getUri());
			}
			
			session.setOntology(activeOnto);
			
			return taskUris;
		} catch (DaoUnavailableException e) {
			e.printStackTrace();
		} catch (OntologyLoadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}

	public Set<INamedIndividual> findInformationProviderForTask(ITask task) {
		StatelessSession session = (StatelessSession) SessionFactory.getInstance().getCurrentSession();
//		IOntology onto = session.getOntology();
		
		IOntology activeOnto = session.getOntology();
		
		try {
			session.setOntology(getPolicyOntology());
			
			IIndividualDao iDao = PersistenceUtil.getDaoManager().getIndividualDao();
			IPropertyMemberAxiomDao pDao = PersistenceUtil.getDaoManager().getPropertyMemberDao();
			INamedIndividual taskIndividual = iDao.findByUri(task.getUri());

			Set<INamedIndividual> informationProviders = new HashSet<INamedIndividual>();
			
			Set<IPropertyMember> props = pDao.findBySourceIndividual(taskIndividual);
			for (IPropertyMember prop : props) {
				if (prop.getProperty().getUri().equals(getEntityUri(HAS_RESOURCE_PROVIDER))) {
					informationProviders.add((INamedIndividual) prop.getTarget());
				}
				
				if (prop.getProperty().getUri().equals(getEntityUri(INSTRUMENT))) {
					informationProviders.add((INamedIndividual) prop.getTarget());
				}
			}
			
			session.setOntology(activeOnto);
			
			return informationProviders;
		} catch (DaoUnavailableException e) {
			e.printStackTrace();
		} catch (OntologyLoadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}

	public void saveTask(ITask task) {
		// TODO Auto-generated method stub
		
	}
}
