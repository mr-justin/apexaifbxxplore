package interfaceElements
{
	import dataStructure.ArraySnippet;
	import dataStructure.Attribute;
	import dataStructure.Concept;
	import dataStructure.Couple;
	import dataStructure.Instance;
	import dataStructure.Litteral;
	import dataStructure.Relation;
	import dataStructure.ResultItem;
	import dataStructure.ResultPage;
	import dataStructure.SeeAlso;
	
	import flash.events.MouseEvent;
	import flash.net.URLRequest;
	
	import mx.collections.ArrayCollection;
	import mx.containers.Canvas;
	import mx.containers.HBox;
	import mx.containers.Panel;
	import mx.containers.Tile;
	import mx.containers.VBox;
	import mx.controls.Alert;
	import mx.controls.HRule;
	import mx.controls.Label;
	import mx.controls.LinkButton;
	import mx.controls.ProgressBar;
	import mx.controls.Spacer;
	import mx.controls.Text;
	
	/**
	 * Class to handle the display of the results
	 * @author tpenin
	 */
	public class ResultView
	{
		// Event handler
		private var eventHandler:EventHandler;
		
		// The place where results will be displayed
		private var resultArea:VBox;
		// The place where resultItems will be specifically drawn
		private var resultItemsArea:VBox;
		// The place where the page numbers will be displayed
		private var pageNumArea:HBox;
		// The result panel itself
		private var resultPanel:Panel;

		// List of the resultItem objects of the current page
		private var resultItemList:ArrayCollection;
		// List of the seeAlso buttons for the current page
		private var seeAlsoBtnList:ArrayCollection;
		// List of the arraySnippet buttons for the current page
		private var arraySnippetBtnList:ArrayCollection;
		// List of the title buttons for the current page
		private var titleBtnList:ArrayCollection;
		// The index that was pressed when dealing with a seeAlso or an ArraySnippet
		private var pressedIndex:int;
		// The page currently displayed
		private var page:int;
		
		/**
		 * Default constructor
		 * @param handler The event handler to consider
		 */
		public function ResultView(handler:EventHandler) : void {
			this.eventHandler = handler;
			this.resultArea = null;
			this.resultItemsArea = null;
			this.pageNumArea = null;
			this.resultItemList = new ArrayCollection;
			this.seeAlsoBtnList = new ArrayCollection;
			this.arraySnippetBtnList = new ArrayCollection;
			this.titleBtnList = new ArrayCollection;
			this.pressedIndex = -1;
			this.resultPanel = null;
			this.page = -1;			
		}		
		
		/**
		 * Register the UI elements that will be controlled by the ResultView object
		 * @param resArea The result area being considered
		 * @param resultItemsVBox The place to draw the result items
		 * @param pageNumHBox The place to put the page numbers
		 * @param resultPnl The result panel
		 */
		public function register(resArea:VBox, resultItemsVBox:VBox, pageNumHBox:HBox, resultPnl:Panel) : void {
			this.resultArea = resArea;
			this.resultItemsArea = resultItemsVBox;
			this.pageNumArea = pageNumHBox;
			this.resultPanel = resultPnl;
		}
		
		/**
		 * Clears the display page
		 */
		public function clear() : void {
			// Clear the display
			this.resultItemsArea.removeAllChildren();
			this.pageNumArea.removeAllChildren();
			// Clear the data
			this.resultItemList.removeAll();
			this.seeAlsoBtnList.removeAll();
			this.arraySnippetBtnList.removeAll();
			this.titleBtnList.removeAll();
			this.pressedIndex = -1;
			this.page = -1;
		}
		
		/**
		 * Hides the result page
		 */
		public function hide() : void {
			this.resultArea.visible = false;
		}
		
		/**
		 * Shows the result page
		 */
		public function show() : void {
			this.resultArea.visible = true;
		}
		
		/**
		 * Display the current result page
		 * @param p The result page to display
		 * @param nbResultPerPage The number of result items that shall be displayed per result page
		 */
		public function displayResultPage(p:ResultPage, nbResultPerPage:int) : void {
			// Clear the page
			clear();
			// Result found?
			if(p.source.resultCount == 0) {
				// Set the title of the panel
				this.resultPanel.title = "Sorry";
				// Apologies
				displayNoResults()
			} else {
				// Set the title of the panel
				this.resultPanel.title = "Browse results from " + p.source.name + " (" + p.source.resultCount.toString() + " results)";
				// Fill the result items area
				for(var i:int = 0; i < p.resultItemList.length; i++) {
					// Display each result in the layout
					if(i == p.resultItemList.length - 1) // Last element
						displayResultItem(ResultItem(p.resultItemList.getItemAt(i)), true);
					else
						displayResultItem(ResultItem(p.resultItemList.getItemAt(i)), false);
				}
				// Update the page number
				this.page = p.pageNum;
				// Fill the footer
				displayFooter(p, nbResultPerPage);
			}
		}
		
		/**
		 * Displays a small message when no results were found
		 */
		private function displayNoResults() : void {
			var sorryTxt:Text = new Text;
			sorryTxt.text = "Sorry, no result was found.";
			sorryTxt.percentWidth = 100;
			sorryTxt.setStyle("textAlign", "center");
			this.resultItemsArea.addChild(sorryTxt);
		}
		
		/**
		 * Display the page numbers
		 * @param p The result page that is currently displayed
		 * @param nbResultPerPage The number of result items displayed per page
		 */
		private function displayFooter(p:ResultPage, nbResultPerPage:int) : void {
			// 1. Get the total number of results for this source
			var totalResultNb:int = p.source.resultCount;
			// 2. Find the number of pages
			var pageCount:int = int(Math.ceil(totalResultNb*1.0 / nbResultPerPage));
			// 3. Find the lower index of the page to display
			var indexMin:int = 1;
			if(p.pageNum > 5)
				indexMin = p.pageNum - 4;
			// 4. Find the upper index of the page to displat
			var indexMax:int = pageCount;
			if(p.pageNum <= pageCount - 5)
				indexMax = p.pageNum + 4;
			// 5. Previous button
			if(p.pageNum > 1) {
				var previous:LinkButton = new LinkButton;
				previous.label = "< Previous";
				previous.addEventListener(MouseEvent.CLICK, pageClicked);
				this.pageNumArea.addChild(previous);
			}
			// 6. Page links
			for(var i:int = indexMin; i <= indexMax; i++) {
				// Create the button
				var page:LinkButton = new LinkButton;
				page.label = i.toString();
				// If it matches the current page, then disable it
				if(p.pageNum == i)
					page.enabled = false;
				// Event handling
				page.addEventListener(MouseEvent.CLICK, pageClicked);
				// Add the button
				this.pageNumArea.addChild(page);
			}
			// 7. Next button
			if(p.pageNum < pageCount) {
				var next:LinkButton = new LinkButton;
				next.label = "Next >";
				next.addEventListener(MouseEvent.CLICK, pageClicked);
				this.pageNumArea.addChild(next);
			}
		}
		
		/**
		 * Function called when the user clicks on a page number
		 */
		private function pageClicked(e:MouseEvent) : void {	
			// Get the label of the pressed button
			var label:String = LinkButton(e.target).label;
			if(label == "< Previous") {
				// Fire an event to the event handler
				this.eventHandler.pageChanged(this.page - 1);
				return;
			}
			if(label == "Next >") {
				// Fire an event to the event handler
				this.eventHandler.pageChanged(this.page + 1);			
				return;
			}
			// Get the page number
			var pageNum:int = int(label);
			// Fire an event to the event handler
			this.eventHandler.pageChanged(pageNum);	
		}
		
		/**
		 * Displays a result item in the area VBox
		 * @param r The result item to display
		 * @param last Set to true if this result item is the last of the page
		 */
		private function displayResultItem(r:ResultItem, last:Boolean) : void {
			// Result canvas
			var resultCanvas:Canvas = new Canvas;
			resultCanvas.percentWidth = 100;
			// Result layout
			var resultBox:VBox = new VBox;
			resultBox.percentWidth = 100;
			resultBox.setStyle("verticalGap", 0);
			resultBox.setStyle("paddingRight", 10);
			// Title box
			var titleBox:HBox = new HBox;
			titleBox.percentWidth = 100;
			titleBox.height = 24;
			titleBox.setStyle("verticalAlign", "middle");
			titleBox.setStyle("horizontalGap", 0);
			// Snippet box
			var snippetBox:VBox = new VBox;
			snippetBox.percentWidth = 100;
			snippetBox.setStyle("paddingLeft", 8);
			snippetBox.setStyle("paddingRight", 8);
			// See also box
			var seeAlsoBox:VBox = new VBox;
			seeAlsoBox.percentWidth = 100;
			// Array snippet box
			var arraySnippetBox:VBox = new VBox;
			arraySnippetBox.percentWidth = 100;
			// Title button
			var titleButton:LinkButton = new LinkButton;
			titleButton.label = r.title;
			titleButton.setStyle("color", "#0000ff");
			// Type label
			var typeLabel:Label = new Label;
			typeLabel.text = "(" + r.type + ")";
			// Head space
			var headSpacer:Spacer = new Spacer;
			headSpacer.percentWidth = 100;
			// Score progress bar
			var scoreBar:ProgressBar = new ProgressBar;
			scoreBar.width = 100;
			scoreBar.label = "Score: " + (int(r.score*100)).toString();
			scoreBar.labelPlacement = "left";
			scoreBar.minimum = 0.0;
			scoreBar.maximum = 1.0;
			scoreBar.indeterminate = false;
			scoreBar.mode = "manual";
			// Snippet text
			var snippetText:Text = new Text;
			snippetText.text = r.snippet;
			snippetText.percentWidth = 100;
			// See also button
			var seeAlsoBtn:LinkButton = new LinkButton;
			seeAlsoBtn.label = "See also...";
			// Array snippet button
			var arraySnippetBtn:LinkButton = new LinkButton;
			arraySnippetBtn.label = "Array snippet...";
			
			// Place the elements
			resultBox.addChild(titleBox);
			resultBox.addChild(snippetBox);
			resultBox.addChild(seeAlsoBox);
			resultBox.addChild(arraySnippetBox);
			titleBox.addChild(titleButton);
			titleBox.addChild(typeLabel);
			titleBox.addChild(headSpacer);
			titleBox.addChild(scoreBar);
			snippetBox.addChild(snippetText);
			seeAlsoBox.addChild(seeAlsoBtn);
			arraySnippetBox.addChild(arraySnippetBtn);
			resultCanvas.addChild(resultBox);
			this.resultItemsArea.addChild(resultCanvas);
			
			// Draw a line ot separate results if needed
			if(!last) {
				var resultSeparator:HRule = new HRule;
				resultSeparator.percentWidth = 100;
				this.resultItemsArea.addChild(resultSeparator);
			}
			// Update the score progress bar
			scoreBar.setProgress(r.score, 1.0);
			
			// Add the elements to the lists used to be able to handle the events
			this.resultItemList.addItem(r);
			this.seeAlsoBtnList.addItem(seeAlsoBtn);
			this.arraySnippetBtnList.addItem(arraySnippetBtn);
			this.titleBtnList.addItem(titleButton);
			
			// Event handlers to deal with the click on "seeAlso" and "ArraySnippet"
			seeAlsoBtn.addEventListener(MouseEvent.MOUSE_DOWN, seeAlsoClicked);
			arraySnippetBtn.addEventListener(MouseEvent.MOUSE_DOWN, arraySnippetClicked);
			titleButton.addEventListener(MouseEvent.MOUSE_DOWN, titleClicked);
		}
		
		/**
		 * Function called when the user clicks a seeAlso button
		 * @param e The event raised
		 */
		private function seeAlsoClicked(e:MouseEvent) : void {
			// 1. Find the index of the pressed link
			this.pressedIndex = this.seeAlsoBtnList.getItemIndex(e.target);
			// 2. Get the corresponding result item
			var r:ResultItem = ResultItem(this.resultItemList.getItemAt(this.pressedIndex));
			// 3. Fire an event to the event handler
			this.eventHandler.seeAlsoBtnClicked(r.URL);
		}
		
		/**
		 * Function called when the user clicks an arraySnippet button
		 * @param e The event raised
		 */
		private function arraySnippetClicked(e:MouseEvent) : void {
			// 1. Find the index of the pressed link
			this.pressedIndex = this.arraySnippetBtnList.getItemIndex(e.target);
			// 2. Get the corresponding result item
			var r:ResultItem = ResultItem(this.resultItemList.getItemAt(this.pressedIndex));
			// 3. Fire an event to the event handler
			this.eventHandler.arraySnippetBtnClicked(r.URL);
		}
		
		/**
		 * Displays a seeAlso element
		 */
		public function displaySeeAlso(s:SeeAlso) : void {
			// Find the see also button that was pressed
			var btn:LinkButton = LinkButton(this.seeAlsoBtnList.getItemAt(this.pressedIndex));
			// Find its parent (VBox)
			var box:VBox = VBox(btn.parent);
			// Remove the button
			box.removeAllChildren();
			// Change the padding of the box
			box.setStyle("paddingLeft", 10);
			box.setStyle("paddingRight", 10);
			box.setStyle("paddingTop", 10);
			box.setStyle("paddingBottom", 10);
			// Create a nice area to display the information
			var canvas:Canvas = new Canvas;
			canvas.percentWidth = 100;
			canvas.setStyle("borderStyle", "solid");
			canvas.setStyle("borderThickness", 1);
			canvas.setStyle("borderColor", "#c0c0c0");
			var tile:Tile = new Tile;
			tile.percentWidth = 100;
			box.addChild(canvas);
			canvas.addChild(tile);
			
			var lbl3:Label = new Label;
				lbl3.text = "See Also:";
				lbl3.setStyle("fontWeight", "bold");
				tile.addChild(lbl3);
			// If s is null, we did not get anything
			
			if(s == null) {
				var lbl:Label = new Label;
				lbl.text = "Sorry, no associated instances were found";
				tile.addChild(lbl);
			} else {
				
				// Add all the instances (to improve)
				for(var j:int = 0; j < s.getFacetList().length; j++) {
					var lbl2:Label = new Label;
					lbl2.text = Instance(s.facetList.getItemAt(j)).label;
					tile.addChild(lbl2);
				}
			}
			// Reinitialize the pressed index
			this.pressedIndex = -1;
		}
				
		
		
		/**
		 * Displays an arraySnippet element
		 */
		public function displayArraySnippet(a:ArraySnippet) : void {
			// Find the array snippet button that was pressed
			var btn:LinkButton = LinkButton(this.arraySnippetBtnList.getItemAt(this.pressedIndex));
			// Find its parent (VBox)
			var box:VBox = VBox(btn.parent);
			// Remove the button
			box.removeAllChildren();
			// Change the padding of the box
			box.setStyle("paddingLeft", 10);
			box.setStyle("paddingRight", 10);
			box.setStyle("paddingTop", 10);
			box.setStyle("paddingBottom", 10);
			// If s is null, we did not get anything
			if(a == null || (a.relation_attribute.length == 0 && a.attribute_value.length == 0 && a.classeList.length == 0)) {
				// Create a nice area to display the information
				var canvas:Canvas = new Canvas;
				canvas.percentWidth = 100;
				canvas.setStyle("borderStyle", "solid");
				canvas.setStyle("borderThickness", 1);
				canvas.setStyle("borderColor", "#c0c0c0");
				var tile:Tile = new Tile;
				tile.percentWidth = 100;
				box.addChild(canvas);
				canvas.addChild(tile);
			
				var lbl3:Label = new Label;
				lbl3.text = "Array Snippet:";
				lbl3.setStyle("fontWeight", "bold");
				tile.addChild(lbl3);

				var lbl:Label = new Label;
				lbl.text = "Sorry, no associated information was found";
				tile.addChild(lbl);
			} else {	
				// If we have relations-attribute to display
				if(a.relation_attribute.length > 0) {
					// Create a nice area to display the relations
					var canvasRelation:Canvas = new Canvas;
					canvasRelation.percentWidth = 100;
					canvasRelation.setStyle("borderStyle", "solid");
					canvasRelation.setStyle("borderThickness", 1);
					canvasRelation.setStyle("borderColor", "#c0c0c0");
					var tileRelation:Tile = new Tile;
					tileRelation.percentWidth = 100;
					canvasRelation.addChild(tileRelation);
					box.addChild(canvasRelation);
					// Label
					var lblRelation:Label = new Label;
					lblRelation.text = "Relation - Attribute:";
					lblRelation.setStyle("fontWeight", "bold");
					tileRelation.addChild(lblRelation);
					// Add all the relations (to improve)
					for(var j:int = 0; j < a.relation_attribute.length; j++) {
						var lblRelation2:Label = new Label;
						lblRelation2.text = Relation(Couple(a.relation_attribute.getItemAt(j)).element1).label + Attribute(Couple(a.relation_attribute.getItemAt(j)).element2).label;
						tileRelation.addChild(lblRelation2);
					}
				}
				// If we have attribute-value to display
				if(a.attribute_value.length > 0) {
					// Create a nice area to display the attributes
					var canvasAttribute:Canvas = new Canvas;
					canvasAttribute.percentWidth = 100;
					canvasAttribute.setStyle("borderStyle", "solid");
					canvasAttribute.setStyle("borderThickness", 1);
					canvasAttribute.setStyle("borderColor", "#c0c0c0");
					var tileAttribute:Tile = new Tile;
					tileAttribute.percentWidth = 100;
					canvasAttribute.addChild(tileAttribute);
					box.addChild(canvasAttribute);
					// Label
					var lblAttribute:Label = new Label;
					lblAttribute.text = "Attribute - Value:";
					lblAttribute.setStyle("fontWeight", "bold");
					tileAttribute.addChild(lblAttribute);
					// Add all the attributes (to improve)
					for(var j2:int = 0; j2 < a.attribute_value.length; j2++) {
						var lblAttribute2:Label = new Label;
						lblAttribute2.text = Attribute(Couple(a.attribute_value.getItemAt(j2)).element1).label + Litteral(Couple(a.attribute_value.getItemAt(j2)).element2).label;
						tileAttribute.addChild(lblAttribute2);
					}
				}
				// If we have classes to display
				if(a.classeList.length > 0) {
					// Create a nice area to display the classes
					var canvasClass:Canvas = new Canvas;
					canvasClass.percentWidth = 100;
					canvasClass.setStyle("borderStyle", "solid");
					canvasClass.setStyle("borderThickness", 1);
					canvasClass.setStyle("borderColor", "#c0c0c0");
					var tileClass:Tile = new Tile;
					tileClass.percentWidth = 100;
					canvasClass.addChild(tileClass);
					box.addChild(canvasClass);
					// Label
					var lblClass:Label = new Label;
					lblClass.text = "Classe:";
					lblClass.setStyle("fontWeight", "bold");
					tileClass.addChild(lblClass);
					// Add all the classes (to improve)
					for(var j3:int = 0; j3 < a.attribute_value.length; j3++) {
						var lblClass2:Label = new Label;
						lblClass2.text = Concept(Couple(a.classeList.getItemAt(j3)).element1).label;
						tileClass.addChild(lblClass2);
					}
				}
			}
			// Reinitialize the pressed index
			this.pressedIndex = -1;
		}
		
		/**
		 * Function called when the user clicks a title
		 * @param e The event raised
		 */
		private function titleClicked(e:MouseEvent) : void {
			// 1. Find the index of the pressed link
			var index:int = this.titleBtnList.getItemIndex(e.target);
			// 2. Get the corresponding result item
			var r:ResultItem = ResultItem(this.resultItemList.getItemAt(index));
			// 3. Open the URL in an other page of the browser
			goToURL(r.URL);
		}
		
		//open URL passed to this method in a new web page
	    private function goToURL(urlStr:String) : void {
	        var webPageURL:URLRequest = new URLRequest( urlStr );
	        flash.net.navigateToURL(webPageURL, '_blank'); 
	    }
	}
}