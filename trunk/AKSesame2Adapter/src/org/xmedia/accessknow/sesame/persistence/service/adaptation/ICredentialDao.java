package org.xmedia.accessknow.sesame.persistence.service.adaptation;

import org.xmedia.accessknow.sesame.persistence.model.adaptation.ICredential;

public interface ICredentialDao extends IDao {

	public void insert(ICredential credential);
	
}
