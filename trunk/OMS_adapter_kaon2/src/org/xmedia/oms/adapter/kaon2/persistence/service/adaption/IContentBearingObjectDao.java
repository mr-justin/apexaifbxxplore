package org.xmedia.oms.adapter.kaon2.persistence.service.adaption;

import org.xmedia.oms.adapter.kaon2.persistence.model.adaptation.IContent;
import org.xmedia.oms.adapter.kaon2.persistence.model.adaptation.IContentBearingObject;
import org.xmedia.oms.adapter.kaon2.persistence.model.adaptation.ICredential;


public interface IContentBearingObjectDao extends IDao {
	
	public void setContent(IContentBearingObject cbo, IContent content);
	
	public void setCredential(IContentBearingObject cbo, ICredential credential);
	
	public void insert(IContentBearingObject cbo);

}
