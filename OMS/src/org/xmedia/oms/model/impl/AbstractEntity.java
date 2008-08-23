package org.xmedia.oms.model.impl;

import org.aifb.xxplore.shared.util.UniqueIdGenerator;
import org.xmedia.oms.model.api.IEntity;
import org.xmedia.oms.model.api.INamedConcept;
import org.xmedia.oms.model.api.IOntology;
import org.xmedia.oms.model.api.IProperty;
import org.xmedia.oms.persistence.DatasourceException;
import org.xmedia.oms.persistence.PersistenceUtil;
import org.xmedia.oms.persistence.SessionFactory;
import org.xmedia.oms.persistence.dao.DaoUnavailableException;

public abstract class AbstractEntity extends Resource implements IEntity {

	/** The URI of the entity. */
	protected String m_uri;

	public AbstractEntity(String uri) {
		super(UniqueIdGenerator.getInstance().getNewId(uri));
		m_uri = uri;
	}

	public AbstractEntity(String uri, IOntology onto) {
		super(UniqueIdGenerator.getInstance().getNewId(uri), onto);
		m_uri = uri;
	}

	public String getUri() {
		return m_uri;
	}

	public String getLabel() {
		
		String label = null;
		try {
			label = PersistenceUtil.getDaoManager().getPropertyDao().findLabel(
					(IProperty) this);
		} catch (DatasourceException e) {
			e.printStackTrace();
		} catch (DaoUnavailableException e) {
			e.printStackTrace();
		}
		
		if (label == null || label.length() == 0) 	
			//if no label is available, generate one from the uri
			return m_uri.substring(SessionFactory.getInstance().getCurrentSession()
					.getConnection().getNamespaces().guessNamespaceEnd(m_uri) + 1);
		else return label; 

	}
}
