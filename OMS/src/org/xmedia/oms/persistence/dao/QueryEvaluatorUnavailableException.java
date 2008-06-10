package org.xmedia.oms.persistence.dao;


public class QueryEvaluatorUnavailableException extends Exception {
	
	private static final long serialVersionUID = -8808283368002621302L;
	
	private int queryLanguage;
	
	public QueryEvaluatorUnavailableException(int queryLanguage) {
		this.queryLanguage = queryLanguage;
	}

	public int getQueryLanguage() {
		return queryLanguage;
	}
	
	@Override
	public String getMessage() {
		return "Query evaluator does not exist for query language: '" + getQueryLanguage() + "'.";
	}

}
