package org.ateam.xxplore.core.service.search;

import java.util.LinkedList;

class Cursor implements Comparable {

	private SummaryGraphElement m_matching;

	private SummaryGraphElement m_element;
	
	private String m_keyword;
	
	private double cost;
	
	private SummaryGraphEdge incomingEdge; 
	
	private LinkedList<SummaryGraphEdge> m_path;
	
	private Cursor m_parent;
	
	public Cursor(){}

	public Cursor(SummaryGraphElement element, SummaryGraphElement matchingElement, SummaryGraphEdge incomingedge, Cursor parent, String keyword, double cost){
		this.m_matching = matchingElement;
		this.cost = cost;
		this.incomingEdge = incomingedge;
		m_parent = parent;
		m_element = element;
		m_keyword = keyword;
		LinkedList<SummaryGraphEdge> path = new LinkedList<SummaryGraphEdge>();
		m_path = getVisitedPath(path, incomingEdge, parent);
	} 
	
	/**
	 * Return the path represented by the cursor. Keyword element is the first element in the list. 
	 * @param path
	 * @param parent
	 * @return
	 */
	private LinkedList<SummaryGraphEdge> getVisitedPath(LinkedList<SummaryGraphEdge> path, SummaryGraphEdge edge, Cursor parent){
		if(edge == null || parent == null) return path;
		path.addFirst(edge);
		return getVisitedPath(path, parent.getEdge(), parent.getParent());
	}

	public boolean hasVisited(SummaryGraphEdge e){
//		System.out.println(m_path.contains(e));
//		System.out.println(m_path.size());
		return m_path.contains(e);
//		for(SummaryGraphEdge edge: m_path)
//			if(edge.getSource().equals(e)||edge.getTarget().equals(e))
//			{//System.out.println("aaa");
//				return true;
//			}
//	
//		return false;
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
		int code = 0;
		code += 7*m_matching.hashCode() + 13*m_path.hashCode();
		return code;
	}

	@Override
	public String toString(){
		return "cost: " + cost 
		+ "\n" + "matchingVertex: " + m_matching
		+ "\n" + "Path: " + m_path
		+ "\n";
	}
	
}