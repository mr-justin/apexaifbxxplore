package org.aifb.xxplore.model;

import java.util.HashSet;
import java.util.Set;

import org.aifb.xxplore.core.ExploreEnvironment;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.xmedia.oms.model.api.IResource;

import prefuse.data.Node;


public class ExploreEditorLabelProvider implements ITableLabelProvider, ILabelProvider{

	protected Set<ILabelProviderListener> m_listeners;
	
	public String getText(Object element) {
		if(element != null){
			Node node = (Node)element;
			Object res = node.get(ExploreEnvironment.RESOURCE);
			if(res != null && res instanceof IResource)
				return ((IResource)res).getLabel();	
		}	
		return "";
	}
	
	public Image getColumnImage(Object element, int columnIndex) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getColumnText(Object element, int columnIndex) {
		//TODO remove mock content
		return "columntext";	
	}

	public void addListener(ILabelProviderListener listener) {
		if (m_listeners==null) {
			m_listeners = new HashSet<ILabelProviderListener>();
		}
		m_listeners.add(listener);
	}

	public void removeListener(ILabelProviderListener listener) {
		m_listeners.remove(listener);// FIXME should it be more than one listener?
	}
	
	public void dispose() {
		m_listeners.clear();
		m_listeners=null;
	}

	public boolean isLabelProperty(Object element, String property) {
		// TODO Auto-generated method stub
		return false;
	}

	public Image getImage(Object element) {
		// TODO Auto-generated method stub
		return null;
	}

	

}
