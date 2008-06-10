package org.aifb.xxplore.model;

import java.util.HashSet;
import java.util.Set;

import org.aifb.xxplore.views.definitionviewer.ITreeNode;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;

public class ModelDefinitionLabelProvider implements ILabelProvider{


	private Set<ILabelProviderListener> m_listeners;
	private ImageRegistry m_imageRegistry;

	public ModelDefinitionLabelProvider() {
		m_listeners = new HashSet<ILabelProviderListener>();
		m_imageRegistry = new ImageRegistry();
	}
	
	public void addListener(ILabelProviderListener listener) {
		m_listeners.add(listener);			
	}

	public void removeListener(ILabelProviderListener listener) {
		m_listeners.remove(listener);			
	}

	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	public void dispose() {		
		m_listeners.clear();
		m_listeners=null;
	}

	public Image getImage(Object element) {
		return ((ITreeNode)element).getImage();
	}

	public String getText(Object element) {
		return ((ITreeNode)element).getLabel();
	}


}
