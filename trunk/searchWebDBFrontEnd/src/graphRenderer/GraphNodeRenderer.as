package graphRenderer
{
	import mx.controls.Label;
	import mx.core.UIComponent;
	
	import render.graph.Node;
	import render.graph.NodeRender;

	/**
	 * Custom node renderer for the graph visualization
	 * @author kzhang, tpenin
	 */
	public class GraphNodeRenderer implements NodeRender {
		
		// Default width of a label
		private static const DefaultWidth:int = 150;
		// Default height of a label
		private static const DefaultHeight:int = 20;
		
		/**
		 * Function called to render the nodes. It displays different colors depending on the type
		 * of node considered
		 * @param node The node to be drawn
		 */
		public function renderNode(node:Node) : UIComponent {
			// Creation of the label
			var l:Label = new Label;
			l.height = DefaultHeight;
			l.text = GraphEdgeRenderer.trim(node.text);
			l.width = DefaultWidth;
			l.setStyle("textAlign", "center");
			l.setStyle("fontSize", 12);
			l.setStyle("fontWeight", "bold");
			// If it starts with "?", it is a concept. Otherwise, it is a litteral
			if(l.text.charAt(0) == "?")
				l.setStyle("color", "#00AD00");
			else
				l.setStyle("color", "#FF0000");
			return l;
		}
	}
}