package interfaceElements
{
	import dataStructure.ArraySnippet;
	import dataStructure.Couple;
	import dataStructure.Facet;
	import dataStructure.ResultItem;
	import dataStructure.ResultPage;
	import dataStructure.SeeAlso;
	
	import flash.events.MouseEvent;
	import flash.net.URLRequest;
	
	import mx.collections.ArrayCollection;
	import mx.containers.Canvas;
	import mx.containers.HBox;
	import mx.containers.Panel;
	import mx.containers.VBox;
	import mx.controls.HRule;
	import mx.controls.LinkButton;
	import mx.controls.ProgressBar;
	import mx.controls.Spacer;
	import mx.controls.Text;
	import mx.controls.TextArea;
	
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
			resultCanvas.horizontalScrollPolicy = "off";
			// Result layout
			var resultBox:VBox = new VBox;
			resultBox.percentWidth = 100;
			resultBox.setStyle("verticalGap", 0);
			resultBox.setStyle("verticalGap", 0);
			
			// Title box
			var titleBox:HBox = new HBox;
			titleBox.percentWidth = 100;
			titleBox.height = 24;
			titleBox.setStyle("verticalAlign", "middle");
			titleBox.setStyle("horizontalGap", 0);
			titleBox.setStyle("paddingRight", 10);
			// Title button
			var titleButton:LinkButton = new LinkButton;
			titleButton.label = r.title;
			titleButton.setStyle("color", "#0000ff");
			// Head space
			var headSpacer:Spacer = new Spacer;
			headSpacer.percentWidth = 100;
			// Score progress bar
			var scoreBar:ProgressBar = new ProgressBar;
			scoreBar.width = 120;
			scoreBar.label = "Score: " + (int(r.score*100)).toString();
			scoreBar.labelPlacement = "left";
			scoreBar.minimum = 0.0;
			scoreBar.maximum = 1.0;
			scoreBar.indeterminate = false;
			scoreBar.mode = "manual";
			// Update the score progress bar
			scoreBar.setProgress(r.score, 1.0);
			
			// Array snippet box
			var arraySnippetBox:VBox = new VBox;
			arraySnippetBox.percentWidth = 100;
			// Array snippet button
			var arraySnippetBtn:LinkButton = new LinkButton;
			arraySnippetBtn.label = "Description...";
			
			// See also box
			var seeAlsoBox:VBox = new VBox;
			seeAlsoBox.percentWidth = 100;
			// See also button
			var seeAlsoBtn:LinkButton = new LinkButton;
			seeAlsoBtn.label = "Same As...";
			
			// URI of the result item
			var URIText:Text = new Text;
			URIText.setStyle("color", "#2a7fff");
			URIText.setStyle("paddingLeft", 10);
			URIText.text = r.URL;
			
			// Place the elements
			resultBox.addChild(titleBox);
			///////////////////////////////// Temp
			//if(r.snippet != "") {
			//	var snip:TextArea = new TextArea;
			//	snip.text = r.snippet;
			//	snip.percentWidth = 100;
			//	resultBox.addChild(snip);
			//}
			////////////////////////////////////////
			resultBox.addChild(arraySnippetBox);
			resultBox.addChild(seeAlsoBox);
			resultBox.addChild(URIText);
			titleBox.addChild(titleButton);
			titleBox.addChild(headSpacer);
			titleBox.addChild(scoreBar);
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
			box.setStyle("paddingTop", 5);
			box.setStyle("paddingBottom", 5);
			box.setStyle("verticalGap", 0);
			// Create a nice area to display the information
			var canvas:Canvas = new Canvas;
			canvas.percentWidth = 100;
			canvas.setStyle("backgroundColor", "#ececec");
			canvas.horizontalScrollPolicy = "off";
			box.addChild(canvas);
			// Create a VBox in the canvas
			var canvasVLayout:VBox = new VBox;
			canvasVLayout.setStyle("verticalGap", 0);
			canvas.addChild(canvasVLayout);
			
			// If s is null, we did not get anything
			if(s == null) {
				var sorry:LinkButton = new LinkButton;
				sorry.label = "Sorry, no associated instance was found";
				sorry.enabled = false;
				sorry.setStyle("disabledColor", "#000000");
				sorry.setStyle("fontStyle", "italic");
				sorry.setStyle("fontWeight", "normal");
				canvasVLayout.addChild(sorry);
			} else {
				// Create 2 columns
				var columnBox1:HBox = new HBox;
				columnBox1.percentWidth = 100;
				canvasVLayout.addChild(columnBox1);
				// Label
				var titleGroup1:LinkButton = new LinkButton;
				titleGroup1.label = "See Also";
				titleGroup1.width = 100;
				titleGroup1.enabled = false;
				titleGroup1.setStyle("disabledColor", "#000000");
				titleGroup1.setStyle("textAlign", "left");
				columnBox1.addChild(titleGroup1);
				// Data
				var dataVBox1:VBox = new VBox;
				dataVBox1.percentWidth = 100;
				dataVBox1.setStyle("verticalGap", 0);
				columnBox1.addChild(dataVBox1);
				// Add all the instances
				for(var j:int = 0; j < s.facetList.length; j++) {			
					var instanceLinkBtn:LinkButton = new LinkButton;
					instanceLinkBtn.label = Facet(s.facetList.getItemAt(j)).label;
					instanceLinkBtn.toolTip = Facet(s.facetList.getItemAt(j)).displayURI;
					// If the URI is non-clickable, disable the button
					if(instanceLinkBtn.toolTip == "") {
						instanceLinkBtn.enabled = false;
						instanceLinkBtn.setStyle("disabledColor", "#000000");
						instanceLinkBtn.setStyle("fontWeight", "normal");
					} else {
						instanceLinkBtn.label = Facet(s.facetList.getItemAt(j)).displayURI;
						instanceLinkBtn.setStyle("color", "#0000ff");
					}
					dataVBox1.addChild(instanceLinkBtn);
				}
			}
			// Button to hide
			btn.removeEventListener(MouseEvent.MOUSE_DOWN, seeAlsoClicked);
			btn.setStyle("fontWeight", "normal");
			btn.label = "(Collapse)";
			btn.addEventListener(MouseEvent.MOUSE_DOWN, hideSeeAlso);
			canvasVLayout.addChild(btn);
			// Reinitialize the pressed index
			this.pressedIndex = -1;
		}
		
		/**
		 * Function called when the user wants to collapse a seeAlso
		 * @param The event raised
		 */	
		private function hideSeeAlso(e:MouseEvent) : void {
			// 1. Find the index of the pressed link
			this.pressedIndex = this.seeAlsoBtnList.getItemIndex(e.target);
			// Find the see also button that was pressed
			var btn:LinkButton = LinkButton(this.seeAlsoBtnList.getItemAt(this.pressedIndex));
			// Find its parent (VBox)
			var box:VBox = VBox(btn.parent.parent.parent);
			// Remove the button
			box.removeAllChildren();
			box.setStyle("paddingLeft", 0);
			box.setStyle("paddingRight", 0);
			box.setStyle("paddingTop", 0);
			box.setStyle("paddingBottom", 0);
			// Change the label
			btn.label = "Same as...";
			btn.removeEventListener(MouseEvent.MOUSE_DOWN, hideSeeAlso);
			btn.setStyle("fontWeight", "bold");
			btn.addEventListener(MouseEvent.MOUSE_DOWN, seeAlsoClicked);
			box.addChild(btn);
		}
		
		/**
		 * Function called when the user wants to collapse an arraySnippet
		 * @param The event raised
		 */	
		private function hideArraySnippet(e:MouseEvent) : void {
			// 1. Find the index of the pressed link
			this.pressedIndex = this.arraySnippetBtnList.getItemIndex(e.target);
			// Find the see also button that was pressed
			var btn:LinkButton = LinkButton(this.arraySnippetBtnList.getItemAt(this.pressedIndex));
			// Find its parent (VBox)
			var box:VBox = VBox(btn.parent.parent.parent);
			// Remove the button
			box.removeAllChildren();
			box.setStyle("paddingLeft", 0);
			box.setStyle("paddingRight", 0);
			box.setStyle("paddingTop", 0);
			box.setStyle("paddingBottom", 0);
			// Change the label
			btn.label = "Description...";
			btn.removeEventListener(MouseEvent.MOUSE_DOWN, hideArraySnippet);
			btn.setStyle("fontWeight", "bold");
			btn.addEventListener(MouseEvent.MOUSE_DOWN, arraySnippetClicked);
			box.addChild(btn);
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
			box.setStyle("paddingTop", 5);
			box.setStyle("paddingBottom", 5);
			box.setStyle("verticalGap", 0);
			// Create a nice area to display the information
			var canvas:Canvas = new Canvas;
			canvas.percentWidth = 100;
			canvas.setStyle("backgroundColor", "#ececec");
			canvas.horizontalScrollPolicy = "off";
			box.addChild(canvas);
			// Create a VBox in the canvas
			var canvasVLayout:VBox = new VBox;
			canvasVLayout.setStyle("verticalGap", 0);
			canvas.addChild(canvasVLayout);
			
			// If a is null, we did not get anything
			if(a == null || (a.relation_attribute.length == 0 && a.attribute_value.length == 0 && a.classeList.length == 0)) {
				var sorry:LinkButton = new LinkButton;
				sorry.label = "Sorry, no description was found";
				sorry.enabled = false;
				sorry.setStyle("disabledColor", "#000000");
				sorry.setStyle("fontStyle", "italic");
				sorry.setStyle("fontWeight", "normal");
				canvasVLayout.addChild(sorry);
			} else {
				// If we have classes to display
				if(a.classeList.length > 0) {
					// Create 2 columns
					var columnBox1:HBox = new HBox;
					columnBox1.percentWidth = 100;
					canvasVLayout.addChild(columnBox1);
					// Label
					var titleGroup1:LinkButton = new LinkButton;
					titleGroup1.label = "Class";
					titleGroup1.width = 100;
					titleGroup1.enabled = false;
					titleGroup1.setStyle("disabledColor", "#000000");
					titleGroup1.setStyle("textAlign", "left");
					columnBox1.addChild(titleGroup1);
					// Data
					var dataVBox1:HBox = new HBox;
					dataVBox1.percentWidth = 100;
					dataVBox1.setStyle("verticalAlign", "middle");
					columnBox1.addChild(dataVBox1);
					// Add all the classes
					for(var j3:int = 0; j3 < a.classeList.length; j3++) {			
						var classLinkBtn:LinkButton = new LinkButton;
						classLinkBtn.label = Facet(a.classeList.getItemAt(j3)).label;
						classLinkBtn.toolTip = Facet(a.classeList.getItemAt(j3)).URI;
						classLinkBtn.enabled = false;
						classLinkBtn.setStyle("fontWeight", "normal");
						classLinkBtn.setStyle("disabledColor", "#000000");
						dataVBox1.addChild(classLinkBtn);
					}
				}
				// If we have relations-attribute or attribute-values to display
				if(a.relation_attribute.length > 0 || a.attribute_value.length > 0) {
					// Create 2 columns
					var columnBox2:HBox = new HBox;
					columnBox2.percentWidth = 100;
					canvasVLayout.addChild(columnBox2);
					// Label
					var titleGroup2:LinkButton = new LinkButton;
					titleGroup2.label = "Description";
					titleGroup2.width = 100;
					titleGroup2.enabled = false;
					titleGroup2.setStyle("disabledColor", "#000000");
					titleGroup2.setStyle("textAlign", "left");
					columnBox2.addChild(titleGroup2);
					// Data
					var dataVBox2:VBox = new VBox;
					dataVBox2.percentWidth = 100;
					dataVBox2.setStyle("verticalGap", 0);
					columnBox2.addChild(dataVBox2);
					// Add all the attributes
					for(var j2:int = 0; j2 < a.attribute_value.length; j2++) {
						var attributeBox:HBox = new HBox;
						attributeBox.setStyle("horizontalGap", 0);			
						var attributeLinkBtn:LinkButton = new LinkButton;
						attributeLinkBtn.label = Facet(Couple(a.attribute_value.getItemAt(j2)).element1).label + ":";
						attributeLinkBtn.toolTip = Facet(Couple(a.attribute_value.getItemAt(j2)).element1).URI;
						attributeLinkBtn.setStyle("paddingRight", 0);
						attributeLinkBtn.enabled = false;
						attributeLinkBtn.setStyle("disabledColor", "#000000");
						attributeLinkBtn.setStyle("fontWeight", "normal");
						var attributeLinkBtn2:LinkButton = new LinkButton;
						attributeLinkBtn2.label = Facet(Couple(a.attribute_value.getItemAt(j2)).element2).label;
						attributeLinkBtn2.toolTip = Facet(Couple(a.attribute_value.getItemAt(j2)).element2).displayURI;
						attributeLinkBtn2.addEventListener(MouseEvent.MOUSE_DOWN, linkClicked);
						attributeLinkBtn2.setStyle("paddingLeft", 0);
						// If the URI is non-clickable, disable the button
						if(attributeLinkBtn2.toolTip == "") {
							attributeLinkBtn2.enabled = false;
							attributeLinkBtn2.setStyle("disabledColor", "#000000");
							attributeLinkBtn2.setStyle("fontWeight", "normal");
						} else {
							attributeLinkBtn2.label = Facet(Couple(a.relation_attribute.getItemAt(j)).element2).displayURI;
							attributeLinkBtn2.setStyle("color", "#0000ff");
						}
						if(attributeLinkBtn.label != "wikilink:" && attributeLinkBtn.label != "reference:" && attributeLinkBtn.label != "relatedInstance:") {
							attributeBox.addChild(attributeLinkBtn);
							attributeBox.addChild(attributeLinkBtn2);
							dataVBox2.addChild(attributeBox);
						}
					}
					// Add all the relations
					for(var j:int = 0; j < a.relation_attribute.length; j++) {
						var relationBox:HBox = new HBox;
						relationBox.setStyle("horizontalGap", 0);			
						var relationLinkBtn:LinkButton = new LinkButton;
						relationLinkBtn.label = Facet(Couple(a.relation_attribute.getItemAt(j)).element1).label + ":";
						relationLinkBtn.toolTip = Facet(Couple(a.relation_attribute.getItemAt(j)).element1).URI;
						relationLinkBtn.setStyle("paddingRight", 0);
						relationLinkBtn.enabled = false;
						relationLinkBtn.setStyle("disabledColor", "#000000");
						relationLinkBtn.setStyle("fontWeight", "normal");
						var relationLinkBtn2:LinkButton = new LinkButton;
						relationLinkBtn2.label = Facet(Couple(a.relation_attribute.getItemAt(j)).element2).label;
						relationLinkBtn2.toolTip = Facet(Couple(a.relation_attribute.getItemAt(j)).element2).displayURI;
						relationLinkBtn2.addEventListener(MouseEvent.MOUSE_DOWN, linkClicked);
						relationLinkBtn2.setStyle("paddingLeft", 0);
						// If the URI is non-clickable, disable the button
						if(relationLinkBtn2.toolTip == "") {
							relationLinkBtn2.enabled = false;
							relationLinkBtn2.setStyle("disabledColor", "#000000");
							relationLinkBtn2.setStyle("fontWeight", "normal");
						} else {
							relationLinkBtn2.label = Facet(Couple(a.relation_attribute.getItemAt(j)).element2).displayURI;
							relationLinkBtn2.setStyle("color", "#0000ff");
						}
						if(relationLinkBtn.label != "wikilink:" && relationLinkBtn.label != "reference:" && relationLinkBtn.label != "relatedInstance:") {
							relationBox.addChild(relationLinkBtn);
							relationBox.addChild(relationLinkBtn2);
							dataVBox2.addChild(relationBox);
						}
					}
				}
				
			}
			// Button to hide
			btn.removeEventListener(MouseEvent.MOUSE_DOWN, arraySnippetClicked);
			btn.setStyle("fontWeight", "normal");
			btn.label = "(Collapse)";
			btn.addEventListener(MouseEvent.MOUSE_DOWN, hideArraySnippet);
			canvasVLayout.addChild(btn);
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
			// 3. Remove "<" and ">" from the URL
			var url:String = r.URL;
			if(url.length > 2 && url.charAt(0) == "<" && url.charAt(url.length - 1) == ">")
				url = url.substr(1, url.length - 2);
			// 4. Open the URL in an other page of the browser
			goToURL(url);
		}
		
		/**
		 * Function called when the user clicks a link
		 * @param e The event raised
		 */
		private function linkClicked(e:MouseEvent) : void {
			// Open the URL in an other page of the browser
			goToURL(LinkButton(e.target).toolTip);
		}
		
		//open URL passed to this method in a new web page
	    private function goToURL(urlStr:String) : void {
	        var webPageURL:URLRequest = new URLRequest( urlStr );
	        flash.net.navigateToURL(webPageURL, '_blank'); 
	    }
	}
}