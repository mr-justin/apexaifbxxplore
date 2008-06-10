package org.xmedia.oms.model.impl;


import java.util.HashSet;
import java.util.Set;

import org.xmedia.oms.model.api.IDataProperty;
import org.xmedia.oms.model.api.IDatatype;
import org.xmedia.oms.model.api.IOntology;
import org.xmedia.oms.model.api.IResource;
import org.xmedia.oms.persistence.PersistenceUtil;
import org.xmedia.oms.persistence.dao.IDatatypeDao;



public class DataProperty extends Property implements IDataProperty{

	private static final long serialVersionUID = 1L;

	private Set<IDatatype> m_ranges;

	public DataProperty(String uri) {
		super(uri);

	}

	public DataProperty(String uri, IOntology ontology) {
		super(uri, ontology);

	}

	public Set<? extends IResource> getDomainsAndRanges() {

		Set<IResource> clazzs = new HashSet<IResource>();
		//add all domain and range descriptions
		clazzs.addAll(getDomains());
		clazzs.addAll(getRanges());
		return clazzs;
	}

	public Set<IDatatype> getRanges() {
		if (m_ranges == null){

			IDatatypeDao dao = (IDatatypeDao) PersistenceUtil.getDaoManager().getAvailableDao(IDatatypeDao.class);
			m_ranges = dao.findDatatypeRanges(this);

		}
		return m_ranges;
	}


}