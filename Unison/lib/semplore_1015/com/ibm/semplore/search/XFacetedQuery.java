/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 */
package com.ibm.semplore.search;

import com.ibm.semplore.model.CatRelConstraint;

/**
 * XFacetedQuery(XFQ) is a extended Facet-enabled query. It enables defining and
 * retreiving facets information of both category and relation along with the
 * query results. Each XFQ contains a query expression, a target pointer in the
 * expression and a result specification of facets.
 * 
 * @author liu Qiaoling
 * 
 */
public interface XFacetedQuery extends Query {

	/**
	 * Set the facets specification for the results of this XFQ.
	 * 
	 * @param resultSpec
	 */
	public void setResultSpec(XFacetedResultSpec resultSpec);

	/**
	 * Returns the facets specification for the results of this XFQ.
	 * 
	 * @return
	 */
	public XFacetedResultSpec getResultSpec();

	/**
	 * Set the query constraint of this XFQ.
	 * 
	 * @param constraint
	 *            TODO
	 * @param exp
	 */
	public void setQueryConstraint(CatRelConstraint constraint);

	/**
	 * Returns the query constraint of this XFQ.
	 * 
	 * @return
	 */
	public CatRelConstraint getQueryConstraint();

	/**
	 * Set the search target in query expression so that results there need to
	 * be returned along with the facets information. It is a pointer ranging
	 * from 0 to (size-1) inside the XFQ expression [C1,R1,C2,R2,C3,R3,...].
	 * 
	 * @param target
	 *            range from 0 to size-1 inside the XFQ expression
	 *            [C1,R1,C2,R2,C3,R3,...].
	 */
	public void setSearchTarget(int target);

	/**
	 * Returns the search target in query expression.
	 * 
	 * @return
	 */
	public int getSearchTarget();

}
