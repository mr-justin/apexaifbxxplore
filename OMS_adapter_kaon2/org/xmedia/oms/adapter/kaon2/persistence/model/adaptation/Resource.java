package org.xmedia.oms.adapter.kaon2.persistence.model.adaptation;


public class Resource implements IResource {

	private String m_uri;

	private IContent content = null;

	private IContentBearingObject cbo = null;

	private int type;
	
	private final int CONTENT = 0;
	
	private final int CBO = 1;

	
	public Resource() {
	}

	public Resource(String uri, int type) {
		m_uri = uri;
		this.type = type; 
		if (this.type == CONTENT)
			content = new Content(uri);
		else if (this.type == CBO)
			cbo = new ContentBearingObject(uri);
	}

	public String getUri() {
		return m_uri;
	}

	public void setCBO(IContentBearingObject cbo) {
		if(content != null)
			content.setCBO(cbo);
	}

	public void setPostContent(IContent content) {
		content.setPostContent(content);
	}

	public void setSubject(IEntity subject) {
		if(content != null)
			content.setSubject(subject);
	}

	public void setContent(IContent content) {
		if(cbo != null)
			cbo.setContent(content);
	}

	public void setCredential(ICredential credential) {
		if(cbo != null)
			cbo.setCredential(credential);
	}

	public void storeContent() {
		if(content != null)
			content.storeContent();
	}

	public void storeCBO() {
		if(cbo != null)
			cbo.storeCBO();
	}
}
