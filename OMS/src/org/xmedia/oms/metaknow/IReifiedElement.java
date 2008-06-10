package org.xmedia.oms.metaknow;

import java.util.Date;
import java.util.Set;

import org.xmedia.oms.model.api.IAxiom;
import org.xmedia.oms.model.api.IEntity;
import org.xmedia.oms.model.api.INamedIndividual;
import org.xmedia.oms.model.api.IResource;

public interface IReifiedElement extends IEntity, IAxiom {
	
	public double[] getConfidenceDegree() throws ProvenanceUnknownException;
	
	public Set<INamedIndividual> getAgents() throws ProvenanceUnknownException;
	
	public Set<IEntity> getMetaknowledgeSources() throws ProvenanceUnknownException;
	
	public Set<Date> getCreationDates() throws ProvenanceUnknownException;
	
	public Set<IProvenance> getProvenances() throws ProvenanceUnknownException;
	
	public IResource getElement();
	
}
