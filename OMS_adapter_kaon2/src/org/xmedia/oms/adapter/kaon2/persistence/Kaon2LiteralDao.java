package org.xmedia.oms.adapter.kaon2.persistence;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.aifb.xxplore.shared.exception.Emergency;
import org.apache.log4j.Logger;
import org.semanticweb.kaon2.api.Axiom;
import org.semanticweb.kaon2.api.KAON2Exception;
import org.semanticweb.kaon2.api.OntologyChangeEvent;
import org.semanticweb.kaon2.api.owl.axioms.DataPropertyMember;
import org.semanticweb.kaon2.api.owl.axioms.DataPropertyRange;
import org.xmedia.businessobject.IBusinessObject;
import org.xmedia.oms.adapter.kaon2.util.Kaon2OMSModelConverter;
import org.xmedia.oms.model.api.IDatatype;
import org.xmedia.oms.model.api.ILiteral;
import org.xmedia.oms.model.impl.Literal;
import org.xmedia.oms.persistence.DatasourceException;
import org.xmedia.oms.persistence.SessionFactory;
import org.xmedia.oms.persistence.StatelessSession;
import org.xmedia.oms.persistence.dao.ILiteralDao;

public class Kaon2LiteralDao implements ILiteralDao {

	private static Logger s_log = Logger.getLogger(Kaon2LiteralDao.class);

	public void delete(IBusinessObject existingBo) throws DatasourceException {
		Emergency.checkPrecondition(existingBo instanceof ILiteral,
				"existingBo instanceof ILiteral");
		ILiteral literal = (ILiteral) existingBo;

		StatelessSession session = (StatelessSession) SessionFactory
				.getInstance().getCurrentSession();
		Kaon2Transaction trans = (Kaon2Transaction) session.getTransaction();

		List<Axiom> toDelete = getRelatedAxioms(literal);

		for (Axiom axiom : toDelete) {
			OntologyChangeEvent event = new OntologyChangeEvent(axiom,
					OntologyChangeEvent.ChangeType.REMOVE);
			trans.addChanges(event);
		}
	}

	private List<Axiom> getRelatedAxioms(ILiteral literal) {
		// TODO Auto-generated method stub
		StatelessSession session = (StatelessSession) SessionFactory
				.getInstance().getCurrentSession();
		Kaon2Ontology onto = (Kaon2Ontology) session.getOntology();

		List<Axiom> axioms = new ArrayList<Axiom>();
		try {
			axioms.addAll(onto.getDelegate().createAxiomRequest(
					DataPropertyMember.class).setCondition("targetValue",
					literal.getDelegate()).getAll());
		} catch (KAON2Exception e) {
			// TODO transaction error
			e.printStackTrace();
		}
		return (List<Axiom>) axioms;
	}

	// need to be checked. what does it means by id in Literal?
	public IBusinessObject findById(String id) throws DatasourceException {
		StatelessSession session = (StatelessSession) SessionFactory
				.getInstance().getCurrentSession();
		Kaon2Ontology onto = (Kaon2Ontology) session.getOntology();
		List<ILiteral> literals = null;

		Set<DataPropertyMember> propmembers = new HashSet<DataPropertyMember>();
		try {

			propmembers = onto.getDelegate().createAxiomRequest(
					DataPropertyMember.class).getAll();
			// no such axioms
			if (propmembers == null || propmembers.size() == 0)
				return null;

			literals = new ArrayList<ILiteral>();
			for (DataPropertyMember propmember : propmembers) {
				if (propmember.getAxiomID().equals(id))
					return (ILiteral) Kaon2OMSModelConverter.convert(propmember
							.getTargetValue(), onto);
			}
		} catch (KAON2Exception e) {
			throw new DatasourceException(e);
		}
		return null;
	}

	public Class getBoClass() {
		// TODO Auto-generated method stub
		return Literal.class;
	}

	public void insert(IBusinessObject newBo) throws DatasourceException {
		throw new UnsupportedOperationException(
				"Insert/update unsupported for literals.");
	}

	public void update(IBusinessObject existingBo) throws DatasourceException {
		throw new UnsupportedOperationException(
				"Insert/update unsupported for literals.");
	}

	public List<ILiteral> findAll() throws DatasourceException {
		StatelessSession session = (StatelessSession) SessionFactory
				.getInstance().getCurrentSession();
		Kaon2Ontology onto = (Kaon2Ontology) session.getOntology();
		List<ILiteral> literals = null;

		Set<DataPropertyMember> propmembers = new HashSet<DataPropertyMember>();
		s_log.debug("find all literals:");
		try {

			propmembers = onto.getDelegate().createAxiomRequest(
					DataPropertyMember.class).getAll();
			// no such axioms
			if (propmembers == null || propmembers.size() == 0)
				return null;

			literals = new ArrayList<ILiteral>();
			for (DataPropertyMember propmember : propmembers) {
				literals.add((ILiteral) Kaon2OMSModelConverter.convert(
						propmember.getTargetValue(), onto));
				if (s_log.isDebugEnabled())
					s_log.debug("property member found: "
							+ propmember.toString());
			}
		} catch (KAON2Exception e) {
			throw new DatasourceException(e);
		}
		return literals;
	}

	public Set<ILiteral> findMemberIndividuals(IDatatype datatype)
			throws DatasourceException {
		// TODO Auto-generated method stub
		StatelessSession session = (StatelessSession) SessionFactory
				.getInstance().getCurrentSession();
		Kaon2Ontology onto = (Kaon2Ontology) session.getOntology();

		Set<DataPropertyRange> dPropRanges = new HashSet<DataPropertyRange>();
		Set<ILiteral> literals = new HashSet<ILiteral>();

		try {
			dPropRanges.addAll(onto.getDelegate().createAxiomRequest(
					DataPropertyRange.class).setCondition("datatype",
					datatype.getDelegate()).getAll());
		} catch (KAON2Exception e) {
			e.printStackTrace();
		}
		if (dPropRanges == null || dPropRanges.size() == 0)
			return null;

		for (DataPropertyRange dProprange : dPropRanges) {
			Set<DataPropertyMember> propmembers = new HashSet<DataPropertyMember>();
			try {
				propmembers = onto.getDelegate().createAxiomRequest(
						DataPropertyMember.class).setCondition("dataProperty",
						dProprange.getDataProperty()).getAll();
			} catch (KAON2Exception e) {
				e.printStackTrace();
			}
			for (DataPropertyMember propmember : propmembers) {
				literals.add((ILiteral) Kaon2OMSModelConverter.convert(
						propmember.getTargetValue(), onto));
			}
		}
		return literals;
	}
}
