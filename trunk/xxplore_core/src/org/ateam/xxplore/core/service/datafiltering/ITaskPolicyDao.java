package org.ateam.xxplore.core.service.datafiltering;

import java.util.Set;

import org.xmedia.oms.model.api.IIndividual;
import org.xmedia.oms.model.api.INamedIndividual;
import org.xmedia.oms.model.api.IOntology;
import org.xmedia.oms.persistence.dao.IDao;

public interface ITaskPolicyDao {
	public IOntology findOrganizationalPolicybyUri(String organizationUri);
	public ITask findTaskPolicyByUri(String taskUri);
	public Set<String> findAllTaskPolicies();
	public Set<INamedIndividual> findAgentsForTask(ITask task);
	public Set<INamedIndividual> findInformationProviderForTask(ITask task);
	public void saveTask(ITask task);
}
