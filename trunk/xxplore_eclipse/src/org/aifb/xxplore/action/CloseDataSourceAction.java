package org.aifb.xxplore.action;

import org.aifb.xxplore.ExplorePlugin;
import org.aifb.xxplore.IExploreCommands;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchWindow;


public class CloseDataSourceAction  extends Action{

	private final IWorkbenchWindow window;	
	
	public CloseDataSourceAction(IWorkbenchWindow window, String label) {
		this.window = window;	
        setText(label);
        // The id is used to refer to the action in a menu or toolbar
		setId(IExploreCommands.CMD_OPEN);
        // Associate the action with a pre-defined command, to allow key bindings.
		setActionDefinitionId(IExploreCommands.CMD_OPEN);
		setImageDescriptor(ExplorePlugin.getImageDescriptor("/icons/open.gif"));
	}
	
	public void run() {
		if(window != null) {				
				window.getActivePage().closeAllPerspectives(false,true);			
		}
	}
	
}
