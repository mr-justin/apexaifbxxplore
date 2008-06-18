/**
 * 
 */
package org.xmedia.oms.adapter.kaon2.persistence;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.aifb.xxplore.shared.exception.Emergency;
import org.apache.log4j.Logger;
import org.semanticweb.kaon2.api.Axiom;
import org.semanticweb.kaon2.api.KAON2Exception;
import org.semanticweb.kaon2.api.KAON2Manager;
import org.semanticweb.kaon2.api.OntologyChangeEvent;
import org.semanticweb.kaon2.api.Request;
import org.semanticweb.kaon2.api.logic.Literal;
import org.semanticweb.kaon2.api.logic.Term;
import org.semanticweb.kaon2.api.logic.Variable;
import org.semanticweb.kaon2.api.owl.axioms.ClassMember;
import org.semanticweb.kaon2.api.owl.axioms.DataPropertyDomain;
import org.semanticweb.kaon2.api.owl.axioms.DataPropertyMember;
import org.semanticweb.kaon2.api.owl.axioms.DisjointClasses;
import org.semanticweb.kaon2.api.owl.axioms.EquivalentClasses;
import org.semanticweb.kaon2.api.owl.axioms.ObjectPropertyDomain;
import org.semanticweb.kaon2.api.owl.axioms.ObjectPropertyMember;
import org.semanticweb.kaon2.api.owl.axioms.ObjectPropertyRange;
import org.semanticweb.kaon2.api.owl.axioms.SubClassOf;
import org.semanticweb.kaon2.api.owl.elements.Description;
import org.semanticweb.kaon2.api.owl.elements.OWLClass;
import org.semanticweb.kaon2.api.reasoner.Query;
import org.semanticweb.kaon2.api.reasoner.Reasoner;
import org.xmedia.businessobject.IBusinessObject;
import org.xmedia.oms.adapter.kaon2.util.Kaon2OMSModelConverter;
import org.xmedia.oms.model.api.IConcept;
import org.xmedia.oms.model.api.IDatatype;
import org.xmedia.oms.model.api.IEntity;
import org.xmedia.oms.model.api.IHierarchicalSchema;
import org.xmedia.oms.model.api.IHierarchicalSchemaNode;
import org.xmedia.oms.model.api.INamedConcept;
import org.xmedia.oms.model.api.INamedIndividual;
import org.xmedia.oms.model.api.IObjectProperty;
import org.xmedia.oms.model.api.IProperty;
import org.xmedia.oms.model.api.IResource;
import org.xmedia.oms.model.api.ISchemaNode;
import org.xmedia.oms.model.impl.DataProperty;
import org.xmedia.oms.model.impl.NamedConcept;
import org.xmedia.oms.model.impl.ObjectProperty;
import org.xmedia.oms.model.impl.PropertyMember;
import org.xmedia.oms.persistence.DatasourceException;
import org.xmedia.oms.persistence.SessionFactory;
import org.xmedia.oms.persistence.StatelessSession;
import org.xmedia.oms.persistence.dao.IConceptDao;


public class Kaon2ConceptDao extends AbstractKaon2Dao implements IConceptDao{


	/** Cached Subsumption hierarchy*/
	//public IHierarchy m_subsumptionhierarchy;

	private static Logger s_log = Logger.getLogger(Kaon2ConceptDao.class);

	public Class getBoClass() {

		return NamedConcept.class;
	}

	public void delete(IBusinessObject existingBo) throws DatasourceException {

		Emergency.checkPrecondition(existingBo instanceof NamedConcept, "existingBo instanceof NamedConcept");
		NamedConcept clazz = (NamedConcept)existingBo;

		StatelessSession session = (StatelessSession)SessionFactory.getInstance().getCurrentSession();
		Kaon2Transaction trans = (Kaon2Transaction)session.getTransaction();

		Set<Axiom> toDelete = getRelatedAxioms(clazz);

		for (Axiom axiom : toDelete){
			OntologyChangeEvent event = new OntologyChangeEvent(
					axiom, 
					OntologyChangeEvent.ChangeType.REMOVE);
			trans.addChanges(event);
		}
	}

