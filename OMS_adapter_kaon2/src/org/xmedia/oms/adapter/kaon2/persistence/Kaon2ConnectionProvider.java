//$Id: DatasourceConnectionProvider.java 10075 2006-07-01 12:50:34 +0000 (Sa, 01 Jul 2006) epbernard $
package org.xmedia.oms.adapter.kaon2.persistence;

import java.sql.CallableStatement;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Savepoint;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.aifb.xxplore.shared.exception.Emergency;
import org.aifb.xxplore.shared.util.PropertyUtils;
import org.aifb.xxplore.shared.util.UniqueIdGenerator;
import org.semanticweb.kaon2.api.DefaultOntologyResolver;
import org.semanticweb.kaon2.api.KAON2Exception;
import org.semanticweb.kaon2.api.KAON2Manager;
import org.semanticweb.kaon2.api.Ontology;
import org.semanticweb.kaon2.api.OntologyManager;
import org.xmedia.oms.model.api.IOntology;
import org.xmedia.oms.model.impl.Namespaces;
import org.xmedia.oms.persistence.AbstractConnection;
import org.xmedia.oms.persistence.DatasourceException;
import org.xmedia.oms.persistence.IConnectionProvider;
import org.xmedia.oms.persistence.IKbConnection;
import org.xmedia.oms.persistence.InvalidParameterException;
import org.xmedia.oms.persistence.KbEnvironment;
import org.xmedia.oms.persistence.MissingParameterException;
import org.xmedia.oms.persistence.OntologyCreationException;
import org.xmedia.oms.persistence.OntologyDeletionException;


/**
 * A connection provider implementation for Kaon2.
 */
public class Kaon2ConnectionProvider implements IConnectionProvider {

	/** TODO implement connection pooling **/ 
	//private final ArrayList pool = new ArrayList();

	//private int poolSize;

	/** TODO implement commit strategy **/ 
	//private boolean autocommit;

	private Kaon2Connection m_conn;

	private Map m_properties;

	private Namespaces m_namespaces;

	/**
	 * Congigure the connection with Kaon2. Unlike a JDBC conncetion, threre is not 
	 * much connection properties required for the configuration with KAON2. URI and other 
	 * information are needed only when a specific ontology is to be loaded via the conncetion.
	 */
	public void configure(Properties props) throws DatasourceException{
		m_properties = PropertyUtils.convertToMap(props);		
		m_namespaces = new Namespaces();
		m_namespaces.registerStandardPrefixes();

		//nothing to configure with KAON2
		//otherwise configuration need to be done with the given props 
	}

	public IKbConnection getConnection() throws DatasourceException {

		if (m_conn == null) m_conn = new Kaon2Connection();

		return m_conn; 

	}

	public void close() throws DatasourceException {
		if (m_conn != null){
			try {
				m_conn.close();
			} 

			catch (SQLException e) {
				throw new DatasourceException(e);
			}

			finally { m_conn = null; }
		}
	}


	public void closeConnection(IKbConnection conn) throws DatasourceException {
		if (m_conn == conn)
			close();

	}

	public class Kaon2Connection extends AbstractConnection{

		/** the kaon2 connection delegate */
		private OntologyManager m_delegate;
		private DefaultOntologyResolver m_resolver;

		public Kaon2Connection(){

			try {
				m_delegate = KAON2Manager.newOntologyManager();
				m_resolver = new DefaultOntologyResolver();
				m_delegate.setOntologyResolver(m_resolver);
			}
			catch(Exception e){
				throw new DatasourceException();
			}

			Emergency.checkPostcondition(m_resolver!=null && m_delegate != null, "m_resolver!=null && m_delegate != null");

		}

		public OntologyManager getConnection() {
			return m_delegate;
		}

