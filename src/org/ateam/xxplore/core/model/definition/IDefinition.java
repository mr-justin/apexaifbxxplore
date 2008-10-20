package org.ateam.xxplore.core.model.definition;

import java.util.Collection;

import org.xmedia.oms.model.api.IResource;
import org.xmedia.oms.persistence.IDataSource;

public interface IDefinition {
	
	/**
	 * the datasource 
	 * @return
	 */
	public IDataSource getDataSource();
	
	public Collection<IResource> getChanges();

	public IDefinition getSuperDefinition();
	
	public void setSuperDefinition(IDefinition parent);
}