	private Set<Axiom> getRelatedAxioms(INamedConcept concept){
		StatelessSession session = (StatelessSession)SessionFactory.getInstance().getCurrentSession();
		Kaon2Ontology onto = (Kaon2Ontology) session.getOntology();

		org.semanticweb.kaon2.api.owl.elements.OWLClass owlClass = 
			(org.semanticweb.kaon2.api.owl.elements.OWLClass)concept.getDelegate();
		
		Set<Axiom> axioms = new HashSet<Axiom>();
		try {
			axioms.addAll(onto.getDelegate().createAxiomRequest(ClassMember.class)
					.setCondition("description", owlClass).getAll());
			
			axioms.addAll(onto.getDelegate().createAxiomRequest(SubClassOf.class)
					.setCondition("subDescription", owlClass).getAll());
			
			axioms.addAll(onto.getDelegate().createAxiomRequest(SubClassOf.class)
					.setCondition("superDescription", owlClass).getAll());
			
			axioms.addAll(onto.getDelegate().createAxiomRequest(DataPropertyDomain.class)
					.setCondition("domain", owlClass).getAll());
			
			axioms.addAll(onto.getDelegate().createAxiomRequest(ObjectPropertyDomain.class)
					.setCondition("domain", owlClass).getAll());
			
			axioms.addAll(onto.getDelegate().createAxiomRequest(ObjectPropertyRange.class)
					.setCondition("range", owlClass).getAll());
		} catch (KAON2Exception e) {
			// TODO transaction error
			e.printStackTrace();
		}
		
		return axioms;
	}

	/**
	 * Find by id that is the uri of the concept
	 */
	public IBusinessObject findById(String id) throws DatasourceException {
		// TODO
		StatelessSession session = (StatelessSession) SessionFactory
				.getInstance().getCurrentSession();
		Kaon2Ontology onto = (Kaon2Ontology) session.getOntology();

		// construct an OWLClass with the id and ask if the ontology contains it
		try {
			// construct a clazz to make the request
			OWLClass owlclazz = KAON2Manager.factory().owlClass(id);
			// TODO this may be not efficitent as retrieving the exsiting
			// concept
			if (onto.getDelegate().containsEntity(owlclazz, true)) {
				if (s_log.isDebugEnabled())
					s_log.debug("concept found: " + id);
				return (INamedConcept) Kaon2OMSModelConverter.convertEntity(
						(OWLClass) owlclazz, onto);
			}
		}

		catch (KAON2Exception e) {
			throw new DatasourceException(e);
		}

		if (s_log.isDebugEnabled())
			s_log.debug("concept could not be found !");
		return null;
	}

	public void insert(IBusinessObject newBo) throws DatasourceException {
		throw new UnsupportedOperationException("Insert/update unsupported for entities.");
	}

	public void update(IBusinessObject existingBo) throws DatasourceException {
		throw new UnsupportedOperationException("Insert/update unsupported for entities.");
	}

	public List<INamedConcept> findAll() throws DatasourceException{

		StatelessSession session = (StatelessSession)SessionFactory.getInstance().getCurrentSession();
		List<INamedConcept> clazzList = new ArrayList<INamedConcept>();
		if (session.isReasoningOn() == false){

			Kaon2Ontology onto = (Kaon2Ontology) session.getOntology();

			if (s_log.isDebugEnabled()) s_log.debug("find all Named Concept of the ontology: " + session.getOntology().getUri());
			Set<OWLClass> clazzs = null;

			//get all owl classes 
			try {
				Request<OWLClass> clazzRequest = onto.getDelegate().createEntityRequest(OWLClass.class);	
				clazzs = clazzRequest.getAll();	


			} catch (KAON2Exception e) {

				throw new DatasourceException(e);
			}

			//no such classes
			if(clazzs == null || clazzs.size() == 0) return null;

			for (OWLClass clazz  : clazzs){
				clazzList.add((INamedConcept)Kaon2OMSModelConverter.convert(clazz, onto));
				if (s_log.isDebugEnabled()) s_log.debug("concept found: " + clazz.getURI());
			}
		}
		
		//else return findAll(boolean inferred);

		return clazzList;
	}

