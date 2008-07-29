package org.aifb.xxplore.builder;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.aifb.xxplore.ExplorePlugin;
import org.aifb.xxplore.wizards.ExploreProjectWizard;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.xmedia.accessknow.sesame.persistence.ConnectionProvider;
import org.xmedia.oms.persistence.DatasourceException;
import org.xmedia.oms.persistence.IDataSource;
import org.xmedia.oms.persistence.InvalidParameterException;
import org.xmedia.oms.persistence.KbEnvironment;
import org.xmedia.oms.persistence.MissingParameterException;
import org.xmedia.oms.persistence.OntologyLoadException;


public class ExploreBuildVisitor implements IResourceVisitor {

	protected IDataSource m_DataSource;
	
	/**
	 * 
	 */
	public boolean visit(IResource resource) throws CoreException {
		
	
		IProject project = (IProject)resource;
		String datasourcefile = project.getPersistentProperty(ExploreProjectWizard.DATASOURCE);
		Properties props = new Properties();
		
		try {
			props.load(new FileInputStream(datasourcefile));
		} catch (FileNotFoundException e) {
			IStatus status = 
				new MultiStatus(ExplorePlugin.PLUGIN_ID,IStatus.ERROR,"The datasource file could not be found",e);
			throw new CoreException(status);
		} catch (IOException e) {
			IStatus status = 
				new MultiStatus(ExplorePlugin.PLUGIN_ID,IStatus.ERROR,"The datasource file could not be loaded",e);
			throw new CoreException(status);
		}
		
		//TODO configure
		Map<String, Object> params = new HashMap<String, Object>();
		params.put(KbEnvironment.PHYSICAL_ONTOLOGY_URI, props.get(KbEnvironment.PHYSICAL_ONTOLOGY_URI));		
		ConnectionProvider prov = new ConnectionProvider();
		try {
			IDataSource datasource = prov.getConnection().loadOntology(params);
		} catch (DatasourceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MissingParameterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidParameterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OntologyLoadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return true;
	}

}