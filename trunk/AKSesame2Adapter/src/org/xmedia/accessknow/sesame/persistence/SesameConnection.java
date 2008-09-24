package org.xmedia.accessknow.sesame.persistence;

import info.aduna.io.FileUtil;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

import org.openrdf.OpenRDFException;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.config.RepositoryConfigUtil;
import org.xmedia.accessknow.sesame.model.SesameOntology;
import org.xmedia.oms.model.api.IOntology;
import org.xmedia.oms.model.impl.Namespaces;
import org.xmedia.oms.persistence.AbstractConnection;
import org.xmedia.oms.persistence.InvalidParameterException;
import org.xmedia.oms.persistence.KbEnvironment;
import org.xmedia.oms.persistence.MissingParameterException;
import org.xmedia.oms.persistence.OntologyCreationException;
import org.xmedia.oms.persistence.OntologyDeletionException;
import org.xmedia.oms.persistence.OntologyLoadException;

public class SesameConnection extends AbstractConnection {

	public static final String REIFICATION_ENABLED = "reification_enabled";

	private SesameRepositoryFactory theSesameFactory = null;


	public SesameConnection() throws RepositoryException {		
		theSesameFactory = new SesameRepositoryFactory(SesameRepositoryFactory.REPO_PATH_DEFAULT);
	}

	public SesameConnection(String repositoryRootPath) throws RepositoryException {
		theSesameFactory = new SesameRepositoryFactory(repositoryRootPath);
	}

	public synchronized void setRepositoryRoot(String repositoryRootPath) throws RepositoryException {
		if (theSesameFactory == null) {
			theSesameFactory = new SesameRepositoryFactory(repositoryRootPath);
		}	
	}

	public SesameRepositoryFactory getRepositoryFactory() {
		return theSesameFactory;
	}

	@SuppressWarnings("unchecked")
	public Map getConfiguration() {
//		TODO 
		return new HashMap();
	}

	/**
	 * @deprecated
	 */
	@Deprecated
	public Namespaces getNamespaces() {
		return null;
	}

	/**
	 * @depracated
	 */
	public void clearWarnings() throws SQLException {}

	/**
	 * @depracated
	 */
	public void commit() throws SQLException {}

	/**
	 * @depracated
	 */
	public Array createArrayOf(String arg0, Object[] arg1) throws SQLException {
		return null;
	}

	/**
	 * @depracated
	 */
	public Blob createBlob() throws SQLException {
		return null;
	}

	/**
	 * @depracated
	 */
	public Clob createClob() throws SQLException {
		return null;
	}

	/**
	 * @depracated
	 */
	public Statement createStatement() throws SQLException {
		return null;
	}

	/**
	 * @depracated
	 */
	public Statement createStatement(int arg0, int arg1) throws SQLException {
		return null;
	}

	/**
	 * @depracated
	 */
	public Statement createStatement(int arg0, int arg1, int arg2)
	throws SQLException {
		return null;
	}

	/**
	 * @depracated
	 */
	public Struct createStruct(String arg0, Object[] arg1) throws SQLException {
		return null;
	}

	/**
	 * @depracated
	 */
	public boolean getAutoCommit() throws SQLException {
		return false;
	}

	/**
	 * @depracated
	 */
	public String getCatalog() throws SQLException {
		return null;
	}

	/**
	 * @depracated
	 */
	public Properties getClientInfo() throws SQLException {
		return null;
	}

	/**
	 * @depracated
	 */
	public String getClientInfo(String arg0) throws SQLException {
		return null;
	}

	/**
	 * @depracated
	 */
	public int getHoldability() throws SQLException {
		return 0;
	}

	/**
	 * @depracated
	 */
	public DatabaseMetaData getMetaData() throws SQLException {
		return null;
	}

	/**
	 * @depracated
	 */
	public int getTransactionIsolation() throws SQLException {
		return 0;
	}

	/**
	 * @depracated
	 */
	public Map<String, Class<?>> getTypeMap() throws SQLException {
		return null;
	}

	/**
	 * @depracated
	 */
	public SQLWarning getWarnings() throws SQLException {
		return null;
	}

	public boolean isClosed() throws SQLException {
		return false;
	}

	/**
	 * @depracated
	 */	
	public boolean isReadOnly() throws SQLException {
		return false;
	}

