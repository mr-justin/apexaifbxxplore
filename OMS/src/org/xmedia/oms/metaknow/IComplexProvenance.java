package org.xmedia.oms.metaknow;

import java.util.Collection;
import java.util.Date;
import java.util.Set;

import org.xmedia.oms.model.api.IEntity;
import org.xmedia.oms.model.api.INamedIndividual;

public interface IComplexProvenance {

	public Set<INamedIndividual> getComplexAgent();
	
	public Collection<Double> getComplexConfidenceDegree();
	
	public Collection<Date> getComplexCreationDate();
	
	public Set<IEntity> getComplexSource();

}
