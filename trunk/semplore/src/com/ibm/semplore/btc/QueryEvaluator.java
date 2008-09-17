/**
 * 
 */
package com.ibm.semplore.btc;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;

import com.ibm.semplore.search.SearchHelper;
import com.ibm.semplore.search.XFacetedResultSet;

/**
 * @author linna
 * 
 */
public interface QueryEvaluator {
	
	/**
	 * Evaluate a query given its query planner.
	 * @param facetedQuery
	 * @return
	 * @throws Exception
	 */
	public XFacetedResultSet evaluate(QueryPlanner planner) throws Exception;
	
	/**
	 * Evaluate a query given its graph.
	 * @param graph
	 * @return
	 * @throws Exception
	 */
	public XFacetedResultSet evaluate(Graph graph) throws Exception;
	
	/**
	 * Evaluate a query given its graph and some start cache.
	 * @param graph
	 * @param helper
	 * @return
	 * @throws Exception
	 */
	public XFacetedResultSet evaluate(Graph graph, SearchHelper helper) throws Exception;
	
	/**
	 * 
	 * @param map
	 */
	public void setPathOfDataSource(Hashtable<String, File> map);
	
	public void setPathOfMappingIndex(File path);
	
	/**
	 * get instances which has mapping to given instance
	 * @param ds
	 * @param instance
	 */
	public ArrayList<String> getSeeAlso(String ds, String instance);
	
	/**
	 * get information about given instance 
	 * @param ds
	 * @param instance
	 */
	public String getArraySnippet(String ds, String instance);

	/**
	 * get datasource facet of last evaluation
	 */
	public ArrayList<String> getDatasourceFacet();	
}
