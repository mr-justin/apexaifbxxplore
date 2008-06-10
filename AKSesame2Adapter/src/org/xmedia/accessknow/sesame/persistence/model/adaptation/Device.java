package org.xmedia.accessknow.sesame.persistence.model.adaptation;

import org.xmedia.accessknow.sesame.persistence.service.adaptation.DeviceDao;
import org.xmedia.accessknow.sesame.persistence.service.adaptation.IDeviceDao;

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
