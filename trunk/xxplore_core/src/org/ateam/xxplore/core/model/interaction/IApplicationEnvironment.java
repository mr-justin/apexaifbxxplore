package org.ateam.xxplore.core.model.interaction;

import java.util.Set;

import org.xmedia.oms.model.api.INamedIndividual;

public interface IApplicationEnvironment {
	
	public abstract String getUri();
	
	public void setDescription(String m_des);
	
	public abstract String getDescription();
	
	public abstract Set<INamedIndividual> getAgents();
	
	public abstract Set<INamedIndividual> getDevices();
}
