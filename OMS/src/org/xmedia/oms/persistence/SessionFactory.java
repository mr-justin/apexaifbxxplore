package org.xmedia.oms.persistence;

import java.util.Map;

import org.xmedia.oms.model.api.IOntology;


public class SessionFactory implements ISessionFactory{

	/**
	 * 
	 */
	private static final long serialVersionUID = -5238793530440840483L;

	// factory is a singleton
	private SessionFactory(){}

	private static class SingletonHolder {
		private static SessionFactory s_instance = new SessionFactory();
	} 

	public static SessionFactory getInstance() {
		return SingletonHolder.s_instance;
	}

	private Map m_properties;

	private IConnectionProvider m_provider;

	// the current session
	private ISession m_currentSession;

	private boolean m_isClosed = false;

	public ISession getCurrentSession() throws DatasourceException {
		if ( m_currentSession == null ) {
			throw new DatasourceException( "No current session configured!" );
		}
		return m_currentSession;
	}


	public String getDialect() {
		return (String)m_properties.get(KbEnvironment.KR_LANGUAGE);
	}

//	public TransactionFactory getTransactionFactory() {
//	return settings.getTransactionFactory();
//	}

//	public TransactionManager getTransactionManager() {
//	return transactionManager;
//	}


	/**
	 * Closes the session factory, releasing all held resources.
	 */
	public void close() throws DatasourceException {

		m_currentSession.close();
		m_provider.close();
		m_properties.clear();
		m_isClosed = true;


	}


	public boolean isClosed() {
		return m_isClosed;
	}


	public ISession openSession(IKbConnection connection, IOntology onto) throws OpenSessionException {
		if(m_currentSession != null) m_currentSession.close();
		m_currentSession = new StatelessSession(connection, onto);
		return m_currentSession;
	}

	public void setSession(ISession session) {
		m_currentSession = session; 
	}

	public void configure(Map<String, Object> settings) {
		m_properties = settings;

	}
	
	public Map getConfiguration() {			
		return m_properties;
	}

}
