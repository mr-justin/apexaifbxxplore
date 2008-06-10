package org.xmedia.oms.persistence;

import java.io.Serializable;
import java.util.Map;

import org.xmedia.oms.model.api.IOntology;

/**
 * Creates <tt>Session</tt>s. Usually an application has a single <tt>SessionFactory</tt>.
 * Threads servicing client requests obtain <tt>Session</tt>s from the factory.<br>
 * <br>
 * Implementors must be threadsafe.<br>
 * <br>
 * 
 * @see ISession
 * @see IConnectionProvider
 * @see TransactionFactory
 */
public interface ISessionFactory extends Serializable {

	public void configure(Map<String, Object> settings);

	/**
	 * Open a <tt>Session</tt> on the given connection.
	 * @param connection a connection provided by the application.
	 * @param the active ontology for the connection
	 * @return Session
	 * @throws Exception 
	 * @throws Exception 
	 */
	public ISession openSession(IKbConnection connection, IOntology onto) throws OpenSessionException;

	/**
	 * Obtains the current session.  The definition of what exactly "current"
	 * means controlled by the {@CurrentSessionContext} impl configured
	 * for use.
	 *
	 * @return The current session.
	 * @throws DatasourceException Indicates an issue locating a suitable current session.
	 */
	public ISession getCurrentSession() throws DatasourceException;

	/**
	 * Destroy this <tt>SessionFactory</tt> and release all resources (caches,
	 * connection pools, etc). It is the responsibility of the application
	 * to ensure that there are no open <tt>Session</tt>s before calling
	 * <tt>close()</tt>.
	 */
	public void close() throws DatasourceException;

	/**
	 * Was this <tt>SessionFactory</tt> already closed?
	 */
	public boolean isClosed();

	/**
	 * Evict all entries from the second-level cache. This method occurs outside
	 * of any transaction; it performs an immediate "hard" remove, so does not respect
	 * any transaction isolation semantics of the usage strategy. Use with care.
	 */
	//public void evict(Class persistentClass) throws DatasourceException;
	/**
	 * Evict an entry from the second-level  cache. This method occurs outside
	 * of any transaction; it performs an immediate "hard" remove, so does not respect
	 * any transaction isolation semantics of the usage strategy. Use with care.
	 */
	//public void evict(Class persistentClass, Serializable id) throws DatasourceException;
	/**
	 * Evict all entries from the second-level cache. This method occurs outside
	 * of any transaction; it performs an immediate "hard" remove, so does not respect
	 * any transaction isolation semantics of the usage strategy. Use with care.
	 */
	//public void evictEntity(String entityName) throws DatasourceException;
	/**
	 * Evict an entry from the second-level  cache. This method occurs outside
	 * of any transaction; it performs an immediate "hard" remove, so does not respect
	 * any transaction isolation semantics of the usage strategy. Use with care.
	 */
	//public void evictEntity(String entityName, Serializable id) throws DatasourceException;
	/**
	 * Evict all entries from the second-level cache. This method occurs outside
	 * of any transaction; it performs an immediate "hard" remove, so does not respect
	 * any transaction isolation semantics of the usage strategy. Use with care.
	 */
	//public void evictCollection(String roleName) throws DatasourceException;
	/**
	 * Evict an entry from the second-level cache. This method occurs outside
	 * of any transaction; it performs an immediate "hard" remove, so does not respect
	 * any transaction isolation semantics of the usage strategy. Use with care.
	 */
	//public void evictCollection(String roleName, Serializable id) throws DatasourceException;

	/**
	 * Evict any query result sets cached in the default query cache region.
	 */
	//public void evictQueries() throws DatasourceException;
	/**
	 * Evict any query result sets cached in the named query cache region.
	 */
	//public void evictQueries(String cacheRegion) throws DatasourceException;

}
