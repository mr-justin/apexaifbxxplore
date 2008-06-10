package org.xmedia.oms.persistence;

import java.util.HashSet;
import java.util.Set;

import org.aifb.xxplore.shared.exception.Emergency;

public abstract class AbstractDataSource implements IDataSource {

	private long m_id;
	
	
	private Set<IDataSourceListener> m_listeners;

	
	public AbstractDataSource(long id) {
		m_id= id;
		m_listeners = new HashSet<IDataSourceListener>();
	}
	
	
	public long getID() {
		return m_id;
	}
	
	
	public void  addMessageListener(IMessageListener msgl) {
		Emergency.checkPrecondition(msgl instanceof IDataSourceListener, "msgl instanceof IDataSourceListener");
		m_listeners.add((IDataSourceListener)msgl);
	}

	public void removeMessageListener(IMessageListener msgl) {
		Emergency.checkPrecondition(msgl instanceof IDataSourceListener, "msgl instanceof IDataSourceListener");
		m_listeners.remove((IDataSourceListener)msgl);
	}

	
}
