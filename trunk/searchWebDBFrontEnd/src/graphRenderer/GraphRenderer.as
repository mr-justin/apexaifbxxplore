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
	}
}