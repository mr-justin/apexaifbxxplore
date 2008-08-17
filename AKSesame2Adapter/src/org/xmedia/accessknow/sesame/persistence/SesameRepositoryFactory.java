package org.xmedia.accessknow.sesame.persistence;

import info.aduna.io.FileUtil;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.openrdf.repository.DelegatingRepository;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.DelegatingRepositoryImplConfig;
import org.openrdf.repository.config.RepositoryConfig;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.config.RepositoryConfigUtil;
import org.openrdf.repository.config.RepositoryFactory;
import org.openrdf.repository.config.RepositoryImplConfig;
import org.openrdf.repository.config.RepositoryRegistry;
import org.openrdf.repository.manager.LocalRepositoryManager;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.Sail;
import org.openrdf.sail.nativerdf.config.NativeStoreConfig;
import org.openrdf.sail.nativerdf.config.NativeStoreFactory;

public class SesameRepositoryFactory {

	private String sesameRootPath = "";
	private static final String SESAME_REPO_DIR = "OpenRDF Sesame";
	public static final String REPO_PATH_DEFAULT = "/sesame_repository/";
	
	public final static String RDFS_MEMORY_PERSISTENT = "urn:sesame2/repository_types/local/SailRepository/RdfsMemoryStore/persistent";
	public final static String RDF_MEMORY_VOLATILE = "urn:sesame2/repository_types/local/SailRepository/RdfMemoryStore/volatile";
	public final static String RDF_NATIVE = "urn:sesame2/repository_types/local/SailRepository/RdfNativeStore";
	public final static String RDFS_NATIVE = "urn:sesame2/repository_types/local/SailRepository/RdfsNativeStore";
	
	
	private enum RepositoryType {

		RDFS_MEMORY_PERSISTENT (SesameRepositoryFactory.RDFS_MEMORY_PERSISTENT),
		RDF_MEMORY_VOLATILE (SesameRepositoryFactory.RDF_MEMORY_VOLATILE),
		RDF_NATIVE (SesameRepositoryFactory.RDF_NATIVE),
		RDFS_NATIVE (SesameRepositoryFactory.RDFS_NATIVE);
		
		
		private final String urn;
	    private static Map<String,RepositoryType> tokenMap;
	    
	    private RepositoryType(String urn){
	        this.urn = urn;
	        map(urn,this);
	    }
	    
	    private void map(String urn, RepositoryType op){
	        if (tokenMap==null) tokenMap = new HashMap<String, RepositoryType>();
	        tokenMap.put(urn,op);
	    }
	    
	    public static RepositoryType forUrn(String urn){
	        return tokenMap.get(urn);
	    }
		
		public String getUrn() {
			return urn;
		}
	}
	
	private LocalRepositoryManager theManager;
	
	protected SesameRepositoryFactory(String sesameRootPath) throws RepositoryException {
		
		this.sesameRootPath = (sesameRootPath.endsWith(File.separator) ? sesameRootPath + SESAME_REPO_DIR : 
			sesameRootPath + File.separator + SESAME_REPO_DIR);
		
		reloadRepositoryManager();
	}
	
	/**
	 * Transduce a Uri to a Winfs compatible syntax.
	 * 
	 * @param aUri
	 * @return
	 */
	protected static String fsTransduceUri(URI aUri) {
		
		String uriAsString = aUri.toString();
		
		uriAsString = StringUtils.replace(uriAsString, ":", "COLON");
		uriAsString = StringUtils.replace(uriAsString, "/", "SLASH");
		uriAsString = StringUtils.replace(uriAsString, "#", "SHARP");
		
		return uriAsString;
	}
	
	private void reloadRepositoryManager() throws RepositoryException {
		theManager = new LocalRepositoryManager(new File(sesameRootPath));
		theManager.initialize();
	}
	
	protected synchronized Repository getSystemRepository() {
		return theManager.getSystemRepository();
	}
	
	protected File getRepositoryDir(URI ontologyUri) {
		
		return theManager.getRepositoryDir(fsTransduceUri(ontologyUri));
		
	}
	
	/**
	 * Assumes that the ontology has been already closed.
	 * 
	 * @throws URISyntaxException 
	 * @throws RepositoryConfigException 
	 * @throws RepositoryException 
	 * 
	 */
	protected synchronized void delete(String ontologyUri) throws RepositoryException, RepositoryConfigException, URISyntaxException {
		
		theManager.removeRepositoryConfig(fsTransduceUri(new URI(ontologyUri)));
		reloadRepositoryManager();
		
	}
	
	protected synchronized void clearAll() throws IOException, RepositoryException {
		
		File repositoriesDir = new File(sesameRootPath + "\\" + LocalRepositoryManager.REPOSITORIES_DIR + "\\");
		
		if (repositoriesDir.exists()) {
			FileUtil.deleteDir(repositoriesDir);
			reloadRepositoryManager();
		}
		
	}

	public synchronized Repository loadRemoteRepository(
			URI ontologyUri,
			SesameRemoteRepositoryHandle remoteHandle) throws RepositoryException {
		Repository repository = remoteHandle.loadHttpRepository();
		
		// Check if the repository exists remotely by calling
		// a method whose execution requires for sure such existence.
		RepositoryConnection connection = repository.getConnection();
		try {
			connection.isEmpty();
		} finally {
			if (connection != null) connection.close();
		}
		
		return repository;
	}
	
