package org.apexlab.service.q2semantic;

import java.util.Collection;
import java.util.Map;

import org.team.xxplore.core.service.api.INamedConcept;
import org.team.xxplore.core.service.api.IResource;
import org.team.xxplore.core.service.impl.DataProperty;



public class SummaryGraphValueElement extends SummaryGraphElement{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Map<DataProperty, Collection<INamedConcept>> neighbors;
	
	public SummaryGraphValueElement(IResource resource){
		super(resource, SummaryGraphElement.VALUE);
	}
	
	public SummaryGraphValueElement(IResource resource, double weight){
		super(resource, SummaryGraphElement.VALUE, weight);
	}
	
	public SummaryGraphValueElement(IResource resource, float score, double weight){
		super(resource, SummaryGraphElement.VALUE, score, weight);
	}
	
	public Map<DataProperty, Collection<INamedConcept>> getNeighbors(){
//		System.out.println(neighbors==null);
		return neighbors;
	}	
	
	public void setNeighbors(Map<DataProperty, Collection<INamedConcept>> neighbor) {
		this.neighbors = neighbor;
	}
}
