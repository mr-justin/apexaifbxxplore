package org.ateam.xxplore.core.model.interaction;

import java.util.Set;

import org.xmedia.oms.model.api.INamedIndividual;

public interface IApplicationEnvironmentDao {
	
	public IApplicationEnvironment findApplicationEnvironmentUri(String envUri);
	
	public Set<IApplicationEnvironment> findAllApplicationEnvironment();

	public Set<INamedIndividual> findAgentsForApplicationEnvironment(IApplicationEnvironment env);
	
	public Set<INamedIndividual> findDevicesForApplicationEnvironment(IApplicationEnvironment env);
	
	public void saveIApplicationEnvironment(IApplicationEnvironment environment);
	
}