	public Set<IConcept> findDisjointConcepts(INamedConcept concept) throws DatasourceException{
		StatelessSession session = (StatelessSession)SessionFactory.getInstance().getCurrentSession();
		Kaon2Ontology onto = (Kaon2Ontology) session.getOntology();
		Set<IConcept> clazzSet = null;

		Set<DisjointClasses> clazzs = new HashSet<DisjointClasses>();
		try {

			concept = (INamedConcept)checkForDelegate(concept); 
			if(concept == null) return null;

			//get disjoint class axioms where given concept is one of the descriptions 
			clazzs = onto.getDelegate().createAxiomRequest(DisjointClasses.class).
			setCondition("descriptions", concept.getDelegate()).getAll();

			//no such axioms 
			if(clazzs == null || clazzs.size() == 0) return null;

			clazzSet = new HashSet<IConcept>();
			for (DisjointClasses clazz  : clazzs){
				for(Description d : clazz.getDescriptions()){
					clazzSet.add((IConcept)Kaon2OMSModelConverter.convert(d, onto));
					if (s_log.isDebugEnabled()) s_log.debug("concept found: " + (d.toString()));
				}
			}
		} 

		catch (KAON2Exception e) {

			throw new DatasourceException(e);
		}

		return clazzSet;
	}

	public Set<IConcept> findEquivalentConcepts(INamedConcept concept) throws DatasourceException{
		StatelessSession session = (StatelessSession)SessionFactory.getInstance().getCurrentSession();
		Kaon2Ontology onto = (Kaon2Ontology) session.getOntology();
		Set<IConcept> clazzSet = null;

		if (s_log.isDebugEnabled()) s_log.debug("find equivalent concepts of: " + concept.getLabel());

		Set<EquivalentClasses> clazzs = new HashSet<EquivalentClasses>();
		try {

			concept = (INamedConcept)checkForDelegate(concept); 
			if(concept == null) return null;

			clazzs = onto.getDelegate().createAxiomRequest(EquivalentClasses.class).
			setCondition("descriptions", concept.getDelegate()).getAll();

			//no such classess
			if(clazzs == null || clazzs.size() == 0) return null;

			clazzSet = new HashSet<IConcept>();
			for (EquivalentClasses clazz  : clazzs){
				for (Description d : clazz.getDescriptions()){
					clazzSet.add((IConcept)Kaon2OMSModelConverter.convert(d, onto));
					if (s_log.isDebugEnabled()) s_log.debug("concept found: " + d.toString());
				}
			}
		} 

		catch (KAON2Exception e) {
			throw new DatasourceException(e);
		}

		return clazzSet;
	}

	public Set<IConcept> findSubconcepts(INamedConcept concept) {
		StatelessSession session = (StatelessSession)SessionFactory.getInstance().getCurrentSession();
		if (session.isReasoningOn() == false)
			return findSubconcepts(concept, false);
		else
			return findSubconcepts(concept, true);
	}

	public Set<IConcept> findSubconcepts(INamedConcept concept, boolean includeInferred) {
		StatelessSession session = (StatelessSession)SessionFactory.getInstance().getCurrentSession();
		Kaon2Ontology onto = (Kaon2Ontology) session.getOntology();

		if (s_log.isDebugEnabled()) s_log.debug("find subconcepts of: " + concept.getLabel() + ", includeInferred: " + includeInferred);
		

		Set<IConcept> subConcepts = new HashSet<IConcept>();
		
		if (includeInferred) {
			IHierarchicalSchema schema = onto.getHierarchicalSchema();

			IHierarchicalSchemaNode node = (IHierarchicalSchemaNode) schema.getNode(concept);
			Set<IHierarchicalSchemaNode> descendants = node.getDescendants();
			for (IHierarchicalSchemaNode descendant : descendants)
				subConcepts.addAll(descendant.getConcepts());
		}
		else {
			Set<SubClassOf> axioms = new HashSet<SubClassOf>();
			try {
				concept = (INamedConcept)checkForDelegate(concept); 
				if(concept == null) return null;
	
				axioms = onto.getDelegate().createAxiomRequest(SubClassOf.class).
				setCondition("superDescription", concept.getDelegate()).getAll();
	
				//no such axioms 
				if(axioms == null || axioms.size() == 0) return null;
	
				subConcepts = new HashSet<IConcept>();
				for (SubClassOf axiom : axioms){
	
					Description desc = axiom.getSubDescription();
								subConcepts.add((IConcept)Kaon2OMSModelConverter.convert(desc, onto));
					if (s_log.isDebugEnabled()) s_log.debug("concept found: " + desc.toString());
	
				}
			} 
			catch (KAON2Exception e) {
				throw new DatasourceException(e);
			}
		}
		
		return subConcepts;
	}

	public Set<IConcept> findSuperconcepts(INamedConcept concept) throws DatasourceException{
		StatelessSession session = (StatelessSession)SessionFactory.getInstance().getCurrentSession();
		if (session.isReasoningOn() == false)
			return findSuperconcepts(concept, false);
		else
			return findSuperconcepts(concept, true);
	}

