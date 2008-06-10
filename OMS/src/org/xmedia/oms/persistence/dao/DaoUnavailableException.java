package org.xmedia.oms.persistence.dao;


public class DaoUnavailableException extends Exception {
	
	private static final long serialVersionUID = -8808283368002621302L;
	
	@Override
	public String getMessage() {
		return "Requested Dao is unavailable for this adaptor.";
	}

}
