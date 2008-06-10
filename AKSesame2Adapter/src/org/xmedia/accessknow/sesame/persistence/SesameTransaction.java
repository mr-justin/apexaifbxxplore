package org.xmedia.accessknow.sesame.persistence;

import java.util.Map;

import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.xmedia.oms.persistence.DatasourceException;
import org.xmedia.oms.persistence.ITransaction;

public class SesameTransaction implements ITransaction {

	private RepositoryConnection connection;
	private SesameSession session;
	
	protected SesameTransaction(RepositoryConnection connection, SesameSession session) {
		this.connection = connection;
		this.session = session;
	}
	
	/**
	 * @deprecated
	 */
	@SuppressWarnings("unchecked")
	public void configure(Map props) {}

	protected RepositoryConnection getConnection() {
		return connection;
	}
	
	public void begin() throws DatasourceException {
		try {
			if (connection.isAutoCommit())
				connection.setAutoCommit(false);
		} catch (RepositoryException e) {
			throw new DatasourceException("Cannot begin transaction.", e);
		}
	}

	public void commit() throws DatasourceException {
		try {
			connection.commit();
		} catch (RepositoryException e) {
			throw new DatasourceException("Cannot commit.", e);
		} finally {
			try {
				connection.setAutoCommit(true);
				session.closeTransaction();
			} catch (RepositoryException e) {
				e.printStackTrace();
			}
		}
	}

	public boolean isActive() throws DatasourceException {
		return true;
	}

	public void rollback() throws DatasourceException {
		try {
			connection.rollback();
		} catch (RepositoryException e) {
			throw new DatasourceException("Cannot rollback.", e);
		} finally {
			try {
				connection.setAutoCommit(true);
				session.closeTransaction();
			} catch (RepositoryException e) {
				e.printStackTrace();
			}
		}
	}

}
