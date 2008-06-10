package org.xmedia.oms.model.impl;


import java.util.HashSet;
import java.util.Set;

import org.aifb.xxplore.shared.exception.EmergencyException;
import org.aifb.xxplore.shared.util.Pair;
import org.aifb.xxplore.shared.util.UniqueIdGenerator;
import org.xmedia.oms.model.api.IConcept;
import org.xmedia.oms.model.api.IIndividual;
import org.xmedia.oms.model.api.INamedConcept;
import org.xmedia.oms.model.api.IOntology;
import org.xmedia.oms.model.api.IProperty;
import org.xmedia.oms.persistence.PersistenceUtil;
import org.xmedia.oms.persistence.SessionFactory;
import org.xmedia.oms.persistence.dao.IConceptDao;
import org.xmedia.oms.persistence.dao.IIndividualDao;
import org.xmedia.oms.persistence.dao.IPropertyDao;

public class NamedConcept extends Concept implements INamedConcept{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	/** The URI of the entity. */
	private String m_uri;
	
	private int numberOfIndividual = -1;

	private Set<IIndividual> m_memberIndidviduals; 

	private Set<IConcept> m_subconcepts;

	private Set<IConcept> m_superconcepts;

	private Set<IConcept> m_equiconcepts;

	private Set<IConcept> m_disjconcepts;

	private Set<IProperty> m_propertiesFrom;

	private Set<IProperty> m_propertiesTo;

	private Set<Pair> m_propRanges;

	public static NamedConcept TOP = new NamedConcept("http://www.w3.org/2002/07/owl#Thing");

	public static NamedConcept BOTTOM = new NamedConcept("http://www.w3.org/2002/07/owl#Nothing");

	public NamedConcept(String uri) {
		m_uri = uri;
		setOid(UniqueIdGenerator.getInstance().getNewId(getUri()));
	}

	public NamedConcept(String uri, IOntology onto){
		super(onto);
		m_uri = uri;
		setOid(UniqueIdGenerator.getInstance().getNewId(getUri()));
	}

	public String getUri() {
		return m_uri;
	}

	public String getLabel(){

		return m_uri.substring(SessionFactory.getInstance().getCurrentSession().getConnection().getNamespaces().guessNamespaceEnd(m_uri)+1);
		//return SessionFactory.getInstance().getCurrentSession().getConnection().getNamespaces().abbreviateAsNamespace(m_uri);
	}

	@Override
	public boolean equals(Object res) {
		if (res instanceof NamedConcept){
			if(((NamedConcept)res).getUri().equals(getUri())) return true;
		}

		return super.equals(res);
	}

	@Override
	public int hashCode() {
		if (m_uri != null) {
			return m_uri.hashCode();
		} else {
			throw new EmergencyException("NamedConcept has no URI!");
		}
	}

	public Set<IIndividual> getMemberIndividuals(){			

		if (m_memberIndidviduals == null){

			IIndividualDao dao = (IIndividualDao) PersistenceUtil.getDaoManager().getAvailableDao(IIndividualDao.class);
			m_memberIndidviduals = dao.findMemberIndividuals(this);

		}

		return m_memberIndidviduals;
	}
	
	public int getNumberOfIndividuals(){			

		if (numberOfIndividual ==  -1){

			IIndividualDao dao = (IIndividualDao) PersistenceUtil.getDaoManager().getAvailableDao(IIndividualDao.class);
			numberOfIndividual = dao.getNumberOfIndividual(this);

		}

		return numberOfIndividual;
	}

	public Set<IIndividual> getMemberIndividuals(boolean includeInferred){			

		if (m_memberIndidviduals == null){

			IIndividualDao dao = (IIndividualDao) PersistenceUtil.getDaoManager().getAvailableDao(IIndividualDao.class);
			m_memberIndidviduals = dao.findMemberIndividuals(this, includeInferred);

		}

		return m_memberIndidviduals;
	}

	public Set<IConcept> getDisjointConcepts() {
		if (m_disjconcepts == null){

			IConceptDao dao = (IConceptDao) PersistenceUtil.getDaoManager().getAvailableDao(IConceptDao.class);
			m_disjconcepts = dao.findDisjointConcepts(this);

		}

		return m_disjconcepts;
	}

	public Set<IConcept> getEquivalentConcepts() {
		if (m_equiconcepts == null){

			IConceptDao dao = (IConceptDao) PersistenceUtil.getDaoManager().getAvailableDao(IConceptDao.class);
			m_equiconcepts = dao.findEquivalentConcepts(this);

		}

		return m_equiconcepts;
	}

	public Set<IConcept> getSubconcepts() {
		if (m_subconcepts == null){

			IConceptDao dao = (IConceptDao) PersistenceUtil.getDaoManager().getAvailableDao(IConceptDao.class);
			m_subconcepts = dao.findSubconcepts(this);

		}

		return m_subconcepts;

	}

	public Set<IConcept> getSuperconcepts() {
		if (m_superconcepts == null){

			IConceptDao dao = (IConceptDao) PersistenceUtil.getDaoManager().getAvailableDao(IConceptDao.class);
			m_superconcepts = dao.findSuperconcepts(this);

		}

		return m_superconcepts;
	}

	public Set<IProperty> getPropertiesFrom() {
		if (m_propertiesFrom == null){

			IPropertyDao dao = (IPropertyDao) PersistenceUtil.getDaoManager().getAvailableDao(IPropertyDao.class);
			m_propertiesFrom  = dao.findPropertiesFrom(this);

		}

		return m_propertiesFrom;
	}

	public Set<Pair> getPropertiesAndRangesFrom() {
		if (m_propRanges == null){
			IPropertyDao dao = (IPropertyDao) PersistenceUtil.getDaoManager().getAvailableDao(IPropertyDao.class);
			m_propRanges = dao.findPropertiesAndRangesFrom(this);
		}
		return m_propRanges;
	}

	public Set<IProperty> getPropertiesTo() {
		if (m_propertiesTo == null){

			IPropertyDao dao = (IPropertyDao) PersistenceUtil.getDaoManager().getAvailableDao(IPropertyDao.class);
			m_propertiesTo = dao.findPropertiesTo(this);

		}

		return m_propertiesTo;
	}

	public Set<IProperty> getProperties() {
		Set<IProperty> props = new HashSet<IProperty>();
		if (m_propertiesFrom == null) getPropertiesFrom();
		if (m_propertiesFrom != null) props.addAll(m_propertiesFrom);
		
		if (m_propertiesTo == null) getPropertiesTo(); 
		if(m_propertiesTo != null) props.addAll(m_propertiesTo);
		return props;
	}

}
