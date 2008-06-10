package org.xmedia.accessknow.sesame.model;

import org.xmedia.oms.model.api.IOntology;

public class NamedConcept extends org.xmedia.oms.model.impl.NamedConcept {
	
	private static final long serialVersionUID = -6022750802186232297L;

	protected NamedConcept(String uri) {
		super(uri);
	}

	protected NamedConcept(String uri, IOntology onto) {
		super(uri, onto);
	}
	
	

}
