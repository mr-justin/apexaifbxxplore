package interfaceElements
{
	import dataStructure.Concept;
	import dataStructure.ConceptSuggestion;
	import dataStructure.Facet;
	import dataStructure.Relation;
	import dataStructure.RelationSuggestion;
	
	import flash.events.MouseEvent;
	
	import mx.collections.ArrayCollection;
	import mx.containers.VBox;
	import mx.controls.Button;
	import mx.controls.Label;
	import mx.controls.LinkButton;
	import mx.controls.TextInput;
	import mx.events.FlexEvent;
	
	/**
	 * Class to handle the display of the refine panel
	 * @author tpenin
	 */
	public class RefineView
	{
		// Event handler
		private var eventHandler:EventHandler;
		
		// The whole refine area
		private var refineArea:VBox;
		// The text input control for keywords
		private var keywordInput:TextInput;
		// The area containing the concept facets for the current source
		private var conceptFacetArea:VBox;
		// The area containing the relation facets for the current source
		private var relationFacetArea:VBox;
		// The area containing the concept facets for the other sources
		private var externConceptFacetArea:VBox;
		// The area containing the relation facets for the other sources
		private var externRelationFacetArea:VBox;
		// The button used to validate the refinement by keyword
		private var goRefineButton:Button;

		// Facet list
		private var facetList:ArrayCollection;
		// List to store the link buttons associated with the facets of the previsou list
		private var facetButtonList:ArrayCollection;
		// True if there are concepts
		private var haveConcept:Boolean;
		// True if there are concept suggestions
		private var haveConceptSuggestion:Boolean;
		// True if there are relations
		private var haveRelations:Boolean;
		// True if there are relation suggestions
		private var haveRelationSuggestions:Boolean;
		
		/**
		 * Default constructor
		 * @param handler The event handler to consider
		 */
		public function RefineView(handler:EventHandler) : void {
			this.facetList = new ArrayCollection;
			this.facetButtonList = new ArrayCollection;
			this.eventHandler = handler;
			this.refineArea = null;
			this.keywordInput = null;
			this.conceptFacetArea = null;
			this.relationFacetArea = null;
			this.externConceptFacetArea = null;
			this.externRelationFacetArea = null;
			this.haveConcept = false;
			this.haveConceptSuggestion = false;
			this.haveRelations = false;
			this.haveRelationSuggestions = false;
			this.goRefineButton = null;
		}
		
		/**
		 * Register the UI elements that will be controlled by the RefineView object
		 * @param refArea The whole refine area
		 * @param txtInput The text input control for keywords
		 * @param cArea The area containing the concept facets for the current source
		 * @param rArea The area containing the relation facets for the current source
		 * @param cAreaSugg The area containing the concept facets for the other sources
		 * @param rAreaSugg The area containing the relation facets for the other sources
		 * @param goRefineBtn The button to submit the keyword refinement
		 */
		public function register(refArea:VBox, txtInput:TextInput, cArea:VBox, rArea:VBox, cAreaSugg:VBox, rAreaSugg:VBox, goRefineBtn:Button) : void {
			// Update the variable
			this.refineArea = refArea;
			this.keywordInput = txtInput;
			this.conceptFacetArea = cArea;
			this.relationFacetArea = rArea;
			this.externConceptFacetArea = cAreaSugg;
			this.externRelationFacetArea = rAreaSugg;
			this.goRefineButton = goRefineBtn;
			// Create the event handlers for the keyword input
			this.keywordInput.addEventListener(FlexEvent.ENTER, keywordBoxEnterPressed);
			this.goRefineButton.addEventListener(FlexEvent.BUTTON_DOWN, keywordBoxEnterPressed);
		}

		/**
		 * Clears the refine display area
		 */
		public function clear() : void {
			// Clear the keywords
			this.keywordInput.text = "";
			// Clear the facet areas
			this.conceptFacetArea.removeAllChildren();
			this.relationFacetArea.removeAllChildren();
			this.externConceptFacetArea.removeAllChildren();
			this.externRelationFacetArea.removeAllChildren();
			// No element in the facet list
			this.facetList.removeAll();
			// No element in the facetButton list
			this.facetButtonList.removeAll();
			// No elements displayed
			this.haveConcept = false;
			this.haveConceptSuggestion = false;
			this.haveRelations = false;
			this.haveRelationSuggestions = false;
		}
		
		/**
		 * Hides the refine display area
		 */
		public function hide() : void {	
			this.refineArea.visible = false;			
		}
		
		/**
		 * Shows the refine display area
		 */
		private function show() : void {
			this.refineArea.visible = true;			
		}
		
		/**
		 * Add the facets from the list given in parameter. It can be all types of facets. They will be displayed at
		 * the right place.
		 * @param facetlist The list of facets to display
		 */
		public function addFacets(facetList:ArrayCollection) : void {
			// Browse the entire list
			for(var i:int = 0; i < facetList.length; i++) {
				// Get the facet
				var f:Facet = Facet(facetList.getItemAt(i));
				// Create the button
				var lnk:LinkButton = new LinkButton;
				if((f is Concept) || (f is Relation)) {
					lnk.label = f.label + " (" + f.resultNb + ") ";
				} else {
					lnk.label = f.label + " (" + f.source.name + ") ";
				}
				lnk.toolTip = lnk.label;
				lnk.percentWidth = 100;
				lnk.setStyle("textAlign", "left");
				lnk.setStyle("cornerRadius", 0);
				lnk.setStyle("textIndent", 4);
				// Add facets to different VBox and give different colors depending on their type
				if(f is Concept) {
					if(!this.haveConcept) {
						this.haveConcept = true;
						if(this.conceptFacetArea.getChildren().length > 0)
							this.conceptFacetArea.removeAllChildren();		
					}
					lnk.setStyle("themeColor", "#a1a1a1");
					this.conceptFacetArea.addChild(lnk);			
				}
				if(f is ConceptSuggestion) {
					if(!this.haveConceptSuggestion) {
						this.haveConceptSuggestion = true;
						if(this.externConceptFacetArea.getChildren().length > 0)
							this.externConceptFacetArea.removeAllChildren();
					}
					lnk.setStyle("themeColor", "#a1a1a1");
					this.externConceptFacetArea.addChild(lnk);
				}
				if(f is Relation) {
					if(!this.haveRelations) {
						this.haveRelations = true;
						if(this.relationFacetArea.getChildren().length > 0)
							this.relationFacetArea.removeAllChildren();
					}
					lnk.setStyle("themeColor", "#0047a8");
					this.relationFacetArea.addChild(lnk);
				}
				if(f is RelationSuggestion) {
					if(!this.haveRelationSuggestions) {
						this.haveRelationSuggestions = true;
						if(this.externRelationFacetArea.getChildren().length > 0)
							this.externRelationFacetArea.removeAllChildren();
					}
					lnk.setStyle("themeColor", "#0047a8");
					this.externRelationFacetArea.addChild(lnk);
				}
				// Manage the clicks from the user
				lnk.addEventListener(MouseEvent.MOUSE_DOWN, facetClicked);
				// Add the facets to the facetList
				this.facetList.addItem(f);
				// Add the buttons IN THE SAME ORDER
				this.facetButtonList.addItem(lnk);
			}
			// Message if nothing to display
			if(!this.haveConcept && this.conceptFacetArea.getChildren().length == 0) {
				var lbl:Label = new Label;
				lbl.text = "Nothing to display";
				lbl.setStyle("color", "#939393");
				lbl.setStyle("textIndent", 4);
				lbl.percentWidth = 100;
				this.conceptFacetArea.addChild(lbl);
			}
			if(!this.haveConceptSuggestion && this.externConceptFacetArea.getChildren().length == 0) {
				var lbl2:Label = new Label;
				lbl2.text = "Nothing to display";
				lbl2.setStyle("color", "#939393");
				lbl2.setStyle("textIndent", 4);
				lbl2.percentWidth = 100;
				this.externConceptFacetArea.addChild(lbl2);
			}
			if(!this.haveRelations && this.relationFacetArea.getChildren().length == 0) {
				var lbl3:Label = new Label;
				lbl3.text = "Nothing to display";
				lbl3.setStyle("color", "#939393");
				lbl3.setStyle("textIndent", 4);
				lbl3.percentWidth = 100;
				this.relationFacetArea.addChild(lbl3);
			}
			if(!this.haveRelationSuggestions && this.externRelationFacetArea.getChildren().length == 0) {
				var lbl4:Label = new Label;
				lbl4.text = "Nothing to display";
				lbl4.setStyle("color", "#939393");
				lbl4.setStyle("textIndent", 4);
				lbl4.percentWidth = 100;
				this.externRelationFacetArea.addChild(lbl4);
			}
			// Show the panel content
			show();
		}
		
		/**
		 * Event triggered by the click on a facet link
		 * @param e The event raised
		 */
		public function facetClicked(e:MouseEvent) : void {
			// 1. Find the link button that triggered the event
			var btn:LinkButton = LinkButton(e.target);
			// 2. Find its index in the list of link buttons
			var index:int = this.facetButtonList.getItemIndex(btn);
			// 3. Find the corresponding facet
			var f:Facet = Facet(this.facetList.getItemAt(index));
			// 4. Fire an event to the event handler
			this.eventHandler.facetClicked(f);
		}
		
		/**
		 * Event triggered by the "enter" key in the keyword refinement field
		 * @param e The event raised
		 */
		public function keywordBoxEnterPressed(e:FlexEvent) : void {
			// 1. Get the string that was input by the user
			var s:String = this.keywordInput.text;
			// 2. If the string is not empty, we propagate the event to the event handler
			if(s != "")
				this.eventHandler.keywordBoxEnterPressed(s);
		}
	}
}