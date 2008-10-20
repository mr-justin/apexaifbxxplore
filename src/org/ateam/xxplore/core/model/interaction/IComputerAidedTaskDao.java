package org.ateam.xxplore.core.model.interaction;

import java.util.Set;

import org.xmedia.oms.model.api.INamedIndividual;

public interface IComputerAidedTaskDao{
	public IComputerAidedTask findComputerAidedTaskByUri(String taskUri);
	public Set<IComputerAidedTask> findAllComputerAidedTasks();
	public Set<INamedIndividual> findAgentsForTask(IComputerAidedTask task);
	public void saveComputerAidedTask(IComputerAidedTask task);
}
