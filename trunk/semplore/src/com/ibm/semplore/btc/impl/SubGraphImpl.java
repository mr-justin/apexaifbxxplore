package com.ibm.semplore.btc.impl;

import java.util.ArrayList;
import java.util.Iterator;

import com.ibm.semplore.btc.NodeInSubGraph;
import com.ibm.semplore.btc.SubGraph;
import com.ibm.semplore.model.CatRelGraph;
import com.ibm.semplore.model.Edge;
import com.ibm.semplore.model.GeneralCategory;
import com.ibm.semplore.model.Relation;
import com.ibm.semplore.model.impl.CatRelGraphImpl;

/**
 * @author xrsun
 *
 */
public class SubGraphImpl implements SubGraph {
	int id;
	String dataSource;
	int target;
	ArrayList<ArrayList<NodeInSubGraph>> mapping;
	CatRelGraph crgraph;

	public SubGraphImpl(int id) {
		this.id = id;
		mapping = new ArrayList<ArrayList<NodeInSubGraph>>();
		crgraph = new CatRelGraphImpl();
	}
	@Override
	public SubGraph add(GeneralCategory cat) {
		crgraph.add(cat);
		mapping.add(new ArrayList<NodeInSubGraph>());
		return this;
	}

	@Override
	public SubGraph add(Relation rel, int fromNodeIndex, int toNodeIndex) {
		crgraph.add(rel, fromNodeIndex, toNodeIndex);
		return this;
	}

	@Override
	public String getDataSource() {
		return dataSource;
	}

	@Override
	public void addMappingConditions(int nodeID, NodeInSubGraph target) {
		ArrayList<NodeInSubGraph> arr = mapping.get(nodeID);
		arr.add(target);
	}

	@Override
	public Iterator<NodeInSubGraph> getMappingConditions(int nodeID) {
		return mapping.get(nodeID).iterator();
	}

	@Override
	public int getTargetVariable() {
		return target;
	}

	@Override
	public void setDataSource(String dataSource) {
		this.dataSource = dataSource;
	}

	@Override
	public void setTargetVariable(int nodeID) {
		target = nodeID;
	}

	@Override
	public Iterator<Edge> getEdges(int nodeIndex) {
		return crgraph.getEdges(nodeIndex);
	}

	@Override
	public GeneralCategory getNode(int nodeIndex) {
		return crgraph.getNode(nodeIndex);
	}

	@Override
	public int numOfNodes() {
		return crgraph.numOfNodes();
	}

	@Override
	public int getSubGraphID() {
		return id;
	}

	public String toString() {
		return crgraph.toString();
	}
	@Override
	public int removeRelation(Relation rel, int nodeIndex) {
		return crgraph.removeRelation(rel, nodeIndex);
	}
}
