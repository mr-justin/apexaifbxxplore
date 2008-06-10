package org.xmedia.oms.model.impl;

import org.aifb.xxplore.shared.util.UniqueIdGenerator;
import org.xmedia.oms.model.api.IEntity;
import org.xmedia.oms.model.api.IOntology;
import org.xmedia.oms.persistence.SessionFactory;

public abstract class AbstractEntity extends Resource implements IEntity{

    /** The URI of the entity. */
    protected String m_uri;
    
	public AbstractEntity(String uri){
		super(UniqueIdGenerator.getInstance().getNewId(uri));
		m_uri = uri;
	}
	
	public AbstractEntity(String uri, IOntology onto){
		super(UniqueIdGenerator.getInstance().getNewId(uri), onto);
		m_uri = uri;
	}
	
    public String getUri() {
        return m_uri;
    }

	public String getLabel(){
		return m_uri.substring(SessionFactory.getInstance().getCurrentSession().getConnection().getNamespaces().guessNamespaceEnd(m_uri)+1);
		//return SessionFactory.getInstance().getCurrentSession().getConnection().getNamespaces().abbreviateAsNamespace(m_uri);
	}
}
