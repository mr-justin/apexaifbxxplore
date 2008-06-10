package org.xmedia.oms.persistence.dao;

import org.xmedia.businessobject.IBusinessObject;

public class BODeletionException extends Exception {

	private static final long serialVersionUID = 6484677174712130670L;
	
	private IBusinessObject object;
	
	public BODeletionException(IBusinessObject theObject) {
		this(theObject, null);
	}
	
	public BODeletionException(IBusinessObject theObject, Exception rootCause) {
		super(rootCause);
		this.object = theObject;
	}

	public IBusinessObject getObject() {
		return object;
	}
	
	@Override
	public String getMessage() {
		return "Cannot delete business object: '" + getObject() + "'";  
	}

}
