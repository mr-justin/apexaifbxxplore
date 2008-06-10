package org.aifb.xxplore.core.model.interaction;

import java.util.Set;

import org.xmedia.oms.model.api.INamedIndividual;

public class ApplicationEnvironment implements IApplicationEnvironment{
	private String m_uri;
	
	private String m_des;
	
	public ApplicationEnvironment(String m_uri){
		this.m_uri = m_uri;
	}
	public String getUri(){
		return this.m_uri;
	}
	public Set<INamedIndividual> getAgents() {
		// TODO Auto-generated method stub
		return ApplicationEnvironmentDao.getInstance().findAgentsForApplicationEnvironment(this);
	}
	
	public Set<INamedIndividual> getDevices(){
		return ApplicationEnvironmentDao.getInstance().findDevicesForApplicationEnvironment(this);
	}
	public String getDescription() {
		// TODO Auto-generated method stub
		return this.m_des;
	}
	public void setDescription(String m_des) {
		// TODO Auto-generated method stub
		this.m_des = m_des;
	}
}
