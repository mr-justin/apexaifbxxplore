package org.xmedia.oms.adapter.kaon2.persistence.service.adaption;

import org.xmedia.oms.adapter.kaon2.persistence.model.adaptation.ICredential;

public interface ICredentialDao extends IDao {

	public void insert(ICredential credential);
	
}
