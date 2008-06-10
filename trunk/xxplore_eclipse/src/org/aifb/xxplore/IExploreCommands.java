package org.aifb.xxplore;

	/**
	 * Interface defining the application's command IDs.
	 * Key bindings can be defined for specific commands.
	 * To associate an action with a command, use IAction.setActionDefinitionId(commandId).
	 *
	 * @see org.eclipse.jface.action.IAction#setActionDefinitionId(String)
	 */
public interface IExploreCommands {

	    public static final String CMD_OPEN = "org.aifb.xxplore.opendatasource";	    
	    public static final String CMD_OPEN_MESSAGE = "org.aifb.xxplore.opendatasource";
	    public static final String CMD_CLOSE = "org.aifb.xxplore.closedatasource";
	    public static final String CMD_CLOSE_MESSAGE = "org.aifb.xxplore.closedatasource";
	    public static final String CMD_REFRESH = "org.aifb.xxplore.refresh";
	    public static final String CMD_REFRESH_MESSAGE = "org.aifb.xxplore.refresh";
	
	
}
