package org.xmedia.accessknow.sesame.persistence.service.adaptation;

import org.xmedia.accessknow.sesame.persistence.model.adaptation.Device;
import org.xmedia.accessknow.sesame.persistence.model.adaptation.IDevice;
import org.aifb.xxplore.shared.exception.Emergency;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.xmedia.oms.model.api.IOntology;
import org.xmedia.accessknow.sesame.model.SesameOntology;


public class DeviceDao extends AbstractDao implements IDeviceDao {

	private static IDeviceDao m_instance = null;
	private final String DEVICE = "Device";

	private DeviceDao() {}
	
	public static IDeviceDao getInstance() {
		if(m_instance == null)m_instance = new DeviceDao();
		return m_instance;
	}
	
	public void insert(IDevice device) {
		
		Emergency.checkPrecondition(device instanceof Device,"device instanceof Device");

//		Axiom axiom = KAON2Manager.factory().classMember(
//				KAON2Manager.factory().owlClass(getEntityUri(DEVICE)),
//				KAON2Manager.factory().individual(device.getUri()));
//
//		List<OntologyChangeEvent> changes = new ArrayList<OntologyChangeEvent>();
//		changes.add(new OntologyChangeEvent(axiom,OntologyChangeEvent.ChangeType.ADD));
//		try {
//			IOntology onto = getAdaptationOntology();
//			if (onto instanceof Kaon2Ontology)
//				((Kaon2Ontology)onto).getDelegate().applyChanges(changes);
//		} catch (KAON2Exception e) {
//			e.printStackTrace();
//		}
		
		IOntology onto = getAdaptationOntology();
		if (onto instanceof SesameOntology)
		{
			ValueFactory factory = ((SesameOntology)onto).getRepository().getValueFactory();

			URI subject = factory.createURI(device.getUri());
			URI object = factory.createURI(getEntityUri(DEVICE));

			try 
			{
				RepositoryConnection con = ((SesameOntology)onto).getRepository().getConnection();
				
				try
				{
					con.add(subject,RDF.TYPE,object);
				}
				finally
				{
					con.close();
				}
			} 
			catch (RepositoryException e) 
			{

				e.printStackTrace();
			}
		}
	}
}
