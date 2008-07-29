package org.aifb.xxplore.views.graphviewer;

import java.awt.event.MouseEvent;
import java.util.Iterator;

import org.ateam.xxplore.core.ExploreEnvironment;

import prefuse.controls.ControlAdapter;
import prefuse.util.ColorLib;
import prefuse.visual.EdgeItem;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;

public class ExploreHoverController extends ControlAdapter {

	private String m_action;
	
	public ExploreHoverController(String action) {
		m_action = action;
	}
	
	private void setLabelVisible(EdgeItem edge, boolean isVisible) {
		if (edge.canSet(ExploreEnvironment.IS_LABEL_VISIBLE, Boolean.class)) {
			edge.set(ExploreEnvironment.IS_LABEL_VISIBLE, Boolean.valueOf(isVisible));
		}
	}
	
	public void itemEntered(VisualItem item, MouseEvent event)
	{
		if (item instanceof EdgeItem)
		{
			EdgeItem edge = ((EdgeItem) item);
			if (edge.getSourceItem().isVisible() && edge.getTargetItem().isVisible()) {
				edge.setHover(true);
//				PrefuseLib.updateVisible(edge, true);
				setLabelVisible(edge, true);
				edge.getSourceItem().setHover(true);
				edge.getTargetItem().setHover(true);
			}
		}
		else if (item instanceof NodeItem)
		{
			for (Iterator e = ((NodeItem)item).edges(); e.hasNext(); ) {
				EdgeItem edge = (EdgeItem) e.next();
				if (edge.getSourceItem().isVisible() && edge.getTargetItem().isVisible()) {
					edge.setHover(true);
					setLabelVisible(edge, true);
//					PrefuseLib.updateVisible(edge, true);
//					edge.setStartVisible(true);
//					edge.setVisible(true);
//					edge.setEndVisible(true);
					edge.setStrokeColor(ColorLib.rgba(100, 100, 255, 255));
					edge.getTargetItem().setHover(true);
					edge.getSourceItem().setHover(true);
				}
//				else
//					edge.setVisible(false);
			}
		}

		item.getVisualization().run(m_action);
		
	}
	public void itemExited(VisualItem item, MouseEvent event)
	{
		if (item instanceof EdgeItem)
		{
			EdgeItem edge = (EdgeItem)item;
			edge.setHover(false);
			setLabelVisible(edge, false);
//			PrefuseLib.updateVisible(edge, false);
			edge.getTargetItem().setHover(false);
			edge.getSourceItem().setHover(false);
		}
		else if (item instanceof NodeItem)
		{
			item.setHover(false);
			for (Iterator e = ((NodeItem)item).edges(); e.hasNext(); ) {
				EdgeItem edge = (EdgeItem) e.next();
				edge.setHover(false);
				setLabelVisible(edge, false);
//				PrefuseLib.updateVisible(edge, false);
				edge.setStrokeColor(ColorLib.rgba(200, 200, 255, 255));
				edge.getTargetItem().setHover(false);
				edge.getSourceItem().setHover(false);
			}
		}
		item.getVisualization().run(m_action);
		
	}
	
}
