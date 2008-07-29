package org.aifb.xxplore.views.graphviewer;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;

import org.ateam.xxplore.core.ExploreEnvironment;

import prefuse.Constants;
import prefuse.data.Edge;
import prefuse.data.Node;
import prefuse.render.EdgeRenderer;
import prefuse.visual.EdgeItem;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;

public class LabeledEdgeRenderer extends EdgeRenderer {
	
	private boolean useStraightLineForSingleEdges; 
	
	public LabeledEdgeRenderer(int edgeType) {
		super(edgeType);
	}
	
	public LabeledEdgeRenderer(int edgeType, int arrowType) {
		super(edgeType, arrowType);
	}
	
	@Override
	public void render(Graphics2D g, VisualItem item) {
		super.render(g, item);
		
		if (item instanceof EdgeItem) {
			EdgeItem edge = (EdgeItem)item;
			
			if (edge.canGet(ExploreEnvironment.IS_LABEL_VISIBLE, Boolean.class) &&
					!((Boolean)edge.get(ExploreEnvironment.IS_LABEL_VISIBLE))) {
				return;
			}
			
			double middleX = 0, middleY = 0;
			@SuppressWarnings("unused")
			double alpha = 0.0;
			if (getEdgeType() == Constants.EDGE_TYPE_LINE) {
				NodeItem source = edge.getSourceItem();
				NodeItem target = edge.getTargetItem();
				
				Rectangle2D sourceBounds = source.getBounds();
				Rectangle2D targetBounds = target.getBounds();

				double vecX = targetBounds.getCenterX() - sourceBounds.getCenterX();
				double vecY = targetBounds.getCenterY() - sourceBounds.getCenterY();
				
				middleX = sourceBounds.getCenterX() + 0.5 * vecX;
				middleY = sourceBounds.getCenterY() + 0.5 * vecY;
				alpha = Math.atan((targetBounds.getCenterY() - sourceBounds.getCenterY()) / (targetBounds.getCenterX() - sourceBounds.getCenterX()));
			} 
			else if (getEdgeType() == Constants.EDGE_TYPE_CURVE) {
				Point2D ctrl1 = m_cubic.getCtrlP1();
				Point2D ctrl2 = m_cubic.getCtrlP2();
				
				double vecX = ctrl2.getX() - ctrl1.getX();
				double vecY = ctrl2.getY() - ctrl1.getY();
				
				middleX = ctrl1.getX() + 0.5 * vecX;
				middleY = ctrl1.getY() + 0.5 * vecY;
				alpha = Math.atan((ctrl2.getY() - ctrl1.getY()) / (ctrl2.getX() - ctrl1.getX()));
			}
			
			g.setFont(edge.getFont());
			g.drawString(edge.getString(ExploreEnvironment.LABEL), (float)middleX - 5, (float)middleY);

		}
	}
	
