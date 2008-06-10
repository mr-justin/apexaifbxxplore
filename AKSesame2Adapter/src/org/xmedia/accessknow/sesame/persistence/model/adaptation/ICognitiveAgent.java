package org.xmedia.accessknow.sesame.persistence.model.adaptation;

import java.util.Set;

public interface ICognitiveAgent {
	
	public String getUri();

	public void setApplicationInteraction(IApplicationInteraction appint);

	public void setInteretingEntity(IEntity entity);

	public void setCredential(ICredential credential);
	
	public Set<IResource> getRecomendation();
	
	public void storeCognitiveAgent();
	
}
