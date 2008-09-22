package org.team.xxplore.core.service.search.datastructure;

import java.util.LinkedList;

/**
 * This class represents query graphs
 * @author tpenin
 */
public class QueryGraph implements Query {
   
	// The list of edges of the graph
	public LinkedList<GraphEdge> edgeList;
	
	/**
	 * Default constructor
	 */
	public QueryGraph() {
		super();
   	}

	/**
	 * @param edgeList
	 */
	public QueryGraph(LinkedList<GraphEdge> edgeList) {
		this.edgeList = edgeList;
	}

	/**
	 * @return the edgeList
	 */
	public LinkedList<GraphEdge> getEdgeList() {
		return edgeList;
	}

	/**
	 * @param edgeList the edgeList to set
	 */
	public void setEdgeList(LinkedList<GraphEdge> edgeList) {
		this.edgeList = edgeList;
	}
}
