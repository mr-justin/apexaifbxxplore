package org.xmedia.oms.adapter.kaon2.persistence.service.adaption;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.xmedia.oms.adapter.kaon2.persistence.model.adaptation.Content;
import org.xmedia.oms.adapter.kaon2.persistence.model.adaptation.IContent;
import org.xmedia.oms.adapter.kaon2.persistence.model.adaptation.IContentBearingObject;
import org.xmedia.oms.adapter.kaon2.persistence.model.adaptation.IEntity;
import org.aifb.xxplore.shared.exception.Emergency;
import org.semanticweb.kaon2.api.Axiom;
import org.semanticweb.kaon2.api.KAON2Exception;
import org.semanticweb.kaon2.api.KAON2Manager;
import org.semanticweb.kaon2.api.OntologyChangeEvent;
import org.semanticweb.kaon2.api.owl.elements.Individual;
import org.xmedia.oms.adapter.kaon2.persistence.Kaon2Ontology;
import org.xmedia.oms.model.api.INamedConcept;
import org.xmedia.oms.model.api.INamedIndividual;
import org.xmedia.oms.model.api.IOntology;
import org.xmedia.oms.model.api.IProperty;


public class ContentDao extends AbstractDao implements IContentDao {

	private static IContentDao m_instance = null;
	
	private final String CONTENT = "Content";

	private final String HAS_SUBJECT = "has_subject";

	private final String IS_POST = "is_post";
	
	private final String EMBODIED_IN = "embodied_in";

	private ContentDao() {
		
	}
	
	public static IContentDao getInstance() {
		if(m_instance == null) {
			m_instance = new ContentDao();
		}
		return m_instance;
	}

	public void setCBO(IContent content, IContentBearingObject cbo) {
		INamedIndividual contentInd = findIndividualByUri(content.getUri());
		if (contentInd == null) {
			content.storeContent();
			contentInd = findIndividualByUri(content.getUri());
		}
		INamedIndividual cboInd = findIndividualByUri(cbo.getUri());
		if (cboInd == null) {
			cbo.storeCBO();
			cboInd = findIndividualByUri(cbo.getUri());
		}	
		
		// Alternative 1:
		INamedConcept con = findConceptByUri(getEntityUri(CONTENT));
		Set<IProperty> props = findPropertyBySourceConcept(con);
		for (IProperty prop : props) {
			if (prop.getUri().equals(getEntityUri(EMBODIED_IN))) {
				Axiom axiom = KAON2Manager.factory().objectPropertyMember(
						(org.semanticweb.kaon2.api.owl.elements.ObjectProperty)prop.getDelegate(),
						(Individual)contentInd.getDelegate(),
						(Individual)cboInd.getDelegate());
				
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
	}
	
	public void setPostContent(IContent content, IContent postContent) {
		INamedIndividual contentInd = findIndividualByUri(content.getUri());
		if (contentInd == null) {
			content.storeContent();
			contentInd = findIndividualByUri(content.getUri());
		}
		INamedIndividual postContentInd = findIndividualByUri(postContent.getUri());
		if (postContentInd == null) {
			postContent.storeContent();
			postContentInd = findIndividualByUri(content.getUri());
		}

		// Alternative 1:
		INamedConcept con = findConceptByUri(getEntityUri(CONTENT));
		Set<IProperty> props = findPropertyBySourceConcept(con);
		for (IProperty prop : props) {
			if (prop.getUri().equals(getEntityUri(IS_POST))) {
				Axiom axiom = KAON2Manager.factory().objectPropertyMember(
						(org.semanticweb.kaon2.api.owl.elements.ObjectProperty)prop.getDelegate(),
						(Individual)contentInd.getDelegate(),
						(Individual)postContentInd.getDelegate());
				
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
	}
	
	public void setSubject(IContent content, IEntity entity) {
		INamedIndividual contentInd = findIndividualByUri(content.getUri());
		if (contentInd == null) {
			content.storeContent();
			contentInd = findIndividualByUri(content.getUri());
		}
		INamedIndividual entityInd = findIndividualByUri(entity.getUri());
		if (entityInd == null) {
			entity.storeEntity();
			entityInd = findIndividualByUri(entity.getUri());
		}
		
		// Alternative 1:
		INamedConcept con = findConceptByUri(getEntityUri(CONTENT));
		Set<IProperty> props = findPropertyBySourceConcept(con);
		for (IProperty prop : props) {
			if (prop.getUri().equals(getEntityUri(HAS_SUBJECT))) {
				Axiom axiom = KAON2Manager.factory().objectPropertyMember(
						(org.semanticweb.kaon2.api.owl.elements.ObjectProperty)prop.getDelegate(),
						(Individual)contentInd.getDelegate(),
						(Individual)entityInd.getDelegate());
				
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
	}
	
	public void insert(IContent resource) {
		Emergency.checkPrecondition(resource instanceof Content,
				"resources instanceof Content");

		Axiom axiom = KAON2Manager.factory().classMember(
				KAON2Manager.factory().owlClass(getEntityUri(CONTENT)),
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
}
