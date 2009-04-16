package graphRenderer
{
	import flash.display.Graphics;
	
	import mx.controls.Label;
	import mx.core.UIComponent;
	
	import render.graph.Edge;
	import render.graph.EdgeRender;

	/**
	 * Custom edge renderer for the graph visualization
	 * @author kzhang, tpenin
	 */
	public class GraphEdgeRenderer implements EdgeRender {
		
		private static const arrowLength:Number = 5;
		private static const arrowWidth:Number = 5 / 2 ;
		
		private static const RELColor:int = 0x9e9e9e;
		private static const RELFontColor:int = 0x0000FF;
		
		private static const DefaultHeight:int = 20;
		
		public function renderEdgeName(backgroundSurface:UIComponent,x:int,y:int,edge:Edge) : void {
			var label:Label = edge.display as Label;
			if(label == null ) label = createLabel(backgroundSurface,edge);
			label.visible = true;
			label.x = x - (label.width/2);
			label.y = y - (label.height/2);	
		}
		
		private function createLabel(backgroundSurface:UIComponent, edge:Edge) : Label {
			var l:Label = new Label();
			l.text = edge.text;
			l.height = DefaultHeight;
			l.setStyle("color",RELFontColor);
			l.setStyle("fontSize",11);
			backgroundSurface.addChild(l);
			edge.display = l;
			return l;
		}
		
		public function renderEdge(backgroundSurface:UIComponent, fx:int, fy:int, tx:int, ty:int, edge:Edge) : void
		{
			var g:Graphics = backgroundSurface.graphics;
			
			var dx:Number = tx - fx;
			var dy:Number = ty - fy;
			
			tx = fx + dx * 0.9;
			ty = fy + dy * 0.9;
			fx = tx - dx * 0.8;
			fy = ty - dy * 0.8;
			
			drawLineWithArrow(g,fx,fy,tx,ty,RELColor);
		}
		
		public function drawLine(graphics:Graphics, fx:int, fy:int, tx:int, ty:int, lineColor:int, alpha:Number = 1) : void {
			graphics.moveTo(fx,fy);
			graphics.lineStyle(2,lineColor,alpha);
			graphics.beginFill(0);
			graphics.lineTo(tx,ty);
			graphics.endFill();
		}
		
		public function drawLineWithArrow(graphics:Graphics, fx:int, fy:int, tx:int, ty:int, lineColor:int, alpha:Number = 1) : void {
			drawLine(graphics,fx,fy,tx,ty,lineColor,alpha);
			drawArrow(graphics,fx,fy,tx,ty,lineColor,alpha);
		}
		
		public function drawArrow(graphics:Graphics, fx:int, fy:int, tx:int, ty:int, arrowColor:int, alpha:Number = 1) : void {
			var dx:Number = tx - fx;
			var dy:Number = ty - fy;
			var len:Number = Math.sqrt(dx * dx + dy * dy);
			
			var sinA:Number = dy / len;
			var cosA:Number = dx / len;
			
			var midX:Number = tx - cosA * arrowLength;
			var midY:Number = ty - sinA * arrowLength;
			
			var ax:Number = midX + sinA * arrowWidth;
			var ay:Number = midY - cosA * arrowWidth;
			
			var bx:Number = midX - sinA * arrowWidth;
			var by:Number = midY + cosA * arrowWidth;
			
			graphics.lineStyle(2,arrowColor,alpha);
	        graphics.beginFill(arrowColor);
	        graphics.moveTo(tx, ty);
	        graphics.lineTo(ax, ay);
	        graphics.lineTo(bx, by);
	        graphics.lineTo(tx, ty);
	        graphics.endFill();
		}
	}
}