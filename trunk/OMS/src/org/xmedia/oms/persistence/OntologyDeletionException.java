package org.xmedia.oms.persistence;

public class OntologyDeletionException extends Exception {

	private static final long serialVersionUID = -2468706011288743819L;

	public OntologyDeletionException(Exception rootCause) {
		super(rootCause);
	}
	
	@Override
	public String getMessage() {
		return "Cannot delete ontology(ies)." + 
		(getCause() != null ? " Root cause: "  + getCause().getMessage() + "." : "");  
	}
	
}
