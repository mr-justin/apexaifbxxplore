package interfaceElements
{
	import dataStructure.ArraySnippet;
	import dataStructure.Concept;
	import dataStructure.ConceptSuggestion;
	import dataStructure.Facet;
	import dataStructure.Keywords;
	import dataStructure.QueryGraph;
	import dataStructure.RelationSuggestion;
	import dataStructure.ResultPage;
	import dataStructure.SeeAlso;
	
	import interfaceBackend.Functions;
	import interfaceBackend.SearchEngine;
	
	import mx.collections.ArrayCollection;
	import mx.controls.Alert;
	import mx.utils.ObjectUtil;

	/**
	 * This class is used to handle all the events that happen on the interface and call the right functions
	 * from the backend when needed
	 * @author tpenin
	 */
	public class EventHandler
	{
		// Search Engine backend
        private var searchEngine:SearchEngine;
        // Initial search view
        private var initialSearchView:InitialSearchView;
        // Search history view
        private var historyView:SearchHistoryView;
        // Search result view
        private var resultView:ResultView;
        // Search refine view
        private var refineView:RefineView;
        // Number of results to display per page (hard coded for now)
        private var numResultsPerPage:int = 10;
        // Function to change the state
        private var changeStateFunction:Function;
        
        /**
        * Default constructor
        * @param stateChangeFn The function to change the state
        */
        public function EventHandler(stateChangeFn:Function) : void {
        	this.searchEngine = null;
        	this.historyView = null;
        	this.resultView = null;
        	this.refineView = null;
        	this.initialSearchView = null;
        	this.changeStateFunction = stateChangeFn;
        }
        
        /**
         * Make sure the event handler knows about the interface components
         * @param hView The search history visualization component 
         * @param rView The result visualization component
         * @param refView The result refinement visualization component
         * @param initView The initial search view
         * @param engine The search engine
         */
        public function registerComponents(hView:SearchHistoryView, rView:ResultView, refView:RefineView, initView:InitialSearchView, engine:SearchEngine) : void {
        	this.searchEngine = engine;
        	this.historyView = hView;
        	this.resultView = rView;
        	this.refineView = refView;
        	this.initialSearchView = initView;
        }
        
        // ================================== Events raised by the Search Engine =====================================
        // (recieved)
        
        /**
         * Answer of the backend if asked for possible query graphs from the user's input
         * @param queryGraphList The ordered list of query graphs proposed by the backend
         */
        public function getPossibleGraphs_answer(queryGraphList:ArrayCollection) : void {
        	if(initialSearchView == null) {
				Alert.show("Your session seems to have expired. Please try refreshing the page.");
			} else {
        		// Call the initial search view to display the graph suggestions
        		this.initialSearchView.setQueryGraphList(queryGraphList);
   			}
        }
        
        /**
         * Answer of the backend if asked for possible suggestions from other data source than the current one
         * @param suggestionList The ordered list of suggestions proposed by the backend
         */
        public function getSuggestionsInOtherSource_answer(suggestionList:ArrayCollection) : void {
        	if(suggestionList == null) {
				Alert.show("Your session seems to have expired. Please try refreshing the page.");
			} else {
        		// Display the suggestions (considered here as a kind of facet)
				this.refineView.addFacets(suggestionList);
			}
        }
        
        /**
         * Answer of the backend if asked for a new search
         * @param resultPage The first result page
         */
        public function search_answer(resultPage:ResultPage) : void {
        	if(resultPage == null) {
				Alert.show("Your session seems to have expired. Please try refreshing the page.");
			} else {
				// 1. Display the results
				this.resultView.displayResultPage(resultPage, this.numResultsPerPage);
				// 2. Display the facets
				this.refineView.addFacets(resultPage.source.facetList);
				// 3. Get the suggestions (fire an event)
				this.searchEngine.getSuggestionsInOtherSource(resultPage.source);
			}
        }
        
        /**
         * Answer of the backend if asked for a page for an existing search
         * @param resultPage The result page
         */
        public function getPage_answer(resultPage:ResultPage) : void {
        	if(resultPage == null) {
				Alert.show("Your session seems to have expired. Please try refreshing the page.");
			} else {
        		// Since the facets did not change, we just need to display the page
        		this.resultView.displayResultPage(resultPage, this.numResultsPerPage);
   			}
        }
        
        /**
         * Answer of the backend if asked for a page for refined search
         * @param resultPage The result page
         */
        public function refine_answer(resultPage:ResultPage) : void {
        	if(resultPage == null) {
				Alert.show("Your session seems to have expired. Please try refreshing the page.");
			} else {
	        	// 1. Display the results
				this.resultView.displayResultPage(resultPage, this.numResultsPerPage);
				// 2. Display the facets
				this.refineView.addFacets(resultPage.source.facetList);
				// 3. Get the suggestions (fire an event)
				this.searchEngine.getSuggestionsInOtherSource(resultPage.source);
			}
        }
        
        /**
         * Answer of the backend if asked to undo the last refinement
         * @param resultPage The result page
         */
        public function undoLastRefinement_answer(resultPage:ResultPage) : void {
        	if(resultPage == null) {
				Alert.show("Your session seems to have expired. Please try refreshing the page.");
			} else {
	        	// 1. Display the results
				this.resultView.displayResultPage(resultPage, this.numResultsPerPage);
				// 2. Display the facets
				this.refineView.addFacets(resultPage.source.facetList);
				// 3. Get the suggestions (fire an event)
				this.searchEngine.getSuggestionsInOtherSource(resultPage.source);
			}
        }
        
        /**
         * Answer of the backend if asked for a seeAlso item
         * @param seeAlso The SeeAlso item returned
         */
        public function getSeeAlsoItem_answer(seeAlso:SeeAlso) : void {
        	this.resultView.displaySeeAlso(seeAlso);
        }
        
        /**
         * Answer of the backend if asked for an ArraySnippet item
         * @param arraySnippet The ArraySnippet item returned
         */
        public function getArraySnippet_answer(arraySnippet:ArraySnippet) : void {
        	this.resultView.displayArraySnippet(arraySnippet);
        }
        
        // ================================== Events raised by the user actions =====================================
        // (recieved)
        
        /**
		 * Actions to accomplish of the user clicks the erase button in the history bar
		 */
		public function historyBack() : void {
			// 1. Clear the Facet Panel
			this.refineView.clear();
			// 2. Clear the Result View
			this.resultView.clear();
			// 3. Send the undo order to the search engine
			this.searchEngine.undoLastRefinement(this.numResultsPerPage);	
		}
        
        /**
		 * Actions to accomplish if the user presses the "New Search" button in the Search History Bar
		 * @param modifyQuery Set to true if the query needs to be modified. If false, it is for a new query.
		 */
		public function newSearchButtonOnClick(modifyQuery:Boolean) : void { 
			// 1. Clear the history
			this.historyView.clear();
			// 2. Clear the results
			this.resultView.clear();
			// 3. Clear the facets
			this.refineView.clear();
			// 4. Clear the query composer if needed
			if(!modifyQuery)
				this.initialSearchView.clear();
			// 5. Show the state to input a query
			this.changeStateFunction("queryState");
		}
		
		/**
		 * Actions to accomplish when the user clicks on another page number
		 * @param pageNum The page number clicked
		 */
		public function pageChanged(pageNum:int) : void {
			// Fire an event to the search engine
			this.searchEngine.getPage(pageNum, this.numResultsPerPage);
		}
		
		/**
		 * Actions to accomplish if the user clicks on the "see also" button of a result item
		 * @param url The URL of the result item to consider
		 */
		public function seeAlsoBtnClicked(url:String) : void {
			// Fire an event to the search engine
			this.searchEngine.getSeeAlsoItem(url);
		}
		
		/**
		 * Actions to accomplish if the user clicks on the "array snippet" button of a result item
		 * @param url The URL of the result item to consider
		 */
		public function arraySnippetBtnClicked(url:String) : void {
			// Fire an event to the search engine
			this.searchEngine.getArraySnippet(url);
		}
		
		/**
		 * Actions to accomplish if the user clicked on a facet associated with a result page
		 * @param f The facet element considered
		 */
		public function facetClicked(f:Facet) : void {
			// 1. Clear the Facet Panel
			this.refineView.clear();
			// 2. Clear the Result View
			this.resultView.clear();
			// 3. Depending on the type of facet, updates the history bar
			if(f is Concept) {
				this.historyView.addConceptFacet(f.label, f.source.name);
			} else if(f is ConceptSuggestion) {
				this.historyView.addConceptSuggestionFacet(f.label, f.source.name);
			} else if(f is RelationSuggestion) {
				this.historyView.addRelationSuggestionFacet(f.label, f.source.name);
			} else {
				this.historyView.addRelationFacet(f.label, f.source.name);
			}
			// 4. Send the refinement to the search engine
			this.searchEngine.refine(f, this.numResultsPerPage);
		}
		
		/**
		 * Actions to accomplish if the user presses the "enter" key to refine with keywords
		 * @param s The string input
		 */
		public function keywordBoxEnterPressed(s:String) : void {
			// 1. Clear the Facet Panel
			this.refineView.clear();
			// 2. Clear the Result View
			this.resultView.clear();
			// 3. Get a list of strings from the original string
			var wordList:ArrayCollection = Functions.getListFromString(s);
			// 4. Create a Keywords object and fill it
			var k:Keywords = new Keywords;
			k.wordList = wordList;
			// 5. Update the search history bar
			this.historyView.addKeywordFacet(Functions.prepareStringForDisplay(s), "");
			// 6. Send the refinement to the search engine and get a new result page
			searchEngine.refine(k, this.numResultsPerPage);	
		}
        
        /**
		 * Actions to accomplish if the user presses starts a search using only keywords
		 * @param text The string input by the user
		 */
		public function keywordSearchOnClick(text:String) : void {
			// 1. Create the keyword object that will be sent
			var keyword:Keywords = new Keywords;
			// 2. Get the list of keywords from the user input
			var tab:ArrayCollection = Functions.getListFromString(text);
			// If the list is not empty
			if(tab.length > 0) {
				// 3. Add the list of words that the user input
				keyword.wordList = tab;
				// 4. Display the state to display results
				changeStateFunction("searchState");
				// 5. Add the keywords to the history bar
				this.historyView.addKeywordFacet(Functions.prepareStringForDisplay(text), "");
				// 6. Send the keywords to the Search Engine
				this.searchEngine.search(keyword, this.numResultsPerPage);
			}			
		}
        
        /**
        * Actions to accomplish when the user clicks on the button to ask for query diambiguation from Q2Semantic
        * @param keywords The string corresponding to the input keywords
        */
        public function disambiguateClicked(keywords:String) : void {
        	// Get the list of keywords from the user input
			var tab:ArrayCollection = Functions.getListFromString(keywords);
			// If the list is not empty
			if(tab.length > 0) {
				// Fire an event to the backend to get the five first possible query graphs
				this.searchEngine.getPossibleGraphs(tab, 5);
			}			
        }
        
        /**
        * Actions to accomplish when the user clicks on the button to ask a search with the currently selected query graph
        * @param text The text of the keyword input
        * @param g The graph that was selected
        */
        public function graphSearchOnClick(text:String, g:QueryGraph) : void {
			// 1. Display the state to display results
			changeStateFunction("searchState");
			// 2. Add the keywords to the history bar
			this.historyView.addKeywordFacet(Functions.prepareStringForDisplay(text), "");
			// 3. Send the graph to the Search Engine
			this.searchEngine.search(g, this.numResultsPerPage);
        }
	}
}