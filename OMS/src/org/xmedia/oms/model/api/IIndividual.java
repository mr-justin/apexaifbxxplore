package org.xmedia.oms.model.api;

import java.util.Set;


public interface IIndividual extends IResource {
	/**
	 * 
	 * @return a set of property members, each containing this individual as the source or the target 
	 */
	public Set<IPropertyMember> getPropertyValues();
	
	/**
	 * 
	 * @return a set of property members, each containing this individual as the source 
	 */
	public Set<IPropertyMember> getPropertyFromValues();
	
	/**
	 * 
	 * @return a set of property members, each containing this individual as the target 
	 */	
	public Set<IPropertyMember> getPropertyToValues();
	
	/**
	 * 
	 * @return a set of property members, each containing this individual as the source or the target.
	 * the other source or target respectively, is an instance of IIndividual 
	 */	
	public Set<IPropertyMember> getObjectPropertyValues();
	
	/**
	 * 
	 * @return a set of property members, each containing this individual as the source. 
	 * the target is an instance of IIndividual 
	 */	
	public Set<IPropertyMember> getObjectPropertyFromValues();

	/**
	 * 
	 * @return a set of property members, each containing this individual as the target. 
	 * the source is an instance of IIndividual 
	 */	
	public Set<IPropertyMember> getObjectPropertyToValues();
}
