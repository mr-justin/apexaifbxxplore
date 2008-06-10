package org.aifb.xxplore.wizards;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.aifb.xxplore.ExplorePlugin;
import org.aifb.xxplore.builder.ExploreNature;
import org.aifb.xxplore.core.ExploreEnvironment;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.xmedia.oms.persistence.KbEnvironment;


/**
 * An explore project Wizard
 * 
 * @author Julien Tane
 * 
 */

public class ExploreProjectWizard extends Wizard implements INewWizard {
	// TODO This should be put somewhere else....
	public static final QualifiedName DATASOURCE = new QualifiedName(ExploreEnvironment.NAME_QUALIFIER,
			ExploreEnvironment.DATASOURCE_LOCALNAME);

	public static final QualifiedName VIEWDEFINITION = new QualifiedName(ExploreEnvironment.NAME_QUALIFIER,
			ExploreEnvironment.VIEWDEFINITION_LOCALNAME);

	/** the workbench calling the Wizard */
	protected IWorkbench m_wokbench;
	/** the current selection */
	protected IStructuredSelection m_selection;
	/** the main page of the wizard*/
	protected ExploreNewProjectWizzardMainPage m_page1;


	public void init(IWorkbench workbench, IStructuredSelection selection) {
		m_wokbench = workbench;
		m_selection = selection;
	}

	/**
	 * The Help should be implemented
	 */
	public boolean isHelpAvailable() {
		return false;
	}

	public boolean performFinish() {
		String comment = m_page1.m_comment.getText();
		IProject project=null;
		try {
			project = getNewProject();
			project.create(null);
			project.open(null);

			ExploreNature.addNature(project, ExplorePlugin.PLUGIN_ID + ExploreEnvironment.XXPLORE_NATURE, null);

			IProjectDescription desc = project.getDescription();
			desc.setComment(comment);

			project.setDescription(desc, null);

			try {
				IFile datasource = createDataSourceFile(project,"datasource.ods",
						m_page1.m_datasource_filename.getText(), null);
				project.setPersistentProperty(DATASOURCE, datasource
						.getFullPath().toPortableString());
				IFile defFile = createDefinitionFile(project,"viewdef.def", 
						datasource.getFullPath().toString() ,"", null);
				project.setPersistentProperty(VIEWDEFINITION, defFile
						.getFullPath().toPortableString());
			} catch (IOException e) {
				System.err
				.println("An error occured while trying to create new DataSource for Project"
						+ e);
				e.printStackTrace();
				return false;
			}

			//						
		} catch (CoreException e) {
			System.err
			.println("An error occured while trying to create new Project"+ e);
			e.printStackTrace();
			try {
				if (project!=null) project.delete(false, null);
			} catch (CoreException e1) {// FIXME should be taken away
				System.err.println(
						"An error occured while delete failed new Project"+ e);
				e1.printStackTrace();
			}
			return false;
		}
		return true;
	}


	/**
	 * The method creates a datasource file (usually with an extension "ods") in the project
	 * given as parameter. The datasource URI given in parameter is the uri of the datasource 
	 * (perhaps a non initialised Datasource object could be given as parameter).
	 * 
	 * @param project - the name of the project into which the datasource is stored.
	 * @param name - the name of the file 
	 * @param datasourceURI - the URI of the data source (N.B: this may change later on)
	 * @param monitor - the listener monitoring the progress of the file creation
	 * @return the IFile resource created
	 * @throws CoreException - A core exception is thrown if ...
	 * @throws IOException - An IO Exception is thrown if  the content can not be written correctly into the file
	 */
	// FIXME This method should probably go somewhere else 
	// it is a fundamental datasource file creation method
	IFile createDataSourceFile(IProject project, String name,String datasourceURI,
			IProgressMonitor monitor) throws CoreException, IOException {
		IPath path = new Path( project.getLocation().getDevice(),name);
		IFile datasource = project.getFile(path);
		InputStream in = null;
		try {
			StringBuffer buff = new StringBuffer();
			buff.append(KbEnvironment.PHYSICAL_ONTOLOGY_URI+"=");
			buff.append(datasourceURI);
			buff.append("\n");
			in = new ByteArrayInputStream(buff.toString().getBytes());
			if (datasource.exists()) {
				datasource.setContents(in, false, true, monitor);
			} else {
				datasource.create(in, false, monitor);
			}
		} finally {
			if (in != null) {
				in.close();
			}
		}
		return datasource;
	}


