package org.xmedia.oms.adapter.kaon2.persistence;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.aifb.xxplore.shared.exception.Emergency;
import org.apache.log4j.Logger;
import org.semanticweb.kaon2.api.Axiom;
import org.semanticweb.kaon2.api.Entity;
import org.semanticweb.kaon2.api.KAON2Exception;
import org.semanticweb.kaon2.api.KAON2Factory;
import org.semanticweb.kaon2.api.KAON2Manager;
import org.semanticweb.kaon2.api.Ontology;
import org.semanticweb.kaon2.api.OntologyChangeEvent;
import org.semanticweb.kaon2.api.OntologyManager;
import org.semanticweb.kaon2.api.Request;
import org.semanticweb.kaon2.api.logic.Constant;
import org.semanticweb.kaon2.api.owl.axioms.DataPropertyMember;
import org.semanticweb.kaon2.api.owl.axioms.EntityAnnotation;
import org.semanticweb.kaon2.api.owl.axioms.OWLAxiom;
import org.semanticweb.kaon2.api.owl.axioms.ObjectPropertyMember;
import org.semanticweb.kaon2.api.owl.axioms.SubDataPropertyOf;
import org.semanticweb.kaon2.api.owl.axioms.SubObjectPropertyOf;
import org.semanticweb.kaon2.api.owl.elements.DataPropertyExpression;
import org.semanticweb.kaon2.api.owl.elements.Individual;
import org.semanticweb.kaon2.api.owl.elements.ObjectPropertyExpression;
import org.xmedia.businessobject.IBusinessObject;
import org.xmedia.oms.adapter.kaon2.persistence.Kaon2ConnectionProvider.Kaon2Connection;
import org.xmedia.oms.adapter.kaon2.util.Kaon2OMSModelConverter;
import org.xmedia.oms.metaknow.IProvenance;
import org.xmedia.oms.metaknow.MetaVocabulary;
import org.xmedia.oms.metaknow.Provenance;
import org.xmedia.oms.metaknow.ProvenanceUnknownException;
import org.xmedia.oms.model.api.IEntity;
import org.xmedia.oms.model.api.IIndividual;
import org.xmedia.oms.model.api.ILiteral;
import org.xmedia.oms.model.api.INamedIndividual;
import org.xmedia.oms.model.api.IProperty;
import org.xmedia.oms.model.api.IPropertyMember;
import org.xmedia.oms.model.api.IResource;
import org.xmedia.oms.model.impl.DataProperty;
import org.xmedia.oms.model.impl.ObjectProperty;
import org.xmedia.oms.model.impl.PropertyMember;
import org.xmedia.oms.persistence.DatasourceException;
import org.xmedia.oms.persistence.PersistenceUtil;
import org.xmedia.oms.persistence.SessionFactory;
import org.xmedia.oms.persistence.StatelessSession;
import org.xmedia.oms.persistence.dao.BODeletionException;
import org.xmedia.oms.persistence.dao.BOInsertionException;
import org.xmedia.oms.persistence.dao.BOsDeletionException;
import org.xmedia.oms.persistence.dao.DaoUnavailableException;
import org.xmedia.oms.persistence.dao.IPropertyMemberAxiomDao;