		public IOntology loadOntology(Map<String, Object> params){

			IOntology ontology=null;	  
			try {			
				String logicalUri = (String) params.get(KbEnvironment.LOGICAL_ONTOLOGY_URI);
				if(logicalUri == null){
					String uri = (String) params.get(KbEnvironment.PHYSICAL_ONTOLOGY_URI);
					logicalUri = m_resolver.registerOntology(uri);
				}

				Collection<String> importedURIs = (Collection<String>)params.get(KbEnvironment.IMPORTED_ONTOLOGY_URI);
				if(importedURIs != null){
					for(String importedURI : importedURIs){
						m_resolver.registerOntology(importedURI);
					}
				}
				long id = UniqueIdGenerator.getInstance().getNewId(logicalUri);
				Ontology delegate = m_delegate.openOntology(logicalUri, params);
				ontology = new Kaon2Ontology(id, delegate);
				// add the ontology to the managed list 
				addOntology(ontology);

				//register prefix
				String ns = Namespaces.guessNamespace(logicalUri);
				//TODO check if the real prefix can be retrieved from ontology properties instead of using 
				//the generated id as prefix
				getNamespaces().registerPrefix(String.valueOf(id), ns);

			} catch (KAON2Exception e) {

				throw new DatasourceException(e);

			} catch (InterruptedException e) {

				new DatasourceException(e);
			}

			return ontology;

		}

		public boolean closeOntology(IOntology onto){
			Emergency.checkPrecondition(onto instanceof Kaon2Ontology, "onto instanceof KAON2Ontology");

			try {

				Set<Ontology> ontos = new HashSet<Ontology>();
				Ontology delegate = (Ontology)((Kaon2Ontology)onto).getDelegate();
				ontos.add(delegate);
				m_delegate.closeOntologies(ontos);

				return super.closeOntology(onto);
			}

			catch (Exception e) {

				return false;

			}

		}

		public IOntology loadOrCreateOntology(Map<String, Object> parameters) throws MissingParameterException, InvalidParameterException, OntologyCreationException {
			// TODO Auto-generated method stub
			return null;
		}

		public IOntology createOntology(Map<String, Object> parameters) throws MissingParameterException, InvalidParameterException, OntologyCreationException {

			IOntology ontology=null;	  
			try {			
				String uri = (String) parameters.get(KbEnvironment.LOGICAL_ONTOLOGY_URI);
				String physicalUri = (String)parameters.get(KbEnvironment.PHYSICAL_ONTOLOGY_URI);
				if(m_delegate != null){

					long id = UniqueIdGenerator.getInstance().getNewId(uri);
					m_resolver.registerReplacement(uri, physicalUri);
					Ontology delegate = m_delegate.createOntology(uri, parameters);
					ontology = new Kaon2Ontology(id, delegate);
					// add the ontology to the managed list 
					addOntology(ontology);

					//register prefix
					String ns = Namespaces.guessNamespace(uri);
					//TODO check if the real prefix can be retrieved from ontology properties instead of using 
					//the generated id as prefix
					getNamespaces().registerPrefix(String.valueOf(id), ns);
				}
				else throw new DatasourceException("Connection not avaiable, please check configuration!");

			} catch (KAON2Exception e) {

				throw new DatasourceException(e);

			}

			return ontology;

		}

		public void deleteOntology(String ontologyUri) throws OntologyDeletionException {
			// TODO Auto-generated method stub

		}

		//** TODO following methods must be implemented for conventional db-style access.**/
		public void clearWarnings() throws SQLException {
			// TODO Auto-generated method stub

		}

		public void close() throws SQLException {
			try {
				m_delegate.close();
				super.close();
			} 

			catch (KAON2Exception e) {
				throw new SQLException();
			}

		}

		public void commit() throws SQLException {
			// TODO Auto-generated method stub

		}

		public Statement createStatement() throws SQLException {
			// TODO Auto-generated method stub
			return null;
		}

		public Statement createStatement(int arg0, int arg1) throws SQLException {
			// TODO Auto-generated method stub
			return null;
		}

