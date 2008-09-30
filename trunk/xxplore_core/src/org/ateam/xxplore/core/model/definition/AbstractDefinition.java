/**
 * 
 */
package org.ateam.xxplore.core.model.definition;

import java.util.Collection;

import org.xmedia.oms.model.api.IResource;
import org.xmedia.oms.persistence.IDataSource;

public abstract class AbstractDefinition implements IDefinition{

	private IDataSource m_datasource;
	
	private Collection<IResource> m_changes;
	
	private IDefinition m_superDefinition;
	
	public AbstractDefinition(IDataSource datasource) {
		m_datasource= datasource;
	}
	
	public IDataSource getDataSource() {
		return m_datasource;
	}

	public Collection<IResource> getChanges() {
		
		return m_changes;
	}
	
	public void clearChanges(){
		m_changes.clear();
	}
	
	public IDefinition getSuperDefinition() {
		return m_superDefinition;
	}
	
	public void setSuperDefinition(IDefinition parent) {
		m_superDefinition = parent;
	}
}