package org.ateam.xxplore.core.model.interaction;

import java.util.Set;


public interface IDeviceDao {
	public IDevice findDeviceByUri(String deviceUri);
	public Set<IDevice> findAllDevices();
	public void saveDevice(IDevice device);
}
