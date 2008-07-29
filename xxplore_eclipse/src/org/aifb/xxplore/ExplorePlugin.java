package org.aifb.xxplore;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.aifb.xxplore.shared.util.PropertyUtils;
import org.apache.log4j.Logger;
import org.ateam.xxplore.core.ExploreEnvironment;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ISaveParticipant;
import org.eclipse.core.resources.ISavedState;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.xmedia.accessknow.sesame.persistence.ConnectionProvider;
import org.xmedia.accessknow.sesame.persistence.ExtendedSesameDaoManager;
import org.xmedia.accessknow.sesame.persistence.SesameRepositoryFactory;
import org.xmedia.accessknow.sesame.persistence.SesameSession;
import org.xmedia.accessknow.sesame.persistence.SesameSessionFactory;
import org.xmedia.oms.adapter.kaon2.persistence.Kaon2ConnectionProvider;
import org.xmedia.oms.adapter.kaon2.persistence.Kaon2DaoManager;
import org.xmedia.oms.model.api.IOntology;
import org.xmedia.oms.model.api.OntologyImportException;
import org.xmedia.oms.persistence.DatasourceException;
import org.xmedia.oms.persistence.IConnectionProvider;
import org.xmedia.oms.persistence.IDataSource;
import org.xmedia.oms.persistence.ISession;
import org.xmedia.oms.persistence.ISessionFactory;
import org.xmedia.oms.persistence.InvalidParameterException;
import org.xmedia.oms.persistence.KbEnvironment;
import org.xmedia.oms.persistence.MissingParameterException;
import org.xmedia.oms.persistence.OntologyCreationException;
import org.xmedia.oms.persistence.OntologyLoadException;
import org.xmedia.oms.persistence.OpenSessionException;
import org.xmedia.oms.persistence.PersistenceUtil;
import org.xmedia.oms.persistence.SessionFactory;
import org.xmedia.uris.impl.XMURIFactoryInsulated;


/**
 * The main plugin class to be used in the desktop.
 */
public class ExplorePlugin extends AbstractUIPlugin {

	//The plug-in ID
	public static final String PLUGIN_ID = "org.aifb.xxplore.gui";

	// Error code for the error when loading definition
	public static final int LOADING_DEFINITION_ERROR 	= 0;
	public static final int NO_DATASOURCE_FILE 			= 1;
	public static final int LOADING_DATASOURCE_ERROR 	= 2;

	//The shared instance.
	private static ExplorePlugin s_plugin;
	private static Map<String,IDataSource> s_datasources;
	private static Logger s_log = Logger.getLogger(ExplorePlugin.class); 

	public static Display s_Display = Display.getCurrent();

	/**
	 * The constructor.
	 */
	public ExplorePlugin() {
		super();
		s_plugin = this;
	}

