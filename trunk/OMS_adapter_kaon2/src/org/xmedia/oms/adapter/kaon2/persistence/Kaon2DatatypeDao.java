package org.xmedia.oms.adapter.kaon2.persistence;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.aifb.xxplore.shared.exception.Emergency;
import org.apache.log4j.Logger;
import org.semanticweb.kaon2.api.Axiom;
import org.semanticweb.kaon2.api.KAON2Exception;
import org.semanticweb.kaon2.api.KAON2Manager;
import org.semanticweb.kaon2.api.OntologyChangeEvent;
import org.semanticweb.kaon2.api.Request;
import org.semanticweb.kaon2.api.owl.axioms.DataPropertyAttribute;
import org.semanticweb.kaon2.api.owl.axioms.DataPropertyDomain;
import org.semanticweb.kaon2.api.owl.axioms.DataPropertyMember;
import org.semanticweb.kaon2.api.owl.axioms.DataPropertyRange;
import org.semanticweb.kaon2.api.owl.axioms.ObjectPropertyAttribute;
import org.semanticweb.kaon2.api.owl.axioms.ObjectPropertyDomain;
import org.semanticweb.kaon2.api.owl.axioms.ObjectPropertyMember;
import org.semanticweb.kaon2.api.owl.axioms.ObjectPropertyRange;
import org.semanticweb.kaon2.api.owl.axioms.SubDataPropertyOf;
import org.semanticweb.kaon2.api.owl.axioms.SubObjectPropertyOf;
import org.semanticweb.kaon2.api.owl.elements.DataProperty;
import org.semanticweb.kaon2.api.owl.elements.Datatype;
import org.semanticweb.kaon2.api.owl.elements.OWLClass;
import org.semanticweb.kaon2.api.owl.elements.OWLEntity;
import org.semanticweb.kaon2.api.owl.elements.ObjectProperty;
import org.xmedia.businessobject.IBusinessObject;
import org.xmedia.oms.adapter.kaon2.util.Kaon2OMSModelConverter;
import org.xmedia.oms.model.api.IDatatype;
import org.xmedia.oms.model.api.ILiteral;
import org.xmedia.oms.model.api.INamedConcept;
import org.xmedia.oms.model.api.IProperty;
import org.xmedia.oms.persistence.DatasourceException;
import org.xmedia.oms.persistence.SessionFactory;
import org.xmedia.oms.persistence.StatelessSession;
import org.xmedia.oms.persistence.dao.IDatatypeDao;

public class Kaon2DatatypeDao implements IDatatypeDao {

	/** Cached Subsumption hierarchy */
	// public IHierarchy m_subsumptionhierarchy;
	private static Logger s_log = Logger.getLogger(Kaon2DatatypeDao.class);

	public Class getBoClass() {
		return IDatatype.class;
	}

	public void delete(IBusinessObject existingBo) throws DatasourceException {
		// TODO
		Emergency.checkPrecondition(existingBo instanceof IDatatype,
				"existingBo instanceof Datatype");
		org.xmedia.oms.model.impl.Datatype type = (org.xmedia.oms.model.impl.Datatype) existingBo;

		StatelessSession session = (StatelessSession) SessionFactory
				.getInstance().getCurrentSession();
		Kaon2Transaction trans = (Kaon2Transaction) session.getTransaction();

		Set<Axiom> toDelete = getRelatedAxioms(type);

		for (Axiom axiom : toDelete) {
			OntologyChangeEvent event = new OntologyChangeEvent(axiom,
					OntologyChangeEvent.ChangeType.REMOVE);
			trans.addChanges(event);
		}
	}

	private Set<Axiom> getRelatedAxioms(org.xmedia.oms.model.impl.Datatype type) {
		// TODO Auto-generated method stub

		StatelessSession session = (StatelessSession) SessionFactory
				.getInstance().getCurrentSession();
		Kaon2Ontology onto = (Kaon2Ontology) session.getOntology();

		Set<Axiom> axioms = new HashSet<Axiom>();
		try {
			axioms.addAll(onto.getDelegate().createAxiomRequest(
					DataPropertyRange.class).setCondition(
					"datatype", type.getDelegate()).getAll());
		} catch (KAON2Exception e) {
			// TODO transaction error
			e.printStackTrace();
		}
		return axioms;
	}

