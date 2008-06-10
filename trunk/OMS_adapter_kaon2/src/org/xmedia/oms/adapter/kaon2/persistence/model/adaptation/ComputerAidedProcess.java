package org.xmedia.oms.adapter.kaon2.persistence.model.adaptation;


import org.xmedia.oms.adapter.kaon2.persistence.service.adaption.ComputerAidedProcessDao;
import org.xmedia.oms.adapter.kaon2.persistence.service.adaption.IComputerAidedProcessDao;

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
