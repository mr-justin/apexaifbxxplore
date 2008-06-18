package org.xmedia.oms.adapter.kaon2.persistence.model.interaction;

import java.util.Set;

public interface ICognitiveAgentDao {
	
	public ICognitiveAgent findCognitiveAgentByUri(String agentUri);
	public Set<ICognitiveAgent> findAllCognitiveAgents();
	public void saveCognitiveAgent(ICognitiveAgent agent);
	
}
