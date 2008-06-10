package org.aifb.xxplore.action;

import org.aifb.xxplore.IExploreCommands;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchWindow;


public class RefreshAction  extends Action{

	private final IWorkbenchWindow window;
	
	private final String viewId;
	
	public RefreshAction(IWorkbenchWindow window, String label, String viewId) {
		this.window = window;
		this.viewId = viewId;
        setText(label);
        // The id is used to refer to the action in a menu or toolbar
		setId(IExploreCommands.CMD_REFRESH);
        // Associate the action with a pre-defined command, to allow key bindings.
		setActionDefinitionId(IExploreCommands.CMD_REFRESH);
		//setImageDescriptor(ExplorePlugin.getImageDescriptor("/icons/open.gif"));
	}
	
	public void run() {
		if(window != null) {				
			window.getActivePage().resetPerspective();			
		}
	}
	
}
