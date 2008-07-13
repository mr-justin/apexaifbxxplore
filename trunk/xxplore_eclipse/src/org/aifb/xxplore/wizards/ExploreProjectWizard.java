package org.aifb.xxplore.wizards;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.aifb.xxplore.ExplorePlugin;
import org.aifb.xxplore.builder.ExploreNature;
import org.aifb.xxplore.core.ExploreEnvironment;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.xmedia.oms.model.api.IOntology;
import org.xmedia.oms.persistence.KbEnvironment;


/**
 * An explore project Wizard
 */

public class ExploreProjectWizard extends Wizard implements INewWizard {

	
	public static final QualifiedName DATASOURCE = new QualifiedName(ExploreEnvironment.NAME_QUALIFIER,ExploreEnvironment.DATASOURCE_LOCALNAME);
	private static final String PROJECT_WIZARD_DESC = "This Wizard is used to create an xxplore project!";	
	private static final String PROJECT_WIZARD_MSG = "Creation of an Exploration Project!";
	private static final String PROJECT_WIZARD_MAINPAGE_TITLE = "Specify Explore Project Data!"; 
	
	/** the main page of the wizard*/
	private ExploreNewProjectWizzardMainPage m_mainPage;


	public void init(IWorkbench workbench, IStructuredSelection selection) {}

//TODO should be implemented
	@Override
	public boolean isHelpAvailable() {
		return false;
	}

