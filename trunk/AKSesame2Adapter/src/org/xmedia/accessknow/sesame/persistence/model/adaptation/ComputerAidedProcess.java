package org.xmedia.accessknow.sesame.persistence.model.adaptation;

import org.xmedia.accessknow.sesame.persistence.service.adaptation.ComputerAidedProcessDao;
import org.xmedia.accessknow.sesame.persistence.service.adaptation.IComputerAidedProcessDao;

public class ComputerAidedProcess extends Process implements IComputerAidedProcess {
	
    public ComputerAidedProcess() {
    } 
    
    public ComputerAidedProcess(String uri) {
    	super(uri);
    }

	public void setApplicationInteraction(IApplicationInteraction interaction) {
		IComputerAidedProcessDao dao = ComputerAidedProcessDao.getInstance();
		dao.setApplicationInteraction(this, interaction);
	}

	public String getUri() {
		return m_uri;
	}

	public void storeComputerAidedProcess() {
		IComputerAidedProcessDao dao = ComputerAidedProcessDao.getInstance();
		dao.insert(this);
	}

}
