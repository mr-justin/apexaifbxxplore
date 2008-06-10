//$Id: ConnectionProvider.java 9191 2006-02-01 14:40:34 +0000 (Mi, 01 Feb 2006) epbernard $
package org.xmedia.oms.persistence;
import java.sql.SQLException;
import java.util.Properties;


/**
 * A strategy for obtaining connections.
 * <br><br>
 * Implementors might also implement connection pooling.<br>
 * <br>
 *
 * @see ConnectionProviderFactory
 */
public interface IConnectionProvider {
	/**
	 * Initialize the connection provider from given properties.
	 * @param props <tt>SessionFactory</tt> properties
	 */
	public void configure(Properties props) throws DatasourceException;
	/**
	 * Grab a connection, with the autocommit mode specified by
	 * <tt>hibernate.connection.autocommit</tt>.
	 * @return a JDBC connection
	 * @throws SQLException
	 */
	public IKbConnection getConnection() throws DatasourceException;
	/**
	 * Dispose of a used connection.
	 * @param conn a connection
	 * @throws SQLException
	 */
	public void closeConnection(IKbConnection conn) throws DatasourceException;

	/**
	 * Release all resources held by this provider. JavaDoc requires a second sentence.
	 * @throws DatasourceException
	 */
	public void close() throws DatasourceException;


}







