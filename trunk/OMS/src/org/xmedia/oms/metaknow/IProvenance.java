package org.xmedia.oms.metaknow;

import java.util.Date;

import org.xmedia.oms.model.api.IEntity;
import org.xmedia.oms.model.api.INamedIndividual;
import org.xmedia.oms.model.api.IResource;

public interface IProvenance extends IResource{

	public String getUri();
	
	public INamedIndividual getAgent();
	
	public Double getConfidenceDegree();
	
	public Date getCreationDate();
	
	public IEntity getSource();
	
}
