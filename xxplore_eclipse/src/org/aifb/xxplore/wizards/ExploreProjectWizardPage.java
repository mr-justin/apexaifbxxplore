package org.aifb.xxplore.wizards;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.aifb.xxplore.core.ExploreEnvironment;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;

public class ExploreProjectWizardPage extends WizardPage implements Listener{

	private Text m_comment_text;
	private Text m_projectname_text;
	
	private static final String PROJECT_WIZARD_DESC = "This Wizard is used to create an xxplore project!";	
	private static final String PROJECT_WIZARD_MSG = "Creation of an Exploration Project!";
	private static final String PROJECT_WIZARD_MAINPAGE_TITLE = "Specify Explore Project Data!"; 
	
	
	public ExploreProjectWizardPage(IWorkbench workbench, IStructuredSelection selection) {
		
		super(PROJECT_WIZARD_MAINPAGE_TITLE);
		super.setTitle(PROJECT_WIZARD_MAINPAGE_TITLE);
		super.setDescription(PROJECT_WIZARD_DESC);
		super.setMessage(PROJECT_WIZARD_MSG);
		
	}

	protected IProject getProjectHandle() {
		return ResourcesPlugin.getWorkspace().getRoot().getProject(m_projectname_text.getText());
	}

	public void createControl(Composite parent) {
		
		Composite comp = new Composite(parent, SWT.NONE);
		GridData gridData = new GridData(SWT.FILL, SWT.None, true, false);

		GridLayout layout = new GridLayout(2, false);
		comp.setLayout(layout);

		Label projectname_label = new Label(comp, SWT.NONE);
		projectname_label.setText("Explore Project Name");

		m_projectname_text = new Text(comp, SWT.BORDER);
		m_projectname_text.setText("explore.project");
		m_projectname_text.setLayoutData(gridData);
		m_projectname_text.addListener(SWT.KeyUp,this);
		m_projectname_text.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				getContainer().updateButtons();
			}
		});
		
		List<String> projects = Arrays.asList(ExploreWizardHelper.getWorkspaceProjects()); 
		if(projects.contains(m_projectname_text.getText().trim())){
			setErrorMessage("A project with this name already exists. Please pick another one.");
		}

		Label projectcomment_label = new Label(comp, SWT.NONE);
		projectcomment_label.setText("Comment");

		m_comment_text = new Text(comp, SWT.BORDER);
		m_comment_text.setLayoutData(gridData);
		m_comment_text.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				getContainer().updateButtons();
			}
		});

		setControl(comp);
	}
	
	@Override
	public boolean isPageComplete() {
		if((m_projectname_text.getText().trim() == null) || m_projectname_text.getText().trim().equals("")){
			return false;
		}
		else{
			
			List<String> projects = Arrays.asList(ExploreWizardHelper.getWorkspaceProjects()); 
			if(projects.contains(m_projectname_text.getText().trim())){
				return false;
			}
			else{
				return true;
			}
		}
	}
	
	public void handleEvent(Event e) { 

		if (e.widget == m_projectname_text) { 
			if ((m_projectname_text.getText() == null) ||
					m_projectname_text.getText().trim().equals("")) {
				setErrorMessage("Please provide a project name.");
				return;
			}
			else{
				List<String> projects = Arrays.asList(ExploreWizardHelper.getWorkspaceProjects()); 
				if(projects.contains(m_projectname_text.getText().trim())){
					setErrorMessage("A project with this name already exists. Please pick another one.");
					return;
				}
			}
		} 

		setErrorMessage(null);
	}
			
	public HashMap<String,String> getSelectedProperties(){
		
		HashMap<String,String> properties = new HashMap<String, String>();
		properties.put(ExploreEnvironment.PROJECT_NAME, m_projectname_text.getText().trim());
		properties.put(ExploreEnvironment.PROJECT_COMMENT, m_comment_text.getText().trim() == "" ? "" : m_comment_text.getText().trim());
		
		return properties;
	}
	
	private String m_lastSavedProjectName = "explore.project";
	
	@Override
	public IWizardPage getNextPage(){
		
		DataSourceWizardPage page = ((ExploreProjectWizard)getWizard()).m_page_2;
		
		if(isPageComplete()){
			page.perparePage(m_lastSavedProjectName,m_lastSavedProjectName = m_projectname_text.getText().trim());
		}
		
		return page;
	}
}
