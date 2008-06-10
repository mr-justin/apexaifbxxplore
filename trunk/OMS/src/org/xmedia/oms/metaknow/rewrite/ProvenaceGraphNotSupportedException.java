package org.xmedia.oms.metaknow.rewrite;


public class ProvenaceGraphNotSupportedException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	String m_tripleURI; 
	public ProvenaceGraphNotSupportedException(String tripleURI, Exception rootCause) {
		super(rootCause);
		m_tripleURI = tripleURI;
	}

	public ProvenaceGraphNotSupportedException(String tripleURI) {
		m_tripleURI = tripleURI;
	}
	
	@Override
	public String getMessage() {
		return "Provenance can not compute for the statement :" + m_tripleURI  + 
		("Root cause: " + getCause().getMessage());
	}


}
