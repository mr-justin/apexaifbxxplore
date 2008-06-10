package org.xmedia.oms.metaknow;

public class ProvenanceUnknownException extends Exception {

	private static final long serialVersionUID = 4962406826325211987L;
	
	private IReifiedElement reifiedElement;
	
	public ProvenanceUnknownException(IReifiedElement theReifiedElement) {
		this(theReifiedElement, null);
	}
	
	public ProvenanceUnknownException(IReifiedElement theObject, Exception rootCause) {
		super(rootCause);
		this.reifiedElement = theObject;
	}
	
	@Override
	public String getMessage() {
		return "Provenance unknown for reified element: '" + getReifiedElement() + "'." +
		(getCause() != null ? "Root cause: " + getCause().getMessage() : "");
	}

	public IReifiedElement getReifiedElement() {
		return reifiedElement;
	}

}
