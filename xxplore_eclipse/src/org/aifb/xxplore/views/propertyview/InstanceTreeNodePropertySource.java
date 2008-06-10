package org.aifb.xxplore.views.propertyview;

import java.util.Set;

import org.aifb.xxplore.model.ElementContentProvider.InstanceTreeNode;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.xmedia.oms.model.api.IConcept;
import org.xmedia.oms.model.api.INamedConcept;
import org.xmedia.oms.model.api.INamedIndividual;
import org.xmedia.oms.model.api.IPropertyMember;
import org.xmedia.oms.model.impl.DataProperty;
import org.xmedia.oms.model.impl.ObjectProperty;

public class InstanceTreeNodePropertySource implements IPropertySource {
	

	private Set<IPropertyMember> m_propertyMembers;	
	private Set<IConcept> m_subconcept;
	private Set<IConcept> m_superconcept;
	private IPropertyDescriptor[] m_descriptors;
	
	private int m_indicator = 0;
	
	private final static int CONCEPT = 1;
	private final static int INDIVIDUAL = 2;
	
	
	public InstanceTreeNodePropertySource(InstanceTreeNode node) {

		super();

		if (node.getValue() instanceof INamedIndividual) {
			
			m_indicator = INDIVIDUAL;
			
			INamedIndividual ind = (INamedIndividual)node.getValue();
			m_propertyMembers = ind.getPropertyFromValues();			
			m_descriptors = new IPropertyDescriptor[m_propertyMembers.size()];
			
			int i = 0;

			for (IPropertyMember propertyMember : m_propertyMembers) {
				
				if (propertyMember.getProperty() instanceof ObjectProperty) {
					
					ObjectProperty objectProperty = (ObjectProperty)propertyMember.getProperty();
					m_descriptors[i++] = new PropertyDescriptor(propertyMember, objectProperty.getLabel());
				}
				else if (propertyMember.getProperty() instanceof DataProperty) {
					
					DataProperty dataProperty = (DataProperty)propertyMember.getProperty();
					m_descriptors[i++] = new PropertyDescriptor(propertyMember, dataProperty.getLabel());
				}
			}
		}
		else if(node.getValue() instanceof INamedConcept){
			
			m_indicator = CONCEPT;
			
			INamedConcept concept = (INamedConcept)node.getValue();
			this.m_subconcept = concept.getSubconcepts();
			this.m_superconcept = concept.getSuperconcepts();
			
			m_descriptors = new IPropertyDescriptor[m_subconcept.size() + m_superconcept.size()];
			int i = 0;
			
			for (IConcept subcon : m_subconcept) {
				m_descriptors[i++] = new PropertyDescriptor(subcon, "subconcept");
			}
			for (IConcept supercon : m_superconcept) {
				m_descriptors[i++] = new PropertyDescriptor(supercon, "superconcept");
			}
		}
	}

	
	public Object getEditableValue() {
		return this;
	}

	public IPropertyDescriptor[] getPropertyDescriptors() {
		return m_descriptors;
	}

	public Object getPropertyValue(Object id) {
		
		if (m_indicator == INDIVIDUAL) {
			
			for(IPropertyMember propertyMember : m_propertyMembers) {
				if (id.equals(propertyMember)) {
					return propertyMember.getTarget().getLabel();
				}
			}
		}
		else if (m_indicator == CONCEPT) {
			
			for (IConcept subcon : m_subconcept) {
				if (id.equals(subcon)) {
					return subcon.getLabel();
				}
			}
			for (IConcept supercon : m_superconcept) {
				if (id.equals(supercon)) {
					return supercon.getLabel();
				}
			}
		}
		
		return null;
	}

	public boolean isPropertySet(Object id) {
		return true;
	}

	public void resetPropertyValue(Object id) {}

	public void setPropertyValue(Object id, Object value) {}

}
