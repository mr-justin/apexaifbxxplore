package org.apexlab.service.session.datastructure;

import java.util.LinkedList;

/**
 * This class represents query graphs
 * @author tpenin
 */
public class QueryGraph implements Query {
   
	// Target variable
	public Facet targetVariable;
	// List of nodes (Facet objects)
	public LinkedList<Facet> vertexList;
	// List of edges (GraphEdge objects)
	public LinkedList<GraphEdge> edgeList;
	// List of mappings
	public LinkedList<GraphEdge> mappingList;
	
	/**
	 * Default constructor
	 */
	public QueryGraph() {
		this.edgeList = new LinkedList<GraphEdge>();
		this.vertexList = new LinkedList<Facet>();
		this.targetVariable = null;
		this.mappingList = null;
	}

	public QueryGraph(Facet targetVariable, LinkedList<Facet> vertexList,
			LinkedList<GraphEdge> edgeList, LinkedList<GraphEdge> mappingList) {
		this.targetVariable = targetVariable;
		this.vertexList = vertexList;
		this.edgeList = edgeList;
		this.mappingList = mappingList;
	}

	public Facet getTargetVariable() {
		return targetVariable;
	}

	public void setTargetVariable(Facet targetVariable) {
		this.targetVariable = targetVariable;
	}

	public LinkedList<Facet> getVertexList() {
		return vertexList;
	}

	public void setVertexList(LinkedList<Facet> vertexList) {
		this.vertexList = vertexList;
	}

	public LinkedList<GraphEdge> getEdgeList() {
		return edgeList;
	}

	public void setEdgeList(LinkedList<GraphEdge> edgeList) {
		this.edgeList = edgeList;
	}
	
	public LinkedList<GraphEdge> getMappingList() {
		return mappingList;
	}

	public void setMappingList(LinkedList<GraphEdge> mappingList) {
		this.mappingList = mappingList;
	}

	public void print()
	{
		System.out.println("Target Set:");
		if(targetVariable!=null)
			System.out.println(targetVariable.getURI());
		else System.out.println("null");
		System.out.println("Vertex Set:");
		if(vertexList!=null)
		for(Facet nodes: vertexList)
		{
			System.out.println(nodes.getURI());
		}
		System.out.println("Edge Set:");
		if(edgeList!=null)
		for(GraphEdge edge: edgeList)
		{
			
			String subject_str = edge.getFromElement() == null ? "null" : edge.getFromElement().getURI();
			String object_str = edge.getToElement() == null ? "null" : edge.getToElement().getURI();
			if(edge.getFromElement() instanceof Concept)
				subject_str += "("+((Concept)edge.getFromElement()).variableLetter+")";
			if(edge.getToElement() instanceof Concept)
				object_str += "("+((Concept)edge.getToElement()).variableLetter+")";
			System.out.println(subject_str+" -> "+edge.getDecorationElement().getURI()+" -> "+object_str);
			
		}
	}
}
