package org.xmedia.oms.adapter.kaon2.persistence.model.adaptation;

import java.util.Set;

import org.xmedia.oms.adapter.kaon2.persistence.service.adaption.CognitiveAgentDao;
import org.xmedia.oms.adapter.kaon2.persistence.service.adaption.ICognitiveAgentDao;

public class CognitiveAgent implements ICognitiveAgent {
	
	private String m_uri;

    public CognitiveAgent() {
    } 
    
    public CognitiveAgent(String uri) {
    	m_uri = uri;
    }


	public Set<IResource> getRecomendation() {
		ICognitiveAgentDao dao = CognitiveAgentDao.getInstance();
		return dao.getRecommendation(this);
	}

	public String getUri() {
		return m_uri;
	}

	public void setApplicationInteraction(IApplicationInteraction interaction) {
		ICognitiveAgentDao dao = CognitiveAgentDao.getInstance();
		dao.setApplicationInteraction(this, interaction);
	}

	public void storeCognitiveAgent() {
		ICognitiveAgentDao dao = CognitiveAgentDao.getInstance();
		dao.insert(this);
	}

	public void setCredential(ICredential credential) {
		ICognitiveAgentDao dao = CognitiveAgentDao.getInstance();
		dao.setCredentail(this, credential);
	}

	public void setInteretingEntity(IEntity entity) {
		ICognitiveAgentDao dao = CognitiveAgentDao.getInstance();
		dao.setInterestingEntity(this, entity);
	}
}
