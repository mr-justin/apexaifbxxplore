package org.xmedia.oms.model.api;

public interface IEntity extends IResource{

	public IOntology getOntology();
	
	/**
	 * every entity must have an uri 
	 * @return the URI of this entity 
	 */
	public String getUri();
	
}
