package org.xmedia.oms.adapter.kaon2.persistence.model.adaptation;

import org.xmedia.oms.adapter.kaon2.persistence.service.adaption.CredentialDao;
import org.xmedia.oms.adapter.kaon2.persistence.service.adaption.ICredentialDao;

public class Credential implements ICredential {

	private String m_uri;

    public Credential() {
    } 
    
    public Credential(String uri) {
    	m_uri = uri;
    }

	public String getUri() {
		return m_uri;
	}

	public void storeCredential() {
		ICredentialDao dao = CredentialDao.getInstance();
		dao.insert(this);
	}	

}
