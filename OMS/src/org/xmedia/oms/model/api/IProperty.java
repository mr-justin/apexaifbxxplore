package org.xmedia.oms.model.api;

import java.util.Set;



public interface IProperty extends IEntity{
	
	
	/**
	 * 
	 * @return classes / datatypes that is referred to as domain or range 
	 */
	public Set<? extends IResource> getDomainsAndRanges();

	/**
	 * 
	 * @return classes that is referred to as domain 
	 */
	public Set<INamedConcept> getDomains();
	
	/**
	 * 
	 * @return classes that is referred to as range 
	 */
	public Set<? extends IResource> getRanges();
	
	
	/**
	 * 
	 * @return classes that is referred to as domain or range 
	 */
	public Set<IPropertyMember> getMemberIndividuals();
	
	public int getNumberOfPropertyMember();
		
}
