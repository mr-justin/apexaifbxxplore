package dataStructure
{
	[RemoteClass(alias="org.team.xxplore.core.service.search.datastructure.RelationSuggestion")]
	
	/**
	 * Represents an RDF relation from another source than the current one
	 * @author tpenin
	 */
	[Bindable]
	public class RelationSuggestion extends Facet
	{
		/**
		 * Default constructor
		 */
		public function RelationSuggestion()
		{
			super();
		}

	}
}