		public Statement createStatement(int arg0, int arg1, int arg2) throws SQLException {
			// TODO Auto-generated method stub
			return null;
		}

		public boolean getAutoCommit() throws SQLException {
			// TODO Auto-generated method stub
			return false;
		}

		public String getCatalog() throws SQLException {
			// TODO Auto-generated method stub
			return null;
		}

		public int getHoldability() throws SQLException {
			// TODO Auto-generated method stub
			return 0;
		}

		public DatabaseMetaData getMetaData() throws SQLException {
			// TODO Auto-generated method stub
			return null;
		}

		public int getTransactionIsolation() throws SQLException {
			// TODO Auto-generated method stub
			return 0;
		}

		public Map<String, Class<?>> getTypeMap() throws SQLException {
			// TODO Auto-generated method stub
			return null;
		}

		public SQLWarning getWarnings() throws SQLException {
			// TODO Auto-generated method stub
			return null;
		}

		public boolean isClosed() throws SQLException {
			// TODO Auto-generated method stub
			return false;
		}

		public boolean isReadOnly() throws SQLException {
			// TODO Auto-generated method stub
			return false;
		}

		public String nativeSQL(String arg0) throws SQLException {
			// TODO Auto-generated method stub
			return null;
		}

		public CallableStatement prepareCall(String arg0) throws SQLException {
			// TODO Auto-generated method stub
			return null;
		}

		public CallableStatement prepareCall(String arg0, int arg1, int arg2) throws SQLException {
			// TODO Auto-generated method stub
			return null;
		}

		public CallableStatement prepareCall(String arg0, int arg1, int arg2, int arg3) throws SQLException {
			// TODO Auto-generated method stub
			return null;
		}

		public PreparedStatement prepareStatement(String arg0) throws SQLException {
			// TODO Auto-generated method stub
			return null;
		}

		public PreparedStatement prepareStatement(String arg0, int arg1) throws SQLException {
			// TODO Auto-generated method stub
			return null;
		}

		public PreparedStatement prepareStatement(String arg0, int[] arg1) throws SQLException {
			// TODO Auto-generated method stub
			return null;
		}

		public PreparedStatement prepareStatement(String arg0, String[] arg1) throws SQLException {
			// TODO Auto-generated method stub
			return null;
		}

		public PreparedStatement prepareStatement(String arg0, int arg1, int arg2) throws SQLException {
			// TODO Auto-generated method stub
			return null;
		}

		public PreparedStatement prepareStatement(String arg0, int arg1, int arg2, int arg3) throws SQLException {
			// TODO Auto-generated method stub
			return null;
		}

		public void releaseSavepoint(Savepoint arg0) throws SQLException {
			// TODO Auto-generated method stub

		}

		public void rollback() throws SQLException {
			// TODO Auto-generated method stub

		}

		public void rollback(Savepoint arg0) throws SQLException {
			// TODO Auto-generated method stub

		}

		public void setAutoCommit(boolean arg0) throws SQLException {
			// TODO Auto-generated method stub

		}

		public void setCatalog(String arg0) throws SQLException {
			// TODO Auto-generated method stub

		}

		public void setHoldability(int arg0) throws SQLException {
			// TODO Auto-generated method stub

		}

		public void setReadOnly(boolean arg0) throws SQLException {
			// TODO Auto-generated method stub

		}

		public Savepoint setSavepoint() throws SQLException {
			// TODO Auto-generated method stub
			return null;
		}

		public Savepoint setSavepoint(String arg0) throws SQLException {
			// TODO Auto-generated method stub
			return null;
		}

		public void setTransactionIsolation(int arg0) throws SQLException {
			// TODO Auto-generated method stub

		}

		public void setTypeMap(Map<String, Class<?>> arg0) throws SQLException {
			// TODO Auto-generated method stub

		}

		public Map getConfiguration() {			
			return m_properties;
		}

		public Namespaces getNamespaces(){
			return m_namespaces;
		}
	}
}







