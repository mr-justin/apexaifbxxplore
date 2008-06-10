package org.xmedia.oms.model.impl;

import java.util.Set;

import org.aifb.xxplore.shared.exception.EmergencyException;
import org.xmedia.oms.model.api.IDatatype;
import org.xmedia.oms.model.api.ILiteral;
import org.xmedia.oms.model.api.IOntology;
import org.xmedia.oms.model.api.IPropertyMember;
import org.xmedia.oms.persistence.PersistenceUtil;
import org.xmedia.oms.persistence.dao.IDatatypeDao;
import org.xmedia.oms.persistence.dao.IPropertyMemberAxiomDao;


public class Literal extends Resource implements ILiteral {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String m_language;

	private Set<IDatatype> m_datatypes;

	private Set<IPropertyMember> m_propertiesToValues;

	private Object m_value;

	public Literal(Object value){	
		m_value = value;
		setLabel(value.toString());
	}

	public Literal(Object value, IOntology onto){	
		super(onto);
		m_value = value;
		setLabel(value.toString());
	}

	public Object getValue() {
		return m_value;
	}

	public void setValue(Object value){
		m_value = value;
		setLabel(value.toString());
	}

	public Set<IDatatype> getDatatypes() {
		if (m_datatypes == null){

			IDatatypeDao dao = (IDatatypeDao) PersistenceUtil.getDaoManager().getAvailableDao(IDatatypeDao.class);
			m_datatypes = dao.findDatatypes(this);

		}

		return m_datatypes;
	}

	public Set<IPropertyMember> getPropertyToValues() {
		if (m_propertiesToValues == null){

			IPropertyMemberAxiomDao dao = (IPropertyMemberAxiomDao) PersistenceUtil.getDaoManager().getAvailableDao(IPropertyMemberAxiomDao.class);
			m_propertiesToValues = dao.findByTargetValue(this);

		}

		return m_propertiesToValues;
	}

	public String getLanguage() {

		return m_language;
	}

	public String getLiteral() {
		return m_value.toString();
	}

	public void setLanguage(String lang) {
		m_language = lang;
	}
}