	public Set<IConcept> findSuperconcepts(INamedConcept concept,
			boolean includeInferred) throws DatasourceException {
		// TODO implement
		StatelessSession session = (StatelessSession) SessionFactory
				.getInstance().getCurrentSession();
		Kaon2Ontology onto = (Kaon2Ontology) session.getOntology();
		Set<IConcept> superConcepts = new HashSet<IConcept>();

		if (s_log.isDebugEnabled())
			s_log.debug("find superconcepts of: " + concept.getLabel());

		if (includeInferred) {
			IHierarchicalSchema schema = onto.getHierarchicalSchema();
			IHierarchicalSchemaNode node = (IHierarchicalSchemaNode) schema
					.getNode(concept);
			Set<IHierarchicalSchemaNode> ancestors = node.getAncestors();
			for (IHierarchicalSchemaNode ancestor : ancestors)
				superConcepts.addAll(ancestor.getConcepts());
		} else {
			Set<SubClassOf> axioms = new HashSet<SubClassOf>();

			try {

				concept = (INamedConcept) checkForDelegate(concept);
				if (concept == null)
					return null;

				axioms = onto.getDelegate()
						.createAxiomRequest(SubClassOf.class).setCondition(
								"superDescription", concept.getDelegate())
						.getAll();

				// no such axioms
				if (axioms == null || axioms.size() == 0)
					return null;

				superConcepts = new HashSet<IConcept>();
				for (SubClassOf axiom : axioms) {

					Description desc = axiom.getSuperDescription();
					superConcepts.add((IConcept) Kaon2OMSModelConverter
							.convert(desc, onto));
					if (s_log.isDebugEnabled())
						s_log.debug("concept found: " + desc.toString());
				}
			}

			catch (KAON2Exception e) {
				throw new DatasourceException(e);
			}
		}
		return superConcepts;
	}
	
	public Set<IConcept> findTypes(INamedIndividual individual) throws DatasourceException{

		//this method require a named individual
		Emergency.checkPrecondition(individual instanceof INamedIndividual, "individual instanceof INamedIndividual");

		StatelessSession session = (StatelessSession)SessionFactory.getInstance().getCurrentSession();
		Kaon2Ontology onto = (Kaon2Ontology) session.getOntology();
		Set<IConcept> clazzSet = null;

		if (s_log.isDebugEnabled()) s_log.debug("find types of: " + individual.getLabel());

		Set<ClassMember> axioms = new HashSet<ClassMember>();
		try {

			individual = (INamedIndividual)checkForDelegate(individual); 
			if(individual == null) return null;

			axioms = onto.getDelegate().createAxiomRequest(ClassMember.class).
			setCondition("individual", individual.getDelegate()).getAll();

			//no such axioms 
			if(axioms == null || axioms.size() == 0) return null;

			clazzSet = new HashSet<IConcept>();
			for (ClassMember axiom : axioms){

				Description desc = axiom.getDescription();
				clazzSet.add((IConcept)Kaon2OMSModelConverter.convert(desc, onto));
				if (s_log.isDebugEnabled()) s_log.debug("concept found: " + desc.toString());
			}
		} 

		catch (KAON2Exception e) {
			throw new DatasourceException(e);
		}

		return clazzSet;
	}


	public Set<INamedConcept> findDomains(IProperty property) throws DatasourceException {
		StatelessSession session = (StatelessSession)SessionFactory.getInstance().getCurrentSession();
		Kaon2Ontology onto = (Kaon2Ontology) session.getOntology();
		Set<INamedConcept> clazzSet = null;

		Set axioms = new HashSet();

		if (s_log.isDebugEnabled()) s_log.debug("find domains of: " + property.getUri());
		try {

			property = (IProperty)checkForDelegate(property); 
			if(property == null) return null;

			if(property instanceof ObjectProperty){

				axioms = onto.getDelegate().createAxiomRequest(ObjectPropertyDomain.class).
				setCondition("objectProperty", property.getDelegate()).getAll();
			}

			else if(property instanceof org.xmedia.oms.model.impl.DataProperty){

				axioms = onto.getDelegate().createAxiomRequest(DataPropertyDomain.class).
				setCondition("dataProperty", property.getDelegate()).getAll();
			}

			//if types has not been specified 
			else{

				//try object property
				axioms = onto.getDelegate().createAxiomRequest(ObjectPropertyDomain.class).
				setCondition("objectProperty", property.getDelegate()).getAll();

				if(axioms == null || axioms.size() == 0)
					//try data property
					axioms = onto.getDelegate().createAxiomRequest(DataPropertyDomain.class).
					setCondition("dataProperty", property.getDelegate()).getAll();
			}		

			//no such axioms 
			if(axioms == null || axioms.size() == 0) return null;

			clazzSet = new HashSet<INamedConcept>();
			for (Object axiom : axioms){

				Description desc = null; 
				if (axiom instanceof ObjectPropertyDomain) desc = ((ObjectPropertyDomain)axiom).getDomain();
				else if (axiom instanceof DataPropertyDomain) desc = ((DataPropertyDomain)axiom).getDomain();

				//retrieve only known concepts with uri, i.e. OWLClass not general descriptions 
				if (desc instanceof OWLClass){					
					clazzSet.add((INamedConcept)Kaon2OMSModelConverter.convertEntity((OWLClass)desc, onto));
					if (s_log.isDebugEnabled()) s_log.debug("concept found: " + ((OWLClass)desc).getURI());
				}
			}
		} 

		catch (KAON2Exception e) {
			throw new DatasourceException(e);
		}

		return clazzSet;
	}
	
