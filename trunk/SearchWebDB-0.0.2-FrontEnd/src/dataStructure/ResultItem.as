package dataStructure
{
	[RemoteClass(alias="org.team.xxplore.core.service.search.datastructure.ResultItem")]
	
	/**
	 * This class represents a result item returned by the search engine
	 * @author tpenin
	 */ 
	[Bindable]
	public class ResultItem
	{
		// The URL that uniquely identify the result item
		public var URL:String;
		// The score of this result item in its source according to the ranking algorithm of the search engine
		public var score:Number;
		// The type of this result item (text document, picture, etc.)
		public var type:String;
		// The title of this result item as it will be displayed
		public var title:String;
		// The text snippet associated with the result item
		public var snippet:String;
		
		/**
		 * Default constructor
		 */
		public function ResultItem(): void {
			this.URL = "";
			this.score = 0.0;
			this.type = "";
			this.title = "";
			this.snippet = "";
		}
	}
}