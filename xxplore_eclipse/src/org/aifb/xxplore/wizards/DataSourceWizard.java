package org.aifb.xxplore.wizards;


import java.io.IOException;
import java.util.HashMap;

import org.ateam.xxplore.core.ExploreEnvironment;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

public class DataSourceWizard extends Wizard implements INewWizard {
	
	private IStructuredSelection m_selection;
	private IWorkbench m_workbench;
	private DataSourceWizardPage m_mainpage;
	
	private static String TITLE = "Create a New Data Source";
	
	public DataSourceWizard() {
		super();
	}
	
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		m_workbench = workbench;
		m_selection = selection;
		setWindowTitle(TITLE);
		setNeedsProgressMonitor(true);
	}
	
	@Override
	public boolean isHelpAvailable() {
		return false;
	}
	
	public IWorkbench getWorkbench() {
		return m_workbench;
	}
	
	public IStructuredSelection getSelection(){
		return m_selection;
	}
	
	@Override
	public void addPages() {
		m_mainpage = new DataSourceWizardPage();
		addPage(m_mainpage);
	}

	@Override
	public boolean performFinish() {

		HashMap<String, String> props = m_mainpage.getSelectedProperties();
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(props.get(ExploreEnvironment.PROJECT_NAME));
		
		try {
			IFile datasource = ExploreWizardHelper.createDataSourceFile(project, props, null);
			project.setPersistentProperty(ExploreWizardHelper.DATASOURCE, datasource.getFullPath().toPortableString());
		} 
		catch (IOException e){
			e.printStackTrace();
			return false;
		}
		catch (CoreException e){
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
}
