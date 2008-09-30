package interfaceElements
{
	import dataStructure.Facet;
	import dataStructure.GraphEdge;
	import dataStructure.QueryGraph;
	
	import mx.collections.ArrayCollection;
	import mx.containers.Canvas;
	import mx.containers.VBox;
	import mx.controls.Button;
	import mx.controls.ComboBox;
	import mx.controls.Label;
	import mx.controls.TextInput;
	import mx.core.ClassFactory;
	import mx.events.FlexEvent;
	
	import org.un.cava.birdeye.ravis.components.renderers.edgeLabels.BaseEdgeLabelRenderer;
	import org.un.cava.birdeye.ravis.components.renderers.nodes.SimpleCircleNodeRenderer;
	import org.un.cava.birdeye.ravis.graphLayout.data.Graph;
	import org.un.cava.birdeye.ravis.graphLayout.data.IGraph;
	import org.un.cava.birdeye.ravis.graphLayout.layout.ForceDirectedLayouter;
	import org.un.cava.birdeye.ravis.graphLayout.layout.ILayoutAlgorithm;
	import org.un.cava.birdeye.ravis.graphLayout.visual.IEdgeRenderer;
	import org.un.cava.birdeye.ravis.graphLayout.visual.IVisualNode;
	import org.un.cava.birdeye.ravis.graphLayout.visual.VisualGraph;
	import org.un.cava.birdeye.ravis.graphLayout.visual.edgeRenderers.DirectedArrowEdgeRenderer;
	
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
		// List of the vertex of the graph
		private var vertexList:ArrayCollection;
		// The index of the graph currently selected
		private var currentIndex:int;
		
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
		// important to ensure we are done with the main initialisation
		//private var initDone:Boolean = false;
		
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
			this.vertexList = new ArrayCollection;
			this.queryGraphList = new ArrayCollection;
			this.currentIndex = -1;
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
			this.vertexList.removeAll();
			this.currentIndex = -1;
			// Gray all the buttons that shall be disabled
			this.previousGraphButton.enabled = false;
			this.nextGraphButton.enabled = false;
			this.variableComboBox.enabled = false;;
			this.graphSearchButton.enabled = false;
			this.keywordSearchButton.enabled = false;
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
		 */
		public function register(graphDrawingZn:VBox, initialKeywordIpt:TextInput, disambiguateBtn:Button, previousGraphBtn:Button, nextGraphBtn:Button, variableCbx:ComboBox, graphSearchBtn:Button, keywordSearchBtn:Button) : void {
			this.initialKeywordInput = initialKeywordIpt;
			this.disambiguateButton = disambiguateBtn;
			this.previousGraphButton = previousGraphBtn;
			this.nextGraphButton = nextGraphBtn;
			this.variableComboBox = variableCbx;
			this.graphSearchButton = graphSearchBtn;
			this.keywordSearchButton = keywordSearchBtn;
			this.graphDrawingZone = graphDrawingZn;
			// Event handlers
			this.disambiguateButton.addEventListener(FlexEvent.BUTTON_DOWN, disambiguateOnClick);
			this.keywordSearchButton.addEventListener(FlexEvent.BUTTON_DOWN, keywordSearchOnClick);
			this.nextGraphButton.addEventListener(FlexEvent.BUTTON_DOWN, displayNextGraph);
			this.previousGraphButton.addEventListener(FlexEvent.BUTTON_DOWN, displayPreviousGraph);
			this.graphSearchButton.addEventListener(FlexEvent.BUTTON_DOWN, graphSearchOnClick);
		}
		
		/**
		 * Save the list of query graph to the view
		 * @param list The list of queryGraph objects to consider
		 */
		public function setQueryGraphList(list:ArrayCollection) : void {
			// If there is at least one suggested graph
			if(list.length > 0) {
				// 1. Store the list
				this.queryGraphList = list;
				// 2. Fill the combobox used to select the target variable
				var g:QueryGraph = QueryGraph(this.queryGraphList.getItemAt(0));
				for(var i:int = 0; i < g.vertexList.length; i++) {
					this.vertexList.addItem(Facet(g.vertexList.getItemAt(i)).label);
				}
				this.variableComboBox.dataProvider = vertexList;
				// 3. Enable the buttons when needed
				this.keywordSearchButton.enabled = true;
				this.graphSearchButton.enabled = true;
				this.variableComboBox.enabled = true;
				if(list.length > 1)
					this.nextGraphButton.enabled = true;
				// 4. Draw the current graph
				this.currentIndex = 0;
				drawGraph(0);
			} else {
				var sorryLbl:Label = new Label;
				sorryLbl.text = "Sorry, no query disambiguation graph was found";
				this.graphDrawingZone.addChild(sorryLbl);
			}
		}
		
		/**
		 * Draw a query graph knowing its index in the list of QueryGrqph objects
		 * @param index The index of the graph being considered
		 */
		private function drawGraph(index:int) : void {
			// 1. Clean the area
			this.graphDrawingZone.removeAllChildren();
			// 2. Create the graph rendering element
			var graphCanvas:Canvas = new Canvas;
			graphCanvas.percentHeight = 100;
			graphCanvas.percentWidth = 100;
			var vgraph:VisualGraph = new VisualGraph;
			vgraph.percentHeight = 100;
			vgraph.percentWidth = 100;
			vgraph.setStyle("backgroundColor", "#ffffff");		
			var itemRendererFactory:ClassFactory = new ClassFactory(SimpleCircleNodeRenderer);
			vgraph.itemRenderer = itemRendererFactory;
			var edgeLabelRendererFactory:ClassFactory = new ClassFactory(BaseEdgeLabelRenderer);
			vgraph.edgeLabelRenderer = edgeLabelRendererFactory;
			vgraph.visibilityLimitActive = true;
			this.graphDrawingZone.addChild(graphCanvas);
			graphCanvas.addChild(vgraph);
			// 3. Create and initialize the graph with the XML data
			graph = new Graph("XMLAsDocsGraph", false, getXMLFromQueryGraph(QueryGraph(this.queryGraphList.getItemAt(index))));
			// 4. Set the graph in the vgraph object to this one
			vgraph.graph = graph;
			// 5. Set the graph layout
			layouter = new ForceDirectedLayouter(vgraph);
			vgraph.layouter = layouter;
			// 6. Let the graph fit the space
			layouter.autoFitEnabled = true;
			vgraph.maxVisibleDistance = 10;
			// 7. Create and set an EdgeRenderer
			vgraph.edgeRenderer = new DirectedArrowEdgeRenderer(vgraph.edgeDrawGraphics);
			// 8. We want to display the edge labels
			vgraph.displayEdgeLabels = true;
			// 9. Draw the graph
			vgraph.draw();
		}
		
		/**
		 * Transform a QueryGraph object into an XML string
		 * @param g The QueryGraph object to transform
		 */
		private function getXMLFromQueryGraph(g:QueryGraph) : XML {
			var str:String = "<Graph>";
			// Create the vertices
			for(var i:int = 0; i < g.vertexList.length; i++) {
				var f:Facet = Facet(g.vertexList.getItemAt(i));
				str += "<Node id='" + f.URI + "' name='" + f.label + "' nodeSize='32'/>";
			}
			// Create the links
			for(var j:int = 0; j < g.edgeList.length; j++) {
				var e:GraphEdge = GraphEdge(g.edgeList.getItemAt(j));
				str += "<Edge fromID='" + e.fromElement.URI + "' toID='" + e.toElement.URI + "' edgeLabel='" + e.decorationElement.label + "'/>";
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
				// Display it
				drawGraph(this.currentIndex + 1);
				// Update the index
				this.currentIndex++;
				// Enable the 'previous' button
				this.previousGraphButton.enabled = true;
				// Disable the 'next' button?
				if(this.currentIndex == this.queryGraphList.length - 1)
					this.nextGraphButton .enabled = false;
			}
		}
		
		/**
		 * Function called when the user wants to display the previous query graph candidate
		 * @param e The event raised
		 */
		private function displayPreviousGraph(e:FlexEvent) : void {
			// If it is possible
			if(this.currentIndex > 1) {
				// Display it
				drawGraph(this.currentIndex - 1);
				// Update the index
				this.currentIndex--;
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
			var name:String = String(this.vertexList.getItemAt(index));
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
			// Fire an event to the event handler
			this.eventHandler.graphSearchOnClick(this.initialKeywordInput.text, g);
		}
	}
}