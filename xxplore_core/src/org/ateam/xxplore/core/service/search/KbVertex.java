package org.ateam.xxplore.core.service.search;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.PriorityBlockingQueue;

import org.ateam.xxplore.core.service.search.NextQueryIntepretationService.Cursor;
import org.xmedia.oms.model.api.IResource;

public class KbVertex implements KbElement{
	
	private IResource resource;
	
	private double cost;
	
	private float score;
	
	private int type;
	
	private Map<String,Queue<Cursor>> cursors;
	
	public KbVertex(IResource resource, int type, double weight) {
		this.resource = resource;
		this.type = type;
		this.cost = weight;
	}
	
	public KbVertex(IResource resource, int type, float score, double weight) {
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
	
	public void createCursors(Set<String> keywords){
		cursors = new HashMap<String,Queue<Cursor>>();
		for(String keyword : keywords){
			cursors.put(keyword, new PriorityQueue<Cursor>());
		}
	}
	
	public Map<String,Queue<Cursor>> getCursors(){
		return cursors;
	}
	
	public boolean equals(Object object){
		if(this == object) return true;
		if(object == null) return false;
		if(!(object instanceof KbVertex)) return false;
		KbVertex vertex = (KbVertex)object;

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
	
}