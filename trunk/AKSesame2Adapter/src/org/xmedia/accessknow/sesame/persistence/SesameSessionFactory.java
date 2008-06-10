package org.xmedia.accessknow.sesame.persistence;

import java.util.Map;

import org.openrdf.repository.RepositoryException;
import org.xmedia.accessknow.sesame.model.SesameOntology;
import org.xmedia.oms.model.api.IOntology;
import org.xmedia.oms.persistence.DatasourceException;
import org.xmedia.oms.persistence.IKbConnection;
import org.xmedia.oms.persistence.ISession;
import org.xmedia.oms.persistence.ISessionFactory;
import org.xmedia.oms.persistence.OpenSessionException;
import org.xmedia.uris.XMURIFactory;

public class SesameSessionFactory implements ISessionFactory {

	private static final long serialVersionUID = -3100658900521218710L;
	
	private XMURIFactory m_uriFactory;
	private ISession m_currentSession;
	
	
	public SesameSessionFactory(XMURIFactory uriFactory) {
		this.m_uriFactory = uriFactory;
	}
	

	public void close() throws DatasourceException {
		m_currentSession.close();
		m_currentSession = null;
	}

	/**
	 * @deprecated
	 */
	public void configure(Map<String, Object> settings) {}


	public ISession getCurrentSession() throws DatasourceException {
		return m_currentSession;
	}


	public boolean isClosed() {
		return m_currentSession == null ? true : false;
	}
	
	/**
	 * @param connection must be of type SesameConnection
	 * @param ontology must be of type SesameOntology
	 * @throws OpenSessionException 
	 */
	public ISession openSession(IKbConnection connection, IOntology ontology) throws OpenSessionException {
		
		ISession theSession = null;
		
		if (!(connection instanceof SesameConnection))
			throw new OpenSessionException(ontology.getUri(), 
					new Exception("Parameter 'connection' must be of type " + SesameConnection.class.getName()));
		else {
			if (!(ontology instanceof SesameOntology))
				throw new OpenSessionException(ontology.getUri(), 
						new Exception("Parameter 'ontology' must be of type " + SesameOntology.class.getName()));
			else
				try {
					theSession = new SesameSession((SesameOntology) ontology, (SesameConnection)connection, m_uriFactory);
				} catch (RepositoryException e) {
					throw new OpenSessionException(ontology.getUri(), e);
				}
		}
		
		return m_currentSession = theSession;
	}

	public void setUriFactory(XMURIFactory uriFactory) {
		this.m_uriFactory = uriFactory;
	}

}
