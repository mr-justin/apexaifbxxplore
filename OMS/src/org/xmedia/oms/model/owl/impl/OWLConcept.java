package org.xmedia.oms.model.owl.impl;

import java.util.Map;
import java.util.Set;

import org.xmedia.oms.model.api.IOntology;
import org.xmedia.oms.model.impl.NamedConcept;
import org.xmedia.oms.model.owl.api.IAnnotationProperty;
import org.xmedia.oms.model.owl.api.IOWLConcept;

public class OWLConcept extends NamedConcept implements IOWLConcept {

	/**
	 * 
	 */
	private static final long serialVersionUID = 920248329483798384L;

	public OWLConcept(String uri) {
		super(uri);
	}
	
	public OWLConcept(String uri, IOntology onto) {
		super(uri, onto);
	}

	public Object getAnnotationValue(IOntology ontology,
			IAnnotationProperty annotationProperty) {
		// TODO Auto-generated method stub
		return null;
	}

	public Map<IAnnotationProperty, Set<Object>> getAnnotationValues(
			IOntology ontology) {
		// TODO Auto-generated method stub
		return null;
	}

	public Set<Object> getAnnotationValues(IOntology ontology,
			IAnnotationProperty annotationProperty) {
		// TODO Auto-generated method stub
		return null;
	}

}
