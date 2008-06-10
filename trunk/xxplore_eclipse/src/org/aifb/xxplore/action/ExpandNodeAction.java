package org.aifb.xxplore.action;

import org.aifb.xxplore.core.ExploreEnvironment;
import org.aifb.xxplore.views.graphviewer.GraphControl;
import org.aifb.xxplore.views.graphviewer.GraphViewer;
import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.xmedia.oms.model.api.IResource;

import prefuse.visual.VisualItem;

public class ExpandNodeAction extends Action {

	private GraphControl m_graphControl;
	private VisualItem m_item;
	private IResource m_res; 
	
	private static Logger s_log = Logger.getLogger(ExpandNodeAction.class);
	
	
	public ExpandNodeAction(VisualItem item, GraphViewer graphViewer) {
		
		m_item = item;			
		m_res = (IResource)m_item.get(ExploreEnvironment.RESOURCE);
		m_graphControl = (GraphControl)graphViewer.getControl();
						
	}
	
	public void run() {
			
		if(m_graphControl.expandNode(m_item))
			s_log.debug("VISUALITEM '"+m_res.getLabel()+"' SUCCESSFULLY EXPANDED");
		else
			s_log.debug("VISUALITEM '"+m_res.getLabel()+"' NOT EXPANDED - SOMETHING WENT WRONG");
		
	}
	
	public String getText() {	
		return "Expand";
	}
	
	public String getToolTipText(){
		return "Displays the direct neighbors of selected node";
	}
}
