/**
 * 
 */
package com.ibm.semplore.btc;

import java.util.Iterator;

import com.ibm.semplore.model.CatRelGraph;
import com.ibm.semplore.model.GeneralCategory;
import com.ibm.semplore.model.Relation;

/**
 * @author linna
 * 
 */
public interface SubGraph extends CatRelGraph {
	public int getSubGraphID();

	public String getDataSource();

	public int getTargetVariable();

	public void addMappingConditions(int nodeID, NodeInSubGraph target);
	
	/**
	 * Return all the mapping conditions for a node; return null if no mapping
	 * condition exists. Note that a mapping (c1,c2) is recorded in both of the
	 * two nodes.
	 * 
	 * @param nodeID
	 * @return
	 */
	public Iterator<NodeInSubGraph> getMappingConditions(int nodeID);

	public void setDataSource(String dataSource);

	public void setTargetVariable(int nodeID);

	/**
	 * Add a node of category to the graph.
	 * 
	 * @param cat
	 */
	public SubGraph add(GeneralCategory cat);

	/**
	 * Append an edge of relation to the graph, linking two nodes of categories.
	 * 
	 * @param rel
	 * @param fromNodeIndex
	 * @param toNodeIndex
	 * @return
	 */
	public SubGraph add(Relation rel, int fromNodeIndex, int toNodeIndex);
}
