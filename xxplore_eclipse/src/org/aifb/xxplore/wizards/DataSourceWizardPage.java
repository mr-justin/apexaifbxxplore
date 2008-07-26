package org.aifb.xxplore.wizards;

import java.io.File;
import java.util.HashMap;
import org.aifb.xxplore.core.ExploreEnvironment;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.xmedia.oms.model.api.IOntology;
import org.xmedia.oms.persistence.KbEnvironment;

public class DataSourceWizardPage extends WizardPage implements Listener{
	
	private Text m_datasource_name_text;
	private Text m_repository_name_text;
	private Text m_datasourceUri_text;
	private Text m_baseUri_text;
	private Text m_resourceLocation_text;
	private Text m_indexLocation_text;
	
	private Combo m_combo_syntax;
	private Combo m_combo_expressivity;
	private Combo m_combo_project;
	
	private Label m_syntax_label;
	private Label m_repository_name_label;
	private Label m_baseUri_label;
	private Label m_line_label;
	
	private Composite m_composite;
	
	private static final String TITLE = "New Ontology Datasource";
	private static final String DESC = "Create a new file representing a XXPlore-Datasource";
	
	private final String[] m_syntax = {IOntology.TRIX_LANGUAGE, IOntology.RDF_XML_LANGUAGE, IOntology.N3_LANGUAGE};		
	private final String[] m_expressivity = {IOntology.OWL_EXPRESSIVITY, IOntology.RDFS_EXPRESSIVITY};
	
	
	public DataSourceWizardPage() {
		super(TITLE);
		super.setDescription(DESC);
		
	}
	
