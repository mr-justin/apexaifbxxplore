package dataStructure
{
	import mx.collections.ArrayCollection;
	
	[RemoteClass(alias="org.team.xxplore.core.service.search.datastructure.Result")]
	
	/**
	 * This class represents all the results returned by a query
	 * @author tpenin
	 */ 
	[Bindable]
	public class Result
	{
		// The list of all the result items that were found by the search engine
		public var resultItemList:ArrayCollection;
		// The list of all the sources that have contributed to the results
		public var source:Source;
		
		/**
		 * Default constructor
		 */
		public function Result() : void {
			this.resultItemList = new ArrayCollection;
			this.source = null;
		}
	}
}