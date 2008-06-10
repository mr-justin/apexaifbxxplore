package org.xmedia.accessknow.sesame.model;

import org.xmedia.oms.model.api.IOntology;

public class DataProperty extends org.xmedia.oms.model.impl.DataProperty {
	
	private static final long serialVersionUID = -5735868367759712594L;

	protected DataProperty(String uri, IOntology ontology) {
		super(uri, ontology);
	}

	protected DataProperty(String uri) {
		super(uri);
	}

	public static DataProperty createDataProperty(String uri) {
		return new DataProperty(uri);
	}
	
	public static DataProperty createDataProperty(String uri, IOntology ontology) {
		return new DataProperty(uri, ontology);
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
}
