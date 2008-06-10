package org.xmedia.oms.model.impl;


import java.util.Set;

import org.aifb.xxplore.shared.exception.EmergencyException;
import org.xmedia.oms.model.api.INamedConcept;
import org.xmedia.oms.model.api.IOntology;
import org.xmedia.oms.model.api.IProperty;
import org.xmedia.oms.model.api.IPropertyMember;
import org.xmedia.oms.persistence.PersistenceUtil;
import org.xmedia.oms.persistence.dao.IConceptDao;
import org.xmedia.oms.persistence.dao.IPropertyMemberAxiomDao;



public abstract class Property extends AbstractEntity implements IProperty{

	private static final long serialVersionUID = 1L;

	private Set<IPropertyMember> m_members; 

	private Set<INamedConcept> m_domains;
	
	private int numberOfPropertyMember = -1;

	public static Property IS_INSTANCE_OF = new ObjectProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
	public static Property SUBCLASS_OF = new ObjectProperty("http://www.w3.org/2000/01/rdf-schema#subClassOf");
	
	public Property(String uri) {
		super(uri);
	}

	public Property(String uri, IOntology ontology) {
		super(uri, ontology);

	}

	public int getNumberOfPropertyMember() {
		if (numberOfPropertyMember == -1){

			IPropertyMemberAxiomDao dao = (IPropertyMemberAxiomDao ) PersistenceUtil.getDaoManager().getAvailableDao(IPropertyMemberAxiomDao.class);
			numberOfPropertyMember = dao.getNumberOfPropertyMember(this);

		}

		return numberOfPropertyMember;	
	}
	
	public Set<IPropertyMember> getMemberIndividuals() {
		if (m_members == null){

			IPropertyMemberAxiomDao dao = (IPropertyMemberAxiomDao ) PersistenceUtil.getDaoManager().getAvailableDao(IPropertyMemberAxiomDao.class);
			m_members = dao.findByProperty(this);

		}

		return m_members;	
	}

	public Set<INamedConcept> getDomains() {
		
		if (m_domains == null){

			IConceptDao dao = (IConceptDao) PersistenceUtil.getDaoManager().getAvailableDao(IConceptDao.class);
			m_domains = dao.findDomains(this);

		}
		return m_domains;

	}

	@Override
	public boolean equals(Object res) {
		if (res instanceof Property){
			if(((Property)res).getUri().equals(getUri())) return true;
		}
		return super.equals(res);
	}
	
	@Override
	public int hashCode() {
		if (m_uri != null) {
			return m_uri.hashCode();
		} else {
			throw new EmergencyException("Property has no URI!");
		}
	}
}