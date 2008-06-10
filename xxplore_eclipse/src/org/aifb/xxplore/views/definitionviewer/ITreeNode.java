package org.aifb.xxplore.views.definitionviewer;

import java.util.List;

import org.eclipse.jface.action.IAction;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.xmedia.oms.model.api.IResource;

public interface ITreeNode {
	public String getLabel();
	public Image getImage();
	public List<ITreeNode> getChildren();
	public boolean hasChildren();
	public ITreeNode getParent();
	public void dropResource(IResource res);
	public List<IAction> getContextMenuActions();
	public void setShell(Shell shell);
}
