/**
 * 
 */
package com.ibm.semplore.btc;

import java.util.Iterator;

import com.ibm.semplore.model.CatRelGraph;
import com.ibm.semplore.model.Edge;
import com.ibm.semplore.model.GeneralCategory;
import com.ibm.semplore.model.Relation;

/**
 * @author linna
 * 
 */
public interface Graph extends CatRelGraph {

	/**
	 * Add a node of category to the graph.
	 * 
	 * @param cat
	 */
	public Graph add(GeneralCategory cat);

	/**
	 * Append an edge of relation to the graph, linking two nodes of categories.
	 * 
	 * @param rel
	 * @param fromNodeIndex
	 * @param toNodeIndex
	 * @return
	 */
	public Graph add(Relation rel, int fromNodeIndex, int toNodeIndex);
	
	/**
	 * Add an I-edge to this graph, linking two nodes which are matched in mapping
	 * @param edge
	 * @return
	 */
	public Graph addIEdges(Edge edge);
	/**
	 * Return the I-edges from or to the given node in the form of (fromNode, toNode, rel), 
	 * where fromNode always equals nodeIndex.
	 * @param nodeIndex
	 * @return
	 */
	public Iterator<Edge> getIEdges(int nodeIndex);
	
	/**
	 * Set the target variable of the query graph.
	 * @param nodeIndex
	 * @throws IndexOutOfBoundsException 
	 */
	public void setTargetVariable(int nodeIndex) throws IndexOutOfBoundsException;
	
	/**
	 * Return the target variable of the query graph.
	 * @return
	 */
	public int getTargetVariable();

	public Graph setDataSource(int nodeIndex, String ds);
	
	public String getDataSource(int nodeIndex);

	/**
     * Remove a relation, given a end node's index, return the other node's index.
     * @param rel
     * @param nodeIndex
     * @return
     */
    public int removeRelation(Relation rel, int nodeIndex);
}