	public synchronized Repository loadRepository(URI theOntologyUri) throws RepositoryException, RepositoryConfigException   {
		
		Repository theRepository = null;
		String repositoryId = fsTransduceUri(theOntologyUri);

		for(String id : theManager.getRepositoryIDs()){
			System.out.println("id:"+id);
		}
		
		RepositoryConfig config = RepositoryConfigUtil.getRepositoryConfig(
				theManager.getSystemRepository(), 
				repositoryId);
		
		if (config != null)
			theRepository = loadRepository(config, repositoryId);
		
		return theRepository;
	}
	
	private Repository loadRepository(RepositoryConfig config, String repositoryId) throws RepositoryConfigException, RepositoryException {
		
		Repository theRepository = createNonInitializedRepository(config.getRepositoryImplConfig());
		theRepository.setDataDir(theManager.getRepositoryDir(repositoryId));
		theRepository.initialize();
		
		return theRepository;
		
	}
	
	/**
	 * Copied from Sesame2 (beta5) RepositoryManager.createRepositoryStack().
	 * 
	 * @param config
	 * @return
	 * @throws RepositoryConfigException
	 */
	private Repository createNonInitializedRepository(RepositoryImplConfig config) throws RepositoryConfigException {
		
		Repository repository = null;
		
		RepositoryFactory factory = RepositoryRegistry.getInstance().get(config.getType());
		repository = factory.getRepository(config);

		if (config instanceof DelegatingRepositoryImplConfig) {
			RepositoryImplConfig delegateConfig = ((DelegatingRepositoryImplConfig)config).getDelegate();

			Repository delegate = createNonInitializedRepository(delegateConfig);

			try {
				((DelegatingRepository)repository).setDelegate(delegate);
			}
			catch (ClassCastException e) {
				throw new RepositoryConfigException(
						"Delegate specified for repository that is not a DelegatingRepository: " + 
						delegate.getClass());
			}
		}
		
		return repository;
		
	}
	
	private RepositoryConfig putRepositoryConfig(
			Repository system,
			URI theOntologyUri, 
			RepositoryType repoType) throws Exception  {
	
		RepositoryConfig config = null;
		
		String repositoryId = fsTransduceUri(theOntologyUri);
		switch (repoType) {
		case RDFS_MEMORY_PERSISTENT:
			config = RdfsMemoryRepositoryFactory.createRepository(
					system, 
					repositoryId);			
			break;
		case RDF_NATIVE:
			config = RdfNativeRepositoryFactory.createRepository(
					system, 
					repositoryId);			
			break;
		case RDFS_NATIVE:
			config = RdfsNativeRepositoryFactory.createRepository(
					system, 
					repositoryId);			
			break;
		default:
			throw new Exception("Unsopported Repository type: '" + repoType.urn + "'.");
		}
		
		return config;
		
	}
	
	/**
	 * 
	 * @param ontologyUri
	 * @param type
	 * @param remoteType must be not null only if itsType is SesameRepositoryFactory.REMOTE
	 * @return
	 * @throws Exception
	 */
	public synchronized Repository createRepository(
			URI ontologyUri, 
			URI type,
			SesameRemoteRepositoryHandle remoteHandle) throws Exception  {
		
		Repository repository = null;
		
		RepositoryType repositoryType = RepositoryType.forUrn(type.toString());
		if (repositoryType == null)
			throw new Exception("Unsopported repository type '" + type + "'.");
		else {
			if (remoteHandle != null) {
				putRepositoryConfig(
						remoteHandle.getRemoteSystem(), 
						remoteHandle.getOntologyUri(), 
						repositoryType);
				repository = remoteHandle.loadHttpRepository();
			} else {
				if (repositoryType.equals(RepositoryType.RDF_MEMORY_VOLATILE))
					repository = RdfMemoryRepositoryFactory.createRepository();
				else {
					repository = loadRepository(
							putRepositoryConfig(getSystemRepository(), ontologyUri, repositoryType), 
							fsTransduceUri(ontologyUri));
				}
			}

		}
		
		return repository;
	}
	
	/**
	 * Method for creating a native RDF repository with specific indices.
	 * 
	 * @param ontologyUri
	 * @param type
	 * @param indices
	 * @return
	 * @throws Exception
	 */
	public synchronized Repository createRepositoryWithIndex(
			URI ontologyUri, 
			URI type,
			String indices) throws Exception  {
		RepositoryType repositoryType = RepositoryType.forUrn(type.toString());
		
		if(repositoryType != RepositoryType.RDFS_NATIVE &&
				repositoryType != RepositoryType.RDF_NATIVE)
			throw new Exception("Unsopported repository type '" + type + "'. Only native store repositories may use indices.");
		
		NativeStoreConfig config = new NativeStoreConfig(indices);
		Sail sail = new NativeStoreFactory().getSail(config);
		
		SailRepository theRepository = new SailRepository(sail);
		theRepository.setDataDir(theManager.getRepositoryDir(fsTransduceUri(ontologyUri)));
		theRepository.initialize();
		
		return theRepository;
	}
	
	public synchronized boolean repositoryExist(URI theOntologyUri) {
		
		boolean exist = false;
		
		try {
			exist = (loadRepository(theOntologyUri) != null); 
		} catch (Exception e) {}
		
		return exist;
	}
	
}
