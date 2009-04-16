package dataStructure
{
	[RemoteClass(alias="org.team.xxplore.core.service.search.datastructure.Concept")]
	
	/**
	 * Represents an RDF class
	 * @author tpenin
	 */
	[Bindable]
	public class Concept extends Facet
	{
		// Letter identifying the variable associated with this concept (ex: 'x' in '?x relation ?y')
		public var variableLetter:String;
		
		/**
		 * Default constructor
		 */
		public function Concept() : void {
			super();
		}
	}
}