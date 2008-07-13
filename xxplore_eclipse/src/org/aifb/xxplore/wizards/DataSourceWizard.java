package org.aifb.xxplore.wizards;


import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.aifb.xxplore.core.ExploreEnvironment;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.xmedia.oms.model.api.IOntology;
import org.xmedia.oms.persistence.KbEnvironment;

public class DataSourceWizard extends Wizard implements INewWizard {
	
	private IStructuredSelection m_selection;
	private IWorkbench m_workbench;
	private DataSourceWizardMainPage m_mainpage;
	
	
	public DataSourceWizard() {
		super();
	}
	
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		m_workbench = workbench;
		m_selection = selection;
		setWindowTitle("New Data Source");
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
		m_mainpage = new DataSourceWizardMainPage();
		addPage(m_mainpage);
	}

	@Override
	public boolean performFinish() {

		HashMap<String, String> props = m_mainpage.getSelectedProperties();
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(props.get(ExploreEnvironment.PROJECT_NAME));
		
		try {
			IFile datasource = WizardHelper.createDataSourceFile(project, props, null);
			project.setPersistentProperty(WizardHelper.DATASOURCE, datasource.getFullPath().toPortableString());
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
	
	
	public class DataSourceWizardMainPage extends WizardPage {
		
		private Text m_datasource_name_text;
		private Text m_repository_name_text;
		private Text m_datasourceUri_text;
		private Text m_baseUri_text;
		
		private Combo m_combo_syntax;
		private Combo m_combo_expressivity;
		private Combo m_combo_project;
		
		private Label m_syntax_label;
		private Label m_repository_name_label;
		private Label m_baseUri_label;
		
		private static final String TITLE = "New Ontology Datasource";
		private static final String DESC = "Create a new file representing a XXPlore-Datasource";
		
		private final String[] m_syntax = {IOntology.TRIX_LANGUAGE, IOntology.RDF_XML_LANGUAGE, IOntology.N3_LANGUAGE};		
		private final String[] m_expressivity = {IOntology.OWL_EXPRESSIVITY, IOntology.RDFS_EXPRESSIVITY};
		private Composite m_composite;
		
		public DataSourceWizardMainPage() {
			super(TITLE);
			super.setDescription(DESC);
		}
		
		public void createControl(Composite parent) {
			
			m_composite = new Composite(parent, SWT.NONE);
			GridData gridData = new GridData(SWT.FILL, SWT.None, true, false);
			String[] projects;
			GridLayout layout = new GridLayout(2, false);
			m_composite.setLayout(layout);
			
			Label m_project_label = new Label(m_composite, SWT.NONE);
			m_project_label.setText("Project");
			m_combo_project = new Combo(m_composite, SWT.DROP_DOWN);
			m_combo_project.setLayoutData(gridData);
			m_combo_project.setItems(projects = getProjects());
			m_combo_project.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					getContainer().updateButtons();
				}
			});
			
			Label m_datasource_name_label = new Label(m_composite, SWT.NONE);
			m_datasource_name_label.setText("Datasource Name");
			
			m_datasource_name_text = new Text(m_composite, SWT.BORDER);
			m_datasource_name_text.setText("datasource");
			m_datasource_name_text.setLayoutData(gridData);
			m_datasource_name_text.setToolTipText("Please enter a name for your new datasource file.");
			m_datasource_name_text.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					getContainer().updateButtons();
				}
			});
			
			Label urilabel = new Label(m_composite, SWT.NONE);
			urilabel.setText("Ontology Location");
			m_datasourceUri_text = new Text(m_composite, SWT.BORDER);
			m_datasourceUri_text.setLayoutData(gridData);
			m_datasourceUri_text.setTextLimit(500);
			m_datasourceUri_text.setToolTipText("Enter the location of the knowledge base you want to explore");
			m_datasourceUri_text.setText("c:/explore.owl");
			m_datasourceUri_text.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					getContainer().updateButtons();
				}
			});
			
			Label expressivity_label = new Label(m_composite, SWT.NONE);
			expressivity_label.setText("Language");
			m_combo_expressivity = new Combo(m_composite, SWT.DROP_DOWN);
			m_combo_expressivity.setLayoutData(gridData);
			m_combo_expressivity.setItems(m_expressivity);
			m_combo_expressivity.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					getContainer().updateButtons();
					updateButtons();
				}
			});
			
			m_syntax_label = new Label(m_composite, SWT.NONE);
			m_syntax_label.setText("Syntax");
			m_combo_syntax = new Combo(m_composite, SWT.DROP_DOWN);
			m_combo_syntax.setLayoutData(gridData);
			m_combo_syntax.setItems(m_syntax);
			m_combo_syntax.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					getContainer().updateButtons();
				}
			});
			
			m_syntax_label.setEnabled(false);
			m_combo_syntax.setEnabled(false);
			
			m_repository_name_label = new Label(m_composite, SWT.NONE);
			m_repository_name_label.setText("Repository Name");
			m_repository_name_text = new Text(m_composite, SWT.BORDER | SWT.FILL);
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
			
			m_baseUri_label = new Label(m_composite, SWT.NONE);
			m_baseUri_label.setText("Base URI");
			m_baseUri_text = new Text(m_composite, SWT.BORDER | SWT.FILL);
			m_baseUri_text.setToolTipText("Please provide your ontology base uri - if none, leave blank.");
			m_baseUri_text.setLayoutData(gridData);
			m_baseUri_text.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					getContainer().updateButtons();
				}
			});
			
			m_baseUri_text.setEnabled(false);
			m_baseUri_text.setEnabled(false);
						
			setControl(m_composite);
			
			if(projects.length == 0){
				noProjectError();
			}
		}
		
		private void updateButtons(){			
			if(m_combo_expressivity.getText().equals(IOntology.OWL_EXPRESSIVITY)){
				m_syntax_label.setEnabled(false);
				m_combo_syntax.setEnabled(false);
				m_repository_name_label.setEnabled(false);
				m_repository_name_text.setEnabled(false);
				m_baseUri_label.setEnabled(false);
				m_baseUri_text.setEnabled(false);
			}
			else if(m_combo_expressivity.getText().equals(IOntology.RDFS_EXPRESSIVITY)){
				m_syntax_label.setEnabled(true);
				m_combo_syntax.setEnabled(true);
				m_repository_name_label.setEnabled(true);
				m_repository_name_text.setEnabled(true);
				m_baseUri_label.setEnabled(true);
				m_baseUri_text.setEnabled(true);
			}
		}
				
		public HashMap<String,String> getSelectedProperties(){
			
			HashMap<String,String> properties = new HashMap<String, String>();
			properties.put(ExploreEnvironment.PROJECT_NAME, m_combo_project.getText().trim());
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
				
		@Override
		public boolean isPageComplete() {
			
			String error = new String();
			boolean is_valid = true;
			
//			check whether all fields are set
			
			if((m_combo_project.getText().trim() == null) || m_combo_project.getText().trim().equals("")){
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
		
		private String[] getProjects(){
			
			int i = 0;
			IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
			
			String[] project_names = new String[projects.length];
			
			for(IProject project : projects){
				project_names[i++] = project.getName();
			}
						
			return project_names;
		}
		
		private void noProjectError(){
			
			String title = "No Project Error";
			String message = "Currently your workspace contains on project.\nBefore creating a datasource file, please create a project.";

			MessageBox mb = new MessageBox(m_composite.getShell(), SWT.ICON_WORKING | SWT.OK);
			mb.setText(title);
			mb.setMessage(message);

            if (mb.open() == SWT.OK) {
            	((WizardDialog)getContainer()).close();
            	return;
            }			
		}
	}
}
