package org.xmedia.oms.adapter.kaon2.persistence.service.adaption;

import org.xmedia.oms.adapter.kaon2.persistence.model.adaptation.IApplicationInteraction;
import org.xmedia.oms.adapter.kaon2.persistence.model.adaptation.IComputerAidedProcess;

public interface IComputerAidedProcessDao extends IDao {

	public void setApplicationInteraction(IComputerAidedProcess process,IApplicationInteraction interaction);
	
	public void insert(IComputerAidedProcess process);
}