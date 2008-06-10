package org.xmedia.oms.adapter.kaon2.persistence.service.adaption;

import java.util.ArrayList;
import java.util.List;

import org.xmedia.oms.adapter.kaon2.persistence.model.adaptation.Device;
import org.xmedia.oms.adapter.kaon2.persistence.model.adaptation.IDevice;
import org.aifb.xxplore.shared.exception.Emergency;
import org.semanticweb.kaon2.api.Axiom;
import org.semanticweb.kaon2.api.KAON2Exception;
import org.semanticweb.kaon2.api.KAON2Manager;
import org.semanticweb.kaon2.api.OntologyChangeEvent;
import org.xmedia.oms.adapter.kaon2.persistence.Kaon2Ontology;
import org.xmedia.oms.model.api.IOntology;


public class DeviceDao extends AbstractDao implements IDeviceDao {

	private static IDeviceDao m_instance = null;

	private final String DEVICE = "Device";

	private DeviceDao() {
		
	}
	
	public static IDeviceDao getInstance() {
		if(m_instance == null)
			m_instance = new DeviceDao();
		return m_instance;
	}
	
	public void insert(IDevice device) {
		Emergency.checkPrecondition(device instanceof Device,
				"device instanceof Device");

		Axiom axiom = KAON2Manager.factory().classMember(
				KAON2Manager.factory().owlClass(getEntityUri(DEVICE)),
				KAON2Manager.factory().individual(device.getUri()));

		List<OntologyChangeEvent> changes = new ArrayList<OntologyChangeEvent>();
		changes.add(new OntologyChangeEvent(axiom,OntologyChangeEvent.ChangeType.ADD));
		try {
			IOntology onto = getAdaptationOntology();
			if (onto instanceof Kaon2Ontology)
				((Kaon2Ontology)onto).getDelegate().applyChanges(changes);
		} catch (KAON2Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
