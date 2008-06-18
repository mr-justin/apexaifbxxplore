package org.xmedia.oms.adapter.kaon2.persistence.service.adaption;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Set;

import org.semanticweb.kaon2.api.KAON2Exception;
import org.semanticweb.kaon2.api.formatting.OntologyFileFormat;
import org.xmedia.oms.adapter.kaon2.persistence.Kaon2Ontology;
import org.xmedia.oms.model.api.INamedConcept;
import org.xmedia.oms.model.api.INamedIndividual;
import org.xmedia.oms.model.api.IOntology;
import org.xmedia.oms.model.api.IProperty;
import org.xmedia.oms.model.api.IPropertyMember;
import org.xmedia.oms.persistence.IKbConnection;
import org.xmedia.oms.persistence.KbEnvironment;
import org.xmedia.oms.persistence.OntologyLoadException;
import org.xmedia.oms.persistence.PersistenceUtil;
import org.xmedia.oms.persistence.SessionFactory;
import org.xmedia.oms.persistence.StatelessSession;
import org.xmedia.oms.persistence.dao.DaoUnavailableException;
import org.xmedia.oms.persistence.dao.IConceptDao;
import org.xmedia.oms.persistence.dao.IIndividualDao;
import org.xmedia.oms.persistence.dao.IPropertyMemberAxiomDao;

public abstract class AbstractDao implements IDao {
	
	protected IOntology getAdaptationOntology() {
		IKbConnection conn = ((StatelessSession)SessionFactory.getInstance().getCurrentSession()).getConnection();
		String ontoUri = (String)conn.getConfiguration().get(KbEnvironment.BASE_ADAPTATION_ONTOLOGY_URI);
		return conn.findOntologyByUri(ontoUri);
	}

	protected String getBaseAdaptaionOntologyUri() {
		return (String)((StatelessSession)SessionFactory.getInstance().getCurrentSession()).getConnection().getConfiguration().get(KbEnvironment.BASE_ADAPTATION_ONTOLOGY_URI);
	}
	
	protected String getEntityUri(String entity) {
		return getBaseAdaptaionOntologyUri() + "#" + entity;
	}
	
	public INamedIndividual findIndividualByUri(String uri) {
		StatelessSession session = (StatelessSession) SessionFactory.getInstance().getCurrentSession();
		INamedIndividual individual = null;
		IOntology activeOnto = session.getOntology();
		
		try {
			IOntology newOnto = getAdaptationOntology();
			session.setOntology(newOnto);
			
			IIndividualDao iDao = PersistenceUtil.getDaoManager().getIndividualDao();
			individual = iDao.findByUri(uri);
			
			session.setOntology(activeOnto);
		} catch (DaoUnavailableException e) {
			e.printStackTrace();
		} catch (OntologyLoadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return individual;
	}
	
	protected Set<IPropertyMember> findPropertyMemberBySourceIndividual(INamedIndividual individual) {
		StatelessSession session = (StatelessSession) SessionFactory.getInstance().getCurrentSession();
		Set<IPropertyMember> props = null;
		IOntology activeOnto = session.getOntology();
		
		try {
			IOntology newOnto = getAdaptationOntology();
			session.setOntology(newOnto);
			
			IPropertyMemberAxiomDao pDao = PersistenceUtil.getDaoManager().getPropertyMemberDao();
			props = pDao.findBySourceIndividual(individual);
			
			session.setOntology(activeOnto);
		} catch (DaoUnavailableException e) {
			e.printStackTrace();
		} catch (OntologyLoadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return props;
	}
	
	protected INamedConcept findConceptByUri(String uri) {
		StatelessSession session = (StatelessSession) SessionFactory.getInstance().getCurrentSession();
		INamedConcept concept = null;
		IOntology activeOnto = session.getOntology();
		
		try {
			IOntology newOnto = getAdaptationOntology();
			session.setOntology(newOnto);
			
			IConceptDao cDao = PersistenceUtil.getDaoManager().getConceptDao();
			concept = cDao.findByUri(uri);
			
			session.setOntology(activeOnto);
		} catch (DaoUnavailableException e) {
			e.printStackTrace();
		} catch (OntologyLoadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return concept;
	}
	
	protected Set<IProperty> findPropertyBySourceConcept(INamedConcept concept) {
		StatelessSession session = (StatelessSession) SessionFactory.getInstance().getCurrentSession();
		Set<IProperty> props = null;
		IOntology activeOnto = session.getOntology();
		
		try {
			IOntology newOnto = getAdaptationOntology();
			session.setOntology(newOnto);
			
			props = concept.getPropertiesFrom();
			
			session.setOntology(activeOnto);
		} catch (OntologyLoadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return props;
	}
	
	public void saveOntology() {
		
		try {
			IOntology onto = getAdaptationOntology();
			if (onto instanceof Kaon2Ontology) {
				((Kaon2Ontology)onto).getDelegate().saveOntology(OntologyFileFormat.OWL_RDF,
					new File((String)SessionFactory.getInstance().getConfiguration().get(KbEnvironment.ADAPTATION_ONTOLOGY_URI)),"ISO-8859-1");
			}
		} catch (KAON2Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}
}