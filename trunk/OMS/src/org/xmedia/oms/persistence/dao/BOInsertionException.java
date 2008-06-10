package org.xmedia.oms.persistence.dao;

import org.xmedia.businessobject.IBusinessObject;

public class BOInsertionException extends Exception {

	private static final long serialVersionUID = 6484677174712130670L;
	
	private IBusinessObject object;
	
	public BOInsertionException(IBusinessObject theObject) {
		this(theObject, null);
	}
	
	public BOInsertionException(IBusinessObject theObject, Exception rootCause) {
		super(rootCause);
		this.object = theObject;
	}

	public IBusinessObject getObject() {
		return object;
	}
	
	@Override
	public String getMessage() {
		return "Cannot insert business object: '" + getObject() + "'";  
	}

}
