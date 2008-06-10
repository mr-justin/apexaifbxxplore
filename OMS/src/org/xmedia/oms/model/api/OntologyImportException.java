package org.xmedia.oms.model.api;

public class OntologyImportException extends Exception {
	
	private static final long serialVersionUID = 7655167017746799468L;
	
	private String ontologyUri;
	private String language;
	
	public OntologyImportException(String ontologyUri, String language) {
		this(ontologyUri, language, null);
	}
	
	public OntologyImportException(String ontologyUri, String language, Exception rootCause) {
		super(rootCause);
		this.ontologyUri = ontologyUri;
		this.language = language;
	}
	
	@Override
	public String getMessage() {
		return "Cannot import data to ontology: '" + ontologyUri + "' using language '" + language + "'." + 
		(getCause() != null ? " Root cause: "  + getCause().getMessage() + "." : "");  
	}

}
