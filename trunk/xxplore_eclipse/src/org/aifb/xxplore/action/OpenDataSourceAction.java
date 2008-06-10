package org.aifb.xxplore.action;

import org.aifb.xxplore.ExplorePlugin;
import org.aifb.xxplore.IExploreCommands;
import org.aifb.xxplore.wizards.ExploreProjectWizard;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;


public class OpenDataSourceAction  extends Action{

	private final IWorkbenchWindow window;
	private int instanceNum = 0;
	private final String viewId;
	
	public OpenDataSourceAction(IWorkbenchWindow window, String label, String viewId) {
		this.window = window;
		this.viewId = viewId;
        setText(label);
        // The id is used to refer to the action in a menu or toolbar
		setId(IExploreCommands.CMD_OPEN);
        // Associate the action with a pre-defined command, to allow key bindings.
		setActionDefinitionId(IExploreCommands.CMD_OPEN);
		setImageDescriptor(ExplorePlugin.getImageDescriptor("/icons/open.gif"));
	}
	
	public void run() {
		if(window != null) {	
			try {
				
				ExploreProjectWizard wiz = new ExploreProjectWizard();
				WizardDialog dialog = new WizardDialog(window.getShell(),wiz);
				dialog.setPageSize(300,100);
				if (dialog.open()== WizardDialog.OK){		
					System.out.println("KSSLSLSLLS");
					window.getActivePage().showView(viewId, Integer.toString(instanceNum++), IWorkbenchPage.VIEW_ACTIVATE);
				}
			} catch (PartInitException e) {
				MessageDialog.openError(window.getShell(), "Error", "Error opening view:" + e.getMessage());
			}
		}
	}
	
}
