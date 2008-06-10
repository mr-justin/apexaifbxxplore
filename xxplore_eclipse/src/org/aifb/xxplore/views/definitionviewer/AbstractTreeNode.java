package org.aifb.xxplore.views.definitionviewer;

import org.aifb.xxplore.model.ModelDefinitionContentProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;

public abstract class AbstractTreeNode implements ITreeNode {

	protected ITreeNode m_parent;
	protected Shell m_shell;
	protected ModelDefinitionContentProvider m_contentProvider;
	
	public AbstractTreeNode(ITreeNode parent, ModelDefinitionContentProvider contentProvider) {
		m_parent = parent;
		m_contentProvider = contentProvider;
	}
	
	public ITreeNode getParent() {
		return m_parent;
	}
	
	protected String truncateUri(String uri) {
		return uri.indexOf("#") >= 0 ? uri.substring(uri.indexOf("#")) : uri;
	}
	
	public Image getImage() {
		return null;
	}
	
	public void setShell(Shell shell) {
		m_shell = shell;
	}
	
	public ModelDefinitionContentProvider getContentProvider() {
		return m_contentProvider;
	}
}
