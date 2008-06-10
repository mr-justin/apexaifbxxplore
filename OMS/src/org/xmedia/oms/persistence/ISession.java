//$Id: StatelessSession.java 9705 2006-03-28 19:59:31 +0000 (Di, 28 Mrz 2006) steve.ebersole@jboss.com $
package org.xmedia.oms.persistence;

import java.io.Serializable;

import org.xmedia.oms.model.api.IOntology;
import org.xmedia.oms.persistence.dao.IDaoManager;

/*
 * A <tt>Session</tt> instance is serializable if its persistent classes are serializable.<br>
 * <br>
 * A typical transaction should use the following idiom:
 * <pre>
 * Session sess = factory.openSession();
 * Transaction tx;
 * try {
 *     tx = sess.beginTransaction();
 *     //do some work
 *     ...
 *     tx.commit();
 * }
 * catch (Exception e) {
 *     if (tx!=null) tx.rollback();
 *     throw e;
 * }
 * finally {
 *     sess.close();
 * }
 * </pre>
 * <br>
 * If the <tt>Session</tt> throws an exception, the transaction must be rolled back
 * and the session discarded. The internal state of the <tt>Session</tt> might not
 * be consistent with the database / knowledgebase after the exception occurs.
 */

public interface ISession extends Serializable {
	/**
	 * Close the stateless session and release the connection.
	 */
	public void close() throws DatasourceException;

	/**
	 * Insert a row.
	 *
	 * @param entity a new transient instance
	 */
//	public Serializable insert(Object entity);

	/**
	 * Insert a row.
	 *
	 * @param entityName The entityName for the entity to be inserted
	 * @param entity a new transient instance
	 * @return the identifier of the instance
	 */
//	public Serializable insert(String entityName, Object entity);

	/**
	 * Update a row.
	 *
	 * @param entity a detached entity instance
	 */
//	public void update(Object entity);

	/**
	 * Update a row.
	 *
	 * @param entityName The entityName for the entity to be updated
	 * @param entity a detached entity instance
	 */
//	public void update(String entityName, Object entity);

	/**
	 * Delete a row.
	 *
	 * @param entity a detached entity instance
	 */
//	public void delete(Object entity);

	/**
	 * Delete a row.
	 *
	 * @param entityName The entityName for the entity to be deleted
	 * @param entity a detached entity instance
	 */
//	public void delete(String entityName, Object entity);

	/**
	 * Retrieve a row.
	 *
	 * @return a detached entity instance
	 */
//	public Object get(String entityName, Serializable id);

	/**
	 * Retrieve a row.
	 *
	 * @return a detached entity instance
	 */
//	public Object get(Class entityClass, Serializable id);

	/**
	 * Retrieve a row, obtaining the specified lock mode.
	 *
	 * @return a detached entity instance
	 */
//	public Object get(String entityName, Serializable id, LockMode lockMode);

	/**
	 * Retrieve a row, obtaining the specified lock mode.
	 *
	 * @return a detached entity instance
	 */
//	public Object get(Class entityClass, Serializable id, LockMode lockMode);

	/**
	 * Refresh the entity instance state from the database.
	 *
	 * @param entity The entity to be refreshed.
	 */
//	public void refresh(Object entity);

	/**
	 * Refresh the entity instance state from the database.
	 *
	 * @param entityName The entityName for the entity to be refreshed.
	 * @param entity The entity to be refreshed.
	 */
//	public void refresh(String entityName, Object entity);

	/**
	 * Refresh the entity instance state from the database.
	 *
	 * @param entity The entity to be refreshed.
	 * @param lockMode The LockMode to be applied.
	 */
//	public void refresh(Object entity, LockMode lockMode);

	/**
	 * Refresh the entity instance state from the database.
	 *
	 * @param entityName The entityName for the entity to be refreshed.
	 * @param entity The entity to be refreshed.
	 * @param lockMode The LockMode to be applied.
	 */
//	public void refresh(String entityName, Object entity, LockMode lockMode);

	/**
	 * Create a new instance of <tt>Query</tt> for the given HQL query string.
	 * Entities returned by the query are detached.
	 */
//	public Query createQuery(String queryString);

	/**
	 * Obtain an instance of <tt>Query</tt> for a named query string defined in
	 * the mapping file. Entities returned by the query are detached.
	 */
//	public Query getNamedQuery(String queryName);

	/**
	 * Check if the session is still open.
	 *
	 * @return boolean
	 */
	public boolean isOpen();


	/**
	 * Check if the session is currently connected.
	 *
	 * @return boolean
	 */
	public boolean isConnected();

	/**
	 * Begin a Hibernate transaction.
	 * @throws Exception 
	 */
	public ITransaction beginTransaction() throws Exception;

	/**
	 * Get the current Hibernate transaction.
	 */
	public ITransaction getTransaction();
	
	
	public IOntology getOntology();
	
	/**
	 * Returns the current KB connection associated with this
	 * instance.<br>
	 */
	public IKbConnection getConnection();

	/**
	 * Disconnect the <tt>Session</tt> from the current connection. 
	 */
	public void disconnect() throws DatasourceException;

	public boolean isReasoningOn();
	
	public void setReasoningOn(boolean on);
	

	/**
	 * Reconnect to the given connection. This is used by applications
	 * which require long transactions and use application-supplied connections.
	 *
	 * @param connection a connection
	 * @see #disconnect()
	 */
	public void reconnect(IKbConnection connection) throws DatasourceException;
	
	public IDaoManager getDaoManager();
	

}
