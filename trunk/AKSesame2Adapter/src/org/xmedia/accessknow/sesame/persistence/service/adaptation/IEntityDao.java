package org.xmedia.accessknow.sesame.persistence.service.adaptation;

import org.xmedia.accessknow.sesame.persistence.model.adaptation.IEntity;
import org.xmedia.accessknow.sesame.persistence.model.adaptation.IProcess;

public interface IEntityDao extends IDao {

	public void setRelatedEntity(IEntity entity, IEntity related);
	
	public void setInvolvingProcess(IEntity entity, IProcess process);
	
	public void insert(IEntity entity);
}
