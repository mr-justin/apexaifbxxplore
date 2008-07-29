package org.aifb.xxplore.views.dnd;


import org.aifb.xxplore.views.definitionviewer.ITreeNode;
import org.apache.log4j.Logger;
import org.ateam.xxplore.core.ExploreEnvironment;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.TransferData;
import org.xmedia.oms.model.api.IResource;

import prefuse.visual.VisualItem;

public class DefinitionDropAdapter extends ViewerDropAdapter {
	
	private static Logger s_log = Logger.getLogger(DefinitionDropAdapter.class);
	
	public DefinitionDropAdapter(TreeViewer viewer) {

		super(viewer);
	}
	
	public void dragEnter(DropTargetEvent event) {

		event.detail = DND.DROP_COPY;
		
		super.dragEnter(event);
	}
	

	public boolean performDrop(Object data) {

		Object target = getCurrentTarget(); 
		
		if (s_log.isDebugEnabled()) s_log.debug("perform drop on " + target);
			
		//must been dragged from awt graph viewer
		if (data == null) {
			
			// this is a hack
			// due to swt awt incomptability, it is yet not possible to transfer the data set on the awt drag event to the swt drop event 
			// therefore, a static variable is used to store the selection 
			VisualItem item = ExploreDragController.s_draggedItem;
			data = item.get(ExploreEnvironment.RESOURCE);
		}
		
		//must been dragged from swt outline page 
		else {
			//support dragging of one element only
			if (data instanceof IResource[] && ((IResource[])data).length == 1)
				data = ((IResource[])data)[0];
		}

		if (data != null && s_log.isDebugEnabled()) s_log.debug("perform drop with "+ data.toString() + " on " + target);

		if (target instanceof ITreeNode && data instanceof IResource) {
			((ITreeNode)target).setShell(getViewer().getControl().getShell());
			((ITreeNode)target).dropResource((IResource)data);
		}

		return true;
	}
	
	public boolean validateDrop(Object target, int operation, TransferData transferType) {		
		//support drop copy copy only 
		if (operation != DND.DROP_COPY) return false; 
		
		else return ElementTransfer.getInstance().isSupportedType(transferType)||
			TextTransfer.getInstance().isSupportedType(transferType);
	}

	
	
}
