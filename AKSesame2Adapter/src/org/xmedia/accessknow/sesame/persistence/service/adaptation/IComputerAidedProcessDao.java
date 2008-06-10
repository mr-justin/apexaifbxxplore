package org.xmedia.accessknow.sesame.persistence.service.adaptation;

import org.xmedia.accessknow.sesame.persistence.model.adaptation.IApplicationInteraction;
import org.xmedia.accessknow.sesame.persistence.model.adaptation.IComputerAidedProcess;

public interface IComputerAidedProcessDao extends IDao {

	public void setApplicationInteraction(IComputerAidedProcess process,IApplicationInteraction interaction);
	
	public void insert(IComputerAidedProcess process);
}
