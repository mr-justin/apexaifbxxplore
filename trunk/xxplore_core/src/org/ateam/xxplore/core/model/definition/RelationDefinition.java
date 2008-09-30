package org.ateam.xxplore.core.model.definition;

import org.xmedia.oms.model.api.IProperty;
import org.xmedia.oms.persistence.IDataSource;

public class RelationDefinition extends AbstractDefinition implements IRelationDefinition {

	public RelationDefinition(IDataSource datasource) {
		super(datasource);
	}
	
	public RelationDefinition(IDataSource datasource, IProperty property) {
		this(datasource);
		m_property = property;
	}

	private IProperty m_property; 
	
	public IProperty getDefinition() {
		
		return m_property;
	}

	public void setDefinition(IProperty description) {
		m_property = description;

	}
	
	public String toString() {
		return "[RelationDefinition, definition: " + (m_property != null ?  m_property.getLabel() : m_property) + "]";
	}
	
	public boolean equals(Object obj) {
		if (obj instanceof RelationDefinition) {
			RelationDefinition rd = (RelationDefinition) obj;
			return (this.m_property == null ? rd.m_property == null : this.m_property.equals(rd.m_property));
		}

		return false;
	}
}
