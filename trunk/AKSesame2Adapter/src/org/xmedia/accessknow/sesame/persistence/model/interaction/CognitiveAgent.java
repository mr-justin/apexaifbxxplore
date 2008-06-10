package org.xmedia.accessknow.sesame.persistence.model.interaction;


public class CognitiveAgent implements ICognitiveAgent{
	
	private String m_uri;
	private String m_name;
	
	public CognitiveAgent(String m_uri){
		this.m_uri = m_uri;
	}
	public String getUri() {
		return m_uri;
	}
	
	public String getName(){
		return this.m_name;
	}
	
	public void setName(String m_name){
		this.m_name = m_name;
	}
}
