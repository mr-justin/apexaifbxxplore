/**
 * 
 */
package org.aifb.xxplore.model;


import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.graphics.Image;

import prefuse.data.Node;

import org.aifb.xxplore.core.ExploreEnvironment;
import org.xmedia.oms.model.api.IResource;



public class ConceptHierachyLabelProvider implements ILabelProvider {

	/**
	 * @see ILabelProvider#getText(Object)
	 */
	public String getText(Object element) {
		
		if(element != null && element instanceof IResource)	
		{
			return ((IResource)element).getLabel();	
		}	
		
		return new String();
	}

	public void dispose() {
	}

	public Image getImage(Object element) {
		return null;
	}

	public void addListener(ILabelProviderListener listener) {		
	}

	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	public void removeListener(ILabelProviderListener listener) {	
	}
}
