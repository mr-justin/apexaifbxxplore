package org.aifb.xxplore.model;

import org.aifb.xxplore.storedquery.IStoredQueryListElement;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;

public class StoredQueryViewLabelProvider implements ILabelProvider {


	private ImageRegistry m_imageregistry;
	
	public StoredQueryViewLabelProvider(){
		m_imageregistry = new ImageRegistry();
	}
	
	public Image getImage(Object element) {
		if (element instanceof IStoredQueryListElement)
			return m_imageregistry.getQueryImage();
		return null;
	}

	public String getText(Object element) {
		if (element instanceof IStoredQueryListElement)
			return ((IStoredQueryListElement)element).getLabel();
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