	public IBusinessObject findById(String id) throws DatasourceException {
		// TODO

		StatelessSession session = (StatelessSession)SessionFactory.getInstance().getCurrentSession();
		Kaon2Ontology onto = (Kaon2Ontology) session.getOntology();

		//construct an data type with the uri and ask if the ontology contains it 
		try {

			//construct a clazz to make the request
			Datatype dType = KAON2Manager.factory().datatype(id);
			//TODO this may be not efficitent as retrieving the exsiting concept
			if (onto.getDelegate().containsEntity(dType, true)) {
				if (s_log.isDebugEnabled()) s_log.debug("datatype found: " + id);
				return (INamedConcept)Kaon2OMSModelConverter.convert(dType, onto);
			}
		}
		catch(KAON2Exception e){
			throw new DatasourceException(e);
		}
		if (s_log.isDebugEnabled()) s_log.debug("data type could not be found !");
		return null;
	}

	public void insert(IBusinessObject newBo) throws DatasourceException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException(
				"Insert/update unsupported for entities.");
	}

	public void update(IBusinessObject existingBo) throws DatasourceException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException(
				"Insert/update unsupported for entities.");
	}

	public List<IDatatype> findAll() throws DatasourceException {
		// TODO
		StatelessSession session = (StatelessSession) SessionFactory
				.getInstance().getCurrentSession();
		Kaon2Ontology onto = (Kaon2Ontology) session.getOntology();

		Set<OWLEntity> dTypes = new HashSet<OWLEntity>();
		List<IDatatype> dTypeList = new ArrayList<IDatatype>();

		if (s_log.isDebugEnabled())
			s_log.debug("find all data types of the ontology: "
					+ session.getOntology().getUri());
		try {
			// get all data types
			Request<Datatype> oDatatypeRequest = onto.getDelegate()
					.createEntityRequest(Datatype.class);
			dTypes.addAll(oDatatypeRequest.getAll());
		} catch (KAON2Exception e) {
			throw new DatasourceException(e);
		}

		// no such data type
		if (dTypes == null || dTypes.size() == 0)
			return null;

		for (OWLEntity dType : dTypes) {
			dTypeList.add((IDatatype) Kaon2OMSModelConverter.convert(dType,
					onto));
			if (s_log.isDebugEnabled())
				s_log.debug("type found: " + dType.getURI());
		}
		return dTypeList;
	}

	public Set<IDatatype> findDatatypes(ILiteral literal)
			throws DatasourceException {
		StatelessSession session = (StatelessSession) SessionFactory
				.getInstance().getCurrentSession();
		Kaon2Ontology onto = (Kaon2Ontology) session.getOntology();

		Set<DataPropertyMember> dPropMembers = new HashSet<DataPropertyMember>();
		Set<IDatatype> dTypes = new HashSet<IDatatype>();

		try {
			dPropMembers.addAll(onto.getDelegate().createAxiomRequest(
					DataPropertyMember.class).setCondition("targetValue",
					literal.getDelegate()).getAll());
		} catch (KAON2Exception e) {
			e.printStackTrace();
		}
		if (dPropMembers == null || dPropMembers.size() == 0)
			return null;

		for (DataPropertyMember dPropMember : dPropMembers) {
			Set<DataPropertyRange> dPropRanges = new HashSet<DataPropertyRange>();
			try {
				dPropRanges.addAll(onto.getDelegate().createAxiomRequest(
						DataPropertyRange.class).setCondition("dataProperty",
						dPropMember.getDataProperty()).getAll());
			} catch (KAON2Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			for (DataPropertyRange dProprange : dPropRanges) {
				dTypes.add((IDatatype) Kaon2OMSModelConverter.convert(
						dProprange.getRange(), onto));
			}
		}
		return dTypes;
	}

	public Set<IDatatype> findDatatypeRanges(IProperty property)
			throws DatasourceException {
		// TODO Auto-generated method stub
		Emergency.checkPrecondition(property instanceof DataProperty,
				"existingBo instanceof Dataproperty");
		StatelessSession session = (StatelessSession) SessionFactory
				.getInstance().getCurrentSession();
		Kaon2Ontology onto = (Kaon2Ontology) session.getOntology();

		Set<DataPropertyRange> dPropRanges = new HashSet<DataPropertyRange>();
		Set<IDatatype> dTypes = new HashSet<IDatatype>();

		try {
			dPropRanges.addAll(onto.getDelegate().createAxiomRequest(
					DataPropertyRange.class).setCondition("dataProperty",
					property.getDelegate()).getAll());
		} catch (KAON2Exception e) {
			e.printStackTrace();
		}
		if (dPropRanges == null || dPropRanges.size() == 0)
			return null;

		for (DataPropertyRange dProprange : dPropRanges) {
			dTypes.add((IDatatype) Kaon2OMSModelConverter.convert(dProprange
					.getRange(), onto));
		}
		return dTypes;
	}
}
