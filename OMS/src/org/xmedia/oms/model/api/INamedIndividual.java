package org.xmedia.oms.model.api;

import java.util.Set;


public interface INamedIndividual extends IEntity, IIndividual{
	
	public Set<IConcept> getTypes();
	
	/**
	 * 
	 * @return all properties where this individual is either the source or the target
	 */

	public Set<IProperty> getProperties();
	
	/**
	 * 
	 * @return all properties where this individual is the source
	 */
	public Set<IProperty> getPropertiesFrom();

	/**
	 * 
	 * @return all properties where this individual is the target
	 */
	public Set<IProperty> getPropertiesTo();
	

	
}

