package org.xmedia.oms.adapter.kaon2.persistence.model.adaptation;

import org.xmedia.oms.adapter.kaon2.persistence.service.adaption.IProcessDao;
import org.xmedia.oms.adapter.kaon2.persistence.service.adaption.ProcessDao;


public abstract class Process implements IProcess {
	
	protected String m_uri;

	public Process() {
	}

	public Process(String uri) {
		m_uri = uri;
	}

	public String getUri() {
		return m_uri;
	}

	public void setAgent(ICognitiveAgent agent) {
		IProcessDao dao = ProcessDao.getInstance();
		dao.setAgent(this, agent);
	}

	public void setInstrument(IDevice instrument) {
		IProcessDao dao = ProcessDao.getInstance();
		dao.setInstrument(this, instrument);
	}

	public void setPostProcess(IProcess process) {
		IProcessDao dao = ProcessDao.getInstance();
		dao.setPostProcess(this, process);
	}

	public void setResource(IContent content) {
		IProcessDao dao = ProcessDao.getInstance();
		dao.setResource(this, content);
	}

	public void setResrouce(IContentBearingObject cbo) {
		IProcessDao dao = ProcessDao.getInstance();
		dao.setResource(this, cbo);
	}
}

