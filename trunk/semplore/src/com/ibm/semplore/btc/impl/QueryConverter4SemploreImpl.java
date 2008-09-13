/**
 * 
 */
package com.ibm.semplore.btc.impl;

import com.ibm.semplore.btc.QueryConverter4Semplore;
import com.ibm.semplore.btc.SubGraph;
import com.ibm.semplore.search.SearchFactory;
import com.ibm.semplore.search.XFacetedQuery;
import com.ibm.semplore.search.impl.SearchFactoryImpl;

/**
 * @author xrsun
 *
 */
public class QueryConverter4SemploreImpl implements QueryConverter4Semplore {
	SearchFactory searchFactory = SearchFactoryImpl.getInstance();

	/* (non-Javadoc)
	 * @see com.ibm.semplore.btc.QueryConverter4Semplore#convertQuery(com.ibm.semplore.btc.SubGraph)
	 */
	@Override
	public XFacetedQuery convertQuery(SubGraph graph) {
		XFacetedQuery query = searchFactory.createXFacetedQuery();
		query.setSearchTarget(graph.getTargetVariable());
		query.setQueryConstraint(graph);
		query.setResultSpec(searchFactory.createXFacetedResultSpec());
		return query;
	}

}
