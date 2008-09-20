package org.team.xxplore.core.service.search.datastructure;

import java.util.LinkedList;

/**
 * This class represents query graphs
 * @author tpenin
 */
public class QueryGraph implements Query {
   
	// The list of edges of the graph
	public LinkedList<Couple> edgeList;
	
	/**
	 * Default constructor
	 */
	public QueryGraph() {
		super();
   	}

	/**
	 * @param edgeList
	 */
	public QueryGraph(LinkedList<Couple> edgeList) {
		this.edgeList = edgeList;
	}

	/**
	 * @return the edgeList
	 */
	public LinkedList<Couple> getEdgeList() {
		return edgeList;
	}

	/**
	 * @param edgeList the edgeList to set
	 */
	public void setEdgeList(LinkedList<Couple> edgeList) {
		this.edgeList = edgeList;
	}
}