	/**
	 * This method is called upon plug-in activation
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		if(s_log.isDebugEnabled()) {
			s_log.debug("ExplorePlugin is starting!");
		}

		super.start(context);
		ISaveParticipant saveParticipant = new ExploreWorkspaceSaveParticipant();
		ISavedState lastState =
			ResourcesPlugin.getWorkspace().addSaveParticipant(this, saveParticipant);
		if (lastState == null) {
			return;
		}
		IPath location = lastState.lookup(new Path("save"));
		if (location == null) {
			return;
		}
		// the plugin instance should read any important state from the file.
		java.io.File f = getStateLocation().append(location).toFile();
		readStateFrom(f);

	}

	protected void readStateFrom(java.io.File target) {
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		// TODO the state of the pluging should be saved here
		//writeImportantState(target); 
		super.stop(context);
		s_plugin = null;
	}

	protected void writeImportantState(java.io.File target) {
	}

	/**
	 * Returns the shared instance.
	 */
	public static ExplorePlugin getDefault() {
		return s_plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path.
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID, path);
	}


	/**
	 * This method returns the datasource given the corresponding source ID
	 * @param string
	 * @return
	 * @throws CoreException
	 */	
	public static IDataSource getDatasource(String path) throws CoreException{

		if (s_datasources == null){
			s_datasources = new HashMap<String, IDataSource>();
		}

		IDataSource datasource = null;

		if ((datasource= s_datasources.get(path))!= null) {
			return datasource;
		} else {
			return loadDatasourceFromProperties(path);
		}

	}

	private static IDataSource loadDatasourceFromProperties(String path) throws CoreException{
		IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(path));

		if (!file.exists()){
			throw new CoreException(new Status(IStatus.ERROR,
					PLUGIN_ID,NO_DATASOURCE_FILE,"There is no source file for "+ path, null));
		}

		else {
			Properties parameters = new Properties();
			try {
				parameters.load(file.getContents());
			} 

			catch (IOException e) {
				IStatus status = new MultiStatus(ExplorePlugin.PLUGIN_ID,
						ExplorePlugin.LOADING_DATASOURCE_ERROR,"The definition could not be correctly loaded",e);
				throw new CoreException(status);
			}


			//create conncetion provider
			IConnectionProvider provider = null;
			String providerClazz = parameters.getProperty(KbEnvironment.CONNECTION_PROVIDER_CLASS);
			if(providerClazz == null) {
				providerClazz = KbEnvironment.DEFAULT_CONNECTION_PROVIDER_CLASS;
			}

			//TODO better do class loading
			try {
				provider = (IConnectionProvider)Class.forName(providerClazz).newInstance();
			} catch (Exception e) {
				IStatus status = new MultiStatus(ExplorePlugin.PLUGIN_ID,
						ExplorePlugin.LOADING_DATASOURCE_ERROR,"The definition could not be correctly loaded",e);
				throw new CoreException(status);
			}

			provider.configure(parameters);

			//load ontology	
			IOntology onto = null;
			try {
				if (provider instanceof ConnectionProvider) {
					onto = provider.getConnection().loadOrCreateOntology(PropertyUtils.convertToMap(parameters));
				} else if (provider instanceof Kaon2ConnectionProvider) {
					onto = provider.getConnection().loadOntology(PropertyUtils.convertToMap(parameters));
				}
			} catch (DatasourceException e) {
				e.printStackTrace();
			} catch (MissingParameterException e) {
				e.printStackTrace();
			} catch (InvalidParameterException e) {
				e.printStackTrace();
			} catch (OntologyCreationException e) {
				e.printStackTrace();
			} catch (OntologyLoadException e) {
				e.printStackTrace();
			}
			
			if (provider instanceof ConnectionProvider) {
				
				try {
					addFileToRepository(onto,PropertyUtils.convertToMap(parameters));
				} catch (MissingParameterException e1) {
					e1.printStackTrace();
				}
				
				SesameSessionFactory sesame_factory = new SesameSessionFactory(new XMURIFactoryInsulated());
				ISession session = null;
				
				try {
					session = sesame_factory.openSession(provider.getConnection(), onto);
				} catch (DatasourceException e) {
					e.printStackTrace();
				} catch (OpenSessionException e) {
					e.printStackTrace();
				}
				//set dao manager
				PersistenceUtil.setDaoManager(ExtendedSesameDaoManager.getInstance((SesameSession)session));
				
				session.close();			
			}
			else if (provider instanceof Kaon2ConnectionProvider) {
				ISessionFactory factory = SessionFactory.getInstance();
				factory.configure(PropertyUtils.convertToMap(parameters));

				PersistenceUtil.setDaoManager(Kaon2DaoManager.getInstance());
			}
			
			ISessionFactory factory = SessionFactory.getInstance();
			PersistenceUtil.setSessionFactory(factory); 
			//open a new session with the ontology
			try {
				factory.openSession(provider.getConnection(),onto);
			} catch (DatasourceException e) {
				e.printStackTrace();
			} catch (OpenSessionException e) {
				e.printStackTrace();
			}
			
//			set resource and index locations ...
			if(parameters.containsKey(ExploreEnvironment.RESOURCE_LOCATION)){
				String location = parameters.getProperty(ExploreEnvironment.RESOURCE_LOCATION);
				ExploreEnvironment.LocationHelper.setResourceLocation(location);
			}
			else{
//				default value
				String location = ResourcesPlugin.getWorkspace().getRoot().getLocation().removeLastSegments(1).toString()
					+ ExploreEnvironment.DEFAULT_RESOURCE_LOCATION_SUFFIX;
				ExploreEnvironment.LocationHelper.setResourceLocation(location);
			}
			if(parameters.containsKey(ExploreEnvironment.INDEX_LOCATION)){
				String location = parameters.getProperty(ExploreEnvironment.INDEX_LOCATION);
				ExploreEnvironment.LocationHelper.setIndexLocation(location);
			}
			else{
//				default value
				String location = ResourcesPlugin.getWorkspace().getRoot().getLocation().removeLastSegments(1).toString()
					+ ExploreEnvironment.DEFAULT_RESOURCE_LOCATION_SUFFIX;
				ExploreEnvironment.LocationHelper.setResourceLocation(location);
			}
						
			return onto;
		}
	}
	
	private static void addFileToRepository(IOntology onto, Map<String, Object> parameters) throws MissingParameterException{
		
		if(!parameters.containsKey(ExploreEnvironment.ONTOLOGY_FILE_PATH)) {
			throw new MissingParameterException(ExploreEnvironment.ONTOLOGY_FILE_PATH+" missing!");
		}
		
		if(!parameters.containsKey(ExploreEnvironment.ONTOLOGY_FILE_NAME)) {
			throw new MissingParameterException(ExploreEnvironment.ONTOLOGY_FILE_NAME+" missing!");
		}
		
		if(!parameters.containsKey(ExploreEnvironment.BASE_ONTOLOGY_URI)) {
			throw new MissingParameterException(ExploreEnvironment.BASE_ONTOLOGY_URI+" missing!");
		}
		
		if(!parameters.containsKey(ExploreEnvironment.LANGUAGE)) {
			throw new MissingParameterException(ExploreEnvironment.LANGUAGE+" missing!");
		}
		
//		Check whether or file already added to repository 
		
		File repositoryDir = new File(SesameRepositoryFactory.REPO_PATH_DEFAULT);
		String path = repositoryDir.getAbsolutePath()+"/"+parameters.get(ExploreEnvironment.ONTOLOGY_FILE_NAME)+"_added";
		boolean already_added = Arrays.asList(repositoryDir.list()).contains(parameters.get(ExploreEnvironment.ONTOLOGY_FILE_NAME)+"_added");
		
		if(already_added) {
			return;
		} else{
			try {
				(new File(path)).createNewFile();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}	
		
		String filePath = (String)parameters.get(ExploreEnvironment.ONTOLOGY_FILE_PATH);
		String baseUri = (String)parameters.get(ExploreEnvironment.BASE_ONTOLOGY_URI);
		String language = (String)parameters.get(ExploreEnvironment.LANGUAGE);
				
//		File has not been added -> add it now
		
		try {
			onto.importOntology(language, baseUri, new FileReader(filePath));
		} 
		catch (FileNotFoundException e) {
			e.printStackTrace();
		} 
		catch (OntologyImportException e) {
			e.printStackTrace();
		}
	}

}