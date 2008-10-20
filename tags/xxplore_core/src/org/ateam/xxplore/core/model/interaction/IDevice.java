package org.ateam.xxplore.core.model.interaction;

public interface IDevice {

	public abstract String getUri();
	
	public abstract void setName(String m_name);
	
	public abstract String getName();
	
	public abstract void setDescription(String m_des);
	
	public abstract String getDescription();
}
