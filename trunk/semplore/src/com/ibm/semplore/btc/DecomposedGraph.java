/**
 * 
 */
package com.ibm.semplore.btc;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author linna
 *
 */
public interface DecomposedGraph {
	/**
	 * add a complete SubGraph;
	 * @param sg
	 * @return this
	 */
	public DecomposedGraph addSubGraph(SubGraph sg);
	
	/**
	 * Return the subgraphs after the decomposition.
	 * @return
	 */
	public Collection<SubGraph> getSubgraphs();
	
	/**
	 * Set the target variable of the query graph.
	 * @param node
	 */
	public void setTargetVariable(NodeInSubGraph node);
	
	/**
	 * Return the target variable of the query graph.
	 * @return
	 */
	public NodeInSubGraph getTargetVariable();

	/**
	 * start a traverse on the I-graph from the root
	 * and calls the two visit functions each time it visit one subquery
	 * which is passed to every visit functions.
	 * The visit functions can be null when they are of no use.
	 * @param pre
	 * @param post
	 */
	public void startTraverse(Visit pre, Visit post);
}
