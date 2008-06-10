package org.xmedia.accessknow.sesame.persistence.service.adaptation;

import org.xmedia.accessknow.sesame.persistence.model.adaptation.ICognitiveAgent;
import org.xmedia.accessknow.sesame.persistence.model.adaptation.IContent;
import org.xmedia.accessknow.sesame.persistence.model.adaptation.IContentBearingObject;
import org.xmedia.accessknow.sesame.persistence.model.adaptation.IDevice;
import org.xmedia.accessknow.sesame.persistence.model.adaptation.IProcess;

public interface IProcessDao extends IDao {

	public void setAgent(IProcess process, ICognitiveAgent agent);
	
	public void setPostProcess(IProcess process, IProcess postProcess);
	
	public void setResource(IProcess process, IContent content);
	
	public void setResource(IProcess process, IContentBearingObject cbo);
	
	public void setInstrument(IProcess process, IDevice instrument);
}
