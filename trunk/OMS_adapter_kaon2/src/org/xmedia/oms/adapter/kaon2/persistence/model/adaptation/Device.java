package org.xmedia.oms.adapter.kaon2.persistence.model.adaptation;

import org.xmedia.oms.adapter.kaon2.persistence.service.adaption.DeviceDao;
import org.xmedia.oms.adapter.kaon2.persistence.service.adaption.IDeviceDao;

public class Device implements IDevice {
	
	private String m_uri;

    public Device() {
    } 
    
    public Device(String uri) {
    	m_uri = uri;
    }

	public String getUri() {
		return m_uri;
	}

	public void storeDevice() {
		IDeviceDao dao = DeviceDao.getInstance();
		dao.insert(this);
	}

}
