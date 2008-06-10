package org.xmedia.oms.persistence.dao;

import java.util.HashSet;
import java.util.Set;

import org.xmedia.businessobject.IBusinessObject;

public class BOsDeletionException extends Exception {

	private static final long serialVersionUID = 6484677174712130670L;
	
	private Set<? extends IBusinessObject> objects;
	
	public BOsDeletionException(Set<? extends IBusinessObject> objects) {
		this(objects, null);
	}
	
	public BOsDeletionException(Set<? extends IBusinessObject> objects, Exception rootCause) {
		super(rootCause);
		this.objects = objects;
	}

	public Set<IBusinessObject> getObjects() {
		return new HashSet<IBusinessObject>(objects);
	}
	
	@Override
	public String getMessage() {
		return "Cannot delete business objects: '" + objects + "'";  
	}

}
