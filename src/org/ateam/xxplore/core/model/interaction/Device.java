package org.ateam.xxplore.core.model.interaction;

public class Device implements IDevice {
	private String m_uri;
	private String m_name;
	private String m_des;
	
	public Device(String m_uri){
		this.m_uri = m_uri;
	}
	public String getUri(){
		return this.m_uri;
	}
	
	public void setName(String m_name){
		this.m_name = m_name;
	}
	public String getDescription() {
		// TODO Auto-generated method stub
		return this.m_des;
	}
	public String getName() {
		// TODO Auto-generated method stub
		return this.m_name;
	}
	public void setDescription(String m_des) {
		// TODO Auto-generated method stub
		this.m_des = m_des;		
	}
}
