package org.xmedia.oms.persistence;

import java.net.URI;

public class OntologyCreationException extends Exception {

	private static final long serialVersionUID = 6484677174712130670L;
	
	private URI ontologyUri;
	
	public OntologyCreationException(URI ontologyUri) {
		this(ontologyUri, null);
	}
	
	public OntologyCreationException(URI ontologyUri, Exception rootCause) {
		super(rootCause);
		this.ontologyUri = ontologyUri;
	}

	public URI getOntologyUri() {
		return ontologyUri;
	}
	
	@Override
	public String getMessage() {
		return "Cannot create ontology: '" + getOntologyUri() + "'." + 
		(getCause() != null ? " Root cause: "  + getCause().getMessage() + "." : "");  
	}

}
