package org.xmedia.oms.persistence;

import java.util.Map;


/**
 * Allows the application to define units of work, while
 * maintaining abstraction from the underlying transaction
 * implementation (eg. JTA, JDBC).<br>
 * <br>
 * A transaction is associated with a <tt>Session</tt> and is
 * usually instantiated by a call to <tt>Session.beginTransaction()</tt>.
 * A single session might span multiple transactions since
 * the notion of a session (a conversation between the application
 * and the datastore) is of coarser granularity than the notion of
 * a transaction. However, it is intended that there be at most one
 * uncommitted <tt>Transaction</tt> associated with a particular
 * <tt>Session</tt> at any time.<br>
 * <br>
 * Implementors are not intended to be threadsafe.
 *
 * @see ISession#beginTransaction()
 * @see org.hibernate.transaction.TransactionFactory
 * @author Anton van Straaten
 */
public interface ITransaction {
	
	/**
	 * Begin a new transaction.
	 */
	public void begin() throws DatasourceException;

	/**
	 * Flush the associated <tt>Session</tt> and end the unit of work (unless
	 * we are in {@link FlushMode#NEVER}.
	 * </p>
	 * This method will commit the underlying transaction if and only
	 * if the underlying transaction was initiated by this object.
	 *
	 * @throws DatasourceException
	 */
	public void commit() throws DatasourceException;

	/**
	 * Force the underlying transaction to roll back.
	 *
	 * @throws DatasourceException
	 */
	public void rollback() throws DatasourceException;

	
	/**
	 * Is this transaction still active?
	 * <p/>
	 * Again, this only returns information in relation to the
	 * local transaction, not the actual underlying transaction.
	 *
	 * @return boolean Treu if this local transaction is still active.
	 */
	public boolean isActive() throws DatasourceException;
	
	public void configure(Map props);

}
