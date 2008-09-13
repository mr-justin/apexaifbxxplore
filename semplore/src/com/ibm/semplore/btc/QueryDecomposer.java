/**
 * 
 */
package com.ibm.semplore.btc;

/**
 * @author linna
 * 
 */
public interface QueryDecomposer {

	/**
	 * Decompose an original query graph to subgraphs so that each subgraph can
	 * be processed by the backend engine.
	 * 
	 * @param graph
	 * @return
	 */
	public DecomposedGraph decompose(Graph graph);

}
