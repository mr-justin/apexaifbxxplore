package org.xmedia.accessknow.sesame.persistence.model.adaptation;

public interface IApplicationInteraction extends IProcess {
	
	public void setComputerAidedProcess(IComputerAidedProcess capro);
	public void storeApplicationInteraction();

}
