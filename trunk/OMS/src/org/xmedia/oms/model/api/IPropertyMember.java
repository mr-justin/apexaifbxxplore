package org.xmedia.oms.model.api;

import org.xmedia.oms.metaknow.IReifiedElement;


public interface IPropertyMember extends IReifiedElement {
	
	public IProperty getProperty();
	
	public IResource getSource();
	
	public IResource getTarget();
	
	public int getType();
	
}
