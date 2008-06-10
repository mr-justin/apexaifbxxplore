package org.xmedia.accessknow.sesame.persistence.service.adaptation;

import java.util.Set;

import org.xmedia.accessknow.sesame.persistence.model.adaptation.IApplicationInteraction;
import org.xmedia.accessknow.sesame.persistence.model.adaptation.ICognitiveAgent;
import org.xmedia.accessknow.sesame.persistence.model.adaptation.ICredential;
import org.xmedia.accessknow.sesame.persistence.model.adaptation.IEntity;
import org.xmedia.accessknow.sesame.persistence.model.adaptation.IResource;

public interface ICognitiveAgentDao extends IDao {

	public Set<IResource> getRecommendation(ICognitiveAgent agent);

	public void setApplicationInteraction(ICognitiveAgent agent, IApplicationInteraction interaction);
	
	public void setCredentail(ICognitiveAgent agent, ICredential credential);
	
	public void setInterestingEntity(ICognitiveAgent agent, IEntity entity);
	
	public void insert(ICognitiveAgent agent);

}
