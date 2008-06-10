package org.xmedia.accessknow.sesame.persistence.model.adaptation;

public interface IComputerAidedProcess extends IProcess {

	public void setApplicationInteraction(IApplicationInteraction appint);
	
	public void storeComputerAidedProcess();
 
}
