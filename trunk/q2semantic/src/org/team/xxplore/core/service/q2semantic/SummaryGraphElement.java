package org.team.xxplore.core.service.q2semantic;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import org.team.xxplore.core.service.api.IResource;
import org.team.xxplore.core.service.impl.Datatype;
import org.team.xxplore.core.service.impl.Literal;
import org.team.xxplore.core.service.impl.NamedConcept;
import org.team.xxplore.core.service.impl.ObjectProperty;
import org.team.xxplore.core.service.impl.Property;



public class SummaryGraphElement implements Serializable,ISummaryGraphElement {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final int CONCEPT = 0;

	public static final int VALUE = 1;
	
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

	protected IResource resource;

	protected double EF;
	
	private double m_totalCost;
	
	private double m_matchingScore;

	protected int type;
	
	protected String datasource;

	protected Map<String,Queue<Cursor>> cursors;

	public Set<Set<Cursor>> m_exploredCursorCombinations;
	
	public Set<Set<Cursor>> m_newCursorCombinations;

	private boolean m_coverageApplied = false; 
	
	public SummaryGraphElement(){}

	public SummaryGraphElement(IResource resource, int type) {
		this.resource = resource;
		this.type = type;
	}

	public SummaryGraphElement(IResource resource, int type, double EF) {
		this.resource = resource;
		this.type = type;
		this.EF = EF;
	}

	public SummaryGraphElement(IResource resource, int type, double score, double EF) {
		this.resource = resource;
		this.type = type;
		this.m_matchingScore = score;
		this.EF = EF;
	}

	public int getType(){
		return type;
	}
	
	public void setType(int type) {
		this.type = type;
	}

	public void setResource(IResource resource){
		this.resource = resource;
	}

	public IResource getResource(){
		return resource;
	}
	public void setEF(double EF){
		this.EF = EF;
	}
	public void setCursors(Map<String,Queue<Cursor>> cursors) {
		this.cursors = cursors;
	}

	public double getEF(){
		return EF;
	}

	public void applyCoverage(int coverage){
		if (!m_coverageApplied) m_totalCost = m_totalCost / coverage; 
	}
	
	public void setMatchingScore(double score){
		this.m_matchingScore = score;
	}

	public double getMatchingScore(){
		return m_matchingScore;
	}
		
	public void setDatasource(String datasrouce){
		this.datasource = datasrouce;
	}
	
	public String getDatasource(){
		return datasource;
	}
	
	public String toString(){
//		if(resource != null) return resource.toString();
		if(resource == null)return null;
		if(resource instanceof NamedConcept)
			return ((NamedConcept)resource).getUri();
		else if(resource instanceof Datatype )
			return ((Datatype)resource).getUri();
		else if(resource instanceof Property )
			return ((Property)resource).getUri();
		else if(resource instanceof Literal)
			return ((Literal)resource).getLabel();
		else return super.toString();
	}

	public Map<String,Queue<Cursor>> getCursors(){
		return cursors;
	}

	public double getTotalCost() {
		return m_totalCost;
	}

	public void setTotalCost(double cost) {
		m_totalCost = cost;
	}

	public void initCursorQueues(Set<String> keywords){
		cursors = new HashMap<String,Queue<Cursor>>();
		for(String keyword : keywords){
			cursors.put(keyword, new PriorityQueue<Cursor>());
		}
	}
	
	public void addCursor(Cursor cursor, String keyword){
		Queue<Cursor> q = cursors.get(keyword);
		q.add(cursor);
		if(isConnectingElement()){
			processCursorCombinations(cursor,keyword);
		}
	}
	
