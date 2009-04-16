package dataStructure
{
	[RemoteClass(alias="org.team.xxplore.core.service.search.datastructure.GraphEdge")]
	
	/**
	 * Represents an edge from a query graph. Note that this is a labeled edge
	 * @author tpenin
	 */
	[Bindable]
	public class GraphEdge
	{
		// Origin of the edge
		public var fromElement:Facet;
		// Destination
		public var toElement:Facet;
		// Decoration
		public var decorationElement:Facet;
		
		/**
		 * Default constructor
		 */
		public function GraphEdge() : void {
			this.fromElement = null;
			this.toElement = null;
			this.decorationElement = null;
		}
	}
}