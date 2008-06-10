package org.xmedia.accessknow.sesame.persistence.service.adaptation;

import org.xmedia.accessknow.sesame.persistence.model.adaptation.IDevice;

public interface IDeviceDao extends IDao {

	public void insert(IDevice device);
	
}
