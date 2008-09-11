package org.ateam.xxplore.core.service.search;

import java.util.Collection;

import org.xmedia.oms.model.api.INamedConcept;
import org.xmedia.oms.model.api.IResource;

public class SummaryGraphAttributeElement extends SummaryGraphElement{
	
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
		return null;
	}
	
	public void setNeighborConcepts(Collection<INamedConcept> neighborConcepts){
		this.neighborConcepts = neighborConcepts;
	}

}
