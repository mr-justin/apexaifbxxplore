package org.team.xxplore.core.service.q2semantic;

import java.util.Collection;

import org.team.xxplore.core.service.api.INamedConcept;
import org.team.xxplore.core.service.api.IResource;



public class SummaryGraphAttributeElement extends SummaryGraphElement{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Collection<INamedConcept> neighborConcepts;
	
	public SummaryGraphAttributeElement(IResource resource){
		super(resource, SummaryGraphElement.VALUE);
	}
	
	public SummaryGraphAttributeElement(IResource resource, double weight){
		super(resource, SummaryGraphElement.VALUE, weight);
	}
	
	public SummaryGraphAttributeElement(IResource resource, float score, double weight){
		super(resource, SummaryGraphElement.VALUE, score, weight);
	}
	
	public Collection<INamedConcept> getNeighborConcepts(){
		return neighborConcepts;
	}
	
	public void setNeighborConcepts(Collection<INamedConcept> neighborConcepts){
		this.neighborConcepts = neighborConcepts;
	}

}