	/**
	 * @depracated
	 */
	public boolean isValid(int arg0) throws SQLException {
		return false;
	}

	/**
	 * @depracated
	 */
	public String nativeSQL(String arg0) throws SQLException {
		return null;
	}

	/**
	 * @depracated
	 */
	public CallableStatement prepareCall(String arg0) throws SQLException {
		return null;
	}

	/**
	 * @depracated
	 */
	public CallableStatement prepareCall(String arg0, int arg1, int arg2)
	throws SQLException {
		return null;
	}

	/**
	 * @depracated
	 */
	public CallableStatement prepareCall(String arg0, int arg1, int arg2,
			int arg3) throws SQLException {
		return null;
	}

	/**
	 * @depracated
	 */
	public PreparedStatement prepareStatement(String arg0) throws SQLException {
		return null;
	}

	/**
	 * @depracated
	 */
	public PreparedStatement prepareStatement(String arg0, int arg1)
	throws SQLException {
		return null;
	}

	/**
	 * @depracated
	 */
	public PreparedStatement prepareStatement(String arg0, int[] arg1)
	throws SQLException {
		return null;
	}

	/**
	 * @depracated
	 */
	public PreparedStatement prepareStatement(String arg0, String[] arg1)
	throws SQLException {
		return null;
	}

	/**
	 * @depracated
	 */
	public PreparedStatement prepareStatement(String arg0, int arg1, int arg2)
	throws SQLException {
		return null;
	}

	/**
	 * @depracated
	 */
	public PreparedStatement prepareStatement(String arg0, int arg1, int arg2,
			int arg3) throws SQLException {
		return null;
	}

	/**
	 * @depracated
	 */
	public void releaseSavepoint(Savepoint arg0) throws SQLException {}

	/**
	 * @depracated
	 */
	public void rollback() throws SQLException {}

	/**
	 * @depracated
	 */
	public void rollback(Savepoint arg0) throws SQLException {}

	/**
	 * @depracated
	 */
	public void setAutoCommit(boolean arg0) throws SQLException {}

	/**
	 * @depracated
	 */
	public void setCatalog(String arg0) throws SQLException {}

	/**
	 * @depracated
	 */
	public void setHoldability(int arg0) throws SQLException {}

	/**
	 * @depracated
	 */
	public void setReadOnly(boolean arg0) throws SQLException {}

	/**
	 * @depracated
	 */
	public Savepoint setSavepoint() throws SQLException {
		return null;
	}

	/**
	 * @depracated
	 */
	public Savepoint setSavepoint(String arg0) throws SQLException {
		return null;
	}

	/**
	 * @depracated
	 */
	public void setTransactionIsolation(int arg0) throws SQLException {}

	/**
	 * @depracated
	 */
	public void setTypeMap(Map<String, Class<?>> arg0) throws SQLException {}

	/**
	 * @depracated
	 */
	public boolean isWrapperFor(Class<?> arg0) throws SQLException {
		return false;
	}

	/**
	 * @depracated
	 */
	public <T> T unwrap(Class<T> arg0) throws SQLException {
		return null;
	}

	private URI getParameterAsUri(Map<String, Object> parameters, String parameter) throws MissingParameterException, InvalidParameterException {

		if(!parameters.containsKey(parameter)) {
			throw new MissingParameterException(parameter);
		}

		URI paramValue;

		Object paramValueRaw = parameters.get(parameter);

		if (paramValueRaw instanceof URI){
			paramValue = (URI) paramValueRaw;
		}
		else if(paramValueRaw instanceof String){
			try {
				paramValue = new URI((String)paramValueRaw);
			} catch (URISyntaxException e) {
				e.printStackTrace();
				throw new InvalidParameterException(parameter, "URI syntax not correct.");
			}
		}
		else{
			throw new InvalidParameterException(parameter, "not an URI.");
		}

		return paramValue;

	}

	protected URI getOntologyURI(Map<String, Object> parameters) throws MissingParameterException, InvalidParameterException {
		return getParameterAsUri(parameters, KbEnvironment.ONTOLOGY_URI);
	}


	public void deleteAllOntologies() throws OntologyDeletionException {

		try {
			getRepositoryFactory().clearAll();
		} catch (Exception e) {
			throw new OntologyDeletionException(e);
		}

	}

