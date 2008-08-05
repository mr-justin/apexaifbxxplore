/**
 * 
 */
package org.aifb.xxplore.model;


import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;

import org.aifb.xxplore.ExplorePlugin;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
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

	@SuppressWarnings("deprecation")
	public Image getImage(Object element) {

		URL relativeURL = Platform.getBundle(ExplorePlugin.PLUGIN_ID).getEntry("/");
		try {
			return new Image(null, new FileInputStream(Platform.resolve(relativeURL).getPath()+"/icons/concept.png"));
		} catch (IOException e) {
			return null;
		}
	}


	public void addListener(ILabelProviderListener listener) {		
	}

	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	public void removeListener(ILabelProviderListener listener) {	
	}
}
