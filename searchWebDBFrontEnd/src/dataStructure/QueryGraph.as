package dataStructure
{
	import mx.collections.ArrayCollection;
	
	[RemoteClass(alias="org.team.xxplore.core.service.search.datastructure.QueryGraph")]
	
	/**
	 * Class that contains an implementation of the graphs used for query disambiguation
	 * @author tpenin
	 */
	[Bindable]
	public class QueryGraph implements Query
	{
		// Target variable
		public var targetVariable:Facet;
		// List of nodes (Facet objects)
		public var vertexList:ArrayCollection;
		// List of edges (GraphEdge objects)
		public var edgeList:ArrayCollection;
		// List representing the mappings (concept-concept and relation-relation)
		public var mappingList:ArrayCollection;
		
		/**
		 * Default constructor
		 */
		public function QueryGraph() : void {
			this.edgeList = new ArrayCollection;
			this.vertexList = new ArrayCollection;
			this.targetVariable = null;
		}
	}
}