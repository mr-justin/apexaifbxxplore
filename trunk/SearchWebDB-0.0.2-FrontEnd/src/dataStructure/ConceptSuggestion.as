package dataStructure
{
	[RemoteClass(alias="org.team.xxplore.core.service.search.datastructure.ConceptSuggestion")]
	
	/**
	 * Represents an RDF class suggested from another source than the current one
	 * @author tpenin
	 */
	[Bindable]
	public class ConceptSuggestion extends Facet
	{
		/**
		 * Default constructor
		 */
		public function ConceptSuggestion()
		{
			super();
		}
	}
}