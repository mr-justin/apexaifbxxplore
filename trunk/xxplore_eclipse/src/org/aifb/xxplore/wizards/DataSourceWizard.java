package org.aifb.xxplore.wizards;


import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;

public class DataSourceWizard extends Wizard implements INewWizard {
	protected IStructuredSelection m_selection;
	protected IWorkbench m_workbench;
	protected DataSourceWizardMainPage m_mainpage;
	
	public DataSourceWizard() {
		super();
	}
	
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		m_workbench = workbench;
		setWindowTitle("New Data Source");
		setNeedsProgressMonitor(true);
	}
	
	public IWorkbench getWorkbench() {
		return m_workbench;
	}
	
	public IStructuredSelection getSelection(){
		return m_selection;
	}
	
	@Override
	public void addPages() {
		m_mainpage = new DataSourceWizardMainPage(getSelection());
		m_mainpage.setTitle("New Ontology Datasource");
		m_mainpage.setDescription("Create a new file representing parameters for a Datasource");
	}

	@Override
	public boolean performFinish() {
		IFile file = m_mainpage.createNewFile();
		if (file == null){
			return false;
		}
		
		IWorkbenchWindow dw = getWorkbench().getActiveWorkbenchWindow();
		try {
			if (dw !=null){
				IWorkbenchPage page = dw.getActivePage();
				if (page != null)
					IDE.openEditor(page,file, true);
			}
		} catch (PartInitException e) {
			//TODO what should we do with the drunken error.....!!!!
			e.printStackTrace();
		}
		
		return false;
	}
	
}
