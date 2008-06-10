package org.xmedia.accessknow.sesame.persistence.service.adaptation;

import org.xmedia.accessknow.sesame.persistence.model.adaptation.IContent;
import org.xmedia.accessknow.sesame.persistence.model.adaptation.IContentBearingObject;
import org.xmedia.accessknow.sesame.persistence.model.adaptation.ICredential;

public interface IContentBearingObjectDao extends IDao {
	
	public void setContent(IContentBearingObject cbo, IContent content);
	
	public void setCredential(IContentBearingObject cbo, ICredential credential);
	
	public void insert(IContentBearingObject cbo);

}
