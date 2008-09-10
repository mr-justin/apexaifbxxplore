package org.ateam.xxplore.core.service.search;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

class Cursor implements Comparable {

	private SummaryGraphElement m_matching;

	private SummaryGraphElement m_element;
	
	private String m_keyword;
	
	private double cost;
	
	private LinkedList<SummaryGraphElement> m_path;
	
	private Cursor m_parent;
	
	public Cursor(){}

	public Cursor(SummaryGraphElement element, SummaryGraphElement matchingElement, Cursor parent, String keyword, double cost){
		this.m_matching = matchingElement;
		this.cost = cost;
		m_parent = parent;
		m_element = element;
		m_keyword = keyword;
		LinkedList<SummaryGraphElement> path = new LinkedList<SummaryGraphElement>();
		path.addFirst(m_element);
		m_path = getVisitedPath(path, parent);
	} 
	
	/**
	 * Return the path represented by the cursor. Keyword element is the first element in the list. 
	 * @param path
	 * @param parent
	 * @return
	 */
	private LinkedList<SummaryGraphElement> getVisitedPath(LinkedList<SummaryGraphElement> path, Cursor parent){
		if(parent == null) return path;
		path.addFirst(parent.getElement());
		return getVisitedPath(path, parent.getParent());
	}

	public boolean hasVisited(SummaryGraphElement e){
		return m_path.contains(e);
	}
	
	
	public double getCost(){
		return cost;
	}

	public LinkedList<SummaryGraphElement> getPath(){
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