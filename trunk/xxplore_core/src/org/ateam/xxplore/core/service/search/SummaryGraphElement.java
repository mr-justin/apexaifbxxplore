package org.ateam.xxplore.core.service.search;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import org.aifb.xxplore.shared.exception.Emergency;
import org.xmedia.oms.model.api.IResource;
import org.xmedia.oms.model.impl.Datatype;
import org.xmedia.oms.model.impl.NamedConcept;
import org.xmedia.oms.model.impl.ObjectProperty;
import org.xmedia.oms.model.impl.Property;

public class SummaryGraphElement implements ISummaryGraphElement {

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

	protected double cost;
	
	private double m_totalScore;
	
	private double m_matchingScore;

	protected int type;
	
	protected String datasource;

	protected Map<String,Queue<Cursor>> cursors;

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
		this.m_matchingScore = score;
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

	public double getEF(){
		return cost;
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

	public boolean equals(Object object){
		if(this == object) return true;
		if(object == null) return false;
		if(!(object instanceof SummaryGraphElement)) return false;
		SummaryGraphElement vertex = (SummaryGraphElement)object;
//		============================================by Kaifeng Xu============================================
		if(resource instanceof NamedConcept && vertex.getResource() instanceof NamedConcept)
			return ((NamedConcept)resource).getUri().equals(((NamedConcept)vertex.getResource()).getUri());
		else if(resource instanceof Datatype && vertex.getResource() instanceof Datatype)
			return ((Datatype)resource).getUri().equals(((Datatype)vertex.getResource()).getUri());
		else if(resource instanceof Property && vertex.getResource() instanceof Property)
			return ((Property)resource).getUri().equals(((Property)vertex.getResource()).getUri());
//		============================================by Kaifeng Xu============================================
		return false;
	}


	public boolean isConnectingElement() {
		if(cursors == null || cursors.size() == 0) return false;
		for(Queue<Cursor> queue : cursors.values()){
			if(queue.isEmpty()){
				return false;
			}	
		}
		return true;
	}
	
	
	public Cursor[][] getCursorCombinations() {
		int size = cursors.size();
		int[] guard = new int[size];
		int i = 0;
		for(Collection<Cursor> list : cursors.values()){
			guard[i++] = list.size()-1;
		}
		
		int entrySize = mul(guard,size);
		Cursor[][] entries = new Cursor[entrySize][size];

		int[] index = new int[size];
		for(int p : index) {
			p = 0;
		} 
		guard[size-1]++;
		i = 0;
//		do {
//			for(int m = 0; m < size; m++){
//				entries[i][m] = cursors.get(m).get(index[m]);
//			}
//			i++;
//			index[0]++;
//			for(int j = 0; j < size; j++){
//				if(index[j] > guard[j]){
//					index[j] = 0;
//					index[j+1]++; 
//				}
//			}
//		}
		while(index[size-1] < guard[size-1]);

		return entries;
	}
	
	private int mul(int a[],int n){
		return n>0?((a[n-1]+1)*mul(a,--n)):1;
	}

	
	
	
	public int hashCode(){
		return resource.hashCode();
	}

	public String toString(){
		if(resource != null) return resource.toString();
		else return super.toString();
	}



	public void initCursorQueues(Set<String> keywords){
		cursors = new HashMap<String,Queue<Cursor>>();
		for(String keyword : keywords){
			cursors.put(keyword, new PriorityQueue<Cursor>());
		}
	}
	
	public void addCursor(Cursor cursor, String keyword){
		Emergency.checkPrecondition(cursors != null && cursors.size() > 0, "Cursor queues not initialized!");
		Queue<Cursor> q = cursors.get(keyword);
		q.add(cursor);
	}

	public Map<String,Queue<Cursor>> getCursors(){
		return cursors;
	}

	public double getTotalScore() {
		return m_totalScore;
	}

	public void setTotalScore(double score) {
		m_totalScore = score;
	}
}
