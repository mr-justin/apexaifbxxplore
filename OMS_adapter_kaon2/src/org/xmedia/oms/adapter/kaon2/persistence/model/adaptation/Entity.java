package org.xmedia.oms.adapter.kaon2.persistence.model.adaptation;

import org.xmedia.oms.adapter.kaon2.persistence.service.adaption.EntityDao;
import org.xmedia.oms.adapter.kaon2.persistence.service.adaption.IEntityDao;


public class Entity implements IEntity {
	
	private String m_uri;

    public Entity() {
    } 
    
    public Entity(String uri) {
    	m_uri = uri;
    }

	public String getUri() {
		return m_uri;
	}

	public void setInvolvingProcess(IProcess process) {
		IEntityDao dao = EntityDao.getInstance();
		dao.setInvolvingProcess(this,process);
	}

	public void setRelatedEntity(IEntity entity) {
		IEntityDao dao = EntityDao.getInstance();
		dao.setRelatedEntity(this,entity);
	}

	public void storeEntity() {
		IEntityDao dao = EntityDao.getInstance();
		dao.insert(this);
	}

}
