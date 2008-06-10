package org.xmedia.oms.adapter.kaon2.persistence.service.adaption;

import java.util.ArrayList;
import java.util.List;

import org.xmedia.oms.adapter.kaon2.persistence.model.adaptation.ApplicationEnvironment;
import org.xmedia.oms.adapter.kaon2.persistence.model.adaptation.IApplicationEnvironment;
import org.aifb.xxplore.shared.exception.Emergency;
import org.semanticweb.kaon2.api.Axiom;
import org.semanticweb.kaon2.api.KAON2Exception;
import org.semanticweb.kaon2.api.KAON2Manager;
import org.semanticweb.kaon2.api.OntologyChangeEvent;
import org.xmedia.oms.adapter.kaon2.persistence.Kaon2Ontology;
import org.xmedia.oms.model.api.IOntology;


public class ApplicationEnvironmentDao extends AbstractDao implements IApplicationEnvironmentDao {

	private static IApplicationEnvironmentDao m_instance = null;

	private final String APPLICATION_ENVIRONMENT = "Application_Environment";

	private ApplicationEnvironmentDao() {
		
	}
	
	public static IApplicationEnvironmentDao getInstance() {
		if(m_instance == null)
			m_instance = new ApplicationEnvironmentDao();
		return m_instance;
	}
	
	public void insert(IApplicationEnvironment environment) {
		Emergency.checkPrecondition(environment instanceof ApplicationEnvironment,
				"environment instanceof ApplicationEnvironment");

		Axiom axiom = KAON2Manager.factory().classMember(
				KAON2Manager.factory().owlClass(getEntityUri(APPLICATION_ENVIRONMENT)),
				KAON2Manager.factory().individual(environment.getUri()));

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
