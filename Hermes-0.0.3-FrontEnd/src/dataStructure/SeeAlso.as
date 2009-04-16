package dataStructure
{
	import mx.collections.ArrayCollection;
	
	[RemoteClass(alias="org.team.xxplore.core.service.search.datastructure.SeeAlso")]
	
	/**
	 * This class contains elements to implement the "see also" functionality
	 * @author tpenin
	 */
	[Bindable]
	public class SeeAlso
	{
		// The result item this SeeAlso object is attached to
		public var resultItem:ResultItem;
		// The list of instances attaached to the result item
		public var facetList:ArrayCollection;
	   
		/**
		 * Default constructor
		 */
		public function SeeAlso() : void {
			this.resultItem = null;
			this.facetList = new ArrayCollection;
		}
	
		/**
		 * @return the facetList
		 */
		public function getFacetList() : ArrayCollection {
			return this.facetList;
		}
	
		/**
		 * @param facetList the facetList to set
		 */
		public function setFacetList(facetList:ArrayCollection) : void {
			this.facetList = facetList;
		}
	
		/**
		 * @return the resultItem
		 */
		public function getResultItem() : ResultItem {
			return this.resultItem;
		}
	
		/**
		 * @param resultItem the resultItem to set
		 */
		public function setResultItem(resultItem:ResultItem) : void {
			this.resultItem = resultItem;
		}

	}
}