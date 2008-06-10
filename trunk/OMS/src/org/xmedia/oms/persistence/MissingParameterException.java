package org.xmedia.oms.persistence;

public class MissingParameterException extends Exception {

	private static final long serialVersionUID = 7092201576099375717L;

	private String parameterName;
	
	public MissingParameterException(String parameterName) {
		this.parameterName = parameterName;
	}

	public String getParamterName() {
		return parameterName;
	}
	
	@Override
	public String getMessage() {
		return "Missing parameter: '" + getParamterName() + "'";  
	}
}
