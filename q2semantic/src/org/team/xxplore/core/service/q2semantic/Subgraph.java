package org.team.xxplore.core.service.q2semantic;

import java.util.List;
import java.util.Set;

import org.jgrapht.graph.Pseudograph;

/**
 * The result of topk.
 * @author jqchen
 *
 */
public class Subgraph extends
		Pseudograph<SummaryGraphElement, SummaryGraphEdge>
		implements Comparable {

	private SummaryGraphElement connectingVertex;

	private Set<List<SummaryGraphEdge>> paths;

	double cost;

	public Subgraph(Class<? extends SummaryGraphEdge> edgeclass) {
		super(edgeclass);
	}

	public Set<List<SummaryGraphEdge>> getPaths() {
		return paths;
	}

	public void setPaths(Set<List<SummaryGraphEdge>> paths) {
		if (paths == null || paths.size() == 0)
			return;
		this.paths = paths;
		for (List<SummaryGraphEdge> path : paths) {
			if (path.size() == 0)
				continue;
			for (SummaryGraphEdge e : path) {
				addVertex(e.getSource());
				addVertex(e.getTarget());
				addEdge(e.getSource(), e.getTarget(), e);
			}
		}
	}

	public SummaryGraphElement getConnectingVertex() {
		return connectingVertex;
	}

	public void setConnectingElement(SummaryGraphElement connectingE) {
		connectingVertex = connectingE;
	}

	public void setCost(double cost) {
		this.cost = cost;
	}

	public double getCost() {
		return cost;
	}

	public int compareTo(Object o) {
		Subgraph other = (Subgraph) o;
		if (this.cost > other.cost) {
			return 1;
		}
		if (this.cost < other.cost) {
			return -1;
		}
		return 0;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof Subgraph)) {
			return false;
		}

		Subgraph other = (Subgraph) o;
		if (!(paths.equals(other.getPaths()))) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		return 13 * paths.hashCode();
	}

	@Override
	public String toString(){
		String ret = "cost: " + cost 
		+ "\n" + "Connecting vertex: " + connectingVertex
		+ "\n" + "Paths: \n";
		for(List<SummaryGraphEdge>path :paths) {
			ret += "************\n";
			for(SummaryGraphEdge edge : path) {
				ret += edge.toString() + "\n";
			}
			ret +="************\n";
		}
		return ret;
	}	
}