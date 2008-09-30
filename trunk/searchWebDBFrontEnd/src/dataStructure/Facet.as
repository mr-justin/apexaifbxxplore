package dataStructure
{
	[RemoteClass(alias="org.team.xxplore.core.service.search.datastructure.Facet")]
	
	/**
 	 * This class represents a facet that may be used to specify queries. A facet represents an RDF element
 	 * @author tpenin
 	 */
 	[Bindable]
	public class Facet implements Query
	{
		// The label can be displayed by the interface
		public var label:String;
		// The URI identifies the facet in a unique manner
		public var URI:String;
		// The data source from which originates the facet
		public var source:Source;
		// The number of results
		public var resultNb:int;
	   
		/**
		 * Default constructor
		 */
		public function Facet() : void {
			this.label = "";
			this.URI = "";
			this.source = new Source;
			this.resultNb = 0;
		}
	}
}