	public void processCursorCombinations(Cursor cursor, String keyword){
		int size = cursors.size();
		int[] guard = new int[size];
		int i = 0;
		Set<String> keywords = cursors.keySet();
		List<List<Cursor>> lists = new ArrayList<List<Cursor>>();
		for(String key : keywords){
			if(key.equals(keyword)){
				List<Cursor> list = new ArrayList<Cursor>();
				list.add(cursor);
				lists.add(list);
				guard[i++] = 0;
			}
			else {
				lists.add(new ArrayList<Cursor>(cursors.get(key)));
				guard[i++] = cursors.get(key).size() - 1;
			}
		}
		m_newCursorCombinations = new HashSet<Set<Cursor>>();
		
		int[] index = new int[size];
		for(int p : index) {
			p = 0;
		} 
		guard[size-1]++;
		do {
			Set<Cursor> combination = new HashSet<Cursor>();
			for(int m = 0; m < size; m++){
				combination.add(lists.get(m).get(index[m]));
			}
			m_newCursorCombinations.add(combination);
			index[0]++;
			for(int j = 0; j < size; j++){
				if(index[j] > guard[j]){
					index[j] = 0;
					index[j+1]++; 
				}
			}
		}
		while(index[size-1] < guard[size-1]);
	}
	
	public boolean isConnectingElement() {
		if(m_exploredCursorCombinations != null && m_exploredCursorCombinations.size() > 0) return true;
		
		if(cursors == null || cursors.size() == 0) return false;
		for(Queue<Cursor> queue : cursors.values()){
			if(queue.isEmpty()){
				return false;
			}	
		}
		return true;
	}
	
	
	public void addExploredCursorCombinations(Set<Set<Cursor>> combinations){
		if(combinations != null && combinations.size() != 0)
		{
			if(m_exploredCursorCombinations==null)m_exploredCursorCombinations = new HashSet<Set<Cursor>>();
			m_exploredCursorCombinations.addAll(combinations);
		}
	}
	
	public Set<Set<Cursor>> getExploredCursorCombinations(){
		return m_exploredCursorCombinations;
	}
	
	/**
	 * Return only the subgraphs that not have been explored before, i.e. only those 
	 * that are not in the list of explored subgraphs (getExploredCursorCombinations())
	 * 
	 */
	public Set<Set<Cursor>> getNewCursorCombinations(){
		return m_newCursorCombinations;
	}
	
	public void clearNewCursorCombinations() {
		m_newCursorCombinations = null;
	}
	
	public boolean equals(Object object){
		if(this == object) return true;
		if(object == null) return false;
		if(!(object instanceof SummaryGraphElement)) return false;
		
		//need to create unique Relation and Attribute Element 
		SummaryGraphElement vertex = (SummaryGraphElement)object;
//		if(vertex.getType() == RELATION || vertex.getType() == ATTRIBUTE) return false;
		
		if(vertex.datasource==null || this.datasource==null || !vertex.datasource.equals(this.datasource)) {
			if (!(vertex.datasource == null && this.datasource == null))
				return false;
		}
		if(resource instanceof NamedConcept && vertex.getResource() instanceof NamedConcept)
			return ((NamedConcept)resource).getUri().equals(((NamedConcept)vertex.getResource()).getUri());
		else if(resource instanceof Datatype && vertex.getResource() instanceof Datatype)
			return ((Datatype)resource).getUri().equals(((Datatype)vertex.getResource()).getUri());
		else if(resource instanceof Property && vertex.getResource() instanceof Property) {
			
			String uri1 = ((Property)resource).getUri();
			String uri2 = ((Property)vertex.getResource()).getUri();
			
//			if(uri1.indexOf("(") != -1 || uri2.indexOf("(") != -1) {
//				return SummaryGraphUtil.removeNum(uri1).equals(SummaryGraphUtil.removeNum(uri2));
//			}
			return uri1.equals(uri2);
		}
		else if(resource instanceof Literal&& vertex.getResource() instanceof Literal)
			return ((Literal)resource).getLabel().equals(((Literal)vertex.getResource()).getLabel());
//		if(resource.getClass().equals(vertex.getResource().getClass()))
//		System.out.println(resource.getClass());
		return false;
	}
	
	public int hashCode(){
//		return SummaryGraphUtil.getResourceUri(this).hashCode();
		return resource.hashCode();
	}

}