	public void createControl(Composite parent) {
		
		m_composite = new Composite(parent, SWT.NONE);
		GridData gridData = new GridData(SWT.FILL, SWT.None, true, false);

		GridLayout layout = new GridLayout(2, false);
		m_composite.setLayout(layout);
	
		Label m_project_label = new Label(m_composite, SWT.NONE);
		m_project_label.setText("Project");
		m_combo_project = new Combo(m_composite, SWT.DROP_DOWN);
		m_combo_project.setLayoutData(gridData);
		m_combo_project.addListener(SWT.Selection, this);
		m_combo_project.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				getContainer().updateButtons();
			}
		});
		
		if(getWizard() instanceof DataSourceWizard){
			
			m_combo_project.setItems(ExploreWizardHelper.getWorkspaceProjects());
		
			if(m_combo_project.getItemCount() == 0){
				setErrorMessage("No Explore Projects found. Please use ExploreProject Wizard first and create one.");
			}
		}

		Label m_datasource_name_label = new Label(m_composite, SWT.NONE);
		m_datasource_name_label.setText("Datasource Name");
		
		m_datasource_name_text = new Text(m_composite, SWT.BORDER);
		m_datasource_name_text.setText("datasource");
		m_datasource_name_text.setLayoutData(gridData);
		m_datasource_name_text.setToolTipText("Please enter a name for your new datasource file.");
		m_datasource_name_text.addListener(SWT.KeyUp, this);
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
		m_datasourceUri_text.addListener(SWT.KeyUp, this);
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
		m_combo_expressivity.addListener(SWT.Selection, this);
		m_combo_expressivity.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				getContainer().updateButtons();
				updateButtonVisibility();
			}
		});
		
		Label resourceLocation_label = new Label(m_composite, SWT.NONE);
		resourceLocation_label.setText("Resource Location");

		m_resourceLocation_text = new Text(m_composite, SWT.BORDER);
		m_resourceLocation_text.setText(ResourcesPlugin.getWorkspace().getRoot().getLocation().removeLastSegments(1).toString()
				+ExploreEnvironment.DEFAULT_RESOURCE_LOCATION_SUFFIX);
		m_resourceLocation_text.setToolTipText("Please provide a location for the needed resources.");
		m_resourceLocation_text.setLayoutData(gridData);
		m_resourceLocation_text.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				getContainer().updateButtons();
			}
		});
		
		Label indexLocation_label = new Label(m_composite, SWT.NONE);
		indexLocation_label.setText("Index Location");

		m_indexLocation_text = new Text(m_composite, SWT.BORDER);
		m_indexLocation_text.setText(ResourcesPlugin.getWorkspace().getRoot().getLocation().removeLastSegments(1).toString()
				+ExploreEnvironment.DEFAULT_RESOURCE_LOCATION_SUFFIX+"/"+m_datasource_name_text.getText().trim().toLowerCase());
		m_indexLocation_text.setToolTipText("Please provide a location for the needed resources.");
		m_indexLocation_text.setLayoutData(gridData);
		m_indexLocation_text.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				getContainer().updateButtons();
			}
		});
		
		m_line_label = new Label(m_composite, SWT.NONE);
		m_line_label.setEnabled(false);
		m_line_label.setText("Sesame Related Data");
		
		Label line = new Label(m_composite, SWT.SEPARATOR|SWT.HORIZONTAL|SWT.BOLD);
		GridData gridData2 = new GridData(GridData.FILL_HORIZONTAL);
		gridData2.horizontalSpan = 4;
		line.setLayoutData(gridData2);

		m_syntax_label = new Label(m_composite, SWT.NONE);
		m_syntax_label.setText("Syntax");
		m_combo_syntax = new Combo(m_composite, SWT.DROP_DOWN);
		m_combo_syntax.setLayoutData(gridData);
		m_combo_syntax.setItems(m_syntax);
		m_combo_syntax.addListener(SWT.KeyUp, this);
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
		m_repository_name_text.addListener(SWT.KeyUp, this);
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
		m_baseUri_text.addListener(SWT.KeyUp, this);
		m_baseUri_text.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				getContainer().updateButtons();
			}
		});
		
		m_baseUri_label.setEnabled(false);
		m_baseUri_text.setEnabled(false);
					

		setControl(m_composite);	
	}
	
	private void updateButtonVisibility(){			
		
		if(m_combo_expressivity.getText().equals(IOntology.OWL_EXPRESSIVITY)){
			
			m_syntax_label.setEnabled(false);
			m_combo_syntax.setEnabled(false);
			m_repository_name_label.setEnabled(false);
			m_repository_name_text.setEnabled(false);
			m_baseUri_label.setEnabled(false);
			m_baseUri_text.setEnabled(false);
			m_line_label.setEnabled(false);
		}
		else if(m_combo_expressivity.getText().equals(IOntology.RDFS_EXPRESSIVITY)){
			
			m_syntax_label.setEnabled(true);
			m_combo_syntax.setEnabled(true);
			m_repository_name_label.setEnabled(true);
			m_repository_name_text.setEnabled(true);
			m_baseUri_label.setEnabled(true);
			m_baseUri_text.setEnabled(true);
			m_line_label.setEnabled(true);
		}
	}
	
	protected void perparePage(String old_input, String new_input){		
		try{
			m_combo_project.remove(old_input);
		}
		catch(IllegalArgumentException e){}
		
		m_combo_project.add(new_input);
	}
	
	public HashMap<String,String> getSelectedProperties(){
		
		HashMap<String,String> properties;
		
		if(getWizard() instanceof ExploreProjectWizard){
			ExploreProjectWizard wizard = (ExploreProjectWizard)getWizard();
			properties = wizard.m_page_1.getSelectedProperties();
		}
		else{
			 properties = new HashMap<String, String>();
			 properties.put(ExploreEnvironment.PROJECT_NAME, m_combo_project.getText().trim());
		}
		
		properties.put(ExploreEnvironment.DATASOURCE_FILENAME, m_datasource_name_text.getText().trim());
		properties.put(ExploreEnvironment.ONTOLOGY_EXPRESSITIVITY, m_combo_expressivity.getText().trim());
		properties.put(ExploreEnvironment.RESOURCE_LOCATION, m_resourceLocation_text.getText().trim());
		properties.put(ExploreEnvironment.INDEX_LOCATION, m_indexLocation_text.getText().trim());
		
		if(m_combo_expressivity.getText().equals(IOntology.OWL_EXPRESSIVITY)){
			properties.put(KbEnvironment.PHYSICAL_ONTOLOGY_URI, ExploreWizardHelper.TextInputHelper.cleanInputText(m_datasourceUri_text.getText().trim()));			
		}
		else if(m_combo_expressivity.getText().equals(IOntology.RDFS_EXPRESSIVITY)){
			
			properties.put(ExploreEnvironment.REPOSITORY_NAME, m_repository_name_text.getText().trim());
			properties.put(ExploreEnvironment.ONTOLOGY_FILE_PATH, ExploreWizardHelper.TextInputHelper.cleanInputText(m_datasourceUri_text.getText().trim()));
			properties.put(ExploreEnvironment.ONTOLOGY_FILE_NAME, (new File(m_datasourceUri_text.getText().trim())).getName());
			properties.put(ExploreEnvironment.BASE_ONTOLOGY_URI, m_baseUri_text.getText().trim());
			properties.put(ExploreEnvironment.LANGUAGE, m_combo_syntax.getText().trim());
			
		}
		
		return properties;
	}
			
	@Override
	public boolean isPageComplete() {

//		check whether all fields are set	
		if((m_combo_project.getText().trim() == null) || m_combo_project.getText().trim().equals("")){
			return false;
		}
		else if((m_datasource_name_text.getText().trim() == null) || m_datasource_name_text.getText().trim().equals("")){
			return false;
		}
		else if((m_combo_expressivity.getText().trim() == null) || m_combo_expressivity.getText().trim().equals("")){
			return false;
		}
		else if(m_combo_expressivity.getText().equals(IOntology.OWL_EXPRESSIVITY)){				
			
			if((m_datasourceUri_text.getText().trim() == null) || m_datasourceUri_text.getText().trim().equals("")){
				return false;
			}
		}
		else if(m_combo_expressivity.getText().equals(IOntology.RDFS_EXPRESSIVITY)){
			
			if((m_repository_name_text.getText().trim() == null) || m_repository_name_text.getText().trim().equals("")){
				return false;
			}
			else if((m_datasourceUri_text.getText().trim() == null) || m_datasourceUri_text.getText().trim().equals("")){
				return false;
			}
//			Note, the base-Uri may be empty!
			else if((m_combo_syntax.getText().trim() == null) || m_combo_syntax.getText().trim().equals("")){
				return false;
			}
		}
		
//		check whether knowledge base exists		
		File file = new File(m_datasourceUri_text.getText().trim());
		if(!file.exists() || file.isDirectory()){
			return false;
		}
		else{
			return true;
		}
	}
		
	public void handleEvent(Event e) { 

		if (e.widget == m_combo_project){			
			if((m_combo_project.getItemCount() == 0)){
				setErrorMessage("Currently your workspace contains on project.\n" +
					"Before creating a datasource file, please create a project.");
				return;
			}
			else if((m_combo_project.getText().trim() == null) || m_combo_project.getText().trim().equals("")){
				setErrorMessage("Project not set. Please provide a valid project.");
				return;
			}
		}
		else if(e.widget == m_datasource_name_text){
			if((m_datasource_name_text.getText().trim() == null) || m_datasource_name_text.getText().trim().equals("")){
				setErrorMessage("Please provide a name for your datasource.");
				return;
			}
			else{
				m_indexLocation_text.setText(ResourcesPlugin.getWorkspace().getRoot().getLocation().removeLastSegments(1).toString()
						+ExploreEnvironment.DEFAULT_RESOURCE_LOCATION_SUFFIX+"/"+m_datasource_name_text.getText().trim().toLowerCase());
			}
		}
		else if(e.widget == m_combo_expressivity){
			if((m_combo_expressivity.getText().trim() == null) || m_combo_expressivity.getText().trim().equals("")){
				setErrorMessage("Please provide the used expressivity.");
				return;
			}
		}
		else if(e.widget == m_datasourceUri_text){
			if((m_datasourceUri_text.getText().trim() == null) || m_datasourceUri_text.getText().trim().equals("")){
				setErrorMessage("Please provide the location of your knowledge base.");
				return;
			}
			else{
				
//				check whether knowledge base exists	
				File file = new File(m_datasourceUri_text.getText().trim());
				
				if(!file.exists() || file.isDirectory()){
					setErrorMessage("Knowledge base not found, using path '"+m_datasourceUri_text.getText().trim()+"'.");
					return;
				}
			}
		}
		else if(e.widget == m_repository_name_text){
			if((m_repository_name_text.getText().trim() == null) || m_repository_name_text.getText().trim().equals("")){
				setErrorMessage("Please provide a name for your repository.");
				return;
			}
		}
		else if(e.widget == m_datasourceUri_text){
			if((m_datasourceUri_text.getText().trim() == null) || m_datasourceUri_text.getText().trim().equals("")){
				setErrorMessage("Please provide the location of your knowledge base.");
				return;
			}
		}
		else if(e.widget == m_combo_syntax){
			if((m_combo_syntax.getText().trim() == null) || m_combo_syntax.getText().trim().equals("")){
				setErrorMessage("Please provide the syntax used in your knowledge base.");
				return;
			}
		}
		setErrorMessage(null);
	}
}
