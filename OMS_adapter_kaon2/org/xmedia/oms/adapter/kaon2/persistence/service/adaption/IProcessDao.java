package org.xmedia.oms.adapter.kaon2.persistence.service.adaption;

import org.xmedia.oms.adapter.kaon2.persistence.model.adaptation.ICognitiveAgent;
import org.xmedia.oms.adapter.kaon2.persistence.model.adaptation.IContent;
import org.xmedia.oms.adapter.kaon2.persistence.model.adaptation.IContentBearingObject;
import org.xmedia.oms.adapter.kaon2.persistence.model.adaptation.IDevice;
import org.xmedia.oms.adapter.kaon2.persistence.model.adaptation.IProcess;


public interface IProcessDao extends IDao {

	public void setAgent(IProcess process, ICognitiveAgent agent);
	
	public void setPostProcess(IProcess process, IProcess postProcess);
	
	public void setResource(IProcess process, IContent content);
	
	public void setResource(IProcess process, IContentBearingObject cbo);
	
	public void setInstrument(IProcess process, IDevice instrument);
}
