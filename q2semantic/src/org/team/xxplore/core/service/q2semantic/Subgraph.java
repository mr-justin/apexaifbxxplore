package org.team.xxplore.core.service.q2semantic;

import java.util.LinkedHashSet;
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
	
	@Override
	public Set<SummaryGraphEdge> edgeSet() {
		// TODO Auto-generated method stub
		return super.edgeSet();
	}
	
	@Override
	public Set<SummaryGraphElement> vertexSet() {
		// TODO Auto-generated method stub
		return super.vertexSet();
	}

	private SummaryGraphElement connectingVertex;

	private Set<SummaryGraphEdge> paths;

	double cost;

	public Subgraph(Class<? extends SummaryGraphEdge> edgeclass) {
		super(edgeclass);
	}

	public Set<SummaryGraphEdge> getPaths() {
		return paths;
	}

	public void setPaths(Set<SummaryGraphEdge> paths) {
		if (paths == null || paths.size() == 0)
			return;
		if(this.paths == null){
			this.paths = new LinkedHashSet<SummaryGraphEdge>();
		}
		for (SummaryGraphEdge e : paths) {
			SummaryGraphEdge edge = SummaryGraphUtil.getGraphEdgeWithoutNum(e);
			this.paths.add(edge);
			addVertex(e.getSource());
			addVertex(e.getTarget());
			addEdge(e.getSource(), e.getTarget(), e);
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
		+ "\n" + "Paths: [EF][MatchingScore][TotalCost]\n";
		ret += "************\n";
		for(SummaryGraphEdge edge : paths) {
			ret += edge.toString() + "\n" + "\n";
		}
		ret +="************\n";
		return ret;
	}	
}