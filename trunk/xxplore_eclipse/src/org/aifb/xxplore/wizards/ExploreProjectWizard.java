package org.aifb.xxplore.wizards;

import java.io.IOException;
import org.aifb.xxplore.ExplorePlugin;
import org.aifb.xxplore.builder.ExploreNature;
import org.aifb.xxplore.core.ExploreEnvironment;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;


/**
 * An explore project Wizard
 */

public class ExploreProjectWizard extends Wizard implements INewWizard{

	public static final QualifiedName DATASOURCE = new QualifiedName(ExploreEnvironment.NAME_QUALIFIER,ExploreEnvironment.DATASOURCE_LOCALNAME);
	
	/** the main page of the wizard*/
	protected ExploreProjectWizardPage m_page_1;
	protected DataSourceWizardPage m_page_2;
	
	private IWorkbench m_workbench;
	private IStructuredSelection m_selection;
	
	private static String TITLE = "Create a new XXPlore Project";
	
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

	@Override
	public boolean performFinish() {
		
		IProject project = null;
		
		try {
			
			project = getNewProject();
			project.create(null);
			project.open(null);

			ExploreNature.addNature(project, ExplorePlugin.PLUGIN_ID + ExploreEnvironment.XXPLORE_NATURE, null);

			IProjectDescription desc = project.getDescription();
			desc.setComment(m_page_1.getSelectedProperties().get(ExploreEnvironment.PROJECT_COMMENT));

			project.setDescription(desc, null);

			try {
				IFile datasource = ExploreWizardHelper.createDataSourceFile(project,m_page_2.getSelectedProperties(),null);
				project.setPersistentProperty(ExploreWizardHelper.DATASOURCE, datasource.getFullPath().toPortableString());
			} 
			catch (IOException e){
				System.err.println("An error occured while trying to create new DataSource for Project"+ e);
				e.printStackTrace();
				return false;
			}					
		} 
		catch (CoreException e) {
			System.err.println("An error occured while trying to create new Project"+ e);
			e.printStackTrace();
			
			try {
				if (project!=null) {
					project.delete(false, null);
				}
			} catch (CoreException e1) {
				System.err.println("An error occured while delete failed new Project"+ e);
				e1.printStackTrace();
			}
			return false;
		}
		return true;
	}
	
	/**
	 * Add the pages which should be used during the datasource creation process
	 */
	@Override
	public void addPages() {
		
		addPage(m_page_1 = new ExploreProjectWizardPage(m_workbench, m_selection));
		addPage(m_page_2 = new DataSourceWizardPage());
		
	}
	
	/**
	 * 
	 * @return the project resource which is to be created
	 * @throws CoreException - An exception is thrown if the project cannot be created as wished
	 */
	private IProject getNewProject() throws CoreException {
		return m_page_1.getProjectHandle();
	}
}