	@Override
	public boolean performFinish() {
		
		String comment = m_mainPage.m_comment_text.getText();
		IProject project = null;
		try {
			
			project = getNewProject();
			project.create(null);
			project.open(null);

			ExploreNature.addNature(project, ExplorePlugin.PLUGIN_ID + ExploreEnvironment.XXPLORE_NATURE, null);

			IProjectDescription desc = project.getDescription();
			desc.setComment(comment);

			project.setDescription(desc, null);

			try {
				IFile datasource = WizardHelper.createDataSourceFile(project,m_mainPage.getSelectedProperties(),null);
				project.setPersistentProperty(WizardHelper.DATASOURCE, datasource.getFullPath().toPortableString());
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
		m_mainPage = new ExploreNewProjectWizzardMainPage(PROJECT_WIZARD_MAINPAGE_TITLE);
		addPage(m_mainPage);
	}

	/**
	 * 
	 * @return the project resource which is to be created
	 * @throws CoreException - An exception is thrown if the project cannot be created as wished
	 */
	private IProject getNewProject() throws CoreException {
		return m_mainPage.getProjectHandle();
	}

	public class ExploreNewProjectWizzardMainPage extends WizardPage {

		private Text m_comment_text;
		private Text m_projectname_text;
		private Text m_repository_name_text;
		private Text m_datasourceUri_text;
		private Text m_baseUri_text;
		private Text m_datasource_name_text;
		
		private Combo m_combo_syntax;
		private Combo m_combo_expressivity;
		
		private Label m_syntax_label;
		private Label m_repository_name_label;
		private Label m_baseUri_label;
		
		private final String[] m_syntax = {IOntology.TRIX_LANGUAGE, IOntology.RDF_XML_LANGUAGE, IOntology.N3_LANGUAGE};		
		private final String[] m_expressivity = {IOntology.OWL_EXPRESSIVITY, IOntology.RDFS_EXPRESSIVITY};
				
		public ExploreNewProjectWizzardMainPage(String pageName) {
			super(pageName);
			super.setTitle(PROJECT_WIZARD_MAINPAGE_TITLE);
			super.setDescription(PROJECT_WIZARD_DESC);
			super.setMessage(PROJECT_WIZARD_MSG);
		}

		private IProject getProjectHandle() {
			return ResourcesPlugin.getWorkspace().getRoot().getProject(m_projectname_text.getText());
		}

		public void createControl(Composite parent) {
			
			Composite comp = new Composite(parent, SWT.NONE);
			GridData gridData = new GridData(SWT.FILL, SWT.None, true, false);

			GridLayout layout = new GridLayout(2, false);
			comp.setLayout(layout);

			Label label = new Label(comp, SWT.NONE);
			label.setText("Eclipse Project Name");

			m_projectname_text = new Text(comp, SWT.BORDER);
			m_projectname_text.setText("explore.project");
			m_projectname_text.setLayoutData(gridData);
			m_projectname_text.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					getContainer().updateButtons();
				}
			});
			
			Label m_datasource_name_label = new Label(comp, SWT.NONE);
			m_datasource_name_label.setText("Datasource Name");
			
			m_datasource_name_text = new Text(comp, SWT.BORDER);
			m_datasource_name_text.setText("datasource");
			m_datasource_name_text.setLayoutData(gridData);
			m_datasource_name_text.setToolTipText("Please enter a name for your new datasource file.");
			m_datasource_name_text.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					getContainer().updateButtons();
				}
			});
			
			Label urilabel = new Label(comp, SWT.NONE);
			urilabel.setText("Knowledge Base Filepath");
			m_datasourceUri_text = new Text(comp, SWT.BORDER);
			m_datasourceUri_text.setLayoutData(gridData);
			m_datasourceUri_text.setToolTipText("Enter the location of the knowledge base you want to explore");
			m_datasourceUri_text.setText("c:/explore.owl");		
			m_datasourceUri_text.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					getContainer().updateButtons();
				}
			});
			
			Label expressivity_label = new Label(comp, SWT.NONE);
			expressivity_label.setText("Language");
			m_combo_expressivity = new Combo(comp, SWT.DROP_DOWN);
			m_combo_expressivity.setLayoutData(gridData);
			m_combo_expressivity.setItems(m_expressivity);
			m_combo_expressivity.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					getContainer().updateButtons();
					updateButtons();
				}
			});
			
			m_syntax_label = new Label(comp, SWT.NONE);
			m_syntax_label.setText("Syntax");
			m_combo_syntax = new Combo(comp, SWT.DROP_DOWN);
			m_combo_syntax.setLayoutData(gridData);
			m_combo_syntax.setItems(m_syntax);
			m_combo_syntax.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					getContainer().updateButtons();
				}
			});
			
			m_syntax_label.setEnabled(false);
			m_combo_syntax.setEnabled(false);
			
			m_repository_name_label = new Label(comp, SWT.NONE);
			m_repository_name_label.setText("Repository Name");
			m_repository_name_text = new Text(comp, SWT.BORDER | SWT.FILL);
			m_repository_name_text.setText("My_repository");
			m_repository_name_text.setToolTipText("Please enter a name for your repository.");
			m_repository_name_text.setLayoutData(gridData);
			m_repository_name_text.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					getContainer().updateButtons();
				}
			});
			
			m_repository_name_label.setEnabled(false);
			m_repository_name_text.setEnabled(false);
			
			m_baseUri_label = new Label(comp, SWT.NONE);
			m_baseUri_label.setText("Base URI");
			m_baseUri_text = new Text(comp, SWT.BORDER | SWT.FILL);
			m_baseUri_text.setToolTipText("Please provide your ontology base uri - if none, leave blank.");
			m_baseUri_text.setLayoutData(gridData);
			m_baseUri_text.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					getContainer().updateButtons();
				}
			});
			
			m_baseUri_text.setEnabled(false);
			m_baseUri_text.setEnabled(false);
			
			Label commentlabel = new Label(comp, SWT.NONE);
			commentlabel.setText("Comment");
			m_comment_text = new Text(comp, SWT.BORDER | SWT.FILL);
			m_comment_text.setToolTipText("You may specify a project description.");
			m_comment_text.setLayoutData(gridData);
			m_comment_text.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					getContainer().updateButtons();
				}
			});
			
			setControl(comp);
		}

		private void updateButtons(){			
			if(m_combo_expressivity.getText().equals(IOntology.OWL_EXPRESSIVITY)){
				m_syntax_label.setEnabled(false);
				m_combo_syntax.setEnabled(false);
				m_repository_name_label.setEnabled(false);
				m_repository_name_text.setEnabled(false);
			}
			else if(m_combo_expressivity.getText().equals(IOntology.RDFS_EXPRESSIVITY)){
				m_syntax_label.setEnabled(true);
				m_combo_syntax.setEnabled(true);
				m_repository_name_label.setEnabled(true);
				m_repository_name_text.setEnabled(true);
			}
		}
		
		@Override
		public boolean isPageComplete() {
			String error = new String();
			boolean is_valid = true;
			
//			check whether all fields are set
			
			if((m_projectname_text.getText().trim() == null) || m_projectname_text.getText().trim().equals("")){
				error = "Project not set. Please provide a valid project.";
				is_valid = false;
			}
			else if((m_datasource_name_text.getText().trim() == null) || m_datasource_name_text.getText().trim().equals("")){
				error = "Please provide a name for your datasource.";
				is_valid = false;
			}
			else if((m_combo_expressivity.getText().trim() == null) || m_combo_expressivity.getText().trim().equals("")){
				error = "Please provide the used expressivity.";
				is_valid = false;
			}
			else if(m_combo_expressivity.getText().equals(IOntology.OWL_EXPRESSIVITY)){				
				
				if((m_datasourceUri_text.getText().trim() == null) || m_datasourceUri_text.getText().trim().equals("")){
					error = "Please provide the location of your knowledge base.";
					is_valid = false;
				}
			}
			else if(m_combo_expressivity.getText().equals(IOntology.RDFS_EXPRESSIVITY)){
				
				if((m_repository_name_text.getText().trim() == null) || m_repository_name_text.getText().trim().equals("")){
					error = "Please provide a name for your repository.";
					is_valid = false;
				}
				else if((m_datasourceUri_text.getText().trim() == null) || m_datasourceUri_text.getText().trim().equals("")){
					error = "Please provide the location of your knowledge base.";
					is_valid = false;
				}
//				Note, the base-Uri may be empty!
				else if((m_combo_syntax.getText().trim() == null) || m_combo_syntax.getText().trim().equals("")){
					error = "Please provide the syntax used in your knowledge base.";
					is_valid = false;
				}
			}

			
//			check whether knowledge base exists
			
			String filepath = m_datasourceUri_text.getText();
			File file = new File(filepath); 					
			if(!file.exists()){
				error = "Knowledge base not found, using path '"+filepath+"'.";
				is_valid = false;
			}
			
			if(!is_valid){
				setErrorMessage(error);
				return false;
			}
			else{
				setErrorMessage(null);
				return true;
			}
		}
		
		public HashMap<String,String> getSelectedProperties(){
			
			HashMap<String,String> properties = new HashMap<String, String>();
			properties.put(ExploreEnvironment.DATASOURCE_FILENAME, m_datasource_name_text.getText().trim());
			properties.put(ExploreEnvironment.ONTOLOGY_EXPRESSITIVITY, m_combo_expressivity.getText().trim());
			
			if(m_combo_expressivity.getText().equals(IOntology.OWL_EXPRESSIVITY)){
				properties.put(KbEnvironment.PHYSICAL_ONTOLOGY_URI, m_datasourceUri_text.getText().trim());			
			}
			else if(m_combo_expressivity.getText().equals(IOntology.RDFS_EXPRESSIVITY)){
				
				properties.put(ExploreEnvironment.REPOSITORY_NAME, m_repository_name_text.getText().trim());
				properties.put(ExploreEnvironment.ONTOLOGY_FILE_PATH, m_datasourceUri_text.getText().trim());
				properties.put(ExploreEnvironment.ONTOLOGY_FILE_NAME, m_datasourceUri_text.getText().trim());
				properties.put(ExploreEnvironment.BASE_ONTOLOGY_URI, m_baseUri_text.getText().trim());
				properties.put(ExploreEnvironment.LANGUAGE, m_combo_syntax.getText().trim());
				
			}
			
			return properties;
		}
	}
}
