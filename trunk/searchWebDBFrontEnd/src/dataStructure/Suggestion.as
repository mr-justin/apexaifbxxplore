package dataStructure
{
	[RemoteClass(alias="org.team.xxplore.core.service.search.datastructure.Suggestion")]
	
	/**
	 * Represents a suggestion of facet from another source than the current one
	 * @author tpenin
	 */
	 [Bindable]
	 public class Suggestion
	 {
	 	// Label that will be displayed
		public var label: String;
		// Source of this facet
		public var source: Source;
		// URI of the element
		public var URI: String;
		// Confidence of the suggestion
		public var conf: Number;

		/**
		 * Default constructor
		 */
		public function Suggestion()
		{
			this.label = "";
			this.source = null;
			this.URI = "";
			this.conf = 0.0;
		}

	 }
}