package dataStructure
{
	import mx.collections.ArrayCollection;
	
	[RemoteClass(alias="org.team.xxplore.core.service.search.datastructure.Keywords")]
	
	/**
 	 * This class represents a list of keywords, that can be handled like a query.
 	 * @author tpenin
 	 */
 	[Bindable]
	public class Keywords implements Query
	{
		// The list of the keywords
		public var wordList:ArrayCollection;
	   
		/**
		 * Default constructor
		 */
		public function Keywords() : void {
			this.wordList = new ArrayCollection;
		}
	}
}