package org.xmedia.oms.adapter.kaon2.persistence.service.adaption;

import org.xmedia.oms.adapter.kaon2.persistence.model.adaptation.IApplicationInteraction;
import org.xmedia.oms.adapter.kaon2.persistence.model.adaptation.IComputerAidedProcess;

public interface IApplicationInteractionDao extends IDao {

	public void setComputerAidedProcess(IApplicationInteraction interaction, IComputerAidedProcess process);
	
	public void insert(IApplicationInteraction interaction);
}
