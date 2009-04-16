package dataStructure
{
	[RemoteClass(alias="org.team.xxplore.core.service.search.datastructure.Couple")]
	
	/**
	 * Represents a Facet couple
	 * @author tpenin
	 */
	[Bindable]
	public class Couple
	{
		public var element1:Facet;
		public var element2:Facet;
		
		public function Couple() : void {
			this.element1 = null;
			this.element2 = null;
		}
	}
}