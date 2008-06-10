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

public class ComputerAidedTaskDao implements IComputerAidedTaskDao{
	private static IComputerAidedTaskDao m_instance = null;
	private final String POLICY_ONTOLOGY_URI = "http://www.applicationontologies/fiat/policy";
	private final String NAME = POLICY_ONTOLOGY_URI + "#name";
	private final String DESCRIPTION = POLICY_ONTOLOGY_URI + "#description";
	private final String COGNITION_AGENT = POLICY_ONTOLOGY_URI + "#cognition agent";
	private final String CREATION_DATE = POLICY_ONTOLOGY_URI + "#creation_date";
	private final String END_DATE = POLICY_ONTOLOGY_URI + "#end_date";


	public static IComputerAidedTaskDao getInstance() {
		if (m_instance == null)
			m_instance = new ComputerAidedTaskDao();
		return m_instance;
	}

	public Set<INamedIndividual> findAgentsForTask(IComputerAidedTask task) {
		try {
			IIndividualDao iDao = PersistenceUtil.getDaoManager().getIndividualDao();
			IPropertyMemberAxiomDao pDao = PersistenceUtil.getDaoManager().getPropertyMemberDao();
			INamedIndividual taskIndividual = iDao.findByUri(task.getUri());

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

	public Set<IComputerAidedTask> findAllComputerAidedTasks() {
		try {
			IConceptDao cDao = PersistenceUtil.getDaoManager().getConceptDao();

			Set<IComputerAidedTask> taskUris = new HashSet<IComputerAidedTask>();
			
			INamedConcept computerAidedTaskConcept = cDao.findByUri(POLICY_ONTOLOGY_URI + "#Computer-aided Task");
			for (IIndividual computerAidedTaskInd : computerAidedTaskConcept.getMemberIndividuals(true)) {
				taskUris.add(new ComputerAidedTask(((INamedIndividual)computerAidedTaskInd).getUri()));
			}
			return taskUris;
			
		} catch (DaoUnavailableException e) {
			e.printStackTrace();
		}
		
		return null;
	}

	public IComputerAidedTask findComputerAidedTaskByUri(String taskUri) {
		// TODO Auto-generated method stub
		IComputerAidedTask task = null;
		
		try {
			IIndividualDao iDao = PersistenceUtil.getDaoManager().getIndividualDao();
			IPropertyMemberAxiomDao pDao = PersistenceUtil.getDaoManager().getPropertyMemberDao();
			INamedIndividual taskIndividual = iDao.findByUri(taskUri);
			
			task = new ComputerAidedTask(taskUri);
			
			Set<IPropertyMember> props = pDao.findBySourceIndividual(taskIndividual);
			for (IPropertyMember prop : props) {
				if (prop.getProperty().getUri().equals(NAME))
					task.setName(((ILiteral)prop.getTarget()).getLiteral());
				
				if (prop.getProperty().getUri().equals(DESCRIPTION))
					task.setDescription(((ILiteral)prop.getTarget()).getLiteral());

				if (prop.getProperty().getUri().equals(CREATION_DATE))
					task.setCreationDate(((ILiteral)prop.getTarget()).getLiteral());
				
				if (prop.getProperty().getUri().equals(END_DATE))
					task.setEndDate(((ILiteral)prop.getTarget()).getLiteral());
			}
		} catch (DaoUnavailableException e) {
			e.printStackTrace();
		}
		
		return task;
	
	}

	public void saveComputerAidedTask(IComputerAidedTask task) {
		// TODO Auto-generated method stub
		
	}
}
