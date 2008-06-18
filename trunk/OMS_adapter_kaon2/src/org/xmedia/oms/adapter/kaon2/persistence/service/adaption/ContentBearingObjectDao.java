package org.xmedia.oms.adapter.kaon2.persistence.service.adaption;

import java.util.ArrayList;
import java.util.List;
import org.xmedia.oms.adapter.kaon2.persistence.model.adaptation.ContentBearingObject;
import org.xmedia.oms.adapter.kaon2.persistence.model.adaptation.IContent;
import org.xmedia.oms.adapter.kaon2.persistence.model.adaptation.IContentBearingObject;
import org.xmedia.oms.adapter.kaon2.persistence.model.adaptation.ICredential;
import org.aifb.xxplore.shared.exception.Emergency;
import org.semanticweb.kaon2.api.Axiom;
import org.semanticweb.kaon2.api.KAON2Exception;
import org.semanticweb.kaon2.api.KAON2Manager;
import org.semanticweb.kaon2.api.OntologyChangeEvent;
import org.semanticweb.kaon2.api.owl.elements.Individual;
import org.xmedia.oms.adapter.kaon2.persistence.Kaon2Ontology;
import org.xmedia.oms.model.api.INamedIndividual;
import org.xmedia.oms.model.api.IOntology;


public class ContentBearingObjectDao extends AbstractDao implements IContentBearingObjectDao {

	private static IContentBearingObjectDao m_instance = null;
	
	private final String CONTENT_BEARING_OBJECT = "Content_Bearing_Object";

	private final String CONTAINSINFORMATION = "containsinformation";
	
	private final String REQUIRED_CREDENTIAL = "required_credential";

	private ContentBearingObjectDao() {
		
	}
	
	public static IContentBearingObjectDao getInstance() {
		if(m_instance == null) {
			m_instance = new ContentBearingObjectDao();
		}
		return m_instance;
	}

	public void insert(IContentBearingObject resource) {
		Emergency.checkPrecondition(resource instanceof ContentBearingObject,
				"resources instanceof ContentBearingObject");

		Axiom axiom = KAON2Manager.factory().classMember(
				KAON2Manager.factory().owlClass(getEntityUri(CONTENT_BEARING_OBJECT)),
				KAON2Manager.factory().individual(resource.getUri()));

		List<OntologyChangeEvent> changes = new ArrayList<OntologyChangeEvent>();
		changes.add(new OntologyChangeEvent(axiom,OntologyChangeEvent.ChangeType.ADD));
		try {
			IOntology onto = getAdaptationOntology();
			if (onto instanceof Kaon2Ontology) {
				((Kaon2Ontology)onto).getDelegate().applyChanges(changes);
			}
		} catch (KAON2Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void setContent(IContentBearingObject cbo, IContent content) {
		INamedIndividual cboInd = findIndividualByUri(cbo.getUri());
		if (cboInd == null) {
			cbo.storeCBO();
			cboInd = findIndividualByUri(cbo.getUri());
		} 
		INamedIndividual contentInd = findIndividualByUri(content.getUri());
		if (contentInd == null) {
			content.storeContent();
			contentInd = findIndividualByUri(content.getUri());
		} 
		
		Axiom axiom = KAON2Manager.factory().objectPropertyMember(
				KAON2Manager.factory().objectProperty(getEntityUri(CONTAINSINFORMATION)),
				(Individual)cboInd.getDelegate(),
				(Individual)contentInd.getDelegate());
		
		List<OntologyChangeEvent> changes = new ArrayList<OntologyChangeEvent>();
		changes.add(new OntologyChangeEvent(axiom,OntologyChangeEvent.ChangeType.ADD));
		try {
			IOntology onto = getAdaptationOntology();
			if (onto instanceof Kaon2Ontology) {
				((Kaon2Ontology)onto).getDelegate().applyChanges(changes);
			}
		} catch (KAON2Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void setCredential(IContentBearingObject cbo, ICredential credential) {
		INamedIndividual cboInd = findIndividualByUri(cbo.getUri());
		if (cboInd == null) {
			cbo.storeCBO();
			cboInd = findIndividualByUri(cbo.getUri());
		} 
		INamedIndividual credentialInd = findIndividualByUri(credential.getUri());
		if (credentialInd == null) {
			credential.storeCredential();
			credentialInd = findIndividualByUri(credential.getUri());
		} 
		
		Axiom axiom = KAON2Manager.factory().objectPropertyMember(
				KAON2Manager.factory().objectProperty(getEntityUri(REQUIRED_CREDENTIAL)),
				(Individual)cboInd.getDelegate(),
				(Individual)credentialInd.getDelegate());
		
		List<OntologyChangeEvent> changes = new ArrayList<OntologyChangeEvent>();
		changes.add(new OntologyChangeEvent(axiom,OntologyChangeEvent.ChangeType.ADD));
		try {
			IOntology onto = getAdaptationOntology();
			if (onto instanceof Kaon2Ontology) {
				((Kaon2Ontology)onto).getDelegate().applyChanges(changes);
			}
		} catch (KAON2Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
