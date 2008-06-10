package org.xmedia.accessknow.sesame.persistence.model.adaptation;

public class ApplicationEnvironment implements IApplicationEnvironment{
	private String m_uri;
	
	public ApplicationEnvironment() {
	}

	public ApplicationEnvironment(String uri) {
		m_uri = uri;
	}
	
	public String getUri() {
		return m_uri;
	}
}
