/**
 * 
 */
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
import org.semanticweb.kaon2.api.owl.axioms.ClassMember;
import org.semanticweb.kaon2.api.owl.axioms.DataPropertyMember;
import org.semanticweb.kaon2.api.owl.axioms.ObjectPropertyMember;
import org.semanticweb.kaon2.api.owl.axioms.SubClassOf;
import org.semanticweb.kaon2.api.owl.elements.Description;
import org.semanticweb.kaon2.api.owl.elements.Individual;
import org.xmedia.businessobject.IBusinessObject;
import org.xmedia.oms.adapter.kaon2.util.Kaon2OMSModelConverter;
import org.xmedia.oms.model.api.IConcept;
import org.xmedia.oms.model.api.IHierarchicalSchema;
import org.xmedia.oms.model.api.IHierarchicalSchemaNode;
import org.xmedia.oms.model.api.IIndividual;
import org.xmedia.oms.model.api.INamedConcept;
import org.xmedia.oms.model.api.INamedIndividual;
import org.xmedia.oms.model.impl.NamedIndividual;
import org.xmedia.oms.persistence.DatasourceException;
import org.xmedia.oms.persistence.SessionFactory;
import org.xmedia.oms.persistence.StatelessSession;
import org.xmedia.oms.persistence.dao.IIndividualDao;

class Kaon2IndividualDao extends AbstractKaon2Dao implements IIndividualDao {

	private static Logger s_log = Logger.getLogger(Kaon2IndividualDao.class);

	public Class getBoClass() {

		return NamedIndividual.class;
	}

	private Set<Axiom> getRelatedAxioms(INamedIndividual individual) {
		StatelessSession session = (StatelessSession) SessionFactory
				.getInstance().getCurrentSession();
		Kaon2Ontology onto = (Kaon2Ontology) session.getOntology();

		org.semanticweb.kaon2.api.owl.elements.Individual owlInd = (org.semanticweb.kaon2.api.owl.elements.Individual) individual
				.getDelegate();

		Set<Axiom> axioms = new HashSet<Axiom>();
		try {
			axioms.addAll(onto.getDelegate().createAxiomRequest(
					ClassMember.class).setCondition("individual", owlInd)
					.getAll());

			axioms.addAll(onto.getDelegate().createAxiomRequest(
					ObjectPropertyMember.class).setCondition(
					"targetIndividual", owlInd).getAll());

			axioms.addAll(onto.getDelegate().createAxiomRequest(
					ObjectPropertyMember.class).setCondition(
					"sourceIndividual", owlInd).getAll());

			axioms.addAll(onto.getDelegate().createAxiomRequest(
					DataPropertyMember.class).setCondition("sourceIndividual",
					owlInd).getAll());
		} catch (KAON2Exception e) {
			// TODO transaction error
			e.printStackTrace();
		}

		return axioms;
	}

	public void delete(IBusinessObject existingBo) throws DatasourceException {
		Emergency.checkPrecondition(existingBo instanceof NamedIndividual,
				"existingBo instanceof NamedIndividual");
		NamedIndividual individual = (NamedIndividual) existingBo;

		Kaon2Transaction trans = getTransaction();

		Set<Axiom> toDelete = getRelatedAxioms(individual);

		for (Axiom axiom : toDelete) {
			OntologyChangeEvent event = new OntologyChangeEvent(axiom,
					OntologyChangeEvent.ChangeType.REMOVE);
			trans.addChanges(event);
		}
	}

	public void insert(IBusinessObject newBo) throws DatasourceException {
		throw new UnsupportedOperationException(
				"Insert/update unsupported for entities.");
	}

	public void update(IBusinessObject existingBo) throws DatasourceException {
		throw new UnsupportedOperationException(
				"Insert/update unsupported for entities.");
	}

