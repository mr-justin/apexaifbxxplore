package dataStructure
{
	
	[RemoteClass(alias="org.team.xxplore.core.service.search.datastructure.ResultPage")]
	
	/**
	 * Class that represents a page of results returned by the search engine
	 * @author tpenin
	 */
	[Bindable]
	public class ResultPage extends Result
	{
		// The number of the current page
		public var pageNum:int;
	   
		/**
		 * Default Constructor
		 */
		public function ResultPage() : void {
			super();
			this.pageNum = 0;
		}
	}
}