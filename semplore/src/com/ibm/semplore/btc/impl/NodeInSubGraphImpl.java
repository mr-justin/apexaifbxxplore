package com.ibm.semplore.btc.impl;

public class NodeInSubGraphImpl implements com.ibm.semplore.btc.NodeInSubGraph {
	int node;
	int subgraph;

	/**
	 * @param node local id within subgraph
	 * @param subgraph
	 */
	public NodeInSubGraphImpl(int node, int subgraph) {
		this.node = node;
		this.subgraph = subgraph;
	}
	
	@Override
	public int getNodeID() {
		return node;
	}

	@Override
	public int getSubGraphID() {
		return subgraph;
	}

}
