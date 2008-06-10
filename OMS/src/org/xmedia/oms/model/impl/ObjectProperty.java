package org.xmedia.oms.model.impl;

import java.util.HashSet;
import java.util.Set;

import org.xmedia.oms.model.api.INamedConcept;
import org.xmedia.oms.model.api.IObjectProperty;
import org.xmedia.oms.model.api.IOntology;
import org.xmedia.oms.persistence.PersistenceUtil;
import org.xmedia.oms.persistence.dao.IConceptDao;

public class ObjectProperty extends Property implements IObjectProperty{

	/**
	 * 
	 */
	private static final long serialVersionUID = -4403488791020901447L;
	private Set<INamedConcept> m_ranges;

	
	public ObjectProperty(String uri) {
		super(uri);

	}

	public ObjectProperty(String uri, IOntology ontology) {
		super(uri, ontology);

	}
	
	public Set<INamedConcept> getDomainsAndRanges() {

		Set<INamedConcept> clazzs = new HashSet<INamedConcept>();
		//add all domain and range descriptions
		clazzs.addAll(getDomains());
		clazzs.addAll(getRanges());
		return clazzs;
	}
	
	public Set<INamedConcept> getRanges() {
		if (m_ranges == null){

			IConceptDao dao = (IConceptDao) PersistenceUtil.getDaoManager().getAvailableDao(IConceptDao.class);
			m_ranges = dao.findConceptRanges(this);

		}
		return m_ranges;
	}
}
