package org.xmedia.accessknow.sesame.model;

import org.xmedia.oms.model.api.IOntology;

public class NamedIndividual extends org.xmedia.oms.model.impl.NamedIndividual {

	private static final long serialVersionUID = 6420367746159249885L;
	
	protected NamedIndividual(String uri, IOntology onto) {
		super(uri, onto);
	}

//	@Override
//	public String getLabel() {
//		// TODO implement it!
//		return toString();
//	}

	@Override
	public String toString() {
		return getUri();
	}
}
