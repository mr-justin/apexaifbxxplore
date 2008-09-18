/**
 * 
 */
package com.ibm.semplore.btc;

import java.util.ArrayList;

/**
 * @author linna
 * 
 */
public interface QueryPlanner {

	/**
	 * Set the decomposed graph to be planned for query evaluation.
	 * 
	 * @param degraph
	 */
	public void setDecomposedGraph(DecomposedGraph degraph);
	
	public DecomposedGraph getDecomposedGraph();

	/**
	 * @see com.ibm.semplore.btc.DecomposedGraph.startTraverse(); 
	 */
	public void startTraverse(Visit pre, Visit post);
}