public class Kaon2PropertyMemberDao extends AbstractKaon2Dao implements
		IPropertyMemberAxiomDao {

	private static Logger s_log = Logger
			.getLogger(Kaon2PropertyMemberDao.class);

	private Ontology m_ontology = null;

	private Ontology m_axiomMetaview = null;

	private String m_axiomMetaviewPrefix = "ax:";

	private final String AXIOM_METAVIEW_ONTOLOGY_URI = "http://www.cs.man.ac.uk/AxiomMetaview";

	private final String XSD_DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

	private final SimpleDateFormat m_xsdDateFormat;

	public Kaon2PropertyMemberDao() {
		m_xsdDateFormat = new SimpleDateFormat(XSD_DATETIME_FORMAT);
	}

	public Class getBoClass() {

		return PropertyMember.class;
	}

	private Ontology getOntology() {
		StatelessSession session = (StatelessSession) SessionFactory
				.getInstance().getCurrentSession();
		return ((Kaon2Ontology) session.getOntology()).getDelegate();
	}

	private StatelessSession getSession() {
		return (StatelessSession) SessionFactory.getInstance()
				.getCurrentSession();
	}

	private void updateMetaviews() {
		if (getOntology() != null) {
			m_ontology = getOntology();
			OntologyManager conn = ((Kaon2Connection) getSession()
					.getConnection()).getConnection();
			try {
				m_axiomMetaview = conn.openOntology(m_axiomMetaviewPrefix
						+ m_ontology.getOntologyURI(),
						new HashMap<String, Object>());
			} catch (KAON2Exception e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private Set<Axiom> getRelatedAxioms(IPropertyMember propMember) {
		StatelessSession session = (StatelessSession) SessionFactory
				.getInstance().getCurrentSession();
		Kaon2Ontology onto = (Kaon2Ontology) session.getOntology();

		Set<Axiom> axioms = new HashSet<Axiom>();
		axioms.add((Axiom) propMember.getDelegate());

		return axioms;
	}

	public void delete(IBusinessObject existingBo) throws DatasourceException {
		Emergency.checkPrecondition(existingBo instanceof PropertyMember,
				"existingBo instanceof PropertyMember");
		try {
			delete((PropertyMember) existingBo);
		} catch (BODeletionException e) {
			throw new DatasourceException();
		}
	}

	public void insert(IBusinessObject newBo) throws DatasourceException {
		Emergency.checkPrecondition(newBo instanceof PropertyMember,
				"newBo instanceof PropertyMember");
		PropertyMember propMember = (PropertyMember) newBo;

		Kaon2Transaction trans = getTransaction();

		Set<Axiom> axioms = new HashSet<Axiom>();

		if (propMember.getType() == PropertyMember.DATA_PROPERTY_MEMBER) {
			DataPropertyMember dpm = KAON2Manager
					.factory()
					.dataPropertyMember(
							(org.semanticweb.kaon2.api.owl.elements.DataProperty) propMember
									.getProperty().getDelegate(),
							(Individual) propMember.getSource().getDelegate(),
							(Constant) propMember.getTarget().getDelegate());
			axioms.add(dpm);
		} else if (propMember.getType() == PropertyMember.OBJECT_PROPERTY_MEMBER) {
			ObjectPropertyMember opm = KAON2Manager
					.factory()
					.objectPropertyMember(
							(org.semanticweb.kaon2.api.owl.elements.ObjectProperty) propMember
									.getProperty().getDelegate(),
							(Individual) propMember.getSource().getDelegate(),
							(Individual) propMember.getTarget().getDelegate());
			axioms.add(opm);
		}

		for (Axiom axiom : axioms) {
			OntologyChangeEvent event = new OntologyChangeEvent(axiom,
					OntologyChangeEvent.ChangeType.ADD);
			trans.addChanges(event);
		}
	}

	public void update(IBusinessObject existingBo) throws DatasourceException {
		Emergency.checkPrecondition(existingBo instanceof PropertyMember,
				"existingBo instanceof PropertyMember");
		delete(existingBo);
		insert(existingBo);
	}

	public List<IPropertyMember> findAll() throws DatasourceException {
		/*
		 * StatelessSession session =
		 * (StatelessSession)SessionFactory.getInstance().getCurrentSession();
		 * Kaon2Ontology onto = (Kaon2Ontology) session.getOntology();
		 * 
		 * Set<OWLAxiom> props = new HashSet<OWLAxiom>(); List<IPropertyMember>
		 * propList = new ArrayList<IPropertyMember>();
		 * 
		 * if (s_log.isDebugEnabled()) s_log.debug("find all property members of
		 * the ontology: " + session.getOntology().getUri()); try {
		 * 
		 * //get all object properties Request<ObjectPropertyMember>
		 * oPropMemberRequest =
		 * onto.getDelegate().createAxiomRequest(ObjectPropertyMember.class);
		 * props.addAll(oPropMemberRequest.getAll());
		 * 
		 * //get all data properties Request<DataPropertyMember>
		 * dPropMemberRequest =
		 * onto.getDelegate().createAxiomRequest(DataPropertyMember.class);
		 * props.addAll(dPropMemberRequest.getAll()); } catch (KAON2Exception e) {
		 * 
		 * throw new DatasourceException(e); }
		 * 
		 * //no such axioms if(props == null || props.size() == 0) return null;
		 * 
		 * for (OWLAxiom prop : props){
		 * propList.add((IPropertyMember)Kaon2OMSModelConverter.convertAxiom(prop,
		 * onto)); if (s_log.isDebugEnabled()) s_log.debug("property member
		 * found: " + prop.toString()); }
		 * 
		 * return propList;
		 */
		StatelessSession session = (StatelessSession) SessionFactory
				.getInstance().getCurrentSession();
		if (session.isReasoningOn() == false)
			return findAll(false);
		else
			return findAll(true);
	}

	public List<IPropertyMember> findAllObjectPropertyMember() throws DatasourceException {
		StatelessSession session = (StatelessSession) SessionFactory
				.getInstance().getCurrentSession();
		if (session.isReasoningOn() == false)
			return findAllObjectPropertyMember(false);
		else
			return findAllObjectPropertyMember(true);
	}
	
	public List<IPropertyMember> findAllDataPropertyMember() throws DatasourceException {
		StatelessSession session = (StatelessSession) SessionFactory
				.getInstance().getCurrentSession();
		if (session.isReasoningOn() == false)
			return findAllDataPropertyMember(false);
		else
			return findAllDataPropertyMember(true);
	}

	public IBusinessObject findById(String id) throws DatasourceException {
		return this.findByUri(id);
	}

	public int getNumberOfPropertyMember(IProperty property) {
		StatelessSession session = (StatelessSession) SessionFactory
				.getInstance().getCurrentSession();
		Kaon2Ontology onto = (Kaon2Ontology) session.getOntology();

		if (s_log.isDebugEnabled())
			s_log.debug("find all members of property: " + property.getUri());
		try {

			property = (IProperty) checkForDelegate(property);
			if (property == null)
				return 0;

			if (property instanceof ObjectProperty) {
				return onto.getDelegate().createAxiomRequest(
						ObjectPropertyMember.class).setCondition(
						"objectProperty", property.getDelegate()).sizeAll();
			}

			else if (property instanceof DataProperty) {
				return onto.getDelegate().createAxiomRequest(
						DataPropertyMember.class).setCondition("dataProperty",
						property.getDelegate()).sizeAll();
			}

		}

		catch (KAON2Exception e) {
			throw new DatasourceException(e);
		}

		return 0;
	}
	
	public Set<IPropertyMember> findByProperty(IProperty property) {
		StatelessSession session = (StatelessSession) SessionFactory
				.getInstance().getCurrentSession();
		Kaon2Ontology onto = (Kaon2Ontology) session.getOntology();
		Set<IPropertyMember> propmemberSet = null;

		Set propmembers = new HashSet();

		if (s_log.isDebugEnabled())
			s_log.debug("find all members of property: " + property.getUri());
		try {

			property = (IProperty) checkForDelegate(property);
			if (property == null)
				return null;

			if (property instanceof ObjectProperty) {
				propmembers = onto.getDelegate().createAxiomRequest(
						ObjectPropertyMember.class).setCondition(
						"objectProperty", property.getDelegate()).getAll();
			}

			else if (property instanceof DataProperty) {
				propmembers = onto.getDelegate().createAxiomRequest(
						DataPropertyMember.class).setCondition("dataProperty",
						property.getDelegate()).getAll();
			}

			else {
				// guessing: try object property fisrt
				propmembers = onto.getDelegate().createAxiomRequest(
						ObjectPropertyMember.class).setCondition(
						"objectProperty", property.getDelegate()).getAll();

				if (propmembers == null || propmembers.size() == 0) {

					propmembers = onto.getDelegate().createAxiomRequest(
							DataPropertyMember.class).setCondition(
							"dataProperty", property.getDelegate()).getAll();
				}
			}

			// no such axioms
			if (propmembers == null || propmembers.size() == 0)
				return null;

			propmemberSet = new HashSet<IPropertyMember>();
			for (Object propmember : propmembers) {
				propmemberSet.add((IPropertyMember) Kaon2OMSModelConverter
						.convertAxiom((OWLAxiom) propmember, onto));
				if (s_log.isDebugEnabled())
					s_log.debug("property member found: "
							+ propmember.toString());
			}
		}

		catch (KAON2Exception e) {
			throw new DatasourceException(e);
		}

		return propmemberSet;
	}

	public Set<IPropertyMember> findObjectPropertyMemberBySource(
			IIndividual individual) throws DatasourceException {
		Emergency.checkPrecondition(individual instanceof INamedIndividual,
				"individual instanceof INamedIndividual");

		StatelessSession session = (StatelessSession) SessionFactory
				.getInstance().getCurrentSession();
		Kaon2Ontology onto = (Kaon2Ontology) session.getOntology();
		Set<IPropertyMember> propmemberSet = null;

		Set<ObjectPropertyMember> propmembers = new HashSet<ObjectPropertyMember>();
		if (s_log.isDebugEnabled())
			s_log.debug("find all object property values from individual: "
					+ individual.getLabel());
		try {

			// TODO support for IInd needed!!
			individual = (INamedIndividual) checkForDelegate((INamedIndividual) individual);
			if (individual == null)
				return null;

			propmembers = onto.getDelegate().createAxiomRequest(
					ObjectPropertyMember.class).setCondition(
					"sourceIndividual", individual.getDelegate()).getAll();

			// no such axioms
			if (propmembers == null || propmembers.size() == 0)
				return null;

			propmemberSet = new HashSet<IPropertyMember>();
			for (ObjectPropertyMember propmember : propmembers) {
				propmemberSet.add((IPropertyMember) Kaon2OMSModelConverter
						.convertAxiom(propmember, onto));
			}
		}

		catch (KAON2Exception e) {
			throw new DatasourceException(e);
		}

		return propmemberSet;
	}

	public Set<IPropertyMember> findObjectPropertyMemberByTarget(
			IIndividual individual) throws DatasourceException {
		Emergency.checkPrecondition(individual instanceof INamedIndividual,
				"individual instanceof INamedIndividual");

		StatelessSession session = (StatelessSession) SessionFactory
				.getInstance().getCurrentSession();
		Kaon2Ontology onto = (Kaon2Ontology) session.getOntology();
		Set<IPropertyMember> propmemberSet = null;

		Set<ObjectPropertyMember> propmembers = new HashSet<ObjectPropertyMember>();
		if (s_log.isDebugEnabled())
			s_log.debug("find all object property values to individual: "
					+ individual.getLabel());
		try {

			// TODO support for IInd needed!!
			individual = (INamedIndividual) checkForDelegate((INamedIndividual) individual);
			if (individual == null)
				return null;

			propmembers = onto.getDelegate().createAxiomRequest(
					ObjectPropertyMember.class).setCondition(
					"targetIndividual", individual.getDelegate()).getAll();

			// no such axioms
			if (propmembers == null || propmembers.size() == 0)
				return null;

			propmemberSet = new HashSet<IPropertyMember>();
			for (ObjectPropertyMember propmember : propmembers) {
				propmemberSet.add((IPropertyMember) Kaon2OMSModelConverter
						.convertAxiom(propmember, onto));
				if (s_log.isDebugEnabled())
					s_log.debug("property member found: "
							+ propmember.toString());
			}
		}

		catch (KAON2Exception e) {
			throw new DatasourceException(e);
		}

		return propmemberSet;
	}

	public Set<IPropertyMember> findObjectPropertyMemberByIndividual(
			IIndividual individual) throws DatasourceException {
		if (s_log.isDebugEnabled())
			s_log.debug("find all object property values of individual: "
					+ individual.getLabel());
		Set<IPropertyMember> propmembers = new HashSet<IPropertyMember>();
		propmembers.addAll(findBySourceIndividual(individual));
		propmembers.addAll(findObjectPropertyMemberByTarget(individual));
		return propmembers;
	}

	public Set<IPropertyMember> findBySourceIndividual(IIndividual individual)
			throws DatasourceException {
		Emergency.checkPrecondition(individual instanceof INamedIndividual,
				"individual instanceof INamedIndividual");

		StatelessSession session = (StatelessSession) SessionFactory
				.getInstance().getCurrentSession();
		Kaon2Ontology onto = (Kaon2Ontology) session.getOntology();
		Set<IPropertyMember> propmemberSet = null;

		Set propmembers = new HashSet();
		if (s_log.isDebugEnabled())
			s_log.debug("find all property from individual: "
					+ individual.getLabel());
		try {

			// TODO support for IInd needed!!
			individual = (INamedIndividual) checkForDelegate((INamedIndividual) individual);
			if (individual == null)
				return null;

			propmembers = onto.getDelegate().createAxiomRequest(
					ObjectPropertyMember.class).setCondition(
					"sourceIndividual", individual.getDelegate()).getAll();

			// also add data property to list
			propmembers.addAll(onto.getDelegate().createAxiomRequest(
					DataPropertyMember.class).setCondition("sourceIndividual",
					individual.getDelegate()).getAll());

			// no such axioms
			if (propmembers == null || propmembers.size() == 0)
				return new HashSet<IPropertyMember>();

			propmemberSet = new HashSet<IPropertyMember>();
			for (Object propmember : propmembers) {
				propmemberSet.add((IPropertyMember) Kaon2OMSModelConverter
						.convertAxiom((OWLAxiom) propmember, onto));
				if (s_log.isDebugEnabled())
					s_log.debug("property member found: "
							+ propmember.toString());
			}
		}

		catch (KAON2Exception e) {
			throw new DatasourceException(e);
		}

		return propmemberSet;
	}

	public Set<IPropertyMember> findByTargetIndividual(IIndividual individual)
			throws DatasourceException {
		Emergency.checkPrecondition(individual instanceof INamedIndividual,
				"individual instanceof INamedIndividual");

		StatelessSession session = (StatelessSession) SessionFactory
				.getInstance().getCurrentSession();
		Kaon2Ontology onto = (Kaon2Ontology) session.getOntology();
		Set<IPropertyMember> propmemberSet = null;

		Set propmembers = new HashSet();
		if (s_log.isDebugEnabled())
			s_log.debug("find all property to individual: "
					+ individual.getLabel());
		try {
			// TODO support for IInd needed!!
			individual = (INamedIndividual) checkForDelegate((INamedIndividual) individual);
			if (individual == null)
				return null;

			propmembers = onto.getDelegate().createAxiomRequest(
					ObjectPropertyMember.class).setCondition(
					"targetIndividual", individual.getDelegate()).getAll();

			// no such axioms
			if (propmembers == null || propmembers.size() == 0)
				return new HashSet<IPropertyMember>();

			propmemberSet = new HashSet<IPropertyMember>();
			for (Object propmember : propmembers) {
				propmemberSet.add((IPropertyMember) Kaon2OMSModelConverter
						.convertAxiom((OWLAxiom) propmember, onto));
				if (s_log.isDebugEnabled())
					s_log.debug("property member found: "
							+ propmember.toString());
			}
		}

		catch (KAON2Exception e) {
			throw new DatasourceException(e);
		}

		return propmemberSet;
	}

	public Set<IPropertyMember> findByTargetValue(ILiteral literal)
			throws DatasourceException {
		Emergency.checkPrecondition(literal instanceof ILiteral,
				"literal instanceof ILiteral");

		StatelessSession session = (StatelessSession) SessionFactory
				.getInstance().getCurrentSession();
		Kaon2Ontology onto = (Kaon2Ontology) session.getOntology();
		Set<IPropertyMember> propmemberSet = null;

		Set propmembers = new HashSet();
		if (s_log.isDebugEnabled())
			s_log.debug("find all property to literal: " + literal.getLabel());
		try {

			propmembers = onto.getDelegate().createAxiomRequest(
					DataPropertyMember.class).setCondition("targetValue",
					literal.getValue()).getAll();

			// no such axioms
			if (propmembers == null || propmembers.size() == 0)
				return null;

			propmemberSet = new HashSet<IPropertyMember>();
			for (Object propmember : propmembers) {
				propmemberSet.add((IPropertyMember) Kaon2OMSModelConverter
						.convertAxiom((OWLAxiom) propmember, onto));
				if (s_log.isDebugEnabled())
					s_log.debug("property member found: "
							+ propmember.toString());
			}
		}

		catch (KAON2Exception e) {
			throw new DatasourceException(e);
		}

		return propmemberSet;
	}

	public Set<IPropertyMember> findByIndividual(IIndividual individual)
			throws DatasourceException {
		if (s_log.isDebugEnabled())
			s_log.debug("find all property of individual: "
					+ individual.getLabel());
		Set<IPropertyMember> propmembers = new HashSet<IPropertyMember>();
		propmembers.addAll(findBySourceIndividual(individual));
		propmembers.addAll(findByTargetIndividual(individual));
		return propmembers;
	}

	public IPropertyMember findByUri(String uri) throws DatasourceException {
		// TODO meta method
		StatelessSession session = (StatelessSession) SessionFactory
				.getInstance().getCurrentSession();
		Kaon2Ontology onto = (Kaon2Ontology) session.getOntology();

		Set<OWLAxiom> props = new HashSet<OWLAxiom>();

		try {
			Request<ObjectPropertyMember> oPropMemberRequest = onto
					.getDelegate().createAxiomRequest(
							ObjectPropertyMember.class);
			oPropMemberRequest.setCondition("objectProperty", KAON2Manager
					.factory().objectProperty(uri));
			oPropMemberRequest.setCondition("sourceIndividual", KAON2Manager
					.factory().individual(uri));
			oPropMemberRequest.setCondition("targetIndividual", KAON2Manager
					.factory().individual(uri));
			props.addAll(oPropMemberRequest.getAll());

			Request<DataPropertyMember> dPropMemberRequest = onto.getDelegate()
					.createAxiomRequest(DataPropertyMember.class);
			dPropMemberRequest.setCondition("dataProperty", KAON2Manager
					.factory().dataProperty(uri));
			dPropMemberRequest.setCondition("sourceIndividual", KAON2Manager
					.factory().individual(uri));

			props.addAll(dPropMemberRequest.getAll());

		} catch (KAON2Exception e) {
			throw new DatasourceException(e);
		}

		// no such axioms
		if (props == null || props.size() == 0)
			return null;

		for (OWLAxiom prop : props) {
			return (IPropertyMember) Kaon2OMSModelConverter.convertAxiom(prop,
					onto);
		}

		return null;
	}

	public IPropertyMember findObjectPropertyMember(String objectPropertyUri,
			String sourceIndividualUri, String targetIndividualUri) {
		StatelessSession session = (StatelessSession) SessionFactory
				.getInstance().getCurrentSession();
		Kaon2Ontology onto = (Kaon2Ontology) session.getOntology();

		Set<OWLAxiom> props = new HashSet<OWLAxiom>();

		try {
			// get all data properties
			Request<ObjectPropertyMember> oPropMemberRequest = onto
					.getDelegate().createAxiomRequest(
							ObjectPropertyMember.class);
			oPropMemberRequest.setCondition("objectProperty", KAON2Manager
					.factory().objectProperty(objectPropertyUri));
			oPropMemberRequest.setCondition("sourceIndividual", KAON2Manager
					.factory().individual(sourceIndividualUri));
			oPropMemberRequest.setCondition("targetIndividual", KAON2Manager
					.factory().individual(targetIndividualUri));
			props.addAll(oPropMemberRequest.getAll());
		} catch (KAON2Exception e) {
			throw new DatasourceException(e);
		}

		// no such axioms
		if (props == null || props.size() == 0)
			return null;

		for (OWLAxiom prop : props) {
			return (IPropertyMember) Kaon2OMSModelConverter.convertAxiom(prop,
					onto);
		}

		return null;
	}

	public IPropertyMember findDataPropertyMember(String dataPropertyUri,
			String sourceIndividualUri, String targetValue) {
		StatelessSession session = (StatelessSession) SessionFactory
				.getInstance().getCurrentSession();
		Kaon2Ontology onto = (Kaon2Ontology) session.getOntology();

		Set<OWLAxiom> props = new HashSet<OWLAxiom>();

		try {
			// get all data properties
			Request<DataPropertyMember> dPropMemberRequest = onto.getDelegate()
					.createAxiomRequest(DataPropertyMember.class);
			dPropMemberRequest.setCondition("dataProperty", KAON2Manager
					.factory().dataProperty(dataPropertyUri));
			dPropMemberRequest.setCondition("sourceIndividual", KAON2Manager
					.factory().individual(sourceIndividualUri));
			dPropMemberRequest.setCondition("targetValue", KAON2Manager
					.factory().constant(targetValue));
			props.addAll(dPropMemberRequest.getAll());
		} catch (KAON2Exception e) {
			throw new DatasourceException(e);
		}

		// no such axioms
		if (props == null || props.size() == 0)
			return null;

		for (OWLAxiom prop : props) {
			return (IPropertyMember) Kaon2OMSModelConverter.convertAxiom(prop,
					onto);
		}

		return null;
	}

	public Set<IPropertyMember> findByAgent(IEntity agent)
			throws DatasourceException {
		return findByMetaAnnotationValue(MetaVocabulary.AGENT, "<"
				+ agent.getUri() + ">");
	}

	public Set<IPropertyMember> findByAgent(String agentUri)
			throws DatasourceException {
		return findByMetaAnnotationValue(MetaVocabulary.AGENT, "<" + agentUri
				+ ">");
	}

	public Set<IPropertyMember> findByConfidenceDegree(double degree,
			int degreeType) throws DatasourceException {
		return findByMetaAnnotationValue(MetaVocabulary.CONFIDENCE_DEGREE, "\""
				+ degree + "\"^^<http://www.w3.org/2001/XMLSchema#float>");
	}

	public Set<IPropertyMember> findByConfidenceDegreeBetween(
			double lowerbound, int degreeTypeLower, long upperbound,
			int degreeTypeUpper) throws DatasourceException {
		// TODO do not implement (for now)
		return null;
	}

	public Set<IPropertyMember> findByCreationDate(Date creationDate, int type)
			throws DatasourceException {
		return findByMetaAnnotationValue(MetaVocabulary.CREATION_TIME, "\""
				+ m_xsdDateFormat.format(creationDate)
				+ "\"^^<http://www.w3.org/2001/XMLSchema#dateTime>");
	}

	public Set<IPropertyMember> findByCreationDateBetween(Date before,
			int beforeType, Date after, int aftertype)
			throws DatasourceException {
		// TODO do not implement (for now)
		return null;
	}

	public Set<IPropertyMember> findBySource(IEntity source)
			throws DatasourceException {
		return findByMetaAnnotationValue(MetaVocabulary.SOURCE, "<"
				+ source.getUri() + ">");
	}

	public Set<IPropertyMember> findBySource(String sourceUri)
			throws DatasourceException {
		return findByMetaAnnotationValue(MetaVocabulary.SOURCE, "<" + sourceUri
				+ ">");
	}

	private Set<IPropertyMember> findByMetaAnnotationValue(String propertyUri,
			String value) {
		Set<IPropertyMember> propertyMembers = new HashSet<IPropertyMember>();
		String query = "SELECT ?e ?a ?b ?c ?p WHERE { \n";
		query += "?e <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <"
				+ AXIOM_METAVIEW_ONTOLOGY_URI + "#DataPropertyAssertion>. \n"
				+ "?e <" + AXIOM_METAVIEW_ONTOLOGY_URI
				+ "#dataProperty> ?a .\n" + "?e <"
				+ AXIOM_METAVIEW_ONTOLOGY_URI + "#sourceIndividual> ?b .\n"
				+ "?e <" + AXIOM_METAVIEW_ONTOLOGY_URI + "#targetValue> ?c .\n"
				+ "?e <" + MetaVocabulary.HAS_PROVENANCE + "> ?p . \n" + "?p <"
				+ propertyUri + "> " + value + " .\n";
		query += "}";

		String query2 = "SELECT ?e ?a ?b ?c WHERE { \n";
		query2 += "?e <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <"
				+ AXIOM_METAVIEW_ONTOLOGY_URI + "#ObjectPropertyAssertion>. \n"
				+ "?e <" + AXIOM_METAVIEW_ONTOLOGY_URI
				+ "#objectProperty> ?a .\n" + "?e <"
				+ AXIOM_METAVIEW_ONTOLOGY_URI + "#sourceIndividual> ?b .\n"
				+ "?e <" + AXIOM_METAVIEW_ONTOLOGY_URI
				+ "#targetIndividual> ?c .\n" + "?e <"
				+ MetaVocabulary.HAS_PROVENANCE + "> ?p . \n" + "?p <"
				+ propertyUri + "> " + value + " .\n" + "}";
		try {
			List<String[]> results = runQuery(m_axiomMetaview, query);
			for (String[] result : results)
				propertyMembers.add(findDataPropertyMember(result[1],
						result[2], result[3]));
			results = runQuery(m_axiomMetaview, query2);
			for (String[] result : results)
				propertyMembers.add(findObjectPropertyMember(result[1],
						result[2], result[3]));
		} catch (KAON2Exception e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return propertyMembers;
	}

	private String getAxiomQueryStatements(IPropertyMember propMember,
			String variable) {
		String query = "";

		if (propMember.getType() == PropertyMember.DATA_PROPERTY_MEMBER) {
			DataPropertyMember dpm = (DataPropertyMember) propMember
					.getDelegate();
			query += variable
					+ " <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <"
					+ AXIOM_METAVIEW_ONTOLOGY_URI
					+ "#DataPropertyAssertion>. \n" + variable + " <"
					+ AXIOM_METAVIEW_ONTOLOGY_URI + "#dataProperty> <"
					+ propMember.getProperty().getUri() + ">.\n" + variable
					+ " <" + AXIOM_METAVIEW_ONTOLOGY_URI
					+ "#sourceIndividual> <"
					+ dpm.getSourceIndividual().getURI() + ">.\n" + variable
					+ " <" + AXIOM_METAVIEW_ONTOLOGY_URI + "#targetValue> <"
					+ dpm.getTargetValue().getValue() + ">.\n";
		} else if (propMember.getType() == PropertyMember.OBJECT_PROPERTY_MEMBER) {
			ObjectPropertyMember opm = (ObjectPropertyMember) propMember
					.getDelegate();
			query += variable
					+ " <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <"
					+ AXIOM_METAVIEW_ONTOLOGY_URI
					+ "#ObjectPropertyAssertion>. \n" + variable + " <"
					+ AXIOM_METAVIEW_ONTOLOGY_URI + "#objectProperty> <"
					+ propMember.getProperty().getUri() + ">.\n" + variable
					+ " <" + AXIOM_METAVIEW_ONTOLOGY_URI
					+ "#sourceIndividual> <"
					+ opm.getSourceIndividual().getURI() + ">.\n" + variable
					+ " <" + AXIOM_METAVIEW_ONTOLOGY_URI
					+ "#targetIndividual> <"
					+ opm.getTargetIndividual().getURI() + ">.\n";
		}

		return query;
	}

	private String getAxiomUri(IPropertyMember propMember) {
		String query = "SELECT ?e WHERE { \n";

		if (propMember.getType() == PropertyMember.DATA_PROPERTY_MEMBER) {
			DataPropertyMember dpm = (DataPropertyMember) propMember
					.getDelegate();
			query += "?e <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <"
					+ AXIOM_METAVIEW_ONTOLOGY_URI
					+ "#DataPropertyAssertion>. \n" + "?e <"
					+ AXIOM_METAVIEW_ONTOLOGY_URI + "#dataProperty> <"
					+ propMember.getProperty().getUri() + ">.\n" + "?e <"
					+ AXIOM_METAVIEW_ONTOLOGY_URI + "#sourceIndividual> <"
					+ dpm.getSourceIndividual().getURI() + ">.\n" + "?e <"
					+ AXIOM_METAVIEW_ONTOLOGY_URI + "#targetValue> <"
					+ dpm.getTargetValue().getValue() + ">.\n";
		} else if (propMember.getType() == PropertyMember.OBJECT_PROPERTY_MEMBER) {
			ObjectPropertyMember opm = (ObjectPropertyMember) propMember
					.getDelegate();
			query += "?e <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <"
					+ AXIOM_METAVIEW_ONTOLOGY_URI
					+ "#ObjectPropertyAssertion>. \n" + "?e <"
					+ AXIOM_METAVIEW_ONTOLOGY_URI + "#objectProperty> <"
					+ propMember.getProperty().getUri() + ">.\n" + "?e <"
					+ AXIOM_METAVIEW_ONTOLOGY_URI + "#sourceIndividual> <"
					+ opm.getSourceIndividual().getURI() + ">.\n" + "?e <"
					+ AXIOM_METAVIEW_ONTOLOGY_URI + "#targetIndividual> <"
					+ opm.getTargetIndividual().getURI() + ">.\n";
		}

		query += "}";

		try {
			System.out.println(query);
			List<String[]> results = runQuery(m_axiomMetaview, query);
			return results.get(0)[0];

		} catch (KAON2Exception e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}

	protected List<String[]> getMetaAnnotationValue(IResource res,
			String propertyUri) {
		Emergency.checkPrecondition(res instanceof PropertyMember,
				"res instanceof PropertyMember");

		updateMetaviews();

		PropertyMember propMember = (PropertyMember) res;

		String axiomUri = getAxiomUri(propMember);
		if (axiomUri == null)
			return null;

		try {
			String query = "SELECT ?a WHERE { \n" + "<" + axiomUri + "> <"
					+ MetaVocabulary.HAS_PROVENANCE + "> ?e . \n" + "?e <"
					+ propertyUri + "> ?a . \n" + "}";
			List<String[]> results = runQuery(m_axiomMetaview, query);
			return results;
		} catch (KAON2Exception e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (DatasourceException e) {
			e.printStackTrace();
		}

		return null;
	}

	public void delete(IPropertyMember propertyMember)
			throws BODeletionException {
		Kaon2Transaction trans = getTransaction();

		Set<Axiom> toDelete = getRelatedAxioms(propertyMember);

		if (toDelete.size() == 0)
			throw new BODeletionException(null);

		for (Axiom axiom : toDelete) {
			OntologyChangeEvent event = new OntologyChangeEvent(axiom,
					OntologyChangeEvent.ChangeType.REMOVE);
			trans.addChanges(event);
		}
	}

	public void delete(INamedIndividual subject, IProperty property,
			IResource object) throws BODeletionException {
		Kaon2Transaction trans = getTransaction();
		Ontology onto = getOntology();

		Set<Axiom> toDelete = new HashSet<Axiom>();

		try {
			if (object instanceof IIndividual) {
				Request<ObjectPropertyMember> request = onto
						.createAxiomRequest(ObjectPropertyMember.class);
				request.setCondition("objectProperty", property.getDelegate());
				request.setCondition("sourceIndividual", subject.getDelegate());
				request.setCondition("targetIndividual", object.getDelegate());
				toDelete.addAll(request.getAll());
			} else if (object instanceof ILiteral) {
				Request<ObjectPropertyMember> request = onto
						.createAxiomRequest(ObjectPropertyMember.class);
				request.setCondition("dataProperty", property.getDelegate());
				request.setCondition("sourceIndividual", subject.getDelegate());
				request.setCondition("targetValue", object.getDelegate());
				toDelete.addAll(request.getAll());
			}
		} catch (KAON2Exception e) {
			throw new BODeletionException(null, e);
		}

		if (toDelete.size() == 0)
			throw new BODeletionException(null);

		for (Axiom axiom : toDelete) {
			OntologyChangeEvent event = new OntologyChangeEvent(axiom,
					OntologyChangeEvent.ChangeType.REMOVE);
			trans.addChanges(event);
		}
	}

	public IProvenance createProvenance(INamedIndividual agent,
			Double confidenceDegree, Date creationDate, IEntity source) {
		Provenance p = new Provenance(confidenceDegree, agent, source,
				creationDate);
		return p;
	}

	public IProvenance createProvenance(String uri, INamedIndividual agent,
			Double confidenceDegree, Date creationDate, IEntity source) {
		Provenance p = new Provenance(uri, confidenceDegree, agent, source,
				creationDate);
		return p;
	}

	public Set<INamedIndividual> getAgents(IPropertyMember res)
			throws ProvenanceUnknownException {
		Set<INamedIndividual> agents = new HashSet<INamedIndividual>();
		for (IProvenance provenance : getProvenances(res))
			agents.add(provenance.getAgent());
		return agents;
	}

	public Double[] getConfidenceDegrees(IPropertyMember res)
			throws ProvenanceUnknownException {
		List<Double> cdegrees = new ArrayList<Double>();
		for (IProvenance provenance : getProvenances(res))
			cdegrees.add(provenance.getConfidenceDegree());
		return cdegrees.toArray(new Double[0]);
	}

	public Set<Date> getCreationDates(IPropertyMember res)
			throws ProvenanceUnknownException {
		Set<Date> dates = new HashSet<Date>();
		for (IProvenance provenance : getProvenances(res))
			dates.add(provenance.getCreationDate());
		return dates;
	}

	public String getProvenanceProperty(String provenanceUri, String property) {
		String query = "SELECT ?e WHERE { \n" + "<" + provenanceUri + "> <"
				+ property + "> ?e . \n" + "}";

		try {
			List<String[]> results = runQuery(m_axiomMetaview, query);
			if (results.size() > 0) {
				return results.get(0)[0];
			}
		} catch (KAON2Exception e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return null;
	}

	public Set<IProvenance> getProvenances(IPropertyMember res)
			throws ProvenanceUnknownException {
		Emergency.checkPrecondition(res instanceof PropertyMember,
				"res instanceof PropertyMember");

		updateMetaviews();

		PropertyMember propMember = (PropertyMember) res;

		Set<IProvenance> provenances = new HashSet<IProvenance>();

		String axiomUri = getAxiomUri(propMember);
		if (axiomUri == null)
			return provenances;

		try {
			String query = "SELECT ?a ?p WHERE { \n"
					+ getAxiomQueryStatements(propMember, "?a") + "?a <"
					+ MetaVocabulary.HAS_PROVENANCE + "> ?p . \n" + "}";
			List<String[]> results = runQuery(m_axiomMetaview, query);

			for (String[] result : results) {
				INamedIndividual agent = null;
				String agentUri = getProvenanceProperty(result[1],
						MetaVocabulary.AGENT);
				if (agentUri != null) {
					agent = (INamedIndividual) Kaon2OMSModelConverter
							.convertEntity(KAON2Manager.factory().individual(
									agentUri), null);
				}

				IEntity source = null;
				String sourceUri = getProvenanceProperty(result[1],
						MetaVocabulary.SOURCE);
				if (sourceUri != null)
					source = PersistenceUtil.getDaoManager().getIndividualDao()
							.findByUri(sourceUri);

				Double cdegree = null;
				String cdegreeString = getProvenanceProperty(result[1],
						MetaVocabulary.CONFIDENCE_DEGREE);
				if (cdegreeString != null) {
					Pattern p = Pattern.compile("\"(\\d+\\.\\d+)\"\\^\\^.*");
					Matcher m = p.matcher(cdegreeString);
					if (m.matches())
						cdegree = Double.valueOf(m.group(1));
				}

				String ctimeString = getProvenanceProperty(result[1],
						MetaVocabulary.CREATION_TIME);
				Date ctime = null;
				if (ctimeString != null) {
					Pattern p = Pattern.compile("\"(.*)\"\\^\\^.*dateTime.*");
					Matcher m = p.matcher(ctimeString);
					if (m.matches())
						ctime = m_xsdDateFormat.parse(m.group(1));
				}

				provenances.add(createProvenance(result[1], agent, cdegree,
						ctime, source));
			}
		} catch (KAON2Exception e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (DatasourceException e) {
			e.printStackTrace();
		} catch (DaoUnavailableException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return provenances;
	}

	public Set<IEntity> getSources(IPropertyMember res)
			throws ProvenanceUnknownException {
		Set<IEntity> sources = new HashSet<IEntity>();
		for (IProvenance provenance : getProvenances(res))
			sources.add(provenance.getSource());
		return sources;
	}

	public void delete(IIndividual subject, IProperty property, IResource object)
			throws BODeletionException {

		// TODO implement

		Kaon2Transaction trans = getTransaction();
		Ontology onto = getOntology();

		Set<Axiom> toDelete = new HashSet<Axiom>();

		try {
			if (object instanceof IIndividual) {
				Request<ObjectPropertyMember> request = onto
						.createAxiomRequest(ObjectPropertyMember.class);
				request.setCondition("objectProperty", property.getDelegate());
				request.setCondition("sourceIndividual", subject.getDelegate());
				request.setCondition("targetIndividual", object.getDelegate());
				toDelete.addAll(request.getAll());
			} else if (object instanceof ILiteral) {
				Request<ObjectPropertyMember> request = onto
						.createAxiomRequest(ObjectPropertyMember.class);
				request.setCondition("dataProperty", property.getDelegate());
				request.setCondition("sourceIndividual", subject.getDelegate());
				request.setCondition("targetValue", object.getDelegate());
				toDelete.addAll(request.getAll());
			}
		} catch (KAON2Exception e) {
			throw new BODeletionException(null, e);
		}

		if (toDelete.size() == 0)
			throw new BODeletionException(null);

		for (Axiom axiom : toDelete) {
			OntologyChangeEvent event = new OntologyChangeEvent(axiom,
					OntologyChangeEvent.ChangeType.REMOVE);
			trans.addChanges(event);
		}
	}

	public IPropertyMember find(IIndividual subject, IProperty property,
			IResource object) throws DatasourceException {
		// TODO implemen
		StatelessSession session = (StatelessSession) SessionFactory
				.getInstance().getCurrentSession();
		if (session.isReasoningOn() == false)
			return find(subject, property, object, false);
		else
			return find(subject, property, object, true);
	}

	public IPropertyMember find(IIndividual subject, IProperty property,
			IResource object, boolean includeInferred)
			throws DatasourceException {

		Set<IPropertyMember> propmembers = this.findByProperty(property,
				includeInferred);
		for (IPropertyMember propmember : propmembers) {
			if (propmember.getSource().equals(subject.getDelegate())
					&& propmember.getTarget().equals(object.getDelegate()))
				return propmember;
		}
		return null;

	}

	public List<IPropertyMember> findAllObjectPropertyMember(
			boolean includeInferred) throws DatasourceException {

		StatelessSession session = (StatelessSession) SessionFactory
				.getInstance().getCurrentSession();
		Kaon2Ontology onto = (Kaon2Ontology) session.getOntology();

		Set<OWLAxiom> props = new HashSet<OWLAxiom>();
		List<IPropertyMember> propMemberList = new ArrayList<IPropertyMember>();

		if (s_log.isDebugEnabled())
			s_log.debug("find all property members of the ontology: "
					+ session.getOntology().getUri());
		try {

			// get all object property members;
			Set<ObjectPropertyMember> opropmembers = new HashSet<ObjectPropertyMember>();
			Request<ObjectPropertyMember> oPropMemberRequest = onto
					.getDelegate().createAxiomRequest(
							ObjectPropertyMember.class);
			opropmembers.addAll(oPropMemberRequest.getAll());
			props.addAll(opropmembers);
			if (includeInferred) {
				for (ObjectPropertyMember opropmember : opropmembers) {
					Request<SubObjectPropertyOf> subObjectPropRequest = onto
							.getDelegate().createAxiomRequest(
									SubObjectPropertyOf.class).setCondition(
									"superDescription",
									opropmember.getObjectProperty());
					Set<SubObjectPropertyOf> subObjectProps = subObjectPropRequest
							.getAll();
					for (SubObjectPropertyOf subObjectProp : subObjectProps) {
						List<ObjectPropertyExpression> objectPropExps = subObjectProp
								.getSubObjectProperties();
						for (ObjectPropertyExpression objectPropExp : objectPropExps)
							props.add(KAON2Manager.factory()
									.objectPropertyMember(objectPropExp,
											opropmember.getSourceIndividual(),
											opropmember.getTargetIndividual()));

					}
				}
			}
		} catch (KAON2Exception e) {
			throw new DatasourceException(e);
		}

		// no such axioms
		if (props == null || props.size() == 0)
			return null;

		for (OWLAxiom prop : props) {
			propMemberList.add((IPropertyMember) Kaon2OMSModelConverter
					.convertAxiom(prop, onto));
			if (s_log.isDebugEnabled())
				s_log.debug("property member found: " + prop.toString());
		}

		return propMemberList;
	}
	
	public List<IPropertyMember> findAllDataPropertyMember(
			boolean includeInferred) throws DatasourceException {

		StatelessSession session = (StatelessSession) SessionFactory
				.getInstance().getCurrentSession();
		Kaon2Ontology onto = (Kaon2Ontology) session.getOntology();

		Set<OWLAxiom> props = new HashSet<OWLAxiom>();
		List<IPropertyMember> propMemberList = new ArrayList<IPropertyMember>();

		if (s_log.isDebugEnabled())
			s_log.debug("find all property members of the ontology: "
					+ session.getOntology().getUri());
		try {

//			 get all data property members
			Set<DataPropertyMember> dpropmembers = new HashSet<DataPropertyMember>();
			Request<DataPropertyMember> dPropMemberRequest = onto.getDelegate()
					.createAxiomRequest(DataPropertyMember.class);
			dpropmembers.addAll(dPropMemberRequest.getAll());
			props.addAll(dpropmembers);
			if (includeInferred) {
				for (DataPropertyMember dpropmember : dpropmembers) {
					Request<SubDataPropertyOf> subDataPropRequest = onto
							.getDelegate().createAxiomRequest(
									SubDataPropertyOf.class).setCondition(
									"superDescription",
									dpropmember.getDataProperty());
					Set<SubDataPropertyOf> subDataProps = subDataPropRequest
							.getAll();
					for (SubDataPropertyOf subDataProp : subDataProps) {
						DataPropertyExpression dataPropExp = subDataProp
								.getSuperDataProperty();
						props.add(KAON2Manager.factory().dataPropertyMember(
								dataPropExp, dpropmember.getSourceIndividual(),
								dpropmember.getTargetValue()));
					}
				}
			}
		} catch (KAON2Exception e) {
			throw new DatasourceException(e);
		}

		// no such axioms
		if (props == null || props.size() == 0)
			return null;

		for (OWLAxiom prop : props) {
			propMemberList.add((IPropertyMember) Kaon2OMSModelConverter
					.convertAxiom(prop, onto));
			if (s_log.isDebugEnabled())
				s_log.debug("property member found: " + prop.toString());
		}

		return propMemberList;
	}

	public List<IPropertyMember> findAll(boolean includeInferred)
			throws DatasourceException {

		StatelessSession session = (StatelessSession) SessionFactory
				.getInstance().getCurrentSession();
		Kaon2Ontology onto = (Kaon2Ontology) session.getOntology();

		Set<OWLAxiom> props = new HashSet<OWLAxiom>();
		List<IPropertyMember> propMemberList = new ArrayList<IPropertyMember>();

		if (s_log.isDebugEnabled())
			s_log.debug("find all property members of the ontology: "
					+ session.getOntology().getUri());
		try {

			// get all object property members;
			Set<ObjectPropertyMember> opropmembers = new HashSet<ObjectPropertyMember>();
			Request<ObjectPropertyMember> oPropMemberRequest = onto
					.getDelegate().createAxiomRequest(
							ObjectPropertyMember.class);
			opropmembers.addAll(oPropMemberRequest.getAll());
			props.addAll(opropmembers);
			if (includeInferred) {
				for (ObjectPropertyMember opropmember : opropmembers) {
					Request<SubObjectPropertyOf> subObjectPropRequest = onto
							.getDelegate().createAxiomRequest(
									SubObjectPropertyOf.class).setCondition(
									"superDescription",
									opropmember.getObjectProperty());
					Set<SubObjectPropertyOf> subObjectProps = subObjectPropRequest
							.getAll();
					for (SubObjectPropertyOf subObjectProp : subObjectProps) {
						List<ObjectPropertyExpression> objectPropExps = subObjectProp
								.getSubObjectProperties();
						for (ObjectPropertyExpression objectPropExp : objectPropExps)
							props.add(KAON2Manager.factory()
									.objectPropertyMember(objectPropExp,
											opropmember.getSourceIndividual(),
											opropmember.getTargetIndividual()));

					}
				}
			}

			// get all data property members
			Set<DataPropertyMember> dpropmembers = new HashSet<DataPropertyMember>();
			Request<DataPropertyMember> dPropMemberRequest = onto.getDelegate()
					.createAxiomRequest(DataPropertyMember.class);
			dpropmembers.addAll(dPropMemberRequest.getAll());
			props.addAll(dpropmembers);
			if (includeInferred) {
				for (DataPropertyMember dpropmember : dpropmembers) {
					Request<SubDataPropertyOf> subDataPropRequest = onto
							.getDelegate().createAxiomRequest(
									SubDataPropertyOf.class).setCondition(
									"superDescription",
									dpropmember.getDataProperty());
					Set<SubDataPropertyOf> subDataProps = subDataPropRequest
							.getAll();
					for (SubDataPropertyOf subDataProp : subDataProps) {
						DataPropertyExpression dataPropExp = subDataProp
								.getSuperDataProperty();
						props.add(KAON2Manager.factory().dataPropertyMember(
								dataPropExp, dpropmember.getSourceIndividual(),
								dpropmember.getTargetValue()));
					}
				}
			}

		} catch (KAON2Exception e) {
			throw new DatasourceException(e);
		}

		// no such axioms
		if (props == null || props.size() == 0)
			return null;

		for (OWLAxiom prop : props) {
			propMemberList.add((IPropertyMember) Kaon2OMSModelConverter
					.convertAxiom(prop, onto));
			if (s_log.isDebugEnabled())
				s_log.debug("property member found: " + prop.toString());
		}

		return propMemberList;
	}

	public Set<IPropertyMember> findByIndividual(IIndividual individual,
			boolean includeInferred) throws DatasourceException {

		if (s_log.isDebugEnabled())
			s_log.debug("find all property of individual: "
					+ individual.getLabel());
		Set<IPropertyMember> propmembers = new HashSet<IPropertyMember>();
		propmembers.addAll(findBySourceIndividual(individual, includeInferred));
		propmembers.addAll(findByTargetIndividual(individual, includeInferred));
		return propmembers;

	}

	public Set<IPropertyMember> findByProperty(IProperty property,
			boolean includeInferred) throws DatasourceException {

		StatelessSession session = (StatelessSession) SessionFactory
				.getInstance().getCurrentSession();
		Kaon2Ontology onto = (Kaon2Ontology) session.getOntology();

		Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
		Set<IPropertyMember> propMemberList = new HashSet<IPropertyMember>();
		try {
			if (property instanceof ObjectProperty) {
				Set<ObjectPropertyMember> opropmembers = new HashSet<ObjectPropertyMember>();
				opropmembers.addAll(onto.getDelegate().createAxiomRequest(
						ObjectPropertyMember.class).setCondition(
						"objectProperty", property.getDelegate()).getAll());

				axioms.addAll(opropmembers);
				if (includeInferred) {
					for (ObjectPropertyMember opropmember : opropmembers) {
						Request<SubObjectPropertyOf> subObjectPropRequest = onto
								.getDelegate().createAxiomRequest(
										SubObjectPropertyOf.class)
								.setCondition("superDescription",
										opropmember.getObjectProperty());
						Set<SubObjectPropertyOf> subObjectProps = subObjectPropRequest
								.getAll();
						for (SubObjectPropertyOf subObjectProp : subObjectProps) {
							List<ObjectPropertyExpression> objectPropExps = subObjectProp
									.getSubObjectProperties();
							for (ObjectPropertyExpression objectPropExp : objectPropExps)
								axioms
										.add(KAON2Manager
												.factory()
												.objectPropertyMember(
														objectPropExp,
														opropmember
																.getSourceIndividual(),
														opropmember
																.getTargetIndividual()));

						}
					}
				}
			} else if (property instanceof DataProperty) {
				Set<DataPropertyMember> dpropmembers = new HashSet<DataPropertyMember>();
				dpropmembers.addAll(onto.getDelegate().createAxiomRequest(
						DataPropertyMember.class).setCondition("dataProperty",
						property.getDelegate()).getAll());
				axioms.addAll(dpropmembers);
				if (includeInferred) {
					for (DataPropertyMember dpropmember : dpropmembers) {
						Request<SubDataPropertyOf> subDataPropRequest = onto
								.getDelegate().createAxiomRequest(
										SubDataPropertyOf.class).setCondition(
										"superDescription",
										dpropmember.getDataProperty());
						Set<SubDataPropertyOf> subDataProps = subDataPropRequest
								.getAll();
						for (SubDataPropertyOf subDataProp : subDataProps) {
							DataPropertyExpression dataPropExp = subDataProp
									.getSuperDataProperty();
							axioms.add(KAON2Manager.factory()
									.dataPropertyMember(dataPropExp,
											dpropmember.getSourceIndividual(),
											dpropmember.getTargetValue()));
						}
					}
				}
			}
		} catch (KAON2Exception e) {
			// TODO transaction error
			e.printStackTrace();
		}
		// no such axioms
		if (axioms == null || axioms.size() == 0)
			return null;

		for (OWLAxiom axiom : axioms) {
			propMemberList.add((IPropertyMember) Kaon2OMSModelConverter
					.convertAxiom(axiom, onto));
			if (s_log.isDebugEnabled())
				s_log.debug("property member found: " + axiom.toString());
		}

		return propMemberList;
	}

	public Set<IPropertyMember> findBySourceIndividual(IIndividual individual,
			boolean includeInferred) throws DatasourceException {

		StatelessSession session = (StatelessSession) SessionFactory
				.getInstance().getCurrentSession();
		Kaon2Ontology onto = (Kaon2Ontology) session.getOntology();

		Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
		Set<IPropertyMember> propMemberList = new HashSet<IPropertyMember>();
		try {
			Set<ObjectPropertyMember> opropmembers = new HashSet<ObjectPropertyMember>();
			opropmembers.addAll(onto.getDelegate().createAxiomRequest(
					ObjectPropertyMember.class).setCondition(
					"sourceIndividual", individual.getDelegate()).getAll());
			axioms.addAll(opropmembers);
			if (includeInferred) {
				for (ObjectPropertyMember opropmember : opropmembers) {
					Request<SubObjectPropertyOf> subObjectPropRequest = onto
							.getDelegate().createAxiomRequest(
									SubObjectPropertyOf.class).setCondition(
									"superDescription",
									opropmember.getObjectProperty());
					Set<SubObjectPropertyOf> subObjectProps = subObjectPropRequest
							.getAll();
					for (SubObjectPropertyOf subObjectProp : subObjectProps) {
						List<ObjectPropertyExpression> objectPropExps = subObjectProp
								.getSubObjectProperties();
						for (ObjectPropertyExpression objectPropExp : objectPropExps)
							axioms.add(KAON2Manager.factory()
									.objectPropertyMember(objectPropExp,
											opropmember.getSourceIndividual(),
											opropmember.getTargetIndividual()));

					}
				}
			}
		} catch (KAON2Exception e) {
			// TODO transaction error
			e.printStackTrace();
		}
		// no such axioms
		if (axioms == null || axioms.size() == 0)
			return null;

		for (OWLAxiom axiom : axioms) {
			propMemberList.add((IPropertyMember) Kaon2OMSModelConverter
					.convertAxiom(axiom, onto));
			if (s_log.isDebugEnabled())
				s_log.debug("property member found: " + axiom.toString());
		}

		return propMemberList;

	}

	public Set<IPropertyMember> findByTargetIndividual(IIndividual individual,
			boolean includeInferred) throws DatasourceException {

		StatelessSession session = (StatelessSession) SessionFactory
				.getInstance().getCurrentSession();
		Kaon2Ontology onto = (Kaon2Ontology) session.getOntology();

		Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
		Set<IPropertyMember> propMemberList = new HashSet<IPropertyMember>();
		try {
			Set<ObjectPropertyMember> opropmembers = new HashSet<ObjectPropertyMember>();
			opropmembers.addAll(onto.getDelegate().createAxiomRequest(
					ObjectPropertyMember.class).setCondition(
					"targetIndividual", individual.getDelegate()).getAll());
			axioms.addAll(opropmembers);
			if (includeInferred) {
				for (ObjectPropertyMember opropmember : opropmembers) {
					Request<SubObjectPropertyOf> subObjectPropRequest = onto
							.getDelegate().createAxiomRequest(
									SubObjectPropertyOf.class).setCondition(
									"superDescription",
									opropmember.getObjectProperty());
					Set<SubObjectPropertyOf> subObjectProps = subObjectPropRequest
							.getAll();
					for (SubObjectPropertyOf subObjectProp : subObjectProps) {
						List<ObjectPropertyExpression> objectPropExps = subObjectProp
								.getSubObjectProperties();
						for (ObjectPropertyExpression objectPropExp : objectPropExps)
							axioms.add(KAON2Manager.factory()
									.objectPropertyMember(objectPropExp,
											opropmember.getSourceIndividual(),
											opropmember.getTargetIndividual()));

					}
				}
			}
		} catch (KAON2Exception e) {
			// TODO transaction error
			e.printStackTrace();
		}
		// no such axioms
		if (axioms == null || axioms.size() == 0)
			return null;

		for (OWLAxiom axiom : axioms) {
			propMemberList.add((IPropertyMember) Kaon2OMSModelConverter
					.convertAxiom(axiom, onto));
			if (s_log.isDebugEnabled())
				s_log.debug("property member found: " + axiom.toString());
		}

		return propMemberList;
	}

	public Set<IPropertyMember> findByTargetValue(ILiteral literal,
			boolean includeInferred) throws DatasourceException {

		// TODO implement
		Emergency.checkPrecondition(literal instanceof ILiteral,
				"literal instanceof ILiteral");

		StatelessSession session = (StatelessSession) SessionFactory
				.getInstance().getCurrentSession();
		Kaon2Ontology onto = (Kaon2Ontology) session.getOntology();

		Set<DataPropertyMember> propmembers = new HashSet<DataPropertyMember>();
		Set<IPropertyMember> propmemberSet = new HashSet<IPropertyMember>();

		if (s_log.isDebugEnabled())
			s_log.debug("find all property to literal: " + literal.getLabel());
		try {

			propmembers = onto.getDelegate().createAxiomRequest(
					DataPropertyMember.class).setCondition("targetValue",
					literal.getValue()).getAll();

			// no such axioms
			if (propmembers == null || propmembers.size() == 0)
				return null;

			for (DataPropertyMember propmember : propmembers) {
				propmemberSet.add((IPropertyMember) Kaon2OMSModelConverter
						.convertAxiom(propmember, onto));

				Set<SubDataPropertyOf> axioms = new HashSet<SubDataPropertyOf>();
				try {
					axioms = onto.getDelegate().createAxiomRequest(
							SubDataPropertyOf.class).setCondition(
							"superDescription", propmember).getAll();

					for (SubDataPropertyOf axiom : axioms) {
						propmemberSet.add((IPropertyMember) KAON2Manager
								.factory().dataPropertyMember(
										propmember.getDataProperty(),
										propmember.getSourceIndividual(),
										propmember.getTargetValue()));
					}
				} catch (KAON2Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}

		catch (KAON2Exception e) {
			throw new DatasourceException(e);
		}
		return propmemberSet;
	}

	public Set<IPropertyMember> findObjectPropertyMemberByIndividual(
			IIndividual individual, boolean includeInferred)
			throws DatasourceException {

		StatelessSession session = (StatelessSession) SessionFactory
				.getInstance().getCurrentSession();
		Kaon2Ontology onto = (Kaon2Ontology) session.getOntology();

		Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
		Set<IPropertyMember> propMemberList = new HashSet<IPropertyMember>();
		try {
			Set<ObjectPropertyMember> opropmembers = new HashSet<ObjectPropertyMember>();
			opropmembers.addAll(onto.getDelegate().createAxiomRequest(
					ObjectPropertyMember.class).setCondition(
					"sourceIndividual", individual.getDelegate()).getAll());
			opropmembers.addAll(onto.getDelegate().createAxiomRequest(
					ObjectPropertyMember.class).setCondition(
					"targetIndividual", individual.getDelegate()).getAll());
			axioms.addAll(opropmembers);
			if (includeInferred) {
				for (ObjectPropertyMember opropmember : opropmembers) {
					Request<SubObjectPropertyOf> subObjectPropRequest = onto
							.getDelegate().createAxiomRequest(
									SubObjectPropertyOf.class).setCondition(
									"superDescription",
									opropmember.getObjectProperty());
					Set<SubObjectPropertyOf> subObjectProps = subObjectPropRequest
							.getAll();
					for (SubObjectPropertyOf subObjectProp : subObjectProps) {
						List<ObjectPropertyExpression> objectPropExps = subObjectProp
								.getSubObjectProperties();
						for (ObjectPropertyExpression objectPropExp : objectPropExps)
							axioms.add(KAON2Manager.factory()
									.objectPropertyMember(objectPropExp,
											opropmember.getSourceIndividual(),
											opropmember.getTargetIndividual()));

					}
				}
			}
		} catch (KAON2Exception e) {
			// TODO transaction error
			e.printStackTrace();
		}
		// no such axioms
		if (axioms == null || axioms.size() == 0)
			return null;

		for (OWLAxiom axiom : axioms) {
			propMemberList.add((IPropertyMember) Kaon2OMSModelConverter
					.convertAxiom(axiom, onto));
			if (s_log.isDebugEnabled())
				s_log.debug("property member found: " + axiom.toString());
		}

		return propMemberList;

	}

	public Set<IPropertyMember> findObjectPropertyMemberBySource(
			IIndividual individual, boolean includeInferred)
			throws DatasourceException {

		StatelessSession session = (StatelessSession) SessionFactory
				.getInstance().getCurrentSession();
		Kaon2Ontology onto = (Kaon2Ontology) session.getOntology();

		Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
		Set<IPropertyMember> propMemberList = new HashSet<IPropertyMember>();
		try {
			Set<ObjectPropertyMember> opropmembers = new HashSet<ObjectPropertyMember>();
			opropmembers.addAll(onto.getDelegate().createAxiomRequest(
					ObjectPropertyMember.class).setCondition(
					"sourceIndividual", individual.getDelegate()).getAll());
			axioms.addAll(opropmembers);
			if (includeInferred) {
				for (ObjectPropertyMember opropmember : opropmembers) {
					Request<SubObjectPropertyOf> subObjectPropRequest = onto
							.getDelegate().createAxiomRequest(
									SubObjectPropertyOf.class).setCondition(
									"superDescription",
									opropmember.getObjectProperty());
					Set<SubObjectPropertyOf> subObjectProps = subObjectPropRequest
							.getAll();
					for (SubObjectPropertyOf subObjectProp : subObjectProps) {
						List<ObjectPropertyExpression> objectPropExps = subObjectProp
								.getSubObjectProperties();
						for (ObjectPropertyExpression objectPropExp : objectPropExps)
							axioms.add(KAON2Manager.factory()
									.objectPropertyMember(objectPropExp,
											opropmember.getSourceIndividual(),
											opropmember.getTargetIndividual()));

					}
				}
			}
		} catch (KAON2Exception e) {
			// TODO transaction error
			e.printStackTrace();
		}
		// no such axioms
		if (axioms == null || axioms.size() == 0)
			return null;

		for (OWLAxiom axiom : axioms) {
			propMemberList.add((IPropertyMember) Kaon2OMSModelConverter
					.convertAxiom(axiom, onto));
			if (s_log.isDebugEnabled())
				s_log.debug("property member found: " + axiom.toString());
		}

		return propMemberList;
	}

	public IPropertyMember insert(IIndividual subject, IProperty property,
			IResource object) throws BOInsertionException {
		StatelessSession session = (StatelessSession) SessionFactory
				.getInstance().getCurrentSession();
		Kaon2Ontology onto = (Kaon2Ontology) session.getOntology();
		Kaon2Transaction trans = getTransaction();

		Set<Axiom> axioms = new HashSet<Axiom>();

		IPropertyMember propMember;
		if (object instanceof ILiteral) {
			DataPropertyMember dpm = KAON2Manager
					.factory()
					.dataPropertyMember(
							(org.semanticweb.kaon2.api.owl.elements.DataProperty) property
									.getDelegate(),
							(Individual) subject.getDelegate(),
							(Constant) object.getDelegate());
			axioms.add(dpm);
			propMember = new PropertyMember(property, subject, object, onto,
					PropertyMember.DATA_PROPERTY_MEMBER);
			propMember.setDelegate(dpm);
		} else {
			ObjectPropertyMember opm = KAON2Manager
					.factory()
					.objectPropertyMember(
							(org.semanticweb.kaon2.api.owl.elements.ObjectProperty) property
									.getDelegate(),
							(Individual) subject.getDelegate(),
							(Individual) object.getDelegate());
			axioms.add(opm);
			propMember = new PropertyMember(property, subject, object, onto,
					PropertyMember.OBJECT_PROPERTY_MEMBER);
			propMember.setDelegate(opm);
		}

		for (Axiom axiom : axioms) {
			OntologyChangeEvent event = new OntologyChangeEvent(axiom,
					OntologyChangeEvent.ChangeType.ADD);
			trans.addChanges(event);
		}

		return propMember;
	}

	public IPropertyMember insert(IIndividual subject, IProperty property,
			IResource object, IProvenance provenance)
			throws BOInsertionException {
		StatelessSession session = (StatelessSession) SessionFactory
				.getInstance().getCurrentSession();
		Kaon2Ontology onto = (Kaon2Ontology) session.getOntology();
		Kaon2Transaction trans = getTransaction();

		Set<Axiom> axioms = new HashSet<Axiom>();

		insert(subject, property, object);

		return null;
	}

	public void attachProvenance(IPropertyMember propMember,
			IProvenance provenance) throws Exception {
		StatelessSession session = (StatelessSession) SessionFactory
				.getInstance().getCurrentSession();
		Ontology onto = ((Kaon2Ontology) session.getOntology()).getDelegate();

		updateMetaviews();

		String axiomUri = getAxiomUri(propMember);
		if (axiomUri == null)
			throw new DatasourceException("URI of axiom not found.");

		for (EntityAnnotation e : onto.createAxiomRequest(
				EntityAnnotation.class).getAll()) {
			System.out.println(e);
		}

		try {
			String provenanceUri;
			int i = 0;
			Request<Entity> req;
			do {
				i++;
				provenanceUri = onto.getOntologyURI() + "#provenance" + i;
			} while (onto.containsEntity(KAON2Manager.factory().individual(
					provenanceUri), true)
					&& i < Integer.MAX_VALUE);

			if (i == Integer.MAX_VALUE)
				throw new Exception("Cannot generate new provenance URI.");

			Set<Axiom> axioms = new HashSet<Axiom>();
			KAON2Factory factory = KAON2Manager.factory();
			Individual prov = factory.individual(provenanceUri);

			if (provenance.getAgent() != null) {
				axioms.add(factory.objectPropertyMember(factory
						.objectProperty(MetaVocabulary.AGENT), prov,
						(Individual) provenance.getAgent().getDelegate()));
			}

			if (provenance.getSource() != null) {
				axioms.add(factory.objectPropertyMember(factory
						.objectProperty(MetaVocabulary.SOURCE), prov,
						(Individual) provenance.getSource().getDelegate()));
			}

			if (provenance.getConfidenceDegree() != null) {
				axioms.add(factory.dataPropertyMember(factory
						.dataProperty(MetaVocabulary.CONFIDENCE_DEGREE), prov,
						factory.constant(provenance.getConfidenceDegree())));
			}

			if (provenance.getCreationDate() != null) {
				axioms.add(factory.dataPropertyMember(factory
						.dataProperty(MetaVocabulary.CREATION_TIME), prov,
						factory.constant(provenance.getCreationDate())));
			}

			List<OntologyChangeEvent> changes = new ArrayList<OntologyChangeEvent>();
			for (Axiom axiom : axioms) {
				OntologyChangeEvent event = new OntologyChangeEvent(axiom,
						OntologyChangeEvent.ChangeType.ADD);
				changes.add(event);
			}

			m_axiomMetaview.applyChanges(changes);
		} catch (KAON2Exception e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (DatasourceException e) {
			e.printStackTrace();
		}
	}

	public void updateProvenance(IPropertyMember propMember,
			IProvenance provenance) {

	}

	public void removeProvenance(IPropertyMember propMember,
			IProvenance provenance) {

	}

	public void removeAllProvenances(IPropertyMember propMember) {

	}

	public List<IPropertyMember> findAll(boolean includeInferred,
			boolean includeProvenanceStatements) throws DatasourceException {
		// TODO Auto-generated method stub
		return null;
	}

	public void delete(Set<IPropertyMember> propertyMembers) throws BOsDeletionException, BODeletionException {
		// TODO Auto-generated method stub
		
	}
}
