package org.xmedia.oms.adapter.kaon2.persistence.model.adaptation;


public interface IContentBearingObject {

	public String getUri();
	
	public void setContent(IContent content);
	
	public void setCredential(ICredential credential);
	
	public void storeCBO();
}