	@Override
	protected void getCurveControlPoints(EdgeItem edge_item, Point2D[] cp, double x1, double y1, double x2, double y2) { 

		Node sourceNode = edge_item.getSourceNode(); 
		Node targetNode = edge_item.getTargetNode(); 
		Iterator edges = sourceNode.edges(); 
		
//		number of equal edges = same target and source 
		int noOfEqualEdges = 0; 
		
//		number of nearequal edges = same nodes, but any order target and source
		int noOfSameNodeEdges = 0; 
		int myEdgeIndex = 0; 
		int row = edge_item.getRow(); 
		while (edges.hasNext()) { 
			Edge edge = (Edge) edges.next(); 
			int edgeRow = edge.getRow(); 
			if ((edge.getSourceNode() == sourceNode) && (edge.getTargetNode() == targetNode)) { 
				if (row == edgeRow) { 
					myEdgeIndex = noOfEqualEdges; 
				} 
				noOfEqualEdges++; 
				noOfSameNodeEdges++; 
			} else if ((edge.getSourceNode() == targetNode) && (edge.getTargetNode() == sourceNode)) { 
				noOfSameNodeEdges++; 
			} 
		} 
		
		double dx = x2 - x1, dy = y2 - y1; 

//		modify to add an offset relative to what this edge's index is 
		dx = dx * (1 + 0.7*myEdgeIndex); 
		dy = dy * (1 + 0.7*myEdgeIndex); 
		cp[0].setLocation(x1 + 2 * dx / 3, y1); 
		cp[1].setLocation(x2 - dx / 8, y2 - dy / 8); 
		
		
		if (useStraightLineForSingleEdges && (myEdgeIndex == 0) && (noOfSameNodeEdges == 1)) { 
			cp[0].setLocation(x2, y2); 
			cp[1].setLocation(x1, y1); 
		}
	}
	
//	protected Shape getRawShape(VisualItem item) {
//        EdgeItem   edge = (EdgeItem)item;
//        VisualItem item1 = edge.getSourceItem();
//        VisualItem item2 = edge.getTargetItem();
//        
//        int type = m_edgeType;
//        
//        getAlignedPoint(m_tmpPoints[0], item1.getBounds(),
//                        m_xAlign1, m_yAlign1);
//        getAlignedPoint(m_tmpPoints[1], item2.getBounds(),
//                        m_xAlign2, m_yAlign2);
//        m_curWidth = (float)(m_width * getLineWidth(item));
//        
//        // create the arrow head, if needed
//        EdgeItem e = (EdgeItem)item;
//        if ( e.isDirected() && m_edgeArrow != Constants.EDGE_ARROW_NONE ) {
//            // get starting and ending edge endpoints
//            boolean forward = (m_edgeArrow == Constants.EDGE_ARROW_FORWARD);
//            Point2D start = null, end = null;
//            start = m_tmpPoints[forward?0:1];
//            end   = m_tmpPoints[forward?1:0];
//            
//            // compute the intersection with the target bounding box
//            VisualItem dest = forward ? e.getTargetItem() : e.getSourceItem();
//            int i = GraphicsLib.intersectLineRectangle(start, end,
//                    dest.getBounds(), m_isctPoints);
//            if ( i > 0 ) end = m_isctPoints[0];
//            
//            // create the arrow head shape
//            AffineTransform at = getArrowTrans(start, end, m_curWidth);
//            m_curArrow = at.createTransformedShape(m_arrowHead);
//            
//            // update the endpoints for the edge shape
//            // need to bias this by arrow head size
//            Point2D lineEnd = m_tmpPoints[forward?1:0]; 
//            lineEnd.setLocation(0, -m_arrowHeight);
//            at.transform(lineEnd, lineEnd);
//        } else {
//            m_curArrow = null;
//        }
//        
//        // create the edge shape
//        Shape shape = null;
//        double n1x = m_tmpPoints[0].getX();
//        double n1y = m_tmpPoints[0].getY();
//        double n2x = m_tmpPoints[1].getX();
//        double n2y = m_tmpPoints[1].getY();
//        switch ( type ) {
//            case Constants.EDGE_TYPE_LINE:          
//                m_line.setLine(n1x, n1y, n2x, n2y);
//                shape = m_line;
//                break;
//            case Constants.EDGE_TYPE_CURVE:
//                getCurveControlPoints(edge, m_ctrlPoints,n1x,n1y,n2x,n2y);
//                m_cubic.setCurve(n1x, n1y,
//                                m_ctrlPoints[0].getX(), m_ctrlPoints[0].getY(),
//                                m_ctrlPoints[1].getX(), m_ctrlPoints[1].getY(),
//                                n2x, n2y);
//                shape = m_cubic;
//                break;
//            default:
//                throw new IllegalStateException("Unknown edge type");
//        }
//        
//        // return the edge shape
//        return shape;
//    }
//	
//    protected static void getAlignedPoint(Point2D p, Rectangle2D r, int xAlign, int yAlign) {
//        
//    	double x = r.getX(), y = r.getY(), w = r.getWidth(), h = r.getHeight();
//        if ( xAlign == Constants.CENTER ) {
//            x = x+(w/2);
//        } else if ( xAlign == Constants.RIGHT ) {
//            x = x+w;
//        }
//        
//        if ( yAlign == Constants.CENTER ) {
//            y = y+(h/2);
//        } else if ( yAlign == Constants.BOTTOM ) {
//            y = y+h;
//        }
//        
//        p.setLocation(x,y);
//    }
}
