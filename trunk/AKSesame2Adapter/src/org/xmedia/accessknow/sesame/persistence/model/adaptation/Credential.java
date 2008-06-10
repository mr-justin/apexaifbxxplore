package org.xmedia.accessknow.sesame.persistence.model.adaptation;

import org.xmedia.accessknow.sesame.persistence.service.adaptation.CredentialDao;
import org.xmedia.accessknow.sesame.persistence.service.adaptation.ICredentialDao;

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
