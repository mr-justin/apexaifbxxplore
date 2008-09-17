package org.ateam.xxplore.core.service.search;

import java.util.Collection;
import java.util.Map;

import org.xmedia.oms.model.api.IDataProperty;
import org.xmedia.oms.model.api.INamedConcept;
import org.xmedia.oms.model.api.IResource;

public class SummaryGraphValueElement extends SummaryGraphElement{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Map<IDataProperty, Collection<INamedConcept>> neighbors;
	
	public SummaryGraphValueElement(IResource resource){
		super(resource, SummaryGraphElement.VALUE);
	}
	
	public SummaryGraphValueElement(IResource resource, double weight){
		super(resource, SummaryGraphElement.VALUE, weight);
	}
	
	public SummaryGraphValueElement(IResource resource, float score, double weight){
		super(resource, SummaryGraphElement.VALUE, score, weight);
	}
	
	public Map<IDataProperty, Collection<INamedConcept>> getNeighbors(){
//		System.out.println(neighbors==null);
		return neighbors;
	}	
	
	public void setNeighbors(Map<IDataProperty, Collection<INamedConcept>> neighbor) {
		this.neighbors = neighbor;
	}
}
