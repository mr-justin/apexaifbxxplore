package org.xmedia.accessknow.sesame.persistence;

import java.sql.SQLException;
import java.util.Properties;

import org.openrdf.repository.RepositoryException;
import org.xmedia.oms.persistence.DatasourceException;
import org.xmedia.oms.persistence.IConnectionProvider;
import org.xmedia.oms.persistence.IKbConnection;

public class ConnectionProvider implements IConnectionProvider {
	
	private SesameConnection m_con;

	public void close() throws DatasourceException {
		
		if (m_con != null)
		{
			try 
			{
				m_con.close();
			} 
			catch(SQLException e)
			{
				throw new DatasourceException(e);
			}
			finally {m_con = null;}
		}
		
	}

	public void closeConnection(IKbConnection con) throws DatasourceException {
		if (m_con == con) close();
	}

	public void configure(Properties props) throws DatasourceException {
		// TODO Auto-generated method stub

	}

	public IKbConnection getConnection() throws DatasourceException {
	
		if(m_con == null){
			
			try {
				m_con = new SesameConnection();
			} 
			catch (RepositoryException e) {
				e.printStackTrace();
				throw new DatasourceException();
			}
		}
		
		return m_con;
	}

}
