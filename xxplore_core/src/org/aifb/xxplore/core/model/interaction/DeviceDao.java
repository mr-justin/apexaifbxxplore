package org.aifb.xxplore.core.model.interaction;

import java.util.HashSet;
import java.util.Set;

import org.xmedia.oms.model.api.IIndividual;
import org.xmedia.oms.model.api.ILiteral;
import org.xmedia.oms.model.api.INamedConcept;
import org.xmedia.oms.model.api.INamedIndividual;
import org.xmedia.oms.model.api.IPropertyMember;
import org.xmedia.oms.persistence.PersistenceUtil;
import org.xmedia.oms.persistence.dao.DaoUnavailableException;
import org.xmedia.oms.persistence.dao.IConceptDao;
import org.xmedia.oms.persistence.dao.IIndividualDao;
import org.xmedia.oms.persistence.dao.IPropertyMemberAxiomDao;

public class DeviceDao implements IDeviceDao{
	private static IDeviceDao m_instance = null;
	
	private final String POLICY_ONTOLOGY_URI = "http://www.applicationontologies/fiat/policy";
	
	private final String NAME = POLICY_ONTOLOGY_URI + "#name";
	
	private final String DESCRIPTION = POLICY_ONTOLOGY_URI + "#description";


	public static IDeviceDao getInstance() {
		if (m_instance == null)
			m_instance = new DeviceDao();
		return m_instance;
	}
	
	public Set<IDevice> findAllDevices() {
	try {
			IConceptDao cDao = PersistenceUtil.getDaoManager().getConceptDao();

			Set<IDevice> deviceUris = new HashSet<IDevice>();
			
			INamedConcept envConcept = cDao.findByUri(POLICY_ONTOLOGY_URI + "#device");
			for (IIndividual processInd : envConcept.getMemberIndividuals(true)) {
				deviceUris.add( new Device(((INamedIndividual)processInd).getUri()));
			}
			return deviceUris;
			
		} catch (DaoUnavailableException e) {
			e.printStackTrace();
		}
		
		return null;
	}

	public IDevice findDeviceByUri(String deviceUri) {
		
		IDevice device = null;
		
		try {
			IIndividualDao iDao = PersistenceUtil.getDaoManager().getIndividualDao();
			IPropertyMemberAxiomDao pDao = PersistenceUtil.getDaoManager().getPropertyMemberDao();
			INamedIndividual deviceIndividual = iDao.findByUri(deviceUri);
			
			device = new Device(deviceUri);
			
			Set<IPropertyMember> props = pDao.findBySourceIndividual(deviceIndividual);
			
			for (IPropertyMember prop : props) {
				if (prop.getProperty().getUri().equals(NAME))
					device.setName(((ILiteral)prop.getTarget()).getLiteral());
				if (prop.getProperty().getUri().equals(DESCRIPTION))
					device.setDescription(((ILiteral)prop.getTarget()).getLiteral());
			}
		} catch (DaoUnavailableException e) {
			e.printStackTrace();
		}
		
		return device;
	}

	public void saveDevice(IDevice device) {
		// TODO Auto-generated method stub
	}
}
