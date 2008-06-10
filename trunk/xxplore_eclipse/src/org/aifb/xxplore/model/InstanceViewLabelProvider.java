package org.aifb.xxplore.model;

import java.util.HashSet;
import java.util.Set;

import org.aifb.xxplore.model.InstanceViewContentProvider.Container;
import org.aifb.xxplore.model.InstanceViewContentProvider.InstanceTreeNode;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.xmedia.oms.model.api.INamedConcept;
import org.xmedia.oms.model.api.INamedIndividual;
import org.xmedia.oms.model.api.IProperty;
import org.xmedia.oms.model.api.IPropertyMember;
import org.xmedia.oms.model.api.IResource;

public class InstanceViewLabelProvider implements ILabelProvider{
	private ImageRegistry m_imageregistry;
	private Set<ILabelProviderListener> m_listeners;

	
	public InstanceViewLabelProvider() {
		m_listeners = new HashSet<ILabelProviderListener>();
		m_imageregistry = new ImageRegistry();
	}

	public Image getImage(Object element) {
		if (element instanceof Container){
			if (InstanceViewContentProvider.INDIVIDUALS.equals (((Container)element).getPredicate()))
				return m_imageregistry.getIndividualImage();
			if (InstanceViewContentProvider.PROPERTIES.equals (((Container)element).getPredicate()))
				return m_imageregistry.getPropertyImage();
		}
		if (element instanceof InstanceTreeNode){
			return getImage(((InstanceTreeNode)element).getValue());
		}
		if (element instanceof INamedConcept){
			return m_imageregistry. getConceptImage(); 
		}
		if (element instanceof INamedIndividual){
			return m_imageregistry. getIndividualImage(); 
		}
		if (element instanceof IProperty){
			return m_imageregistry. getPropertyImage(); 
		}
		if (element instanceof IPropertyMember){
//			return m_imageregistry. getPropertyInstanceImage();
			return null;
		}
		return null;
	}

	
	

	public String getText(Object element) {
		if (element instanceof Container)
			return ((Container)element).getPredicate();
		if (element instanceof String)
			return (String)element;
		if (element instanceof IResource){
			if (element instanceof IPropertyMember) {
				IPropertyMember prop = (IPropertyMember)element;
				String name = prop.getProperty().getLabel() + ": " +
					prop.getTarget().getLabel();
				return name;
			}
			else {
				String name=((IResource)element).getLabel();
				int i =-1;
				if ((i = name.lastIndexOf("/"))!=-1){
					return name.substring(i+1);
				}
				return name;
			}
		}
		if (element instanceof InstanceTreeNode)
			return getText(((InstanceTreeNode)element).getValue());
		return element.toString();
	}

	public void addListener(ILabelProviderListener listener) {
		m_listeners.add(listener);
	}

	public void removeListener(ILabelProviderListener listener) {		
		m_listeners.remove(listener);
	}

	
	public void dispose() {
		m_imageregistry.dispose();
		m_imageregistry =null;
		m_listeners.clear();
		m_listeners= null;
	}

	public boolean isLabelProperty(Object element, String property) {

		return false;
	}


}
