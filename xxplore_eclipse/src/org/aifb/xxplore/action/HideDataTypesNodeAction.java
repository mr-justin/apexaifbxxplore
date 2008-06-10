package org.aifb.xxplore.action;

import org.aifb.xxplore.core.ExploreEnvironment;
import org.aifb.xxplore.views.graphviewer.GraphControl;
import org.aifb.xxplore.views.graphviewer.GraphViewer;
import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.xmedia.oms.model.api.IResource;

import prefuse.visual.VisualItem;

public class HideDataTypesNodeAction extends Action {

	private GraphControl m_graphControl;
	private VisualItem m_item;
	private IResource m_res; 
	
	private static Logger s_log = Logger.getLogger(GetDataTypesNodeAction.class);
	
	
	public HideDataTypesNodeAction(VisualItem item, GraphViewer graphViewer) {
		
		m_item = item;			
		m_res = (IResource)m_item.get(ExploreEnvironment.RESOURCE);
		m_graphControl = (GraphControl)graphViewer.getControl();

						
	}
	
	public void run() {
			
		if(m_graphControl.hideDatatypes(m_item))
			s_log.debug("VISUALITEM '"+m_res.getLabel()+"': SHOWING DATATYPES");
		else
			s_log.debug("'"+m_res.getLabel()+"' ERROR WHILE RETRIEVING DATATYPES");
		
	}
	
	public String getText() {		
		return "Hide Datatypes";
	}
}
