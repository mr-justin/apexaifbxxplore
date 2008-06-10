package org.xmedia.accessknow.sesame.persistence.service.adaptation;

import org.xmedia.accessknow.sesame.persistence.model.adaptation.IApplicationInteraction;
import org.xmedia.accessknow.sesame.persistence.model.adaptation.IComputerAidedProcess;

public interface IApplicationInteractionDao extends IDao {

	public void setComputerAidedProcess(IApplicationInteraction interaction, IComputerAidedProcess process);
	
	public void insert(IApplicationInteraction interaction);
}
