package org.xmedia.oms.adapter.kaon2.persistence.service.adaption;

import org.xmedia.oms.adapter.kaon2.persistence.model.adaptation.IContent;
import org.xmedia.oms.adapter.kaon2.persistence.model.adaptation.IContentBearingObject;
import org.xmedia.oms.adapter.kaon2.persistence.model.adaptation.IEntity;

public interface IContentDao extends IDao {
	
	public void setCBO(IContent content, IContentBearingObject cbo);
	
	public void setPostContent(IContent content, IContent postContent);
	
	public void setSubject(IContent content, IEntity entity);
	
	public void insert(IContent resource);

}
