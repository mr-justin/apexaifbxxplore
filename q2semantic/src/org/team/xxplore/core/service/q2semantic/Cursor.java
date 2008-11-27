package org.team.xxplore.core.service.q2semantic;

import java.util.LinkedList;

/**
 * 
 * @author jqchen
 *
 */
public class Cursor implements Comparable {

	private SummaryGraphElement m_matching;

	private SummaryGraphElement m_element;
	
	private String m_keyword;
	
	private double cost;
	
	private SummaryGraphEdge incomingEdge; 
	
	private LinkedList<SummaryGraphEdge> m_path;
	
	private Cursor m_parent;
	
	private int hashcode;
	
	public Cursor(){}

	public Cursor(SummaryGraphElement element, SummaryGraphElement matchingElement, SummaryGraphEdge incomingedge, Cursor parent, String keyword, double cost){
		this.m_matching = matchingElement;
		this.cost = cost;
		this.incomingEdge = incomingedge;
		m_parent = parent;
		m_element = element;
		m_keyword = keyword;
		getVisitedPath();
		int code = 0;
		
		int code1 = parent == null ? 0 : parent.hashCode();
		int code2 = incomingEdge == null ? 0 : incomingEdge.hashCode();
		code += 7*m_matching.hashCode() + 13*(code1 + code2);
		this.hashcode = code;
	} 
	
	/**
	 * Return the path represented by the cursor. Keyword element is the first element in the list. 
	 * @param path
	 * @param parent
	 * @return
	 */
	private void getVisitedPath(){
		if(m_path == null) m_path = new LinkedList<SummaryGraphEdge>();
		
		if(m_parent != null) {
			m_path.addAll(m_parent.m_path);
		}
		
		if(incomingEdge != null) {
			m_path.add(incomingEdge);
		}

		//return getVisitedPath(path, parent.getEdge(), parent.getParent());
	}

	public boolean hasVisited(SummaryGraphEdge e){
		return e.equals(incomingEdge); 
	}
	
	
	public SummaryGraphEdge getEdge(){
		return incomingEdge;
	}
	
	public double getCost(){
		return cost;
	}

	public LinkedList<SummaryGraphEdge> getPath(){
		return m_path;
	}

	public Cursor getParent(){
		return m_parent;
	}
	
	public String getKeyword(){
		return m_keyword;
	}
	
	public SummaryGraphElement getMatchingElement(){
		return m_matching;
	}

	public SummaryGraphElement getElement(){
		return m_element;
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
	
	public int getLength(){
		if (m_path == null) return 0;
		else return m_path.size();
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
		if(!(m_matching.equals(other.getMatchingElement()))) {
			return false;
		}
		
		if(!(m_path.equals(other.getPath()))) {
			return false;
		}
		return true;
	}
	
	@Override
	public int hashCode(){
		return this.hashcode;
	}

	@Override
	public String toString(){
		return "cost: " + cost 
		+ "\n" + "Vertex: " + m_element
		+ "\n" + "matchingVertex: " + m_matching
		+ "\n" + "Path: " + m_path
		+ "\n";
	}
	
}