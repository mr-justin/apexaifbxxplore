package interfaceBackend
{
	import dataStructure.ArraySnippet;
	import dataStructure.Concept;
	import dataStructure.Facet;
	import dataStructure.Query;
	import dataStructure.ResultPage;
	import dataStructure.SeeAlso;
	import dataStructure.Source;
	
	import interfaceElements.EventHandler;
	
	import mx.collections.ArrayCollection;
	import mx.controls.Alert;
	import mx.rpc.events.FaultEvent;
	import mx.rpc.events.ResultEvent;
	import mx.rpc.remoting.mxml.RemoteObject;
	import mx.utils.ObjectUtil;
	
	/**
	 * This class is used to handle the communication with the Java backend
	 * @author tpenin
	 */
	public class SearchEngine
	{
		// The remote object used to query the Semplore backend
		private var ro1:RemoteObject;
		// The remote object used to query the Q2Semantic backend
		private var ro2:RemoteObject;
		// The event handler
		private var eventHandler:EventHandler;
		// The cache
		private var cache:SearchEngineCache;
		
		/**
		 * Default constructor
		 * @param handler The event handler associated
		 */
		public function SearchEngine(handler:EventHandler) : void {
			this.ro1 = null;
			this.ro2 = null;
			this.eventHandler = handler;
			this.cache = new SearchEngineCache;
		}
		
		/**
		 * Set the remote object used to query the Java backend
		 * @param r1 The remote object to consider for the Semplore backend
		 * @param r2 The remote object to consider for the Q2Semantic backend
		 */
		public function setRemoteObjects(r1:RemoteObject, r2:RemoteObject) : void {
			this.ro1 = r1;
			this.ro2 = r2;
		}
		
		/**
		 * Server failure handling function
		 * @param event The event raised
		 */
		public function serverFailure(event:FaultEvent) : void {
			Alert.show(ObjectUtil.toString(event.fault));
		}

		// ====================================== Q2Semantic Web Service API ========================================
		
		/**
		 * Call to the backend for a list of suggested QueryGraph objects
		 * @param keywordList The list of the keywords input by the user
		 * @param topNbGraphs The number of graphs to return
		 */
		public function getPossibleGraphs(keywordList:ArrayCollection, topNbGraphs:int) : void {
			this.ro2.getPossibleGraphs(keywordList, topNbGraphs);
		}
		
		/**
		 * Answer of the backend following a call for a list of suggested QueryGraph objects
		 * @param event The event raised
		 */
		public function getPossibleGraphs_answer(event:ResultEvent) : void {
			// Get the list of graphs
			var graphList:ArrayCollection = event.result as ArrayCollection;
			// Fire an event to the event handler
			this.eventHandler.getPossibleGraphs_answer(graphList);
		}
		
		// ----------------------------------------------------------------------------------------------------------
		
		/**
		 * Call to the backend for suggestions from other sources
		 * @param currentSource The source of the currently displayed objects
		 */
		public function getSuggestionsInOtherSource(currentSource:Source) : void {
			// Create the list of concept objects for the current source
			var list:ArrayCollection = new ArrayCollection;
			for(var i:int = 0; i < currentSource.facetList.length; i++) {
				var obj:Facet = Facet(currentSource.facetList.getItemAt(i));
				if(obj is Concept)
					list.addItem(Facet(obj));
			}
			// Call Q2Semantic
			this.ro2.getSuggestion(list, currentSource.name);
		}
		
		/**
		 * Answer of the backend following a call for suggestions from other sources
		 * @param event The event raised
		 */
		public function getSuggestionsInOtherSource_answer(event:ResultEvent) : void {
			// Get the list of suggestions
			var suggestionList:ArrayCollection = event.result as ArrayCollection;
			// Fire an event to the event handler
			this.eventHandler.getSuggestionsInOtherSource_answer(suggestionList);
		}
		
		// ======================================== Semplore Web Service API =========================================
		
		/**
		 * Call to the backend for a new search
		 * @param query The query to submit to Semplore
		 * @param nbResultsPerPage The number of results to display per ResultPage object
		 */
		public function search(query:Query, nbResultsPerPage:int) : void {
			// Since we start a new search, empty the cache
			this.cache.emptyCache();
			// Ask for the new search
			this.ro1.search(query, nbResultsPerPage);
		}
		
		/**
		 * Answer of the backend following a new search call
		 * @param event The event raised
		 */
		public function search_answer(event:ResultEvent) : void {
			// Get the page
			var r:ResultPage = event.result as ResultPage;
			// Add it to the cache
			this.cache.add(r);
			// Fire an event to the event handler
			this.eventHandler.search_answer(r);
		}
		
		// ----------------------------------------------------------------------------------------------------------
		
		/**
		 * Call to the backend to get a page for an existing query
		 * @param pageNum The number of the page to return
		 * @param nbResultsPerPage The number of results per page to return
		 */
		public function getPage(pageNum:int, nbResultsPerPage:int) : void {	
			// If the page is already in the cache, return it
			var temp:ResultPage = this.cache.getResultPage(pageNum);
			if(temp != null) {
				// Fire an event to the event handler
				this.eventHandler.getPage_answer(temp);
			} else {
				// Ask the server	
				this.ro1.getPage(pageNum, nbResultsPerPage);
			}
		}
		
		/**
		 * Answer of the backend following a get page call for an existing query
		 * @param event The event raised
		 */
		public function getPage_answer(event:ResultEvent) : void {
			// Get the page
			var r:ResultPage = event.result as ResultPage;	
			// Add it to the cache
			this.cache.add(r);
			// Fire an event to the event handler
			this.eventHandler.getPage_answer(r);
		}
		
		// ----------------------------------------------------------------------------------------------------------
	
		/**
		 * Call to the backend to refine an existing query
		 * @param query The part of the query to be added to the existing query
		 * @param nbResultsPerPage The number of results to display per page
		 */
		public function refine(query:Query, nbResultsPerPage:int) : void {
			// Since it is like a new search, empty the cache
			this.cache.emptyCache();
			// Ask for the refinement
			this.ro1.refine(query, nbResultsPerPage);
		}
		
		/**
		 * Answer of the backend following the call to refine an existing query
		 */
		public function refine_answer(event:ResultEvent) : void {
			// Get the page
			var r:ResultPage = event.result as ResultPage;	
			// Add it to the cache
			this.cache.add(r);
			// Fire an event to the event handler
			this.eventHandler.refine_answer(r);
		}
		
		// ----------------------------------------------------------------------------------------------------------
		
		/**
		 * Call to the backend to undo the last refinement
		 * @param nbResultsPerPage The number of results to display per page
		 */
		public function undoLastRefinement(nbResultsPerPage:int) : void {
			// Since it is like a new search, empty the cache
			this.cache.emptyCache();
			// Ask for the result
			this.ro1.undoLastRefinement(nbResultsPerPage);
		}
		
		/**
		 * Answer of the backend following a call to undo the last refinement of a query
		 */
		public function undoLastRefinement_answer(event:ResultEvent) : void {
			// Get the page
			var r:ResultPage = event.result as ResultPage;	
			// Add it to the cache
			this.cache.add(r);
			// Fire an event to the event handler
			this.eventHandler.undoLastRefinement_answer(r);
		}
		
		// ----------------------------------------------------------------------------------------------------------
		
		/**
		 * Call to the backend to get a SeeAlso item
		 * @param url The result item to consider
		 */
		public function getSeeAlsoItem(url:String) : void {
			this.ro1.getSeeAlsoItem(url);
		}
		
		/**
		 * Answer of the backend following a call to get a SeeAlso item
		 */
		public function getSeeAlsoItem_answer(event:ResultEvent) : void {
			// Get the seeAlso item
			var s:SeeAlso = event.result as SeeAlso;
			// Add it to the cache
			this.cache.add(s);
			// Fire an event to the event handler
			this.eventHandler.getSeeAlsoItem_answer(s);
		}
		
		// ----------------------------------------------------------------------------------------------------------
		
		/**
		 * Call to the backend to get an ArraySnippet item
		 * @param url The result item to consider
		 */
		public function getArraySnippet(url:String) : void {
			this.ro1.getArraySnippet(url);
		}
		
		/**
		 * Answer of the backend following a call to get an ArraySnippet item
		 */
		public function getArraySnippet_answer(event:ResultEvent) : void {
			// Get the arraySnippet item
			var a:ArraySnippet = event.result as ArraySnippet;
			// Add it to the cache
			this.cache.add(a);
			// Fire an event to the event handler
			this.eventHandler.getArraySnippet_answer(a);
		}
	}
}