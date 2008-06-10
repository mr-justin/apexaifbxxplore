package org.xmedia.oms.adapter.kaon2.persistence.service.adaption;

import java.util.ArrayList;
import java.util.List;

import org.xmedia.oms.adapter.kaon2.persistence.model.adaptation.Credential;
import org.xmedia.oms.adapter.kaon2.persistence.model.adaptation.ICredential;
import org.aifb.xxplore.shared.exception.Emergency;
import org.semanticweb.kaon2.api.Axiom;
import org.semanticweb.kaon2.api.KAON2Exception;
import org.semanticweb.kaon2.api.KAON2Manager;
import org.semanticweb.kaon2.api.OntologyChangeEvent;
import org.xmedia.oms.adapter.kaon2.persistence.Kaon2Ontology;
import org.xmedia.oms.model.api.IOntology;


public class CredentialDao extends AbstractDao implements ICredentialDao {

	private static ICredentialDao m_instance = null;

	private final String CREDENTIAL = "Credential";
	
	private CredentialDao() {
		
	}
	
	public static ICredentialDao getInstance() {
		if (m_instance == null)
			m_instance = new CredentialDao();
		return m_instance;
	}
	
	public void insert(ICredential credential) {
		Emergency.checkPrecondition(credential instanceof Credential,
				"credential instanceof Credential");

		Axiom axiom = KAON2Manager.factory().classMember(
				KAON2Manager.factory().owlClass(getEntityUri(CREDENTIAL)),
				KAON2Manager.factory().individual(credential.getUri()));

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
