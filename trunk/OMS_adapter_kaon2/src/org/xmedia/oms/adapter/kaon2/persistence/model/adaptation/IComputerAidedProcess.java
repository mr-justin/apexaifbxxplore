package org.xmedia.oms.adapter.kaon2.persistence.model.adaptation;


public interface IComputerAidedProcess extends IProcess {

	public void setApplicationInteraction(IApplicationInteraction appint);
	
	public void storeComputerAidedProcess();
 
}
