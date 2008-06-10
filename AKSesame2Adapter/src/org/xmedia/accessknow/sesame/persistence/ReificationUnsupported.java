package org.xmedia.accessknow.sesame.persistence;

import org.xmedia.oms.model.api.IOntology;

public class ReificationUnsupported extends Exception {

	private static final long serialVersionUID = 4569889501736335036L;

	private IOntology ontology = null;
	
	public ReificationUnsupported(IOntology ontology) {
		this.ontology = ontology;
	}
	
	public IOntology getOntology() {
		return ontology;
	}
	
	@Override
	public String getMessage() {
		return "Reification unsupported for ontology: '" + getOntology().getUri() + "'";  
	}
}
