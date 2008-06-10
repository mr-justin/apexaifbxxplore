package org.xmedia.accessknow.sesame.persistence.model.adaptation;

public interface IEntity {
	
	public String getUri();

	public void setRelatedEntity(IEntity entity); 

	public void setInvolvingProcess(IProcess process);
	
	public void storeEntity();

}
