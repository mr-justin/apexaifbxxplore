package org.aifb.xxplore.wizards;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import org.aifb.xxplore.core.ExploreEnvironment;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;
import org.xmedia.accessknow.sesame.persistence.SesameRepositoryFactory;
import org.xmedia.oms.model.api.IOntology;
import org.xmedia.oms.persistence.KbEnvironment;

public class ExploreWizardHelper {

	public static final QualifiedName DATASOURCE = new QualifiedName(ExploreEnvironment.NAME_QUALIFIER, ExploreEnvironment.DATASOURCE_LOCALNAME);
	
	/**
	 * The method creates a datasource file (usually with an extension "ods") in the project
	 * given as parameter. The datasource URI given in parameter is the uri of the datasource 
	 * (perhaps a non initialised Datasource object could be given as parameter).
	 * 
	 * @param project - the name of the project into which the datasource is stored.
	 * @param file_name - the name of the file 
	 * @param datasourceURI - the URI of the data source (N.B: this may change later on)
	 * @param monitor - the listener monitoring the progress of the file creation
	 * @return the IFile resource created
	 * @throws CoreException - A core exception is thrown if ...
	 * @throws IOException - An IO Exception is thrown if  the content can not be written correctly into the file
	 */
	public static IFile createDataSourceFile(IProject project, HashMap<String,String> properties, IProgressMonitor monitor) 
		throws CoreException, IOException {
		
		IPath path = new Path(project.getLocation().getDevice(),properties.get(ExploreEnvironment.DATASOURCE_FILENAME)+ExploreEnvironment.DATASOURCE_EXTENSION);
		IFile datasource = project.getFile(path);
		InputStream in = null;
		
		try {		
			StringBuffer buff = new StringBuffer();
					
			if(properties.get(ExploreEnvironment.ONTOLOGY_EXPRESSITIVITY).equals(IOntology.OWL_EXPRESSIVITY)){
				
				buff.append(KbEnvironment.PHYSICAL_ONTOLOGY_URI+"=");
				buff.append(properties.get(KbEnvironment.PHYSICAL_ONTOLOGY_URI));
				buff.append("\n");
				
				buff.append(KbEnvironment.CONNECTION_PROVIDER_CLASS+"="+
						"org.xmedia.oms.adapter.kaon2.persistence.Kaon2ConnectionProvider\n");
				
				buff.append("session.factory_class=" +
						"org.xmedia.oms.adapter.kaon2.persistence.Kaon2SessionFactory\n");
				
			}
			else if(properties.get(ExploreEnvironment.ONTOLOGY_EXPRESSITIVITY).equals(IOntology.RDFS_EXPRESSIVITY)){
				
				buff.append(ExploreEnvironment.REPOSITORY_NAME+"=");
				buff.append(properties.get(ExploreEnvironment.REPOSITORY_NAME));
				buff.append("\n");
				
				buff.append(ExploreEnvironment.ONTOLOGY_FILE_PATH+"=");
				buff.append(properties.get(ExploreEnvironment.ONTOLOGY_FILE_PATH));
				buff.append("\n");
				
				buff.append(ExploreEnvironment.ONTOLOGY_FILE_NAME+"=");
				buff.append(properties.get(ExploreEnvironment.ONTOLOGY_FILE_NAME));
				buff.append("\n");
				
				buff.append(ExploreEnvironment.BASE_ONTOLOGY_URI+"=");
				buff.append(properties.get(ExploreEnvironment.BASE_ONTOLOGY_URI));
				buff.append("\n");
				
				buff.append(ExploreEnvironment.LANGUAGE+"=");
				buff.append(properties.get(ExploreEnvironment.LANGUAGE));
				buff.append("\n");
				
				buff.append(KbEnvironment.ONTOLOGY_TYPE+"=");
				buff.append(SesameRepositoryFactory.RDFS_MEMORY_PERSISTENT);
				buff.append("\n");
				
				buff.append(KbEnvironment.CONNECTION_PROVIDER_CLASS+"="+
						"org.xmedia.accessknow.sesame.persistence.ConnectionProvider\n");
				
				buff.append("session.factory_class="+
						"org.xmedia.accessknow.sesame.persistence.SesameSessionFactory\n");
				
			}
			
			buff.append(KbEnvironment.REASONER_ON+"=false\n");
			buff.append(ExploreEnvironment.RESOURCE_LOCATION+"="
					+properties.get(ExploreEnvironment.RESOURCE_LOCATION)+"\n");
			buff.append(ExploreEnvironment.INDEX_LOCATION+"="
					+properties.get(ExploreEnvironment.INDEX_LOCATION));
			
			
			in = new ByteArrayInputStream(buff.toString().getBytes());
			if(datasource.exists()){
				datasource.setContents(in, false, true, monitor);
			} else {
				datasource.create(in, false, monitor);
			}
		} 
		finally {
			if(in != null){
				in.close();
			}
		}
		return datasource;
	}
		
	public static String[] getWorkspaceProjects(){
		
		int i = 0;
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		
		String[] project_names = new String[projects.length];
		
		for(IProject project : projects){
			project_names[i++] = project.getName();
		}
					
		return project_names;
	}
	
	public static class TextInputHelper{
		
		public static String cleanInputText(String input){
			
			String out = new String(input);
			out = out.replace("\\", "\\\\");
			
			return out;
		}
	}
}
