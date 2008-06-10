package org.xmedia.oms.adapter.kaon2.persistence.service.adaption;

import org.xmedia.oms.adapter.kaon2.persistence.model.adaptation.IDevice;

public interface IDeviceDao extends IDao {

	public void insert(IDevice device);
	
}