	/**
	 * This method creates a definition file with the given name, the corresponding content
	 * in the given project. 
	 * @param project  - the name of the project into which the definition is stored.
	 * @param name - the name of the file containing the definition
	 * @param content - the content of the definition (N.B: this may change later on)
	 * @param monitor - the object monitoring the progress of the file creation
	 * @return the IFile resource created
	 * @throws CoreException - An CoreException is thrown if the touch could not be performed correctly
	 */
	private IFile createDefinitionFile(IProject project, String name, String datasource, 
			String content, IProgressMonitor monitor) throws CoreException,IOException {
		IPath path = new Path( project.getLocation().getDevice(),name);
		IFile definitionfile = project.getFile(path);
		InputStream in = null;
		try {
			StringBuffer buff = new StringBuffer();
			buff.append(ExploreEnvironment.DATASOURCE+datasource+"\n");
			buff.append(content);
			in = new ByteArrayInputStream(buff.toString().getBytes());
			definitionfile.create(in, false, monitor);
		} finally {
			if (in != null) {
				in.close();
			}
		}

		return definitionfile;
	}

	/**
	 * Add the pages which should be used during the datasource creation process
	 */
	public void addPages() {
		m_page1 = new ExploreNewProjectWizzardMainPage(ExploreEnvironment.PROJECT_WIZARD_PAGE1_TITLE);
		addPage(m_page1);
	}

	/**
	 * 
	 * @return the project resource which is to be created
	 * @throws CoreException - An exception is thrown if the project cannot be created as wished
	 */
	private IProject getNewProject() throws CoreException {
		return m_page1.getProjectHandle();
	}

	class ExploreNewProjectWizzardMainPage extends WizardPage {

		private Text m_comment;

		private Text m_projectname;

		private Text m_datasource_filename;

		public ExploreNewProjectWizzardMainPage(String pageName) {
			super(pageName);
			setDescription(ExploreEnvironment.PROJECT_WIZARD_DESC);
			setMessage(ExploreEnvironment.PROJECT_WIZARD_MSG);
		}

		public IProject getProjectHandle() {
			return ResourcesPlugin.getWorkspace().getRoot().getProject(
					getProjectName());
		}

		private String getProjectName() {
			return m_projectname.getText();
		}

		public void createControl(Composite parent) {
			Composite comp = new Composite(parent, SWT.NONE);
			GridData gridData = new GridData(SWT.FILL, SWT.None, true, false);

			GridLayout layout = new GridLayout(2, false);
			comp.setLayout(layout);

			Label label = new Label(comp, SWT.NONE);
			label.setText("Name");

			m_projectname = new Text(comp, SWT.BORDER);
			m_projectname.setText("explore.project");
			m_projectname.addModifyListener(new ModifyListener() {

				public void modifyText(ModifyEvent e) {
					getContainer().updateButtons();
				}
			});
			m_projectname.setLayoutData(gridData);
			Label urilabel = new Label(comp, SWT.NONE);
			urilabel.setText("Knowledge Base URI");
			m_datasource_filename = new Text(comp, SWT.BORDER);
			m_datasource_filename.setLayoutData(gridData);
			m_datasource_filename.setTextLimit(500);
			m_datasource_filename.setToolTipText("Enter the location of the knowledge base you want to explore");
			m_datasource_filename.setText("file:/home/jta/owl/explore.owl");
			//TODO check whether this is O.K then uncomment
			// m_datasourceuri.addVerifyListener(new VerifyListener(){
			// public void verifyText(VerifyEvent e) {
			// String oldText= m_datasourceuri.getText();
			// String newText = oldText.substring(0,e.start)
			// +e.text+ oldText.substring(e.end);
			// try{
			// new URI(newText);
			// }catch (URISyntaxException ex) {
			// e.doit= false;
			// }
			// }
			// });

			m_datasource_filename.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					getContainer().updateButtons();
				}
			});
			Label commentlabel = new Label(comp, SWT.NONE);
			commentlabel.setText("Comment");
			m_comment = new Text(comp, SWT.BORDER | SWT.FILL);
			m_comment.setLayoutData(gridData);
			setControl(comp);
		}

		@Override
		public boolean isPageComplete() {
			return validate() && super.isPageComplete();
		}

		private boolean validate() {
			String mes = (m_datasource_filename.getText().trim().length() == 0) ? "No Data source URI has been set." : null;
			if (mes == null) mes = (m_projectname.getText().length() == 0) ? "The project name should be non empty." : null;				
			if (mes == null) mes = (!ResourcesPlugin.getWorkspace().validateName(m_projectname.getText(), IResource.PROJECT).isOK()) ? 
									"Invalid project name!" : null;
			
			
			if (mes != null) {
				setErrorMessage(mes);
				return false;
			}
			setErrorMessage(null);
			return true;
			
		}

		public String getResult() {
			return m_projectname.getText();
		}
	}
}
