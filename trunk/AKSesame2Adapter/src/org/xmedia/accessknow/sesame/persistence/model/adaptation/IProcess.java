package org.xmedia.accessknow.sesame.persistence.model.adaptation;

public interface IProcess {
	
	public String getUri();
	
	public void setPostProcess(IProcess process);
	
	public void setResource(IContent content);
	
	public void setResrouce(IContentBearingObject cbo);
	
	public void setAgent(ICognitiveAgent agent);
	
	public void setInstrument(IDevice instrument);
	
}
