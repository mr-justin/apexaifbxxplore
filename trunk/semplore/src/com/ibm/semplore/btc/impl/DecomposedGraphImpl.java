/**
 * 
 */
package com.ibm.semplore.btc.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeMap;

import com.ibm.semplore.btc.DecomposedGraph;
import com.ibm.semplore.btc.NodeInSubGraph;
import com.ibm.semplore.btc.SubGraph;
import com.ibm.semplore.btc.Visit;

/**
 * @author xrsun
 *
 */
public class DecomposedGraphImpl implements DecomposedGraph {
	TreeMap<Integer, SubGraph> subgraphs = new TreeMap<Integer, SubGraph>(); 
//	ArrayList<SubGraph> subgraphs = new ArrayList<SubGraph>();
	NodeInSubGraph targetVariable;

	/* (non-Javadoc)
	 * @see com.ibm.semplore.btc.DecomposedGraph#getSubgraphs()
	 */
	@Override
	public Collection<SubGraph> getSubgraphs() {
		return subgraphs.values();
	}

	/* (non-Javadoc)
	 * @see com.ibm.semplore.btc.DecomposedGraph#getTargetVariable()
	 */
	@Override
	public NodeInSubGraph getTargetVariable() {
		return targetVariable;
	}

	/* (non-Javadoc)
	 * @see com.ibm.semplore.btc.DecomposedGraph#setTargetVariable(com.ibm.semplore.btc.NodeInSubGraph)
	 */
	@Override
	public void setTargetVariable(NodeInSubGraph node) {
		targetVariable = node;
	}

	@Override
	public DecomposedGraph addSubGraph(SubGraph sg) {
		subgraphs.put(sg.getSubGraphID(), sg);
		return this;
	}

	private Visit pre, post;
	private HashSet<Integer> visit;
	@Override
	public void startTraverse(Visit pre, Visit post) {
		this.pre = pre; this.post = post;
		visit = new HashSet<Integer>();
		traverse(null, subgraphs.get(targetVariable.getSubGraphID()));
	}
	
	private void traverse(SubGraph parent, SubGraph g) {
		visit.add(g.getSubGraphID());
		if (pre!=null) pre.visit(parent, g);
		for (SubGraph child: getEdges(g))
			if (!visit.contains(child.getSubGraphID()))
				traverse(g, child);
		if (post!=null) post.visit(parent, g);
	}

	private ArrayList<SubGraph> getEdges(SubGraph g) {
		ArrayList<SubGraph> arr = new ArrayList<SubGraph>();
		for (int i=0; i<g.numOfNodes(); i++) {
			Iterator<NodeInSubGraph> mapping = g.getMappingConditions(i);
			while (mapping.hasNext()) {
				arr.add(subgraphs.get(mapping.next().getSubGraphID()));
			}
		}
		return arr;
	}
}
