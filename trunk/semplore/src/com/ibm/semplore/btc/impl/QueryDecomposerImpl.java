/**
 * 
 */
package com.ibm.semplore.btc.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import com.ibm.semplore.btc.DecomposedGraph;
import com.ibm.semplore.btc.Graph;
import com.ibm.semplore.btc.NodeInSubGraph;
import com.ibm.semplore.btc.QueryDecomposer;
import com.ibm.semplore.btc.SubGraph;
import com.ibm.semplore.model.Edge;
import com.ibm.semplore.model.GeneralCategory;

/**
 * @author xrsun
 *
 */
public class QueryDecomposerImpl implements QueryDecomposer {
	Graph graph;
	
	int numSubGraph = 0;
	// the map from subquery id to subgraph
	HashMap<Integer, SubGraph> map = new HashMap<Integer, SubGraph>();
	// the map from one subquery id to its internal id mapping
	HashMap<Integer, HashMap<Integer, Integer>> id = new HashMap<Integer, HashMap<Integer, Integer>>();
	                   //global_id -> local_id
	HashSet<Integer> visit_gid;

	public NodeInSubGraph convertToInternalID(int gid) {
		for (int i=0; i<numSubGraph; i++)
			if (id.get(i).get(gid)!=null) return new NodeInSubGraphImpl( id.get(i).get(gid), i); 
		return null;
	}
	
	private void traverse(int gid, int qid) {
		visit_gid.add(gid);
		// construct local graph id
		SubGraph sg = map.get(qid);
		HashMap<Integer, Integer> idmap = id.get(qid);
		if (sg==null) {
			sg = new SubGraphImpl(qid);
			sg.setDataSource(graph.getDataSource(gid));
			map.put(qid, sg);
			idmap = new HashMap<Integer,Integer>();
			id.put(qid, idmap);
		}
		sg.add(graph.getNode(gid));
		idmap.put(gid, idmap.size());
		
		// next we can add relations
		for (Iterator<Edge> itr = graph.getEdges(gid); itr.hasNext(); ) {
			Edge e = itr.next();
			//ensure that every relation be stored once
			if (e.getFromNode() == gid && visit_gid.contains(e.getToNode())) continue;
			if (e.getToNode() == gid && visit_gid.contains(e.getFromNode())) continue;
			int toID = e.getFromNode() + e.getToNode() - gid;
			
			traverse(toID, qid);
			
			idmap = id.get(qid);
			sg.add(e.getRelation(), 
					idmap.get(e.getFromNode()), 
					idmap.get(e.getToNode()));
		}
		
		int gid_local = id.get(qid).get(gid);
		// at the same time, construct I-graph
		for (Iterator<Edge> itr = graph.getIEdges(gid); itr.hasNext(); ) {
			Edge e = itr.next();
			if (e.getFromNode() == gid) {
				if (visit_gid.contains(e.getToNode())) continue;
				int newQID = map.size();
				traverse(e.getToNode(), newQID);
				
				int qid_local = id.get(newQID).get(e.getToNode());
				//and set target variable
				map.get(newQID).setTargetVariable(qid_local);
				map.get(newQID).addMappingConditions(qid_local,
						new NodeInSubGraphImpl(gid_local, qid));
				map.get(qid).addMappingConditions(gid_local,
						new NodeInSubGraphImpl(qid_local, newQID));
			} else throw new Error("fromNode not on this edge");
		}

	}
	
	/* (non-Javadoc)
	 * @see com.ibm.semplore.btc.QueryDecomposer#decompose(com.ibm.semplore.btc.Graph)
	 */
	@Override
	public DecomposedGraph decompose(Graph graph) {
		this.graph = graph;

		//construct i-graph
		//start from overall target, which is contained in subquery 0
		visit_gid = new HashSet<Integer>();
		traverse(graph.getTargetVariable(), 0); 

		DecomposedGraphImpl dgraph = new DecomposedGraphImpl();
		numSubGraph = 0;
		for (SubGraph g:map.values()) {
			dgraph.addSubGraph(g);
			numSubGraph ++;
		}
		dgraph.setTargetVariable(new NodeInSubGraphImpl(0,0)); //(0,0) is because target is the first to visit
		return dgraph;
	}
}
