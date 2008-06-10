package org.xmedia.oms.persistence;

public class InvalidParameterException extends Exception {

	private static final long serialVersionUID = -175581902968462376L;

	private String message = "";
	private String parameterName;
	
	public InvalidParameterException(String parameterName, String message, Throwable cause) {
		super(cause);
		
		this.parameterName = parameterName;
		this.message = (message == null ? "" : message); 
	}
	
	public InvalidParameterException(String parameterName, String message) {
		this(parameterName, message, null); 
	}
	
	public InvalidParameterException(String parameterName, Throwable cause) {
		this(parameterName, null, cause);
	}
	
	public InvalidParameterException(String parameterName) {
		this(parameterName, null, null);
	}

	public String getParamterName() {
		return parameterName;
	}
	
	@Override
	public String getMessage() {
		return "Invalid parameter: '" + getParamterName() + "'" + (message.length() > 0 ? ": " + message : "");  
	}
	
}
