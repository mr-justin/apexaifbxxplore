package org.ateam.xxplore.core.model.definition;

import java.util.Set;

import org.aifb.xxplore.shared.exception.Emergency;
import org.xmedia.oms.model.api.INamedConcept;
import org.xmedia.oms.model.api.IResource;
import org.xmedia.oms.model.owl.api.ICompositeConcept;
import org.xmedia.oms.persistence.IDataSource;

public class EntityDefinition extends AbstractDefinition implements IEntityDefinition {

	private int m_type;

	private IResource m_definition;
	
	public EntityDefinition(IDataSource datasource, int type) {
		super(datasource);
		Emergency.checkPrecondition(type == IEntityDefinition.SUBJECT || type == IEntityDefinition.OBJECT, "type == SUBJECT || type == OBJECT");
		m_type = type; 
	}

	public EntityDefinition(IDataSource datasource, int type, IResource definition) {
		this(datasource, type);
		m_definition = definition;
	}
	
	public IResource getDefinition() {
		
		return m_definition;

	}

	public void setDefinition(IResource description) {
		m_definition = description;

	}

	public void addDefinition(IResource definition, int type) {
		Emergency.checkPrecondition(m_definition != null, "no definition set yet!");
		Emergency.checkPrecondition(type == IEntityDefinition.OR_COMBINATION || type == IEntityDefinition.AND_COMBINATION, 
				"type == IEntityDefinition.OR_COMBINATION || type == IEntityDefinition.AND_COMBINATION");
		
	}	
	
	public int getType(){
		
		return m_type;
	}

	public Set getSubDefinition() {
		if (!hasSubDefinition()) return null;
		else return ((ICompositeConcept)m_definition).getConcepts();
	}

	public boolean hasSubDefinition() {
		if (m_definition == null || !(m_definition instanceof ICompositeConcept)) return false;
		else return true;
	}

	public String toString() {
		return "[EntityDefinition, type: " + m_type + ", definition: " + (m_definition != null ? m_definition.getLabel() : m_definition ) + "]";
	}

	public boolean equals(Object obj) {
		if (obj instanceof EntityDefinition) {
			EntityDefinition ed = (EntityDefinition) obj;
			return (m_type == ed.getType())
					&& (this.m_definition == null ? ed.m_definition == null : this.m_definition.equals(ed.m_definition));
		}
		return false;
	}
}
