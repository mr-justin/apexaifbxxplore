package org.xmedia.oms.persistence.dao;

import org.xmedia.oms.model.api.IEntity;
import org.xmedia.oms.model.impl.AbstractEntity;
import org.xmedia.oms.persistence.DatasourceException;

public interface IEntityDao<Entity extends IEntity> extends IDao{

	public Entity findByUri(String uri) throws DatasourceException;

}
