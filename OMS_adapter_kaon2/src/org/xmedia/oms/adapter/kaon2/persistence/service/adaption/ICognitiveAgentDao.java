package org.xmedia.oms.adapter.kaon2.persistence.service.adaption;

import java.util.Set;

import org.xmedia.oms.adapter.kaon2.persistence.model.adaptation.IApplicationInteraction;
import org.xmedia.oms.adapter.kaon2.persistence.model.adaptation.ICognitiveAgent;
import org.xmedia.oms.adapter.kaon2.persistence.model.adaptation.ICredential;
import org.xmedia.oms.adapter.kaon2.persistence.model.adaptation.IEntity;
import org.xmedia.oms.adapter.kaon2.persistence.model.adaptation.IResource;

public interface ICognitiveAgentDao extends IDao {

	public Set<IResource> getRecommendation(ICognitiveAgent agent);

	public void setApplicationInteraction(ICognitiveAgent agent, IApplicationInteraction interaction);
	
	public void setCredentail(ICognitiveAgent agent, ICredential credential);
	
	public void setInterestingEntity(ICognitiveAgent agent, IEntity entity);
	
	public void insert(ICognitiveAgent agent);

}
