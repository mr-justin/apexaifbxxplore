package org.aifb.xxplore.model;

import java.util.HashSet;
import java.util.Set;

import org.aifb.xxplore.model.ElementContentProvider.Container;
import org.aifb.xxplore.model.ElementContentProvider.InstanceTreeNode;
import org.aifb.xxplore.model.ElementContentProvider.OutlineTreeNode;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.xmedia.oms.model.api.IDataProperty;
import org.xmedia.oms.model.api.INamedConcept;
import org.xmedia.oms.model.api.INamedIndividual;
import org.xmedia.oms.model.api.IObjectProperty;
import org.xmedia.oms.model.api.IProperty;
import org.xmedia.oms.model.api.IPropertyMember;
import org.xmedia.oms.model.api.IResource;

public class ElementLabelProvider implements ILabelProvider {
	
	private ImageRegistry m_imageregistry;
	
	private Set<ILabelProviderListener> m_listeners;

	public ElementLabelProvider() {
		m_listeners = new HashSet<ILabelProviderListener>();
		m_imageregistry = new ImageRegistry();
	}

	public Image getImage(Object element) {
		if (element instanceof Container){
			if (ElementContentProvider.INDIVIDUALS.equals (((Container)element).getPredicate()))
				return m_imageregistry.getIndividualImage();
			if (ElementContentProvider.PROPERTIES.equals (((Container)element).getPredicate()))
				return m_imageregistry.getPropertyImage();
		}
		if (element instanceof OutlineTreeNode){
			return getImage(((OutlineTreeNode)element).getValue());
		}
		if (element instanceof InstanceTreeNode){
			return getImage(((InstanceTreeNode)element).getValue());
		}
		if (element instanceof INamedConcept){
			return m_imageregistry.getConceptImage(); 
		}
		if (element instanceof INamedIndividual){
			return m_imageregistry.getIndividualImage(); 
		}
		if (element instanceof IObjectProperty){
			return m_imageregistry.getObjectPropertyImage(); 
		}
		if (element instanceof IDataProperty){
			return m_imageregistry.getDataPropertyImage(); 
		}
		if (element instanceof IPropertyMember){
			IProperty prop  = ((IPropertyMember)element).getProperty();
			if(prop instanceof IObjectProperty)
				return m_imageregistry.getObjectPropertyImage();
			else if(prop instanceof IDataProperty)
				return m_imageregistry.getDataPropertyImage(); 
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
		if (element instanceof OutlineTreeNode)
			return getText(((OutlineTreeNode)element).getValue());
		else if (element instanceof InstanceTreeNode)
			return getText(((InstanceTreeNode)element).getValue());
		else if (element != null) return element.toString();
		else return "";
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
