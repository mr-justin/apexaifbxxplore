package org.ateam.xxplore.core.service.q2semantic;

import java.util.HashSet;
import java.util.LinkedList;


public class Cursor implements Comparable {

	private SummaryGraphElement m_matching;

	private SummaryGraphElement m_element;
	
	private String m_keyword;
	
	private double cost;
	
	private SummaryGraphEdge incomingEdge; 
	
	private LinkedList<SummaryGraphEdge> m_path;
//	private HashSet<SummaryGraphEdge> m_path_set;
	
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
		LinkedList<SummaryGraphEdge> path = new LinkedList<SummaryGraphEdge>();
		getVisitedPath();
		int code = 0;
		
		int code1 = parent == null ? 0 : parent.hashCode();
		int code2 = incomingEdge == null ? 0 : incomingEdge.hashCode();
		code += 7*m_matching.hashCode() + 13*(code1 + code2);
		this.hashcode = code;
//		m_path_set = new HashSet<SummaryGraphEdge>();
//		for(SummaryGraphEdge edge : m_path) {
//			m_path_set.add(edge);
//		}
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
//		System.out.println(m_path.contains(e));
//		System.out.println(m_path.size());
		return e.equals(incomingEdge); 
//		return m_path.contains(e);
//		return m_path_set.contains(e);
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
		
//		if( !(this.m_element.equals(other.m_element))) {
//			return false;
//		}
		// == 可能有问题，因为从开始节点到末节点不止只有一条路径 ==
		if(!(m_path.equals(other.getPath()))) {
			return false;
		}
		return true;
	}
	
//	private int getHashCode() {
//		int code = 0;
//		code += 7*m_matching.hashCode() + 13*m_path.hashCode();
//		return code;
//	}

	@Override
	public int hashCode(){
		return this.hashcode;
	}

	@Override
	public String toString(){
		return "cost: " + cost 
		+ "\n" + "matchingVertex: " + m_matching
		+ "\n" + "Path: " + m_path
		+ "\n";
	}
	
}