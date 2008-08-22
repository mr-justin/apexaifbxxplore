package org.xmedia.accessknow.sesame.persistence.converter;

import org.xmedia.oms.model.api.IResource;

public class DelegatesManager {

	private static final boolean SET_DELEGATES = true;
	
	public static void setDelegate(IResource aResource, Object itsDelegate) {
		
		if (SET_DELEGATES)
			aResource.setDelegate(itsDelegate);
		
	}
	
}
