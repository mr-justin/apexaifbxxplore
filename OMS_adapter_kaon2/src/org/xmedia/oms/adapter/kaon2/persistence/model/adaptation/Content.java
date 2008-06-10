package org.xmedia.oms.adapter.kaon2.persistence.model.adaptation;

import org.xmedia.oms.adapter.kaon2.persistence.service.adaption.ContentDao;
import org.xmedia.oms.adapter.kaon2.persistence.service.adaption.IContentDao;


public class Content implements IContent {
	
	private String m_uri;

    public Content() {
    } 
    
    public Content(String uri) {
    	m_uri = uri;
    }

	public String getUri() {
		return m_uri;
	}

	public void setCBO(IContentBearingObject cbo) {
		IContentDao dao = ContentDao.getInstance();
		dao.setCBO(this,cbo);
	}

	public void setPostContent(IContent content) {
		IContentDao dao = ContentDao.getInstance();
		dao.setPostContent(this,content);
	}

	public void setSubject(IEntity subject) {
		IContentDao dao = ContentDao.getInstance();
		dao.setSubject(this,subject);
	}

	public void storeContent() {
		IContentDao dao = ContentDao.getInstance();
		dao.insert(this);
	}

}
