package org.xmedia.oms.adapter.kaon2.persistence.model.adaptation;

import java.util.Set;

import org.xmedia.oms.adapter.kaon2.persistence.service.adaption.ApplicationInteractionDao;
import org.xmedia.oms.adapter.kaon2.persistence.service.adaption.IApplicationInteractionDao;

public class ApplicationInteraction extends Process implements IApplicationInteraction {
	
    public ApplicationInteraction() {
    } 
    
    public ApplicationInteraction(String uri) {
    	super(uri);
    }

	public Set<IComputerAidedProcess> getComputerAidedProcess() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setComputerAidedProcess(IComputerAidedProcess process) {
		IApplicationInteractionDao dao = ApplicationInteractionDao.getInstance();
		dao.setComputerAidedProcess(this, process);
	}

	public String getUri() {
		return m_uri;
	}

	public void storeApplicationInteraction() {
		IApplicationInteractionDao dao = ApplicationInteractionDao.getInstance();
		dao.insert(this);
	}
    
}
