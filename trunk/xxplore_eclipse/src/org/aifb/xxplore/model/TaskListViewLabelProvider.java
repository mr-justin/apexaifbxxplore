package org.aifb.xxplore.model;

import org.aifb.xxplore.task.ITaskListElement;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;

public class TaskListViewLabelProvider implements ILabelProvider {

	private ImageRegistry m_imageregistry;
	
	public TaskListViewLabelProvider(){
		m_imageregistry = new ImageRegistry();
	}
	
	public Image getImage(Object element) {
		if (element instanceof ITaskListElement)
			return m_imageregistry.getTaskImage();
		return null;
	}

	public String getText(Object element) {
		if (element instanceof ITaskListElement)
			return ((ITaskListElement)element).getLabel();
		return null;
	}

	public void addListener(ILabelProviderListener listener) {

	}

	public void dispose() {

	}

	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	public void removeListener(ILabelProviderListener listener) {

	}

}
