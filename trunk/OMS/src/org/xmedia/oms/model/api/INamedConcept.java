package org.xmedia.oms.model.api;

import java.util.Set;

import org.aifb.xxplore.shared.util.Pair;

public interface INamedConcept extends IEntity, IConcept{
	
	public Set<IIndividual> getMemberIndividuals();
	
	public Set<IIndividual> getMemberIndividuals(boolean includeInferred);
	
	public Set<IConcept> getSubconcepts();
	
	public Set<IConcept> getSuperconcepts();
	
	public Set<IProperty> getProperties();
	
	public Set<IProperty> getPropertiesFrom();
	
	public Set<Pair> getPropertiesAndRangesFrom();
	
	public Set<IProperty> getPropertiesTo();
	
	public int getNumberOfIndividuals();
	
}
