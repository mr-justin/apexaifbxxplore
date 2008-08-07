/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 */
package com.ibm.semplore.model;

import java.util.Iterator;

/**
 * This interface provides descriptive information about a graph, in which nodes are categories and edges are relations.
 * @author liu Qiaoling
 *
 */
public interface CatRelGraph extends CatRelConstraint {

	/**
	 * Add a node of category to the graph.
	 * @param cat
	 */
	public CatRelGraph add(GeneralCategory cat);
	
	/**
	 * Append an edge of relation to the graph, linking two nodes of categories.
	 * @param rel
	 * @param fromNodeIndex
	 * @param toNodeIndex
	 * @return
	 */
	public CatRelGraph add(Relation rel, int fromNodeIndex, int toNodeIndex);
	
    /**
     * Returns the node of category in the graph with given index.
     * @param nodeIndex
     * @return
     */
    public GeneralCategory getNode(int nodeIndex);
    
    /**
     * Return the edges from or to the given node in the form of (fromNode, toNode, rel).
     * @param nodeIndex
     * @return
     */
    public Iterator getEdges(int nodeIndex);
    
    /**
     * Returns the number of nodes in the graph.
     * @return
     */
    public int numOfNodes();
        
}
