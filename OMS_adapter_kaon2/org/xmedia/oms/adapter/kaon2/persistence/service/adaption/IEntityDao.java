package org.xmedia.oms.adapter.kaon2.persistence.service.adaption;

import org.xmedia.oms.adapter.kaon2.persistence.model.adaptation.IEntity;
import org.xmedia.oms.adapter.kaon2.persistence.model.adaptation.IProcess;


public interface IEntityDao extends IDao {

	public void setRelatedEntity(IEntity entity, IEntity related);
	
	public void setInvolvingProcess(IEntity entity, IProcess process);
	
	public void insert(IEntity entity);
}
