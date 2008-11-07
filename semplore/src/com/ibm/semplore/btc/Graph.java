/**
 * 
 */
package com.ibm.semplore.btc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
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
	 * Set the index of target variable of the query graph.
	 * This node should exist in this graph before calling
	 * @param nodeIndex
	 * @throws IndexOutOfBoundsException 
	 */
	public void setTargetVariable(int nodeIndex) throws IndexOutOfBoundsException;
	
	/**
	 * Return the target variable of the query graph.
	 * @return
	 */
	public int getTargetVariable();

	/**
	 * Set data source related with one node
	 * @param nodeIndex
	 * @param ds
	 * @return
	 */
	public Graph setDataSource(int nodeIndex, String ds);
	
	/**
	 * get data source related with one node
	 * @param nodeIndex
	 * @return
	 */
	public String getDataSource(int nodeIndex);

	/**
     * Remove a relation, given an end node's index, return the other node's index.
     * @param rel
     * @param nodeIndex
     * @return
     */
    public int removeRelation(Relation rel, int nodeIndex);
    
    /**
     * Remove an IEdge, given an end node's index, return the other node's index.
     * @param edge
     * @param nodeIndex
     * @return
     */
    public int removeIEdge(Edge edge, int nodeIndex);
    
    /**
     * Load graph from given file outputed from Graph.toString() 
     * @param file
     * @throws FileNotFoundException 
     * @throws IOException 
     * @throws NumberFormatException 
     */
    public void load(File file) throws FileNotFoundException, NumberFormatException, IOException;
}
