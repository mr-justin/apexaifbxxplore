package org.xmedia.oms.model.impl;

import java.util.HashSet;
import java.util.Set;

import org.xmedia.oms.model.api.IIndividual;
import org.xmedia.oms.model.api.IOntology;
import org.xmedia.oms.model.api.IPropertyMember;
import org.xmedia.oms.persistence.PersistenceUtil;
import org.xmedia.oms.persistence.dao.IPropertyMemberAxiomDao;

public class Individual extends Resource implements IIndividual {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8925079039600753294L;
	private Set<IPropertyMember> m_objectpropertiesFromValues;
	private Set<IPropertyMember> m_objectpropertiesToValues;
	private Set<IPropertyMember> m_propertiesFromValues;
	private Set<IPropertyMember> m_propertiesToValues;
	
	public Individual(){super();};

	public Individual(IOntology onto){
		super(onto);
	}

	public Individual(String label){
		super(label);
	}
	
	public Individual(String label, IOntology onto){
		super(label, onto);
	}

	public Set<IPropertyMember> getObjectPropertyFromValues() {
		if (m_objectpropertiesFromValues == null){
	
			IPropertyMemberAxiomDao dao = (IPropertyMemberAxiomDao) PersistenceUtil.getDaoManager().getAvailableDao(IPropertyMemberAxiomDao.class);
			m_objectpropertiesFromValues = dao.findBySourceIndividual(this);
	
		}
	
		return m_objectpropertiesFromValues;
	}

	public Set<IPropertyMember> getObjectPropertyToValues() {
		if (m_objectpropertiesToValues == null){
	
			IPropertyMemberAxiomDao dao = (IPropertyMemberAxiomDao) PersistenceUtil.getDaoManager().getAvailableDao(IPropertyMemberAxiomDao.class);
			m_objectpropertiesToValues = dao.findByTargetIndividual(this);
	
		}
	
		return m_objectpropertiesToValues;
	}

	public Set<IPropertyMember> getObjectPropertyValues() {
		HashSet<IPropertyMember> propvalues= new HashSet<IPropertyMember>();
		propvalues.addAll(getObjectPropertyFromValues());
		propvalues.addAll(getObjectPropertyToValues());
	
		return propvalues;
	}

	public Set<IPropertyMember> getPropertyFromValues() {
		if (m_propertiesFromValues == null){
	
			
			IPropertyMemberAxiomDao dao = (IPropertyMemberAxiomDao) PersistenceUtil.getDaoManager().getAvailableDao(IPropertyMemberAxiomDao.class);
			m_propertiesFromValues = dao.findBySourceIndividual(this);
	
		}
	
		return m_propertiesFromValues;
	}

	public Set<IPropertyMember> getPropertyToValues() {
		if (m_propertiesToValues == null){
	
			IPropertyMemberAxiomDao dao = (IPropertyMemberAxiomDao) PersistenceUtil.getDaoManager().getAvailableDao(IPropertyMemberAxiomDao.class);
			m_propertiesToValues = dao.findByTargetIndividual(this);
	
		}
	
		return m_propertiesToValues;
	}

	public Set<IPropertyMember> getPropertyValues() {
		HashSet<IPropertyMember> propvalues= new HashSet<IPropertyMember>();
		propvalues.addAll(getPropertyFromValues());
		propvalues.addAll(getPropertyToValues());
	
		return propvalues;
	}


}
