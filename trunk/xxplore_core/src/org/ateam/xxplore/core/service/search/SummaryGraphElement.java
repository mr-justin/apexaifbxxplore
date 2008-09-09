package org.ateam.xxplore.core.service.search;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import org.xmedia.oms.model.api.IResource;
import org.xmedia.oms.model.impl.ObjectProperty;

public class SummaryGraphElement implements ISummaryGraphElement {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final int CONCEPT = 0;

	public static final int DATATYPE = 5;
	
	public static final int ATTRIBUTE = 2;

	public static final int RELATION = 3;

	public static final int DUMMY_VALUE = 4;

	public static final String DUMMY_VALUE_LABEL = "dummy_value";
	
	public static final String DUMMY_DATATYPE_LABEL = "dummy_datattype";

	public static final String SUBCLASS_ELEMENT_URI = "http://subclass_uri";

	public static final double SUBCLASS_ELEMENT_DEFAULT_SCORE = 0;

	public static final SummaryGraphElement SUBCLASS = new SummaryGraphElement(
			new ObjectProperty(SUBCLASS_ELEMENT_URI), RELATION, SUBCLASS_ELEMENT_DEFAULT_SCORE);

	private IResource resource;

	private double cost;

	private float score;

	private int type;

	private Map<String,Queue<Cursor>> cursors;

	public SummaryGraphElement(){}

	public SummaryGraphElement(IResource resource, int type) {
		this.resource = resource;
		this.type = type;
	}

	public SummaryGraphElement(IResource resource, int type, double weight) {
		this.resource = resource;
		this.type = type;
		this.cost = weight;
	}

	public SummaryGraphElement(IResource resource, int type, float score, double weight) {
		this.resource = resource;
		this.type = type;
		this.score = score;
		this.cost = weight;
	}

	public int getType(){
		return type;
	}

	public void setResource(IResource resource){
		this.resource = resource;
	}

	public IResource getResource(){
		return resource;
	}
	public void setCost(double weight){
		this.cost = weight;
	}

	public double getCost(){
		return cost;
	}

	public void setScore(float score){
		this.score = score;
	}

	public float getScore(){
		return score;
	}

	public boolean equals(Object object){
		if(this == object) return true;
		if(object == null) return false;
		if(!(object instanceof SummaryGraphElement)) return false;
		SummaryGraphElement vertex = (SummaryGraphElement)object;

		if (!resource.equals(vertex.getResource()))  return false;
		return true;
	}


	public int hashCode(){
		return resource.hashCode();
	}

	public String toString(){
		if(resource != null) return resource.toString();
		else return super.toString();
	}



	public void createCursors(Set<String> keywords){
		cursors = new HashMap<String,Queue<Cursor>>();
		for(String keyword : keywords){
			cursors.put(keyword, new PriorityQueue<Cursor>());
		}
	}

	public Map<String,Queue<Cursor>> getCursors(){
		return cursors;
	}


	class Cursor implements Comparable {

		private SummaryGraphElement m_matching;

		private double cost;

		List<SummaryGraphEdge> edges;

		public Cursor(){}

		public Cursor(SummaryGraphElement matchingElement, double cost, List<SummaryGraphEdge> edges){
			this.m_matching = matchingElement;
			this.cost = cost;
			this.edges = edges;
		} 

		public double getCost(){
			return cost;
		}

		public List<SummaryGraphEdge> getPath(){
			return edges;
		}

		public SummaryGraphElement getMatchingVertex(){
			return m_matching;
		}

		public int compareTo(Object o) {
			Cursor other = (Cursor)o;
			if(cost > other.cost) {
				return 1;
			}
			if(cost < other.cost) {
				return -1;
			}
			return 0;
		}

		@Override
		public boolean equals(Object o){
			if(this == o) {
				return true;
			}
			if(!(o instanceof Cursor)) {
				return false;
			}
			Cursor other = (Cursor)o;
			if(cost != other.getCost()) {
				return false;
			}
			if(!(m_matching.equals(other.getMatchingVertex()))) {
				return false;
			}
			if(!(edges.equals(other.getPath()))) {
				return false;
			}
			return true;
		}

		@Override
		public int hashCode(){
			int code = 0;
			code += 7*m_matching.hashCode() + 13*edges.hashCode();
			return code;
		}

		@Override
		public String toString(){
			return "cost: " + cost 
			+ "\n" + "matchingVertex: " + m_matching
			+ "\n" + "Path: " + edges
			+ "\n";
		}
		
	}


}
