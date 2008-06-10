package org.xmedia.oms.model.api;

public class OntologyExportException extends Exception {
	
	private static final long serialVersionUID = 7655167017746799468L;
	
	private String ontologyUri;
	private String language;
	
	public OntologyExportException(String ontologyString, String language) {
		this(ontologyString, language, null);
	}
	
	public OntologyExportException(String ontologyString, String language, Exception rootCause) {
		super(rootCause);
		this.ontologyUri = ontologyString;
		this.language = language;
	}
	
	@Override
	public String getMessage() {
		return "Cannot export ontology: '" + ontologyUri + "' using language '" + language + "'." + 
		(getCause() != null ? " Root cause: "  + getCause().getMessage() + "." : "");  
	}

}