	public IOntology loadOrCreateOntology(Map<String, Object> parameters) throws MissingParameterException, InvalidParameterException, OntologyCreationException {

		IOntology theOntology = null;

		try {
			theOntology = loadOntology(parameters);
		} catch (OntologyLoadException e) {
			theOntology = createOntology(parameters);
		}

		return theOntology;
	}



	/**
	 * Acquire a reference to an existing ontology.
	 * Parameter KbEnvironment.ONTOLOGY_URI must be set.
	 * The ontology must already exist.
	 * 
	 * @throws OntologyLoadException 
	 * @throws MissingParameterException 
	 * @throws InvalidParameterException 
	 * 
	 */
	public IOntology loadOntology(Map<String, Object> parameters) throws MissingParameterException, InvalidParameterException, OntologyLoadException {

		URI ontologyUri = getOntologyURI(parameters);
		Repository itsRepository = null;
		IOntology onto = null;
		try {
			if (getRemoteHandle(ontologyUri) != null) {
				itsRepository = getRepositoryFactory().loadRemoteRepository(ontologyUri, getRemoteHandle(ontologyUri));
			} else {
				itsRepository = getRepositoryFactory().loadRepository(ontologyUri);
			}

			if (itsRepository == null) {
				throw new OntologyLoadException(ontologyUri, new Exception("Ontology '" + ontologyUri + "' does not exist."));
			}

			onto =  new SesameOntology(
					itsRepository,
					ontologyUri,
					this,
					getReificationSwitch(parameters));
			addOntology(onto);

		} catch (RepositoryException e) {
			throw new OntologyLoadException(ontologyUri, e);
		} catch (RepositoryConfigException e) {
			throw new OntologyLoadException(ontologyUri, e);
		}

		return onto;
	}

	protected boolean isRemoteOntology(Map<String, Object> parameters) {

		boolean isRemote = false;
		try {
			isRemote = (getRemoteHandle(getOntologyURI(parameters)) != null);
		} catch (Exception e) {}

		return isRemote;
	}

	private SesameRemoteRepositoryHandle getRemoteHandle(URI ontologyUri) throws InvalidParameterException {

		SesameRemoteRepositoryHandle remoteHandle = null;
		try {
			remoteHandle = SesameRemoteRepositoryHandle.buildRemoteHandle(ontologyUri);
		} catch (MalformedURLException e) {
			throw new InvalidParameterException(KbEnvironment.ONTOLOGY_URI, "Invalid server url.", e);
		} catch (URISyntaxException e) {
			throw new InvalidParameterException(KbEnvironment.ONTOLOGY_URI, e);
		}

		return remoteHandle;
	}

