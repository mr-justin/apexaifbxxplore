package dataStructure
{
	import mx.collections.ArrayCollection;
	
	[RemoteClass(alias="org.team.xxplore.core.service.search.datastructure.Source")]
	
	/**
	 * This class represents a data source
	 * @author tpenin
	 */
	[Bindable]
	public class Source
	{
		// The name of the source
		public var name:String;
		// The list of facets associated with this source
		public var facetList:ArrayCollection;
		// The number of results associated with this source returned by the search engine
		public var resultCount:int;
	   
		/**
		 * Default constructor
		 */
		public function Source() : void {
			this.name = "";
			this.facetList = new ArrayCollection;
			this.resultCount = 0;
		}
	}
}