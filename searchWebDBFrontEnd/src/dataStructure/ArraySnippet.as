package dataStructure
{
	import mx.collections.ArrayCollection;
	
	[RemoteClass(alias="org.team.xxplore.core.service.search.datastructure.ArraySnippet")]
	
	/**
	 * This class contains elements to implement the "array snippet" functionality
	 * @author tpenin
	 */
	[Bindable]
	public class ArraySnippet
	{
		// The result item this ArraySnippet object is attached to
		public var resultItem:ResultItem;
		// The list of relation-attribute couples attached to this result item
		public var relation_attribute:ArrayCollection;
		// The list of attribute-value couples attached to this result item. Note that the value can be a litteral or not
		public var attribute_value:ArrayCollection;
		// The list of the classes attached to this result item
		public var classeList:ArrayCollection;
		
		/**
		 * Default constructor
		 */
		public function ArraySnippet() : void {
			this.resultItem = null;
			this.relation_attribute = new ArrayCollection;
			this.attribute_value = new ArrayCollection;
			this.classeList = new ArrayCollection;
		}
	}
}