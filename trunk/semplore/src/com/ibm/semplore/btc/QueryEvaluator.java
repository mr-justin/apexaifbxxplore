/**
 * 
 */
package com.ibm.semplore.btc;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;

import com.ibm.semplore.model.SchemaObjectInfo;
import com.ibm.semplore.search.XFacetedResultSet;
import com.ibm.semplore.xir.DocStream;

/**
 * @author linna
 * 
 */
public interface QueryEvaluator {

	/**
	 * Evaluate a query given its query planner.
	 * 
	 * @param facetedQuery
	 * @return
	 * @throws Exception
	 */
	public XFacetedResultSetForMultiDataSources evaluate(QueryPlanner planner) throws Exception;

	/**
	 * Evaluate a query given its graph.
	 * 
	 * @param graph
	 * @return
	 * @throws Exception
	 */
	public XFacetedResultSetForMultiDataSources evaluate(Graph graph) throws Exception;

	/**
	 * Evaluate a query given its graph and some start cache, each cache
	 * consists of (index of node, result doc stream).
	 * 
	 * @param graph
	 * @param cache
	 * @return
	 * @throws Exception
	 */
	public XFacetedResultSetForMultiDataSources evaluate(Graph graph,
			HashMap<Integer, DocStream> startCache) throws Exception;

	/**
	 * 
	 * @param map
	 */
	public void setPathOfDataSource(Hashtable<String, File> map);

	public void setPathOfMappingIndex(File path);

	/**
	 * Get instances which has mapping to given an instance that is represented
	 * by (data source, doc id, URI).
	 * 
	 * @param dataSource
	 * @param docID
	 * @param URI
	 * @return
	 */
	public ArrayList<SchemaObjectInfoForMultiDataSources> getSeeAlso(String dataSource, int docID,
			String URI);

	/**
	 * Get information about given an instance that is represented by (data
	 * source, doc id, URI).
	 * 
	 * @param dataSource
	 * @param docID
	 * @param URI
	 * @return
	 */
	public String getArraySnippet(String dataSource, int docID, String URI);
	
	public Collection<String> getAvailableDatasources();

}
