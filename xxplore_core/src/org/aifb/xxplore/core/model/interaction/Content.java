package org.aifb.xxplore.core.model.interaction;

public class Content implements IContent{
	
	private String m_uri;
	
	private String m_des;
	
	public Content(String m_uri){
		this.m_uri = m_uri;
	}
	public String getUri(){
		return this.m_uri;
	}
	public String getDes(){
		return this.m_des;
	}
	public void setUri(String m_uri){
		this.m_uri = m_uri;
	}
	public void setDes(String m_des){
		this.m_des = m_des;
	}
}
