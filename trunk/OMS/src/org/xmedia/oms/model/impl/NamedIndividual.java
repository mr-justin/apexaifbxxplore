package org.xmedia.oms.model.impl;


import java.util.HashSet;
import java.util.Set;

import org.aifb.xxplore.shared.exception.EmergencyException;
import org.aifb.xxplore.shared.util.UniqueIdGenerator;
import org.xmedia.oms.model.api.IConcept;
import org.xmedia.oms.model.api.INamedIndividual;
import org.xmedia.oms.model.api.IOntology;
import org.xmedia.oms.model.api.IProperty;
import org.xmedia.oms.persistence.PersistenceUtil;
import org.xmedia.oms.persistence.SessionFactory;
import org.xmedia.oms.persistence.dao.IConceptDao;
import org.xmedia.oms.persistence.dao.IPropertyDao;


public class NamedIndividual extends Individual implements INamedIndividual {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/** The URI of the entity. */
    private String m_uri;
    
	private Set<IProperty> m_propertiesFrom;

	private Set<IProperty> m_propertiesTo;

	public NamedIndividual(String uri) {
		m_uri = uri;
		setOid(UniqueIdGenerator.getInstance().getNewId(getUri()));
	}

	public NamedIndividual(String uri, IOntology onto){
		super(onto);
		m_uri = uri;
		setOid(UniqueIdGenerator.getInstance().getNewId(getUri()));
	}
		
    public String getUri() {
        return m_uri;
    }

	public String getLabel()
	{
		return m_uri.substring(SessionFactory.getInstance().getCurrentSession().getConnection().getNamespaces().guessNamespaceEnd(m_uri)+1);
	}

	@Override
	public boolean equals(Object res) {
		if (res instanceof NamedIndividual){
			if(((NamedIndividual)res).getUri().equals(getUri())) return true;
		}
		
		return super.equals(res);
	}
	
	@Override
	public int hashCode() {
		if (m_uri != null) {
			return m_uri.hashCode();
		} else {
			throw new EmergencyException("NamedIndvidual has no URI!");
		}
	}
	
	public Set<IProperty> getProperties() {
		HashSet<IProperty> props = new HashSet<IProperty>();
		Set<IProperty> from = getPropertiesFrom();
		if (from != null) props.addAll(from);
		Set<IProperty>to = getPropertiesTo();
		if(to != null) props.addAll(to);

		return props;
	}

	public Set<IProperty> getPropertiesFrom() {
		if (m_propertiesFrom == null){

			IPropertyDao dao = (IPropertyDao) PersistenceUtil.getDaoManager().getAvailableDao(IPropertyDao.class);
			m_propertiesFrom = dao.findPropertiesFrom(this);

		}

		return m_propertiesFrom;
	}

	public Set<IProperty> getPropertiesTo() {
		if (m_propertiesTo == null){

			IPropertyDao dao = (IPropertyDao) PersistenceUtil.getDaoManager().getAvailableDao(IPropertyDao.class);
			m_propertiesTo = dao.findPropertiesTo(this);

		}

		return m_propertiesTo;
	}
	
}