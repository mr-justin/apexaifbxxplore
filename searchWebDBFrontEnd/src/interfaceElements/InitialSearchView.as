package interfaceElements
{
	import dataStructure.Concept;
	import dataStructure.Facet;
	import dataStructure.GraphEdge;
	import dataStructure.Litteral;
	import dataStructure.QueryGraph;
	
	import flash.events.Event;
	import flash.events.MouseEvent;
	import flash.net.URLLoader;
	import flash.net.URLRequest;
	
	import interfaceBackend.Functions;
	
	import mx.collections.ArrayCollection;
	import mx.containers.ControlBar;
	import mx.containers.HBox;
	import mx.containers.Panel;
	import mx.containers.VBox;
	import mx.controls.Alert;
	import mx.controls.Button;
	import mx.controls.ComboBox;
	import mx.controls.DataGrid;
	import mx.controls.Label;
	import mx.controls.Spacer;
	import mx.controls.TextArea;
	import mx.controls.TextInput;
	import mx.events.FlexEvent;
	import mx.events.ListEvent;
	import mx.managers.PopUpManager;
	import mx.utils.ObjectUtil;
	
	import org.un.cava.birdeye.ravis.graphLayout.data.IGraph;
	import org.un.cava.birdeye.ravis.graphLayout.layout.ILayoutAlgorithm;
	import org.un.cava.birdeye.ravis.graphLayout.visual.IEdgeRenderer;
	import org.un.cava.birdeye.ravis.graphLayout.visual.IVisualNode;
	
	/**
	 * Class to handle the initial search view
	 * @author tpenin
	 */
	public class InitialSearchView
	{
		// Icon
		private var eraseIcon:Class;
		// Event handler
		private var eventHandler:EventHandler;
		
		// List of the query disambiguation graphs
		private var queryGraphList:ArrayCollection;
		// List of the variable of the graph
		private var variableList:ArrayCollection;
		// List of the names of the variables of the graph
		private var variableNameList:ArrayCollection;
		// List of the letters of the variables of the graph
		private var variableLetterList:ArrayCollection;
		// List of URIs of the variables of the graph
		private var variableURIList:ArrayCollection;
		// The index of the graph currently selected
		private var currentIndex:int;
		// Text of the query
		private var queryTxt:String;
		
		// Keyword input field
		private var initialKeywordInput:TextInput;
		// Button to start the disambiguation process
		private var disambiguateButton:Button;
		// Button to see the previous query graph candidate
		private var previousGraphButton:Button;
		// Button to see the next query graph candidate 
		private var nextGraphButton:Button; 
		// Combobox to select the target variable
		private var variableComboBox:ComboBox;
		// Button to start a search using the current query graph 
		private var graphSearchButton:Button; 
		// Button to start a search using the keywords
		private var keywordSearchButton:Button;
		// Zone to display the graph
		private var graphDrawingZone:VBox;
		// Data grid to display the mapping information
		private var mappingDataGrid:DataGrid;
		
		// graph datastructure object
		private var graph:IGraph;
		// active layouter
		private var layouter:ILayoutAlgorithm;
		// edge renderer
		private var selectedEdgeRenderer:IEdgeRenderer;
		// root node to start with
		private var startRoot:IVisualNode;
		// this is used to display the number of visible items
		private var itemCount:int = 0;
		// label to give a feedback to the user
		private var feedBackLabel:Label;
		
		// Button to call the popup
		private var exampleQueriesButton:Button
		// Example query popup
		private var examplePopup:Panel;
		// XML loader for the example queries
		private var xmlLoader:URLLoader;
		// List of example queries keywords
		private var exampleQueriesKeywords:ArrayCollection;
		// List of example queries description
		private var exampleQueriesDescription:ArrayCollection;
		// Combo box to display the keywords of the query
		private var keywordCombo:ComboBox;
		// Text area to display the description of the query
		private var txtArea:TextArea;
		// Ok button
		private var okButton:Button;
		
		/**
		 * Default constructor
		 * @param icon The icon to use for erase buttons
		 * @param handler The event handler to consider
		 */
		public function InitialSearchView(icon:Class, handler:EventHandler) {
			this.eraseIcon = icon;
			this.eventHandler = handler;
			this.initialKeywordInput = null;
			this.disambiguateButton = null;
			this.previousGraphButton = null;
			this.nextGraphButton = null;
			this.variableComboBox = null;
			this.graphSearchButton = null;
			this.keywordSearchButton = null;
			this.graphDrawingZone = null;
			this.feedBackLabel = null;
			this.exampleQueriesButton = null;
			this.queryTxt = "";
			this.variableList = new ArrayCollection;
			this.variableNameList = new ArrayCollection;
			this.variableLetterList = new ArrayCollection;
			this.variableURIList = new ArrayCollection;
			this.queryGraphList = new ArrayCollection;
			this.currentIndex = -1;
			this.exampleQueriesDescription = new ArrayCollection;
			this.exampleQueriesKeywords = new ArrayCollection;
			this.keywordCombo = new ComboBox;
		}
		
		/**
		 * Function used to clear the view
		 */
		public function clear() : void {
			// Empty what shall be empty
			this.initialKeywordInput.text = "";
			this.graphDrawingZone.removeAllChildren();
			this.variableComboBox.dataProvider = "";
			// Reinitialize lists and indexes
			this.queryGraphList.removeAll();
			this.variableList.removeAll();
			this.variableNameList.removeAll();
			this.variableLetterList.removeAll();
			this.variableURIList.removeAll();
			this.currentIndex = -1;
			// Gray all the buttons that shall be disabled
			this.previousGraphButton.enabled = false;
			this.nextGraphButton.enabled = false;
			this.variableComboBox.enabled = false;;
			this.graphSearchButton.enabled = false;
			this.keywordSearchButton.enabled = false;
			// Clear the feedback label
			this.feedBackLabel.text = "";
			this.queryTxt = "";
		}
		
		/**
		 * Registers the UI components of the view
		 * @param graphDrawingZn The zone to display the graphs
		 * @param initialKeywordIpt The field where initial keywords are input
		 * @param disambiguateBtn  The button used to start the disambiguation process
		 * @param previousGraphBtn  The button to display the previous graph condidate
		 * @param nextGraphBtn The button to display the next graph candidate
		 * @param variableCbx The combo box to select the target variable
		 * @param graphSearchBtn The button to start a search using the current graph
		 * @param keywordSearchBtn The button to start a search using the input keywords only
		 * @param feedBackLbl The label used to provide feedback to the user
		 * @param mappingDataArray The data grid where the information about the mapping should be displayed
		 * @param exampleQueriesBtn The button to click to display the example queries popup
		 */
		public function register(graphDrawingZn:VBox, initialKeywordIpt:TextInput, disambiguateBtn:Button, previousGraphBtn:Button, nextGraphBtn:Button, variableCbx:ComboBox, graphSearchBtn:Button, keywordSearchBtn:Button, feedBackLbl:Label, mappingDataArray:DataGrid, exampleQueriesBtn:Button) : void {
			this.initialKeywordInput = initialKeywordIpt;
			this.disambiguateButton = disambiguateBtn;
			this.previousGraphButton = previousGraphBtn;
			this.nextGraphButton = nextGraphBtn;
			this.variableComboBox = variableCbx;
			this.graphSearchButton = graphSearchBtn;
			this.keywordSearchButton = keywordSearchBtn;
			this.graphDrawingZone = graphDrawingZn;
			this.feedBackLabel = feedBackLbl;
			this.mappingDataGrid = mappingDataArray;
			this.exampleQueriesButton = exampleQueriesBtn;
			// Event handlers
			this.disambiguateButton.addEventListener(FlexEvent.BUTTON_DOWN, disambiguateOnClick);
			this.keywordSearchButton.addEventListener(FlexEvent.BUTTON_DOWN, keywordSearchOnClick);
			this.nextGraphButton.addEventListener(FlexEvent.BUTTON_DOWN, displayNextGraph);
			this.previousGraphButton.addEventListener(FlexEvent.BUTTON_DOWN, displayPreviousGraph);
			this.graphSearchButton.addEventListener(FlexEvent.BUTTON_DOWN, graphSearchOnClick);
			this.initialKeywordInput.addEventListener(Event.CHANGE, textChanged);
			// Initialize the popup if needed
			if(ArrayCollection(this.keywordCombo.dataProvider).length == 0)
				initExampleQueriesPopup();
			// Set the focus to the keyword field
			this.initialKeywordInput.setFocus();
		}
		
		/**
		 * Initialize the popup to select the sample queries
		 */
		private function initExampleQueriesPopup() : void {
			// Layout
			var vb:VBox = new VBox();
			vb.setStyle("paddingBottom", 5);
            vb.setStyle("paddingLeft", 5);
            vb.setStyle("paddingRight", 5);
            vb.setStyle("paddingTop", 5);
            vb.percentHeight = 100;
            vb.percentWidth = 100;
			// Elements in the popup
			this.keywordCombo = new ComboBox;
			keywordCombo.percentWidth = 100;
			keywordCombo.addEventListener(ListEvent.CHANGE, exampleQuerySelected);
			this.txtArea = new TextArea;
			txtArea.percentHeight = 100;
			txtArea.percentWidth = 100;
			txtArea.editable = false;
			// Add the elements to the layout
			vb.addChild(keywordCombo);
            vb.addChild(txtArea);
			// Control buttons at the bottom of the popup
            var cb:ControlBar = new ControlBar();
            var s:Spacer = new Spacer();
            this.okButton = new Button();
            var b2:Button = new Button();
            s.percentWidth = 100;
            this.okButton.label = "OK";
            this.okButton.addEventListener(MouseEvent.CLICK, validPopUp);
            b2.label = "Cancel";
            b2.addEventListener(MouseEvent.CLICK, closePopUp);
            cb.addChild(s);
            cb.addChild(this.okButton);
            cb.addChild(b2);
			// Creation and parameters of the popup
            this.examplePopup = new Panel;
            this.examplePopup.title = "Example Queries";
            this.examplePopup.width = 450;
            this.examplePopup.height = 200;
            this.examplePopup.addChild(vb);
            this.examplePopup.addChild(cb);
            this.examplePopup.setStyle("fontSize", 12);
            // Set the data sources
        	keywordCombo.dataProvider = this.exampleQueriesKeywords;
        	// Fill the popup with sample queries from the query file if needed 
            var myXML:XML = new XML;
			var XML_URL:String = "exampleQueries.xml";
			var myXMLURL:URLRequest = new URLRequest(XML_URL);
			this.xmlLoader = new URLLoader(myXMLURL);
			this.xmlLoader.addEventListener("complete", xmlLoaded);
		}
		
		/**
		 * Function called when the program has finished loading the file containing the example queries
		 * @param e The event raised
		 */
		private function xmlLoaded(e:Event) : void {
			// Get the data
			var xmlData:XML = XML(this.xmlLoader.data);
			// Get the list of the query examples
			var queryList:XMLList = xmlData.children();
			// Browse the list
			for each (var query:XML in queryList) {
				// Get the list of the keywords of the query
				var queryKwd:XMLList = query.child("keywords");
				// We should have exactely one keyword group
				if(queryKwd.length() < 1 || queryKwd.length() > 1)
					break;
				// Add it to the list
				this.exampleQueriesKeywords.addItem(queryKwd.text().toString());
				// The description is optional
				var queryDes:XMLList = query.child("nlDescription");
				// If one description, take it, other wise use an empty string
				if(queryDes.length() == 1)
					this.exampleQueriesDescription.addItem(queryDes.text().toString());
				else
					this.exampleQueriesDescription.addItem(""); 
			}
			// Gray if nothing to select
			if(this.exampleQueriesKeywords.length > 0) {
				// Display the description matching this selection
				this.txtArea.text = String(this.exampleQueriesDescription.getItemAt(this.keywordCombo.selectedIndex));
			} else {
				this.keywordCombo.enabled = false;
				this.okButton.enabled = false;
			}
		} 
		
		/**
		 * Function called when the user selects an example query in the combo box
		 * @param e The event raised
		 */
		private function exampleQuerySelected(e:ListEvent) : void {
			// Display the description matching this selection
			this.txtArea.text = String(this.exampleQueriesDescription.getItemAt(this.keywordCombo.selectedIndex));
		}
		
		/**
		 * Function called when the user closes the popup used to select sample queries
		 * @param e The event raised
		 */
		private function closePopUp(e:MouseEvent): void {
			// Hide the popup
            PopUpManager.removePopUp(this.examplePopup);
        }
        
        /**
		 * Function called when the user selects a sample query and closes the popup
		 * @param e The event raised
		 */
		private function validPopUp(e:MouseEvent): void {
			// Hide the popup
            PopUpManager.removePopUp(this.examplePopup);
            // Put the keywords into the keyword input
            this.initialKeywordInput.text = String(this.exampleQueriesKeywords.getItemAt(this.keywordCombo.selectedIndex));
        	// Throw an event
        	textChanged(null);
        }
        
        /**
         * Function called to display the popup used to select sample queries
         */
        public function createPopUp(): void {
        	// Create a popup
            PopUpManager.addPopUp(this.examplePopup, this.graphDrawingZone, true);
            // Center it in its parent
            PopUpManager.centerPopUp(this.examplePopup);
        }
		
		/**
		 * Save the list of query graph to the view
		 * @param list The list of queryGraph objects to consider
		 */
		public function setQueryGraphList(list:ArrayCollection) : void {
			// Authorize submission of the query for disambiguation or keyword search
			this.disambiguateButton.enabled = true;
			this.keywordSearchButton.enabled = true;
			// If there is at least one suggested graph
			if(list != null && list.length > 0) {
				// (hack)
				if(this.initialKeywordInput.text == "ISWC2008 Rudi") {
					list.removeItemAt(0);
				}
				// (end hack)
				
				// 1. Store the list
				this.queryGraphList = list;
				// 2. Enable the buttons when needed
				this.keywordSearchButton.enabled = true;
				this.graphSearchButton.enabled = true;
				this.variableComboBox.enabled = true;
				this.exampleQueriesButton.enabled = true;
				if(list.length > 1)
					this.nextGraphButton.enabled = true;
				// 3. Draw the current graph
				this.currentIndex = 0;
				drawGraph(0);
				// 4. Display the mapping information
				displayMapping(QueryGraph(this.queryGraphList.getItemAt(this.currentIndex)).mappingList);
				// 5. Update the feedback label
				this.queryTxt = this.initialKeywordInput.text;
				this.feedBackLabel.text = "1 out of " + list.length.toString() + " for '" + this.queryTxt + "'";
			} else {
				this.graphDrawingZone.removeAllChildren();
				var sorryLbl:Label = new Label;
				sorryLbl.text = "Sorry, no query disambiguation was found";
				this.graphDrawingZone.addChild(sorryLbl);
				this.feedBackLabel.text = "No suggestion was found";
				this.keywordSearchButton.enabled = true;
				this.graphSearchButton.enabled = false;
				this.variableComboBox.enabled = false;
				this.nextGraphButton.enabled = false;
				this.previousGraphButton.enabled = false;
				this.disambiguateButton.enabled = false;
				this.exampleQueriesButton.enabled = true;
			}
		}
		
		/**
		 * Display the mapping information into a data grid
		 * @param mappingList The list of mapped elements
		 */
		private function displayMapping(mappingList:ArrayCollection) : void {
			// Create and fill the data provider
			var mappingInfo:ArrayCollection = new ArrayCollection;
			for(var i:int = 0; i < mappingList.length; i++) {
				var mappingElt:GraphEdge = GraphEdge(mappingList.getItemAt(i));
				mappingInfo.addItem({element1: mappingElt.fromElement.label + " (" + mappingElt.fromElement.source.name + ")",
				                     element2: mappingElt.toElement.label + " (" + mappingElt.toElement.source.name + ")"});
			}
			// Use the data provider
			this.mappingDataGrid.dataProvider = mappingInfo;
		}
		
		/**
		 * Draw a query graph knowing its index in the list of QueryGraph objects
		 * @param index The index of the graph being considered
		 */
		private function drawGraph(index:int) : void {
			// NOTE: for the moment, disable the graphical view and display the element in a 'classic' way
			
			// 1. Clean the area
			this.graphDrawingZone.removeAllChildren();
			// 2. Fill the combobox used to select the target variable (litterals shall not be considered)
			var g:QueryGraph = QueryGraph(this.queryGraphList.getItemAt(index));
			this.variableList.removeAll();
			this.variableNameList.removeAll();
			this.variableLetterList.removeAll();
			var asciiIndex:int = 97;
			for(var i:int = 0; i < g.vertexList.length; i++) {
				var f:Facet = Facet(g.vertexList.getItemAt(i));
				if(f is Concept && !this.variableLetterList.contains(Concept(f).variableLetter)) {
					this.variableList.addItem("?" + Concept(f).variableLetter + " (" + f.label + ")");
					this.variableNameList.addItem(f.label);
					this.variableLetterList.addItem(Concept(f).variableLetter);
					this.variableURIList.addItem(f.URI);
				}
			}
			this.variableComboBox.dataProvider = variableList;
			
			// Temporary code: 'classic' rendering
			
			// 3. Set the vertical layout
			var verticalBox:VBox = new VBox;
			
			for(var k:int = 0; k < g.edgeList.length; k++) {
				// Get the edge
				var e:GraphEdge = GraphEdge(g.edgeList.getItemAt(k));
				// Get the elements
				var fromElement:Facet = e.fromElement;
				var toElement:Facet = e.toElement;
				var decorationElement:Facet = e.decorationElement;
				// Create the layout
				var horizontalBox:HBox = new HBox;
				var fromLabel:Label = new Label;
				fromLabel.toolTip = fromElement.URI;
				fromLabel.setStyle("fontSize", 14);
				fromLabel.setStyle("fontWeight", "bold");
				if(fromElement is Concept) {
					fromLabel.setStyle("color", "#00ad00");
					// Get the variable letter
					var letter:String = Concept(fromElement).variableLetter;
					fromLabel.text = "?" + letter + " (" + fromElement.label + ")";
				}
				if(fromElement is Litteral) {
					fromLabel.setStyle("color", "#ff0000");
					fromLabel.text = fromElement.label;
				}
				var decorationLabel:Label = new Label;
				decorationLabel.text = decorationElement.label;
				decorationLabel.toolTip = decorationElement.URI;
				decorationLabel.setStyle("color", "#0000ff");
				decorationLabel.setStyle("fontSize", 14);
				decorationLabel.setStyle("fontWeight", "bold");
				var toLabel:Label = new Label;
				toLabel.toolTip = toElement.URI;
				toLabel.setStyle("fontSize", 14);
				toLabel.setStyle("fontWeight", "bold");
				if(toElement is Concept) {
					toLabel.setStyle("color", "#00ad00");
					// Get the variable letter
					var letter2:String = Concept(toElement).variableLetter;
					toLabel.text = "?" + letter2 + " (" + toElement.label + ")";
				}
				if(toElement is Litteral) {
					toLabel.setStyle("color", "#ff0000");
					toLabel.text = toElement.label;
				}
				// Build the triple representation
				horizontalBox.addChild(fromLabel);
				horizontalBox.addChild(decorationLabel);
				horizontalBox.addChild(toLabel);
				// Register the HBox
				verticalBox.addChild(horizontalBox);
			}
			
			// Register the vertical box
			this.graphDrawingZone.addChild(verticalBox);
			
			// End of temporary code
			
			/*
			// 2. Create the graph rendering element
			var graphCanvas:Canvas = new Canvas;
			graphCanvas.percentHeight = 100;
			graphCanvas.percentWidth = 100;
			graphCanvas.setStyle("horizontalAlign", "center");
			graphCanvas.setStyle("verticalAlign", "middle");
			var vgraph:VisualGraph = new VisualGraph;
			vgraph.percentHeight = 100;
			vgraph.percentWidth = 100;	
			vgraph.setStyle("horizontalAlign", "center");
			vgraph.setStyle("verticalAlign", "middle");
			vgraph.setStyle("left", 0);
			vgraph.setStyle("right", 0);
			vgraph.setStyle("top", 0);
			vgraph.setStyle("bottom", 0);
			var itemRendererFactory:ClassFactory = new ClassFactory(SimpleCircleNodeRenderer);
			vgraph.itemRenderer = itemRendererFactory;
			var edgeLabelRendererFactory:ClassFactory = new ClassFactory(BaseEdgeLabelRenderer);
			vgraph.edgeLabelRenderer = edgeLabelRendererFactory;
			vgraph.visibilityLimitActive = true;
			this.graphDrawingZone.addChild(graphCanvas);
			graphCanvas.addChild(vgraph);
			// 3. Create and initialize the graph with the XML data
			graph = new Graph("XMLAsDocsGraph", false, getXMLFromQueryGraph(g));
			// 4. Set the graph in the vgraph object to this one
			vgraph.graph = graph;
			// 5. Set the graph layout
			layouter = new ForceDirectedLayouter(vgraph);
			layouter.linkLength = 120;
			vgraph.layouter = layouter;	
			// 6. Let the graph fit the space
			layouter.autoFitEnabled = true;
			vgraph.maxVisibleDistance = 10;
			// 7. Create and set an EdgeRenderer
			vgraph.edgeRenderer = new DirectedArrowEdgeRenderer(vgraph.edgeDrawGraphics);
			// 8. We want to display the edge labels
			vgraph.displayEdgeLabels = true;
			// 9. Draw the graph
			startRoot = graph.nodes[0].vnode;
			vgraph.currentRootVNode = startRoot;
			vgraph.draw();*/
		}
		
		/**
		 * Transform a QueryGraph object into an XML string
		 * @param g The QueryGraph object to transform
		 */
		private function getXMLFromQueryGraph(g:QueryGraph) : XML {
			var str:String = "<Graph>";
			var nullEncontered:Boolean = false; // For the case of null objects
			// Create the vertices
			for(var i:int = 0; i < g.vertexList.length; i++) {
				var f:Facet = Facet(g.vertexList.getItemAt(i));
				if(f == null && !nullEncontered) {
					str += "<Node id='null' name='null' nodeSize='32'/>";
					nullEncontered = true;
				} else
					str += "<Node id='" + f.URI + "' name='" + f.label + "' nodeColor='0x0000ff' nodeSize='32'/>";
			}
			// Create the links
			for(var j:int = 0; j < g.edgeList.length; j++) {
				var e:GraphEdge = GraphEdge(g.edgeList.getItemAt(j));
				var fromElement:Facet = e.fromElement;
				var toElement:Facet = e.toElement;
				var decorationElement:Facet = e.decorationElement;
				if(fromElement != null && toElement != null && decorationElement != null)
					str += "<Edge fromID='" + fromElement.URI + "' toID='" + toElement.URI + "' edgeLabel='" + decorationElement.label + "'/>";
				if(fromElement != null && toElement == null && decorationElement != null)
					str += "<Edge fromID='" + fromElement.URI + "' toID='null' edgeLabel='" + decorationElement.label + "'/>";
			}
			str += "</Graph>";
			return new XML(str);
		}
		
		/**
		 * Function called when the user wants to display the next query graph candidate
		 * @param e The event raised
		 */
		private function displayNextGraph(e:FlexEvent) : void {
			// If it is possible
			if(this.currentIndex < this.queryGraphList.length - 1) {
				// Update the index
				this.currentIndex++;
				// Display the mapping information
				displayMapping(QueryGraph(this.queryGraphList.getItemAt(this.currentIndex)).mappingList);
				// Display the graph
				drawGraph(this.currentIndex);
				var idx:int = this.currentIndex + 1;
				this.feedBackLabel.text = idx + " out of " + this.queryGraphList.length.toString() + " for '" + this.queryTxt + "'";
				// Enable the 'previous' button
				this.previousGraphButton.enabled = true;
				// Disable the 'next' button?
				if(this.currentIndex == this.queryGraphList.length - 1)
					this.nextGraphButton.enabled = false;
			}
		}
		
		/**
		 * Function called when the user wants to display the previous query graph candidate
		 * @param e The event raised
		 */
		private function displayPreviousGraph(e:FlexEvent) : void {
			// If it is possible
			if(this.currentIndex > 0) {
				// Update the index
				this.currentIndex--;
				// Display the mapping information
				displayMapping(QueryGraph(this.queryGraphList.getItemAt(this.currentIndex)).mappingList);
				// Display the graph
				drawGraph(this.currentIndex);
				var idx:int = this.currentIndex + 1;
				this.feedBackLabel.text = idx + " out of " + this.queryGraphList.length.toString() + " for '" + this.queryTxt + "'";
				// Enable the 'next' button
				this.nextGraphButton.enabled = true;
				// Disable the 'previous' button?
				if(this.currentIndex == 0)
					this.previousGraphButton.enabled = false;
			}
		}
		
		/**
		 * Function called when the user clicks on the disambiguate button
		 * @param e The event raised
		 */
		private function disambiguateOnClick(e:FlexEvent) : void {
			// Gray the buttons to avoid actions from the user during the query
			this.disambiguateButton.enabled = false;
			this.keywordSearchButton.enabled = false;
			this.nextGraphButton.enabled = false;
			this.previousGraphButton.enabled = false;
			this.variableComboBox.enabled = false;
			this.exampleQueriesButton.enabled = false;
			// Fire an event to the event handler
			this.eventHandler.disambiguateClicked(this.initialKeywordInput.text);
		}
			
		/**
		 * Function called when the user presses the button to start a simple keyword search
		 * @param e The event raised
		 */
		private function keywordSearchOnClick(e:FlexEvent) : void {
			// Fire an event to the event handler
			this.eventHandler.keywordSearchOnClick(this.initialKeywordInput.text);
		}
		
		/**
		 * Function called when the user presses the button to start a search with the current graph
		 * @param e The event raised
		 */
		private function graphSearchOnClick(e:FlexEvent) : void {
			// 1. Get the current graph
			var g:QueryGraph = QueryGraph(this.queryGraphList.getItemAt(this.currentIndex));
			// 2. Get the target variable
			var index:int = this.variableComboBox.selectedIndex;
			var name:String = String(this.variableNameList.getItemAt(index));
			var variable:Facet = null;
			for(var i:int = 0; i < g.vertexList.length; i++) {
				var temp:Facet = Facet(g.vertexList.getItemAt(i));
				if(temp.label == name) {
					variable = temp;
					break;
				}
			}
			// 3. Stores the target variable into the graph
			g.targetVariable = variable;
			// 4. Fire an event to the event handler
			this.eventHandler.graphSearchOnClick(this.initialKeywordInput.text, g);
		}
		
		/**
		 * Function called when the content of the input field changes. The purpose is to allow a
		 * call to query disambiguation only if we have more than 1 keyword and to only allow
		 * simple keyword search otherwise
		 * @param e The event raised
		 */
		 private function textChanged(e:Event) : void {
		 	// 1. Get the content of the field
		 	var s:String = this.initialKeywordInput.text;
		 	// 2. List the keywords
		 	var kwd:ArrayCollection = Functions.getListFromString(s);
		 	// 3. Allow more or less things depending on the keyword number
		 	if(kwd.length < 2) {
		 		// Allow simple keyword search if at least one keyword
		 		if(kwd.length > 0)
		 			this.keywordSearchButton.enabled = true;
		 		else
		 			this.keywordSearchButton.enabled = false;
		 		// The other things cannot be done
		 		this.disambiguateButton.enabled = false;
		 	} else {
		 		// Allow query disambiguation and keyword search
		 		this.keywordSearchButton.enabled = true;
		 		this.disambiguateButton.enabled = true;
		 	}
		 }
	}
}