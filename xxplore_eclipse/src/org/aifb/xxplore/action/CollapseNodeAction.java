package org.aifb.xxplore.action;

import org.aifb.xxplore.views.graphviewer.GraphControl;
import org.apache.log4j.Logger;
import org.ateam.xxplore.core.ExploreEnvironment;
import org.eclipse.jface.action.Action;
import org.xmedia.oms.model.api.IResource;

import prefuse.visual.VisualItem;

public class CollapseNodeAction extends Action {
	
	private VisualItem m_item;
	private IResource m_res;
	private GraphControl m_graphControl;
	
	private static Logger s_log = Logger.getLogger(CollapseNodeAction.class);
	
	
	public CollapseNodeAction(VisualItem item, GraphControl graphControl) {
		
		m_graphControl = graphControl;
		m_item = item;
		m_res = (IResource)m_item.get(ExploreEnvironment.RESOURCE);

		
	}
	
	public void run() {
		
		if(m_graphControl.collapseNode(m_item))
			s_log.debug("VISUALITEM '"+m_res.getLabel()+"' SUCCESSFULLY COLLAPSED");
		else
			s_log.debug("VISUALITEM '"+m_res.getLabel()+"' NOT COLLAPSED - PROPABLY WASN'T EXPANDED.");
		
	}
	
	public String getText() {
		return "Collapse";
	}
	
	public String getToolTipText(){
		return "Hides direct neighbors of selected node";
	}
}