	public Set<INamedConcept> findConceptRanges(IObjectProperty property) throws DatasourceException {

		StatelessSession session = (StatelessSession)SessionFactory.getInstance().getCurrentSession();
		Kaon2Ontology onto = (Kaon2Ontology) session.getOntology();
		Set<INamedConcept> clazzSet = null;

		Set axioms = new HashSet();

		if (s_log.isDebugEnabled()) s_log.debug("find ranges of: " + property.getUri());

		try {

			property = (ObjectProperty) checkForDelegate(property); 
			if(property == null) return null;

			axioms = onto.getDelegate().createAxiomRequest(ObjectPropertyRange.class).
			setCondition("objectProperty", property.getDelegate()).getAll();


			clazzSet = new HashSet<INamedConcept>();

			//no such axioms 
			if(axioms == null || axioms.size() == 0) return null;

			for (Object axiom : axioms){

				Description desc = ((ObjectPropertyRange)axiom).getRange();

				//retrieve only known concepts with uri, i.e. OWLClass not general descriptions 
				if (desc instanceof OWLClass){
					clazzSet.add((INamedConcept)Kaon2OMSModelConverter.convertEntity((OWLClass)desc, onto));
					if (s_log.isDebugEnabled()) s_log.debug("concept found: " + ((OWLClass)desc).getURI());
				}
			}
		} 

		catch (KAON2Exception e) {
			throw new DatasourceException(e);
		}

		return clazzSet;
	}

	public INamedConcept findByUri(String uri) throws DatasourceException {
		StatelessSession session = (StatelessSession)SessionFactory.getInstance().getCurrentSession();
		Kaon2Ontology onto = (Kaon2Ontology) session.getOntology();

		//Alternative one: construct an entity request and setCondition(uri, uri)
		//as this condition is yet not supported by KAON2 we have to checked on our own 

//		if (s_log.isDebugEnabled()) s_log.debug("find concept by following uri: " + uri);

//		Cursor<OWLClass> clazzs; 
//		try {
//		clazzs = onto.getDelegate().createEntityRequest(OWLClass.class).
//		setCondition("uri", uri).openCursor();

//		while(clazzs.hasNext()){

//		OWLClass clazz;
//		clazz = clazzs.next();
//		if(clazz.getURI().equals(uri)){
//		return ((INamedConcept)Kaon2OMSModelConverter.convertEntity(clazz, onto));
//		}

//		}
//		} catch (KAON2Exception e) {
//		// TODO Auto-generated catch block
//		e.printStackTrace();
//		}


		//Alternative 2: construct an OWLClass with the uri and ask if the ontology contains it 
		try {

			//construct a clazz to make the request
			OWLClass owlclazz = KAON2Manager.factory().owlClass(uri);
			//TODO this may be not efficitent as retrieving the exsiting concept
			if (onto.getDelegate().containsEntity(owlclazz, true)) {
				if (s_log.isDebugEnabled()) s_log.debug("concept found: " + uri);
				return (INamedConcept)Kaon2OMSModelConverter.convertEntity((OWLClass)owlclazz, onto);
			}
		}

		catch(KAON2Exception e){
			throw new DatasourceException(e);
		}

		if (s_log.isDebugEnabled()) s_log.debug("concept could not be found !");
		return null;
	}
}