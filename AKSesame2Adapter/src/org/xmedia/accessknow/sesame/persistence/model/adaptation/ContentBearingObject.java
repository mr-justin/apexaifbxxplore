package org.xmedia.accessknow.sesame.persistence.model.adaptation;

import org.xmedia.accessknow.sesame.persistence.service.adaptation.ContentBearingObjectDao;
import org.xmedia.accessknow.sesame.persistence.service.adaptation.IContentBearingObjectDao;

public class ContentBearingObject implements IContentBearingObject {
	
	private String m_uri;

    public ContentBearingObject() {
    } 
    
    public ContentBearingObject(String uri) {
    	m_uri = uri;
    }

	public String getUri() {
		return m_uri;
	}

	public void setContent(IContent content) {
		IContentBearingObjectDao dao = ContentBearingObjectDao.getInstance();
		dao.setContent(this, content);
	}

	public void setCredential(ICredential credential) {
		IContentBearingObjectDao dao = ContentBearingObjectDao.getInstance();
		dao.setCredential(this, credential);
	}

	public void storeCBO() {
		IContentBearingObjectDao dao = ContentBearingObjectDao.getInstance();
		dao.insert(this);
	}

}
