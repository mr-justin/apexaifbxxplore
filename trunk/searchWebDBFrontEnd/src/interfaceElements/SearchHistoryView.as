package interfaceElements
{
	import flash.events.Event;
	import flash.events.MouseEvent;
	
	import mx.collections.ArrayCollection;
	import mx.containers.HBox;
	import mx.controls.Button;
	import mx.controls.Image;
	
	/**
	 * Class to handle the search history bar
	 * @author tpenin
	 */
	public class SearchHistoryView
	{
		// Part of the history bar that can be modified
		private var historyArea:HBox;
		// List of the HBox allowing the display of the couples Button/EraseButton
		private var historyHBoxList:ArrayCollection;
		// Icon of the erase button
		private var eraseIcon:Class;
		// Event handler
		private var eventHandler:EventHandler;
		// List of history element type ("keyword", "relation", "concept", "relationSuggestion", "conceptSuggestion")
		private var elementTypeList:ArrayCollection;
		
		/**
		 * Default constructor
		 * @param icon The icon to use for erase buttons
		 * @param handler The event handler to consider
		 */
		public function SearchHistoryView(icon:Class, handler:EventHandler) : void {
			this.historyArea = new HBox;
			this.historyHBoxList = new ArrayCollection;
			this.eraseIcon = icon;
			this.elementTypeList = new ArrayCollection;
			this.eventHandler = handler;
		}
		
		/**
		 * Register the area that will be controlled by the history view
		 * @param historyBarArea The HBox being considered
		 */
		public function register(historyBarArea:HBox) : void {
			this.historyArea = historyBarArea;
		}
		
		/**
		 * Clear the history bar
		 */
		public function clear() : void {
			// Remove all children
			this.historyArea.removeAllChildren();
			// Clear the history
			this.historyHBoxList.removeAll();
			// There is no element any more
			this.elementTypeList.removeAll();
		}
		
		/**
		 * Adds a keyword facet to the history bar
		 * @param text The text to display on the button
		 * @param sourceName The name of the associated source
		 */
		public function addKeywordFacet(text:String, sourceName:String) : void {
			this.elementTypeList.addItem("keyword");
			// Find if we need to put an arrow and pass the parameters to the general function
			if(this.elementTypeList.length > 2 && (this.elementTypeList.getItemAt(this.elementTypeList.length - 2) == "relation"
			   || this.elementTypeList.getItemAt(this.elementTypeList.length - 2) == "relationSuggestion"))
				addGenericFacet(text, sourceName, "#0080ff", true);
			else
				addGenericFacet(text, sourceName, "#0080ff", false);	
		}
		
		/**
		 * Adds a concept facet to the history bar
		 * @param text The text to display on the button
		 * @param sourceName The name of the associated source
		 */
		public function addConceptFacet(text:String, sourceName:String) : void {
			this.elementTypeList.addItem("concept");
			// Find if we need to put an arrow and pass the parameters to the general function
			if(this.elementTypeList.length > 2 && (this.elementTypeList.getItemAt(this.elementTypeList.length - 2) == "relation"
			   || this.elementTypeList.getItemAt(this.elementTypeList.length - 2) == "relationSuggestion"))
				addGenericFacet(text, sourceName, "#a1a1a1", true);
			else			
				addGenericFacet(text, sourceName, "#a1a1a1", false);
		}
		
		/**
		 * Adds a concept suggestion facet to the history bar
		 * @param text The text to display on the button
		 * @param sourceName The name of the associated source
		 */
		public function addConceptSuggestionFacet(text:String, sourceName:String) : void {
			this.elementTypeList.addItem("conceptSuggestion");
			// Find if we need to put an arrow and pass the parameters to the general function
			if(this.elementTypeList.length > 2 && (this.elementTypeList.getItemAt(this.elementTypeList.length - 2) == "relation"
			   || this.elementTypeList.getItemAt(this.elementTypeList.length - 2) == "relationSuggestion"))
				addGenericFacet(text, sourceName, "#a1a1a1", true);
			else			
				addGenericFacet(text, sourceName, "#a1a1a1", false);
		}
		
		/**
		 * Adds a relation facet to the history bar
		 * @param text The text to display on the button
		 * @param sourceName The name of the associated source
		 */
		public function addRelationFacet(text:String, sourceName:String) : void {
			// Pass the parameters to the general function
			this.elementTypeList.addItem("relation");
			addGenericFacet(text, sourceName, "#0047a8", true);
		}
		
		/**
		 * Adds a relation suggestion facet to the history bar
		 * @param text The text to display on the button
		 * @param sourceName The name of the associated source
		 */
		public function addRelationSuggestionFacet(text:String, sourceName:String) : void {
			// Pass the parameters to the general function
			this.elementTypeList.addItem("relationSuggestion");
			addGenericFacet(text, sourceName, "#0047a8", true);
		}
		
		/**
		 * A generique function called to add all the sorts of buttons
		 * @param text The text to display on the button
		 * @param sourceName The name of the associated source
		 * @param color A string representing the hexadecimal value of the color
		 * @param arrow Set to true if an arrow should be placed
		 */
		private function addGenericFacet(text:String, sourceName:String, color:String, arrow:Boolean) : void {
			// 1. Create the button
			var btn:Button = new Button;
			// 2. Set its properties
			btn.label = text;
			btn.enabled = false;
			btn.setStyle("disabledColor", "#000000");
			btn.setStyle("borderColor", "#000000");
			btn.setStyle("fillColors", ["#ffffff", color]);
			btn.setStyle("alpha", 1.0);
			btn.setStyle("fillAlphas", [1, 1]);
			btn.toolTip = sourceName;
			// 3. Adapt the bar for the new button
			adaptBarToNewcomers(arrow);
			// 4. Create the erase button
			var eraseBtn:Button = new Button;
			// 5. Initialize its properties
			eraseBtn.width = 22;
			eraseBtn.setStyle("icon", this.eraseIcon);
			eraseBtn.addEventListener(MouseEvent.CLICK, historyBack);
			if(this.historyHBoxList.length > 1)
				eraseBtn.toolTip = "Click to undo the last query refinement";
			else
				eraseBtn.toolTip = "Click to modify your initial search";
			// 6. Create a layout and gather both button
			var btnLayout:HBox = new HBox;
			btnLayout.setStyle("horizontalGap", "0");
			btnLayout.addChild(btn);
			btnLayout.addChild(eraseBtn);
			// 7. Add this layout to the internal list
			this.historyHBoxList.addItem(btnLayout);
			// 8. Add this layout to the search history bar
			this.historyArea.addChild(btnLayout);
		}
		
		/**
		 * Clean the bar to be able to draw the attention on the new button and not the old ones
		 * @param arrow Set to true if an arrow should be placed
		 */
		private function adaptBarToNewcomers(arrow:Boolean) : void {
			// If there was a previous facet
			if(this.historyHBoxList.length > 0) {
				// Remove the EraseButton that was with the last facet
				var btnHBox:HBox = HBox(this.historyHBoxList.getItemAt(this.historyHBoxList.length - 1));
				btnHBox.removeChildAt(1);
				// Add an arrow after the last button if needed
				if(arrow) {
					var img:Image = new Image;
					img.source = "../Pictures/step2.png";					
					this.historyArea.addChild(img);
				}
			}
		}
		
		/**
		 * Function called by the event listener associated to the erase buttons
		 * @param event
		 */
		private function historyBack(event:Event) : void {
			// 1. Find the HBox containing the button
			var btnHBox:HBox = HBox(this.historyHBoxList.getItemAt(this.historyHBoxList.length - 1));
			// 2. Remove the button and its associated erase button
			this.historyArea.removeChild(btnHBox);
			// 3. Remove it from the list
			this.historyHBoxList.removeItemAt(this.historyHBoxList.length - 1);	
			// If this was not the only button
			if(this.historyHBoxList.length > 0) {
				// 4. Remove the last arrow if it exists
				if(this.elementTypeList.getItemAt(this.elementTypeList.length - 1) == "relation"
				   || this.elementTypeList.getItemAt(this.elementTypeList.length - 1) == "relationSuggestion")
					this.historyArea.removeChildAt(this.historyArea.getChildren().length - 1);
				else
					if(this.historyHBoxList.length > 1 && (this.elementTypeList.getItemAt(this.elementTypeList.length - 2) == "relation"
					   || this.elementTypeList.getItemAt(this.elementTypeList.length - 2) == "relationSuggestion"))
						this.historyArea.removeChildAt(this.historyArea.getChildren().length - 1);
				// 5. Remove the element
				this.elementTypeList.removeItemAt(this.elementTypeList.length - 1);
				// 6. Get the HBox with the new final button
				btnHBox = HBox(this.historyHBoxList.getItemAt(this.historyHBoxList.length - 1));
				// 7. Create the erase button
				var eraseBtn:Button = new Button;
				// 8. Initialize its properties
				eraseBtn.width = 22;
				eraseBtn.setStyle("icon", this.eraseIcon);
				eraseBtn.addEventListener(MouseEvent.CLICK, historyBack);
				if(this.historyHBoxList.length > 1)
					eraseBtn.toolTip = "Click to undo the last query refinement";
				else
					eraseBtn.toolTip = "Click to modify your initial search";
				// 9. Add it to the layout
				btnHBox.addChild(eraseBtn);
				// 10. Fire an event to the event handler
				this.eventHandler.historyBack();
			} else {
				// Go to the query composer panel without clearing it to modify the query
				this.eventHandler.newSearchButtonOnClick();
			}
		}
		
		/**
		 * Prepare a keyword string for display in the history bar
		 * @param s The string to consider
		 */
	/*	private function prepareStringForDisplay(s:String) : String {
			// Temporary string
			var acc:String = "";
			// Browse the string
			for(var i:int = 0; i < s.length; i++) {
				// Get the character
				var str:String = s.charAt(i);
				// If it does not match an accepted character, we drop it
				var pattern:RegExp = /[A-Za-z]/;
				if(str != " " && str != "\"" && !pattern.test(str))
					continue;
				// If it is a space
				if(str == " ") {
					if(acc.length > 0 && acc.charAt(acc.length - 1) != "") {
						acc += " ";
					}
					continue;
				}
				// Else, add the char
				acc += str;
			}	
			return acc;
		}*/
	}
}