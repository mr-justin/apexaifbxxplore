package graphRenderer
{
	import render.SpringGraphRender;

	/**
	 * Custom graph class in order to define our own edge and node renderers
	 * @author tpenin
	 */
	public class GraphRenderer extends SpringGraphRender {
		/**
		 * Default constructor
		 */
		public function GraphRenderer() {
			super();
			this._edgeRender = new GraphEdgeRenderer;
			this._nodeRender = new GraphNodeRenderer;
		}
		
		/**
		 * Returns the URI part of a node (stored before the first ! found in the string)
		 * @param nodeStr The string stored in the node
		 */
		public static function getLabel(nodeStr:String) : String {
			if(nodeStr.indexOf("!") != -1)
				return nodeStr.substring(0, nodeStr.indexOf("!"));
			else
				return nodeStr;
		}
		
		/**
		 * Returns the URI part of a node (stored after the first ! found in the string)
		 * @param nodeStr The string stored in the node
		 */
		public static function getURI(nodeStr:String) : String {
			if(nodeStr.indexOf("!") != -1 && nodeStr.indexOf("!") != nodeStr.length - 1)
				return nodeStr.substring(nodeStr.indexOf("!") + 1, nodeStr.length);
			else
				return nodeStr;
		}
	}
}