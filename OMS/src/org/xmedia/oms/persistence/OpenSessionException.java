package org.xmedia.oms.persistence;


public class OpenSessionException extends Exception {

	private static final long serialVersionUID = 6484677174712130670L;
	
	private String ontologyUri;
	
	public OpenSessionException(String ontologyUri) {
		this(ontologyUri, null);
	}
	
	public OpenSessionException(String ontologyUri, Exception rootCause) {
		super(rootCause);
		this.ontologyUri = ontologyUri;
	}

	public String getOntologyUri() {
		return ontologyUri;
	}
	
	@Override
	public String getMessage() {
		return "Cannot open session for ontology: '" + getOntologyUri() + "'." + 
		(getCause() != null ? " Root cause: "  + getCause().getMessage() + "." : "");  
	}

}