	/**
	 * An ontology is a separate Sesame Repository.
	 * Parameter KbEnvironment.ONTOLOGY_TYPE and KbEnvironment.ONTOLOGY_URI must be set, 
	 * and they are supposed to be URNs defining the repository type and the repository name.
	 *  
	 * Currently supported type URNs are listed as constants in SesameRepositoryFactory.
	 * 
	 * @throws OntologyCreationException 
	 * @throws MissingParameterException
	 * @throws InvalidParameterException 
	 * 
	 */
	public IOntology createOntology(Map<String, Object> parameters) throws MissingParameterException, InvalidParameterException, OntologyCreationException {

		IOntology ontology = null;
		URI ontologyUri = getOntologyURI(parameters);

		if (getRepositoryFactory().repositoryExist(ontologyUri)) {
			throw new OntologyCreationException(ontologyUri, new Exception("Ontology '" + ontologyUri + "' already exists."));
		}

		URI ontologyType = getParameterAsUri(parameters, KbEnvironment.ONTOLOGY_TYPE);
		SesameRemoteRepositoryHandle remoteHandle = getRemoteHandle(ontologyUri);

		if(parameters.containsKey(KbEnvironment.ONTOLOGY_INDEX)){

			try {
				ontology = createOntologyWithIndex(
						ontologyUri,
						(String)parameters.get(KbEnvironment.ONTOLOGY_INDEX),
						ontologyType,
						this, 
						getReificationSwitch(parameters));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		else{
			Repository itsRepository = null;
			try {
				itsRepository = getRepositoryFactory().createRepository(
						ontologyUri, 
						ontologyType, 
						remoteHandle);
				
				if (itsRepository == null) {
					throw new OntologyCreationException(ontologyUri);
				}
				
				ontology = new SesameOntology(
						itsRepository,
						ontologyUri,
						this,
						getReificationSwitch(parameters));
			} catch (OntologyCreationException e) {
				throw e;
			} catch (Exception e) {
				throw new OntologyCreationException(ontologyUri, e);
			}
		}

		addOntology(ontology);	
		return ontology;
	}
	
	private IOntology createOntologyWithIndex(
			URI theOntologyUri,
			String indices,
			URI itsType,  
			SesameConnection connection, 
			boolean reificationEnabled) throws OntologyCreationException,Exception {
		
		Repository itsRepository = null;
		
		try {
			itsRepository = connection.getRepositoryFactory().createRepositoryWithIndex(
					theOntologyUri, 
					itsType, 
					indices);
			
			if (itsRepository == null) {
				throw new OntologyCreationException(theOntologyUri);
			}
			
			return new SesameOntology(
					itsRepository,
					theOntologyUri,
					connection,
					reificationEnabled);
		} catch (OntologyCreationException e) {
			throw e;
		} catch (Exception e) {
			throw new OntologyCreationException(theOntologyUri, e);
		}
	}

	private boolean getReificationSwitch(Map<String, Object> parameters) {

		boolean reificationEnabled = SesameOntology.REIFICATION_ENABLED_DEFAULT;

		Object reificationEnabledParam = parameters.get(REIFICATION_ENABLED);
		if (reificationEnabledParam != null) {
			reificationEnabled = Boolean.valueOf(((String)reificationEnabledParam));
		}

		return reificationEnabled;
	}

	@Override
	public boolean closeOntology(IOntology ontology) {

		if ((ontology != null) && (ontology instanceof SesameOntology)) {
			try {			
				((SesameOntology)ontology).shutdown();
			} catch (RepositoryException e) {
				e.printStackTrace();
			}
		}

		return super.closeOntology(ontology);
	}

	private void deleteLocalOntology(String ontologyUri) throws Exception {

		URI theOntologyUri = new URI(ontologyUri);

		Map<String, Object> parameters = new Hashtable<String, Object>();
		parameters.put(KbEnvironment.ONTOLOGY_URI, theOntologyUri);

		// Remove the configuration
		getRepositoryFactory().delete(ontologyUri);

		// Possibly remove the data directory
		File dataDir = getRepositoryFactory().getRepositoryDir(theOntologyUri);
		if (dataDir.exists()) {
			try {
				FileUtil.deleteDir(dataDir);
			} catch (IOException e) {
				throw new Exception("Cannot delete the data dir '" + 
						dataDir + "' for ontology '" + ontologyUri + "'.", e);
			}
		}

	}

	private void deleteRemoteOntology(SesameRemoteRepositoryHandle remoteHandle) throws OntologyDeletionException {

		try {
			RepositoryConfigUtil.removeRepositoryConfigs(
					remoteHandle.getRemoteSystem(), 
					SesameRepositoryFactory.fsTransduceUri(remoteHandle.getOntologyUri()));
		} catch (OpenRDFException e) {
			throw new OntologyDeletionException(e);
		}

	}

	public void deleteOntology(String ontologyUri) throws OntologyDeletionException {

		try {

			SesameRemoteRepositoryHandle remoteHandle = getRemoteHandle(new URI(ontologyUri));
			if (remoteHandle != null) {
				deleteRemoteOntology(remoteHandle);
			} else {
				deleteLocalOntology(ontologyUri.toString());
			}

		} catch (URISyntaxException e) {
			throw new OntologyDeletionException(e);
		} catch (RepositoryException e) {
			throw new OntologyDeletionException(e);
		} catch (RepositoryConfigException e) {
			throw new OntologyDeletionException(e);
		} catch (Exception e) {
			throw new OntologyDeletionException(e);
		}

	}
	
	public void close(){
		try {
			theSesameFactory.clearAll();
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		theSesameFactory = null;
	}

	@Override
	public NClob createNClob() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SQLXML createSQLXML() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setClientInfo(Properties arg0) throws SQLClientInfoException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setClientInfo(String arg0, String arg1)
			throws SQLClientInfoException {
		// TODO Auto-generated method stub
		
	}

}