	public List<IIndividual> findAll() throws DatasourceException {
		StatelessSession session = (StatelessSession) SessionFactory
				.getInstance().getCurrentSession();
		Kaon2Ontology onto = (Kaon2Ontology) session.getOntology();

		if (s_log.isDebugEnabled())
			s_log.debug("find all named individuals of the ontology: "
					+ session.getOntology().getUri());

		Set<Individual> inds = null;
		List<IIndividual> indList = new ArrayList<IIndividual>();

		// get all individuals
		try {
			Request<Individual> indRequest = onto.getDelegate()
					.createEntityRequest(Individual.class);
			inds = indRequest.getAll();

		} catch (KAON2Exception e) {

			throw new DatasourceException(e);
		}

		// no such individuals
		if (inds == null || inds.size() == 0)
			return null;

		for (Individual ind : inds) {
			indList.add((INamedIndividual) Kaon2OMSModelConverter
					.convertEntity(ind, onto));
			if (s_log.isDebugEnabled())
				s_log.debug("individual found: " + ind.getURI());
		}
		return indList;
	}

	public IBusinessObject findById(String id) throws DatasourceException {
		// TODO
		return findByUri(id);
	}

	public Set<IIndividual> findMemberIndividuals(INamedConcept concept)
			throws DatasourceException {
		/*
		 * StatelessSession session =
		 * (StatelessSession)SessionFactory.getInstance().getCurrentSession();
		 * Kaon2Ontology onto = (Kaon2Ontology) session.getOntology();
		 * 
		 * Set<ClassMember> kaon2inds = null; Set<IIndividual> inds = new
		 * HashSet<IIndividual>();
		 * 
		 * if (s_log.isDebugEnabled()) s_log.debug("find member individuals of
		 * concept: " + concept.getLabel()); try {
		 * 
		 * concept = (INamedConcept)checkForDelegate(concept); if(concept ==
		 * null) return null;
		 * 
		 * kaon2inds = onto.getDelegate().createAxiomRequest(ClassMember.class).
		 * setCondition("description", concept.getDelegate()).getAll();
		 * 
		 * //no such axioms if(kaon2inds == null || kaon2inds.size() == 0)
		 * return null;
		 * 
		 * inds = new HashSet<IIndividual>(); for (ClassMember ind :
		 * kaon2inds){
		 * inds.add((INamedIndividual)Kaon2OMSModelConverter.convertEntity(ind.getIndividual(),
		 * onto)); if (s_log.isDebugEnabled()) s_log.debug("individual found: " +
		 * ind.getIndividual().getURI());
		 *  } }
		 * 
		 * catch (KAON2Exception e) { throw new DatasourceException(e); }
		 * 
		 * return inds;
		 */
		StatelessSession session = (StatelessSession) SessionFactory
				.getInstance().getCurrentSession();
		if (session.isReasoningOn() == false)
			return findMemberIndividuals(concept, false);
		else
			return findMemberIndividuals(concept, true);
	}

	public int getNumberOfIndividual(INamedConcept concept) throws DatasourceException{
		StatelessSession session = (StatelessSession) SessionFactory
				.getInstance().getCurrentSession();
		if (session.isReasoningOn() == false)
			return getNumberOfIndividual(concept, false);
		else
			return getNumberOfIndividual(concept, true);
	}
	
