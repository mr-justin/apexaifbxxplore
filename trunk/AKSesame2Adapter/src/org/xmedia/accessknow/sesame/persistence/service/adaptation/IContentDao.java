package org.xmedia.accessknow.sesame.persistence.service.adaptation;

import org.xmedia.accessknow.sesame.persistence.model.adaptation.IContent;
import org.xmedia.accessknow.sesame.persistence.model.adaptation.IContentBearingObject;
import org.xmedia.accessknow.sesame.persistence.model.adaptation.IEntity;


public interface IContentDao extends IDao {
	
	public void setCBO(IContent content, IContentBearingObject cbo);
	
	public void setPostContent(IContent content, IContent postContent);
	
	public void setSubject(IContent content, IEntity entity);
	
	public void insert(IContent resource);

}
