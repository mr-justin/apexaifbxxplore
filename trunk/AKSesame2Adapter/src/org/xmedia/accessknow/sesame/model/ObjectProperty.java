package org.xmedia.accessknow.sesame.model;

import org.xmedia.oms.model.api.IOntology;

public class ObjectProperty extends org.xmedia.oms.model.impl.ObjectProperty {
	
	private static final long serialVersionUID = 3573042979429917254L;

	protected ObjectProperty(String uri) {
		super(uri);
	}

	protected ObjectProperty(String uri, IOntology ontology) {
		super(uri, ontology);
	}

//	/**
//	 * @deprecated
//	 */
//	@Override
//	public String getLabel() {
//		return toString();
//	}

	@Override
	public String toString() {
		return getUri();
	}
	
	public static ObjectProperty createObjectProperty(String uri) {
		return new ObjectProperty(uri);
	}
	
	public static ObjectProperty createObjectProperty(String uri, IOntology ontology) {
		return new ObjectProperty(uri, ontology);
	}
}