	public int getNumberOfIndividual(INamedConcept concept,
			boolean includeInferred) throws DatasourceException{
		StatelessSession session = (StatelessSession) SessionFactory
				.getInstance().getCurrentSession();
		Kaon2Ontology onto = (Kaon2Ontology) session.getOntology();

		Set<INamedConcept> concepts = new HashSet<INamedConcept>();
		concepts.add(concept);

		if (includeInferred) {
			IHierarchicalSchema schema = onto.getHierarchicalSchema();
			IHierarchicalSchemaNode node = (IHierarchicalSchemaNode) schema
					.getNode(concept);
			Set<IHierarchicalSchemaNode> descendants = node.getDescendants();
			for (IHierarchicalSchemaNode descendant : descendants)
				concepts.addAll(descendant.getConcepts());
		} else {
			Set<SubClassOf> axioms = new HashSet<SubClassOf>();

			try {
				axioms = onto.getDelegate()
						.createAxiomRequest(SubClassOf.class).setCondition(
								"superDescription", concept.getDelegate())
						.getAll();

				for (SubClassOf axiom : axioms) {
					Description desc = axiom.getSubDescription();
					concepts.add((INamedConcept) Kaon2OMSModelConverter
							.convert(desc, onto));
				}
			} catch (KAON2Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		if (s_log.isDebugEnabled())
			s_log.debug("find member individuals of concept: "
					+ concept.getLabel());
		try {
			int num = 0;
			for (INamedConcept nConcept : concepts) {
				nConcept = (INamedConcept) checkForDelegate(nConcept);
				if (nConcept == null)
					continue;
				num += onto.getDelegate().createAxiomRequest(
						ClassMember.class).setCondition("description",
						nConcept.getDelegate()).sizeAll();
			}

			return num;
		}

		catch (KAON2Exception e) {
			throw new DatasourceException(e);
		}
	}
	
	public Set<IIndividual> findMemberIndividuals(INamedConcept concept,
			boolean includeInferred) throws DatasourceException {
		StatelessSession session = (StatelessSession) SessionFactory
				.getInstance().getCurrentSession();
		Kaon2Ontology onto = (Kaon2Ontology) session.getOntology();

		Set<ClassMember> kaon2inds = new HashSet<ClassMember>();
		Set<IIndividual> inds = new HashSet<IIndividual>();
		Set<INamedConcept> concepts = new HashSet<INamedConcept>();
		concepts.add(concept);

		if (includeInferred) {
			IHierarchicalSchema schema = onto.getHierarchicalSchema();
			IHierarchicalSchemaNode node = (IHierarchicalSchemaNode) schema
					.getNode(concept);
			Set<IHierarchicalSchemaNode> descendants = node.getDescendants();
			for (IHierarchicalSchemaNode descendant : descendants)
				concepts.addAll(descendant.getConcepts());
		} else {
			Set<SubClassOf> axioms = new HashSet<SubClassOf>();

			try {
				axioms = onto.getDelegate()
						.createAxiomRequest(SubClassOf.class).setCondition(
								"superDescription", concept.getDelegate())
						.getAll();

				for (SubClassOf axiom : axioms) {
					Description desc = axiom.getSubDescription();
					concepts.add((INamedConcept) Kaon2OMSModelConverter
							.convert(desc, onto));
				}
			} catch (KAON2Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		if (s_log.isDebugEnabled())
			s_log.debug("find member individuals of concept: "
					+ concept.getLabel());
		try {
			for (INamedConcept nConcept : concepts) {
				nConcept = (INamedConcept) checkForDelegate(nConcept);
				if (nConcept == null)
					continue;
				kaon2inds.addAll(onto.getDelegate().createAxiomRequest(
						ClassMember.class).setCondition("description",
						nConcept.getDelegate()).getAll());
			}

			// no such axioms
			if (kaon2inds == null || kaon2inds.size() == 0)
				return null;

			inds = new HashSet<IIndividual>();
			for (ClassMember ind : kaon2inds) {
				inds.add((INamedIndividual) Kaon2OMSModelConverter
						.convertEntity(ind.getIndividual(), onto));
				if (s_log.isDebugEnabled())
					s_log.debug("individual found: "
							+ ind.getIndividual().getURI());
			}
		}

		catch (KAON2Exception e) {
			throw new DatasourceException(e);
		}
		return inds;
	}

	public INamedIndividual findByUri(String uri) throws DatasourceException {
		StatelessSession session = (StatelessSession) SessionFactory
				.getInstance().getCurrentSession();
		Kaon2Ontology onto = (Kaon2Ontology) session.getOntology();
		// construct an Individual with the uri and ask if the ontology contains
		// it
		try {
			Individual individual = KAON2Manager.factory().individual(uri);
			// TODO this may be not efficitent as retrieving the exsiting
			// concept
			if (onto.getDelegate().containsEntity(individual, true)) {
				if (s_log.isDebugEnabled())
					s_log.debug("individual found: " + uri);
				return (INamedIndividual) Kaon2OMSModelConverter.convertEntity(
						individual, onto);
			}
		}

		catch (KAON2Exception e) {
			throw new DatasourceException(e);
		}

		if (s_log.isDebugEnabled())
			s_log.debug("individual could not be found !");
		return null;
	}
}