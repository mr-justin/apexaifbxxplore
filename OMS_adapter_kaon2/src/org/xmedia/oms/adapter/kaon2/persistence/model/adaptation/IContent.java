package org.xmedia.oms.adapter.kaon2.persistence.model.adaptation;


public interface IContent {
	
	public String getUri();
	
	public void setSubject(IEntity subject);
	
	public void setPostContent(IContent content);

	public void setCBO(IContentBearingObject cbo);
	
	public void storeContent();

}
