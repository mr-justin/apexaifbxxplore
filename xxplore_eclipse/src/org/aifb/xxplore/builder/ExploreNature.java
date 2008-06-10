package org.aifb.xxplore.builder;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

public class ExploreNature implements IProjectNature {

	 
	
	//	is this normal that there is only one? 
	private IProject m_project;
	
	public void configure() throws CoreException {
		
		  IProjectDescription desc = m_project.getDescription();
		   ICommand[] commands = desc.getBuildSpec();
		   boolean found = false;

		   for (int i = 0; i < commands.length; ++i) {
		      if (commands[i].getBuilderName().equals(ExploreDataSourceBuilder.BUILDER_ID)) {
		         found = true;
		         break;
		      }
		   }
		   if (!found) { 
		      //add builder to project
		      ICommand command = desc.newCommand();
		      command.setBuilderName(ExploreDataSourceBuilder.BUILDER_ID);
		      ICommand[] newCommands = new ICommand[commands.length + 1];

		      // Add it before other builders.
		      System.arraycopy(commands, 0, newCommands, 1, commands.length);
		      newCommands[0] = command;
		      desc.setBuildSpec(newCommands);
		      m_project.setDescription(desc, null);
		   }
	}

	public void deconfigure() throws CoreException {
		  IProjectDescription desc = m_project.getDescription();
		   ICommand[] commands = desc.getBuildSpec();
		   
		   int length = commands.length;
		   ICommand[] newcommands = new ICommand[length-1];
			for (int i = 0; i<length ; i++){
				if (commands[i].getBuilderName().equals(ExploreDataSourceBuilder.BUILDER_ID)){
					if (i>0)
						System.arraycopy(commands,0,newcommands, 0, i-1);
					if (i<length)
						System.arraycopy(commands,i+1,newcommands, i, length-1);
					desc.setBuildSpec(newcommands);
					m_project.setDescription(desc, null);
					break;
				}
			}
		   

	}

	public IProject getProject() {
		return m_project;
	}

	public void setProject(IProject project) {
		m_project = project;
	}

	public static void addNature(IProject project, String nature, IProgressMonitor monitor) throws CoreException{
		if (project.hasNature(nature)) return;
		IProjectDescription desc= project.getDescription();
		String[] natures = desc.getNatureIds();
		String[] newnatures = new String[natures.length+1];
		System.arraycopy(natures,0, newnatures, 0, natures.length);
		newnatures[natures.length]= nature;
		desc.setNatureIds(newnatures);
		//project.setDescription(desc, monitor);
	}
	
	public static void removeNature(IProject project, String nature, IProgressMonitor monitor) throws CoreException{
		IProjectDescription desc= project.getDescription();
		String[] natures = desc.getNatureIds();
		int length = natures.length;
		String[] newnatures = new String[length-1];
		for (int i = 0; i<length ; i++){
			if (natures[i].equals(nature)){
				if (i>0)
					System.arraycopy(natures,0,newnatures, 0, i-1);
				if (i<length)
					System.arraycopy(natures,i+1,newnatures, i, length-1);
				desc.setNatureIds(newnatures);
				project.setDescription(desc, monitor);
				break;
			}
		}
	}

}
