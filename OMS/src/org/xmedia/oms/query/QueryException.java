package org.xmedia.oms.query;


public class QueryException extends Exception {
	
	private static final long serialVersionUID = -9112755214922255865L;
	
	private IQueryWrapper query;
	
	public QueryException(IQueryWrapper query) {
		this(query, null);
	}
	
	public QueryException(IQueryWrapper query, Exception rootCause) {
		super(rootCause);
		this.query = query;
	}

	public IQueryWrapper getQuery() {
		return query;
	}
	
	@Override
	public String getMessage() {
		return "Error occurred while executing query: '" + getQuery().getQuery() + "'." + 
		(getCause() != null ? " Root cause: "  + getCause().getMessage() + "." : "");  
	}

}
