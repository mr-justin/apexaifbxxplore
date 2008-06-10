package org.xmedia.oms.adapter.kaon2.persistence.model.adaptation;


public interface IApplicationInteraction extends IProcess {

	public void setComputerAidedProcess(IComputerAidedProcess capro);
	
	public void storeApplicationInteraction();

}
