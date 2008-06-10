package org.xmedia.oms.model.api;

import org.xmedia.businessobject.IBusinessObject;


public interface IResource extends IBusinessObject{

	public String getLabel();
	
	public IOntology getOntology();
	
	/**
	 * set the object that is wrapped by this object
	 *
	 */
	public void setDelegate(Object delegate);
	
	public Object getDelegate();
	

}
