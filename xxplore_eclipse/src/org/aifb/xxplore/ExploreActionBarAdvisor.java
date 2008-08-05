package org.aifb.xxplore;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;

/**
 * An action bar advisor is responsible for creating, adding, and disposing of
 * the actions added to a workbench window. Each window will be populated with
 * new actions.
 */
public class ExploreActionBarAdvisor extends ActionBarAdvisor {

	// Actions - important to allocate these only in makeActions, and then use
	// them
	// in the fill methods. This ensures that the actions aren't recreated
	// when fillActionBars is called with FILL_PROXY.
	private IWorkbenchAction exitAction;
//	private IWorkbenchAction openAction;
	private IWorkbenchAction refreshAction;
	private IWorkbenchAction closeAction;

	public ExploreActionBarAdvisor(IActionBarConfigurer configurer) {
		super(configurer);
	}

	protected void makeActions(final IWorkbenchWindow window) {
		// Creates the actions and registers them.
		// Registering is needed to ensure that key bindings work.
		// The corresponding commands keybindings are defined in the plugin.xml
		// file.
		// Registering also provides automatic disposal of the actions when
		// the window is closed.
//		openAction = ActionFactory.OPEN_PERSPECTIVE_DIALOG.create(window);
		refreshAction = ActionFactory.REFRESH.create(window);
		closeAction = ActionFactory.CLOSE.create(window);
		exitAction = ActionFactory.QUIT.create(window);
		register(exitAction);
	}

	protected void fillMenuBar(IMenuManager menuBar) {
		MenuManager fileMenu = new MenuManager("&File",
				IWorkbenchActionConstants.M_FILE);
		menuBar.add(fileMenu);
//		fileMenu.add(openAction);
		fileMenu.add(refreshAction);
		fileMenu.add(closeAction);
		fileMenu.add(exitAction);
		
//		MenuManager helpMenu = new MenuManager("&Help",
//				IWorkbenchActionConstants.M_HELP);
//		menuBar.add(helpMenu);
	}

}
