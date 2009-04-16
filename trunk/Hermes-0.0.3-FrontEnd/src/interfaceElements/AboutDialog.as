package interfaceElements
{
	import flash.display.DisplayObject;
	
	import mx.containers.TitleWindow;
	import mx.events.CloseEvent;
	import mx.managers.PopUpManager;
	
	/**
	 * This class is used to code the "About" dialog, which can be invoked statically.
	 * @author tpenin
	 */
	public class AboutDialog
	{
		// About dialog
		private static var aboutDialog:TitleWindow;
		
		/**
		 * Default constructor (not used)
		 */
		public function AboutDialog() {}

		/**
		 * Used to statically display the "About" dialog
		 * @param parent The parent object in which the dialog will be centered
		 */
		public static function showAboutDialog(parent:DisplayObject) : void {
			// Create the about dialog if it is not already done
			if(AboutDialog.aboutDialog == null)
				AboutDialog.createAboutDialog();
			// Create a popup
            PopUpManager.addPopUp(AboutDialog.aboutDialog, parent, true);
            // Center it in its parent
            PopUpManager.centerPopUp(AboutDialog.aboutDialog);
		}
		
		/**
		 * Function used to create the about dialog in the memory but does not show it
		 */
		private static function createAboutDialog() : void {
			// Create the dialog
			AboutDialog.aboutDialog = new TitleWindow;
			AboutDialog.aboutDialog.title = "About..."
			AboutDialog.aboutDialog.showCloseButton = true;
		 	AboutDialog.aboutDialog.addEventListener(CloseEvent.CLOSE, AboutDialog.closeAboutDialog);
			AboutDialog.aboutDialog.setStyle("borderColor", "#3B85FF");
			AboutDialog.aboutDialog.setStyle("borderAlpha", 0.75);
			AboutDialog.aboutDialog.setStyle("paddingLeft", 0);
			AboutDialog.aboutDialog.setStyle("paddingRight", 0);
			AboutDialog.aboutDialog.setStyle("paddingTop", 0);
			AboutDialog.aboutDialog.setStyle("paddingBottom", 0);
			// Create its content
			var content:AboutDialogContent = new AboutDialogContent;
			AboutDialog.aboutDialog.addChild(content);
		}
		
		/**
		 * Function called when closing the about dialog
		 * @param e The event raised
		 */
		private static function closeAboutDialog(e:CloseEvent) : void {
			// Hide the popup
            PopUpManager.removePopUp(AboutDialog.aboutDialog);
		}
	}
}