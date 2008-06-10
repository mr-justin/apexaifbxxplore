package org.xmedia.oms.query;

public interface IQueryEvaluator {
	
	public IQueryResult evaluate(IQueryWrapper query) throws QueryException;
	
	public IQueryResult evaluateWithProvenance(IQueryWrapper query) throws QueryException;

}
