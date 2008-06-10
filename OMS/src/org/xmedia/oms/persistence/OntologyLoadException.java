package org.xmedia.oms.persistence;

import java.net.URI;

public class OntologyLoadException extends Exception {

	private static final long serialVersionUID = 6484677174712130670L;
	
	private URI ontologyUri;
	
	public OntologyLoadException(URI ontologyUri) {
		this(ontologyUri, null);
	}
	
	public OntologyLoadException(URI ontologyUri, Exception rootCause) {
		super(rootCause);
		this.ontologyUri = ontologyUri;
	}

	public URI getOntologyUri() {
		return ontologyUri;
	}
	
	@Override
	public String getMessage() {
		return "Cannot load ontology: '" + getOntologyUri() + "'";  
	}

}
