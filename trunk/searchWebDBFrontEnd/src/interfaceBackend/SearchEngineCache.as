package interfaceBackend
{
	import dataStructure.ArraySnippet;
	import dataStructure.ResultPage;
	import dataStructure.SeeAlso;
	
	import mx.collections.ArrayCollection;
	import mx.controls.Alert;
	import mx.utils.ObjectUtil;
	
	/**
	 * This class implements a cache for the search engine to limit its exchange with the server to a minimum
	 * @author tpenin
	 */
	public class SearchEngineCache
	{
		// List of result pages in the cache
		private var pageList:ArrayCollection;
		// List of SeeAlso items in the cache
		private var seeAlsoList:ArrayCollection;
		// List of ArraySnippet items in the cache
		private var arraySnippetList:ArrayCollection;
		
		/**
		 * Default constructor
		 */
		public function SearchEngineCache() : void {
			this.pageList = new ArrayCollection;
			this.seeAlsoList = new ArrayCollection;
			this.arraySnippetList = new ArrayCollection;
		}

		/**
		 * Empty the cache
		 */
		public function emptyCache() : void {
			this.pageList.removeAll();
			this.seeAlsoList.removeAll();
			this.arraySnippetList.removeAll();
		}
		
		/**
		 * Adds an object to the cache. It may be a ResultPage, a SeeAlso item or an ArraySnippet item
		 * @param obj The object to add to the cache
		 */
		public function add(obj:Object) : void {
			// ResultPage objects are placed into pageList
			if(obj is ResultPage) {
				var temp1:ResultPage = obj as ResultPage;
				// Is this object already in the cache?
				for(var i:int = 0; i < this.pageList.length; i++) {
					var temp1_1:ResultPage = ResultPage(this.pageList.getItemAt(i));
					if(temp1_1.pageNum == temp1.pageNum)
						return; // Already in the cache
				}
				// Add it to the cache
				this.pageList.addItem(temp1);
				return;
			}
			// SeeAlso objects are placed into seeAlsoList
			if(obj is SeeAlso) {
				var temp2:SeeAlso = obj as SeeAlso;
				// Is this object already in the cache?
				for(var j:int = 0; j < this.seeAlsoList.length; j++) {
					var temp2_1:SeeAlso = SeeAlso(this.seeAlsoList.getItemAt(j));
					if(temp2_1.resultItem.URL == temp2.resultItem.URL)
						return; // Already in the cache
				}
				// Add it to the cache
				this.seeAlsoList.addItem(temp2);
				return;
			}
			// ArraySnippet objects are placed into arraySnippetList
			if(obj is ArraySnippet) {
				var temp3:ArraySnippet = obj as ArraySnippet;
				// Is this object already in the cache?
				for(var k:int = 0; k < this.arraySnippetList.length; k++) {
					var temp3_1:ArraySnippet = ArraySnippet(this.arraySnippetList.getItemAt(k));
					if(temp3_1.resultItem.URL == temp3.resultItem.URL)
						return; // Already in the cache
				}
				// Add it to the cache
				this.arraySnippetList.addItem(temp3);
				return;
			}
			// Else do nothing
			return;
		}
		
		/**
		 * Return a result page from the cache knowing its number. If the page is not in the cache, return null
		 * @param pageNum The number of the page to return
		 * @return the ResultPage matching the page number given in parameters, null if it is not in the cache
		 */
		public function getResultPage(pageNum:int) : ResultPage {
			// Look for the page
			for(var i:int = 0; i < this.pageList.length; i++) {
				var temp:ResultPage = ResultPage(this.pageList.getItemAt(i));
				if(temp.pageNum == pageNum)
					return temp;
			}
			// Not found
			return null;
		}
		
		/**
		 * Return a SeeAlso object from the cache knowing the URL of its associated result item. If it is not in 
		 * the cache, return null
		 * @param URL The URL of the result item associated with the SeeAlso object
		 * @return the SeeAlso object matching the result item URL given in parameter, null if it is not in the cache
		 */
		public function getSeeAlso(URL:String) : SeeAlso {
			// Look for the SeeAlso object
			for(var i:int = 0; i < this.seeAlsoList.length; i++) {
				var temp:SeeAlso = SeeAlso(this.seeAlsoList.getItemAt(i));
				if(temp.resultItem.URL == URL)
					return temp;
			}
			// Not found
			return null;
		}
		
		/**
		 * Return an ArraySnippet object from the cache knowing the URL of its associated result item. If it is not in 
		 * the cache, return null
		 * @param URL The URL of the result item associated with the ArraySnippet object
		 * @return the ArraySnippet object matching the result item URL given in parameter, null if it is not in the cache
		 */
		public function getArraySnippet(URL:String) : ArraySnippet {
			// Look for the ArraySnippet object
			for(var i:int = 0; i < this.arraySnippetList.length; i++) {
				var temp:ArraySnippet = ArraySnippet(this.arraySnippetList.getItemAt(i));
				if(temp.resultItem.URL == URL)
					return temp;
			}
			// Not found
			return null;
		}
	}
}