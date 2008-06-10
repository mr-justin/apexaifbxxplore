/**
 * 
 */
package org.xmedia.oms.adapter.kaon2.persistence;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.aifb.xxplore.shared.exception.Emergency;
import org.aifb.xxplore.shared.util.Pair;
import org.apache.log4j.Logger;
import org.semanticweb.kaon2.api.Axiom;
import org.semanticweb.kaon2.api.KAON2Exception;
import org.semanticweb.kaon2.api.KAON2Manager;
import org.semanticweb.kaon2.api.OntologyChangeEvent;
import org.semanticweb.kaon2.api.Request;
import org.semanticweb.kaon2.api.owl.axioms.ClassMember;
import org.semanticweb.kaon2.api.owl.axioms.DataPropertyAttribute;
import org.semanticweb.kaon2.api.owl.axioms.DataPropertyDomain;
import org.semanticweb.kaon2.api.owl.axioms.DataPropertyMember;
import org.semanticweb.kaon2.api.owl.axioms.DataPropertyRange;
import org.semanticweb.kaon2.api.owl.axioms.ObjectPropertyAttribute;
import org.semanticweb.kaon2.api.owl.axioms.ObjectPropertyDomain;
import org.semanticweb.kaon2.api.owl.axioms.ObjectPropertyMember;
import org.semanticweb.kaon2.api.owl.axioms.ObjectPropertyRange;
import org.semanticweb.kaon2.api.owl.axioms.SubClassOf;
import org.semanticweb.kaon2.api.owl.axioms.SubDataPropertyOf;
import org.semanticweb.kaon2.api.owl.axioms.SubObjectPropertyOf;
import org.semanticweb.kaon2.api.owl.elements.DataAll;
import org.semanticweb.kaon2.api.owl.elements.DataCardinality;
import org.semanticweb.kaon2.api.owl.elements.DataHasValue;
import org.semanticweb.kaon2.api.owl.elements.DataProperty;
import org.semanticweb.kaon2.api.owl.elements.DataPropertyExpression;
import org.semanticweb.kaon2.api.owl.elements.DataSome;
import org.semanticweb.kaon2.api.owl.elements.Description;
import org.semanticweb.kaon2.api.owl.elements.OWLClass;
import org.semanticweb.kaon2.api.owl.elements.OWLEntity;
import org.semanticweb.kaon2.api.owl.elements.ObjectAll;
import org.semanticweb.kaon2.api.owl.elements.ObjectAnd;
import org.semanticweb.kaon2.api.owl.elements.ObjectCardinality;
import org.semanticweb.kaon2.api.owl.elements.ObjectHasValue;
import org.semanticweb.kaon2.api.owl.elements.ObjectOr;
import org.semanticweb.kaon2.api.owl.elements.ObjectProperty;
import org.semanticweb.kaon2.api.owl.elements.ObjectSome;
import org.xmedia.businessobject.IBusinessObject;
import org.xmedia.oms.adapter.kaon2.util.Kaon2OMSModelConverter;
import org.xmedia.oms.model.api.IConcept;
import org.xmedia.oms.model.api.IDatatype;
import org.xmedia.oms.model.api.IEntity;
import org.xmedia.oms.model.api.INamedConcept;
import org.xmedia.oms.model.api.INamedIndividual;
import org.xmedia.oms.model.api.IProperty;
import org.xmedia.oms.model.api.IResource;
import org.xmedia.oms.model.impl.NamedIndividual;
import org.xmedia.oms.model.impl.Property;
import org.xmedia.oms.persistence.DatasourceException;
import org.xmedia.oms.persistence.SessionFactory;
import org.xmedia.oms.persistence.StatelessSession;
import org.xmedia.oms.persistence.dao.IPropertyDao;


class Kaon2PropertyDao extends AbstractKaon2Dao implements IPropertyDao{

	private static Logger s_log = Logger.getLogger(Kaon2PropertyDao.class);

	public Class getBoClass() throws DatasourceException{

		return Property.class;
	}

	public void delete(IBusinessObject existingBo) throws DatasourceException {
		Emergency.checkPrecondition(existingBo instanceof Property, "existingBo instanceof Property");
		Property property = (Property)existingBo;

		StatelessSession session = (StatelessSession)SessionFactory.getInstance().getCurrentSession();
		Kaon2Transaction trans = (Kaon2Transaction)session.getTransaction();

		Set<Axiom> toDelete = getRelatedAxioms(property);

		for (Axiom axiom : toDelete){
			OntologyChangeEvent event = new OntologyChangeEvent(
					axiom, 
					OntologyChangeEvent.ChangeType.REMOVE);
			trans.addChanges(event);
		}
	}

	private Set<Axiom> getRelatedAxioms(Property property) {
		StatelessSession session = (StatelessSession)SessionFactory.getInstance().getCurrentSession();
		Kaon2Ontology onto = (Kaon2Ontology) session.getOntology();

		Set<Axiom> axioms = new HashSet<Axiom>();
		
		if (property instanceof DataProperty) {
			org.semanticweb.kaon2.api.owl.elements.DataProperty owlProp = 
				(org.semanticweb.kaon2.api.owl.elements.DataProperty)property.getDelegate();
			
			try {
				axioms.addAll(onto.getDelegate().createAxiomRequest(DataPropertyAttribute.class)
						.setCondition("dataProperty", owlProp).getAll());
				
				axioms.addAll(onto.getDelegate().createAxiomRequest(DataPropertyDomain.class)
						.setCondition("dataProperty", owlProp).getAll());
				
				axioms.addAll(onto.getDelegate().createAxiomRequest(DataPropertyMember.class)
						.setCondition("dataProperty", owlProp).getAll());
				
				axioms.addAll(onto.getDelegate().createAxiomRequest(DataPropertyRange.class)
						.setCondition("dataProperty", owlProp).getAll());
				
				axioms.addAll(onto.getDelegate().createAxiomRequest(SubDataPropertyOf.class)
						.setCondition("subDataProperty", owlProp).getAll());
				
				axioms.addAll(onto.getDelegate().createAxiomRequest(SubDataPropertyOf.class)
						.setCondition("superDataProperty", owlProp).getAll());
			} catch (KAON2Exception e) {
				// TODO transaction error
				e.printStackTrace();
			}
		}
		else if (property instanceof ObjectProperty) {
			org.semanticweb.kaon2.api.owl.elements.ObjectProperty owlProp = 
				(org.semanticweb.kaon2.api.owl.elements.ObjectProperty)property.getDelegate();
			
			try {
				axioms.addAll(onto.getDelegate().createAxiomRequest(ObjectPropertyAttribute.class)
						.setCondition("objectProperty", owlProp).getAll());
				
				axioms.addAll(onto.getDelegate().createAxiomRequest(ObjectPropertyDomain.class)
						.setCondition("objectProperty", owlProp).getAll());
				
				axioms.addAll(onto.getDelegate().createAxiomRequest(ObjectPropertyMember.class)
						.setCondition("objectProperty", owlProp).getAll());
				
				axioms.addAll(onto.getDelegate().createAxiomRequest(ObjectPropertyRange.class)
						.setCondition("objectProperty", owlProp).getAll());
				
				axioms.addAll(onto.getDelegate().createAxiomRequest(SubObjectPropertyOf.class)
						.setCondition("subObjectProperty", owlProp).getAll());
				
				axioms.addAll(onto.getDelegate().createAxiomRequest(SubObjectPropertyOf.class)
						.setCondition("superObjectProperty", owlProp).getAll());
			} catch (KAON2Exception e) {
				// TODO transaction error
				e.printStackTrace();
			}
		}
		
		return axioms;
	}

	public void insert(IBusinessObject newBo) throws DatasourceException {
		throw new UnsupportedOperationException("Insert/update unsupported for entities.");
	}

	public void update(IBusinessObject existingBo) throws DatasourceException {
		throw new UnsupportedOperationException("Insert/update unsupported for entities.");
	}

	public List<IProperty> findAll() throws DatasourceException{
		StatelessSession session = (StatelessSession)SessionFactory.getInstance().getCurrentSession();
		Kaon2Ontology onto = (Kaon2Ontology) session.getOntology();

		Set<OWLEntity> props = new HashSet<OWLEntity>();
		List<IProperty> propList = new ArrayList<IProperty>();

		if (s_log.isDebugEnabled()) s_log.debug("find all properties of the ontology: " + session.getOntology().getUri());
		//get all properties
		try {

			//get all object properties			
			Request<ObjectProperty> oPropRequest = onto.getDelegate().createEntityRequest(ObjectProperty.class);	

			Set<ObjectProperty> result1 = oPropRequest.getAll();
			if (result1 != null) props.addAll(result1);	

			//get all data properties			
			Request<DataProperty> dPropRequest = onto.getDelegate().createEntityRequest(DataProperty.class);	
			Set<DataProperty> result2 = dPropRequest.getAll();
			if(result2 != null) props.addAll(result2);


		} catch (KAON2Exception e) {

			throw new DatasourceException(e);
		}

		//no such entities
		if(props == null || props.size() == 0) return null;

		for (OWLEntity prop : props){
			propList.add((IProperty)Kaon2OMSModelConverter.convertEntity(prop, onto));
			if (s_log.isDebugEnabled()) s_log.debug("property found: " + prop.getURI());

		}

		return propList;
	}

	public List<IProperty> findAllObjectProperty() throws DatasourceException{
		StatelessSession session = (StatelessSession)SessionFactory.getInstance().getCurrentSession();
		Kaon2Ontology onto = (Kaon2Ontology) session.getOntology();

		Set<OWLEntity> props = new HashSet<OWLEntity>();
		List<IProperty> propList = new ArrayList<IProperty>();

		if (s_log.isDebugEnabled()) s_log.debug("find all properties of the ontology: " + session.getOntology().getUri());
		//get all properties
		try {

			//get all object properties			
			Request<ObjectProperty> oPropRequest = onto.getDelegate().createEntityRequest(ObjectProperty.class);	

			Set<ObjectProperty> result1 = oPropRequest.getAll();
			if (result1 != null) props.addAll(result1);	

		} catch (KAON2Exception e) {

			throw new DatasourceException(e);
		}

		//no such entities
		if(props == null || props.size() == 0) return null;

		for (OWLEntity prop : props){
			propList.add((IProperty)Kaon2OMSModelConverter.convertEntity(prop, onto));
			if (s_log.isDebugEnabled()) s_log.debug("property found: " + prop.getURI());

		}

		return propList;
	}
	
	public List<IProperty> findAllDataProperty() throws DatasourceException{
		StatelessSession session = (StatelessSession)SessionFactory.getInstance().getCurrentSession();
		Kaon2Ontology onto = (Kaon2Ontology) session.getOntology();

		Set<OWLEntity> props = new HashSet<OWLEntity>();
		List<IProperty> propList = new ArrayList<IProperty>();

		if (s_log.isDebugEnabled()) s_log.debug("find all properties of the ontology: " + session.getOntology().getUri());
		//get all properties
		try {

			//get all data properties			
			Request<DataProperty> dPropRequest = onto.getDelegate().createEntityRequest(DataProperty.class);	
			Set<DataProperty> result2 = dPropRequest.getAll();
			if(result2 != null) props.addAll(result2);

		} catch (KAON2Exception e) {

			throw new DatasourceException(e);
		}

		//no such entities
		if(props == null || props.size() == 0) return null;

		for (OWLEntity prop : props){
			propList.add((IProperty)Kaon2OMSModelConverter.convertEntity(prop, onto));
			if (s_log.isDebugEnabled()) s_log.debug("property found: " + prop.getURI());

		}

		return propList;
	}
	
	public IBusinessObject findById(String id) throws DatasourceException{
		//TODO implement
		StatelessSession session = (StatelessSession) SessionFactory
				.getInstance().getCurrentSession();
		Kaon2Ontology onto = (Kaon2Ontology) session.getOntology();

		// construct an property with the id and ask if the ontology contains it
		try {
			org.semanticweb.kaon2.api.owl.elements.DataProperty dProp = KAON2Manager.factory().dataProperty(id);
			org.semanticweb.kaon2.api.owl.elements.ObjectProperty oProp = KAON2Manager.factory().objectProperty(id);
			if (onto.getDelegate().containsEntity(dProp, true)) {
				return Kaon2OMSModelConverter.convertEntity(
						dProp, onto);
			}
			else 
				if (onto.getDelegate().containsEntity(oProp, true)) {
					return Kaon2OMSModelConverter.convertEntity(
							oProp, onto);
				}
		}
		catch (KAON2Exception e) {
			throw new DatasourceException(e);
		}

		if (s_log.isDebugEnabled())
			s_log.debug("concept could not be found !");
		return null;	
	}

	public Set<IProperty> findProperties(INamedConcept concept) throws DatasourceException {
		Set<IProperty> props = new HashSet<IProperty>();

		Set<IProperty> result1 = findPropertiesFrom(concept);
		if(result1 != null) props.addAll(result1);

		Set<IProperty> result2 = findPropertiesTo(concept);
		if(result2 != null) props.addAll(result2);
		return props;
	}

	public Set<Pair> findPropertiesAndRangesFrom(INamedConcept concept) throws DatasourceException {

		StatelessSession session = (StatelessSession)SessionFactory.getInstance().getCurrentSession();
		Set<Pair> propSet = new HashSet<Pair>();

		if (session.isReasoningOn() == false){
			Kaon2Ontology onto = (Kaon2Ontology) session.getOntology();
			if (s_log.isDebugEnabled()) s_log.debug("find all properties and concepts from concept: " + concept.getLabel());

			Set<Pair> result1 = findPropertiesAndRangesViaDomain(onto, concept);
			if(result1 != null) propSet.addAll(result1);

			Set<Pair> result2 = findPropertiesAndRangesViaDescriptions(onto, concept);
			if(result2 != null) propSet.addAll(result2);

		}

		return propSet;
	}

	private Set<Pair> findPropertiesAndRangesViaDomain(Kaon2Ontology onto, INamedConcept concept){

		Set<Pair> propset = new HashSet<Pair>();
		if (s_log.isDebugEnabled()) s_log.debug("find all properties and concepts from concept via domain axiom: " + concept.getLabel());
		try {

			// get properties specified via domain axiom
			Set props = new HashSet();

			concept = (INamedConcept)checkForDelegate(concept); 
			if(concept == null) return null;


			Set<ObjectPropertyDomain> result1  = onto.getDelegate().createAxiomRequest(ObjectPropertyDomain.class).setCondition("domain", concept.getDelegate()).getAll();
			Set<DataPropertyDomain> result2 = onto.getDelegate().createAxiomRequest(DataPropertyDomain.class).setCondition("domain", concept.getDelegate()).getAll();

			// also add data property to list 
			if (result1 != null) props.addAll(result1);
			if (result2 != null) props.addAll(result2);

			//no such axioms 
			if(props == null || props.size() == 0) return null;

			for (Object prop : props){

				IProperty p = null;
				IResource range = null;

				if(prop instanceof ObjectPropertyDomain){ 				
					p = (IProperty)Kaon2OMSModelConverter.convert(((ObjectPropertyDomain)prop).getObjectProperty(), onto);
				}
				else if(prop instanceof DataPropertyDomain) {
					p = (IProperty)Kaon2OMSModelConverter.convert(((DataPropertyDomain)prop).getDataProperty(), onto);
				}

				if (p != null ) {
					Set<ObjectPropertyRange> objectranges = null; 
					Set<DataPropertyRange> dataranges = null; 
					if(p instanceof ObjectProperty){
						objectranges = onto.getDelegate().createAxiomRequest(ObjectPropertyRange.class).
						setCondition("objectProperty", p.getDelegate()).getAll();
					}
					//dataproperty
					else{
						dataranges = onto.getDelegate().createAxiomRequest(DataPropertyRange.class).
						setCondition("dataProperty", p.getDelegate()).getAll();
					}
					Set ranges = null; 
					if(objectranges != null) ranges = objectranges;
					else ranges = dataranges;

					if(ranges != null) {
						for (Object o : ranges) {
							if(o instanceof ObjectPropertyRange){
								range = (IConcept)Kaon2OMSModelConverter.convert(((ObjectPropertyRange)o).getRange(), onto);
							}
							else{
								range = (IDatatype)Kaon2OMSModelConverter.convert(((DataPropertyRange)o).getRange(), onto);
							}
							propset.add(new Pair(p,range));
						}
					}
				}
			}
		}
		catch (KAON2Exception e) {
			throw new DatasourceException(e);
		}			

		return propset;
	}

	private Set<Pair> findPropertiesAndRangesViaDescriptions(Kaon2Ontology onto, INamedConcept concept){

		Set<Pair> propset = new HashSet<Pair>();

		//	get properties via other axioms 
		Set<SubClassOf> cons = null;
		try {
			concept = (INamedConcept)checkForDelegate(concept); 
			if(concept == null) return null;
			cons = onto.getDelegate().createAxiomRequest(SubClassOf.class).setCondition("subDescription", concept.getDelegate()).getAll();
		} 
		catch (KAON2Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (cons == null) return propset;

		while (cons.size() > 0){

			Set<SubClassOf> temp = new HashSet<SubClassOf>();	
			for (SubClassOf con : cons){
				Description parent; 
				parent = con.getSuperDescription();

				IProperty prop; 
				IResource range; 

				List<DataPropertyExpression> props;

				if(parent instanceof ObjectSome){
					prop = (IProperty)Kaon2OMSModelConverter.convert(((ObjectSome)parent).getObjectProperty(), onto);
					range = (IConcept)Kaon2OMSModelConverter.convert(((ObjectSome)parent).getDescription(), onto);
					propset.add(new Pair(prop,range));
				}

				else if (parent instanceof ObjectAll){
					prop = (IProperty)Kaon2OMSModelConverter.convert(((ObjectAll)parent).getObjectProperty(), onto);
					range = (IConcept)Kaon2OMSModelConverter.convert(((ObjectAll)parent).getDescription(), onto);
					propset.add(new Pair(prop,range));
				}

				else if (parent instanceof ObjectCardinality){
					prop = (IProperty)Kaon2OMSModelConverter.convert(((ObjectCardinality)parent).getObjectProperty(), onto);
					range = (IConcept)Kaon2OMSModelConverter.convert(((ObjectCardinality)parent).getDescription(), onto);
					propset.add(new Pair(prop,range));
				}

				else if(parent instanceof DataSome){
					props = ((DataSome)parent).getDataProperties();
					if(props != null && props.size() > 0){
						for (DataPropertyExpression p : props){
							prop = ((IProperty)Kaon2OMSModelConverter.convert(p, onto));						
							range = (IDatatype)Kaon2OMSModelConverter.convert(((DataSome)parent).getDataRange(), onto);
							propset.add(new Pair(prop,range));
						}
					}
				}

				else if (parent instanceof DataAll){

					props = ((DataAll)parent).getDataProperties();
					if(props != null && props.size() > 0){
						for (DataPropertyExpression p : props){
							prop = ((IProperty)Kaon2OMSModelConverter.convert(p, onto));						
							range = (IDatatype)Kaon2OMSModelConverter.convert(((DataAll)parent).getDataRange(), onto);
							propset.add(new Pair(prop,range));
						}
					}

				}

				else if (parent instanceof ObjectAnd || parent instanceof ObjectOr){
					Set<Description> descs;

					if(parent instanceof ObjectAnd) descs = ((ObjectAnd)parent).getDescriptions();
					else descs = ((ObjectOr)parent).getDescriptions();

					if(descs != null && descs.size() > 0){
						for (Description desc : descs){
							if(desc instanceof OWLClass){
								range = (IConcept)Kaon2OMSModelConverter.convert(desc, onto);				
								propset.addAll(findPropertiesAndRangesFrom((INamedConcept)range));
							}
						}
					}
				}

				else if (parent instanceof OWLClass){
					range = (IConcept)Kaon2OMSModelConverter.convertEntity((OWLClass) parent, onto);
					propset.addAll(findPropertiesAndRangesFrom((INamedConcept)range));
				}


				//add parents of parent 
				try {
					temp.addAll(onto.getDelegate().createAxiomRequest(SubClassOf.class).
							setCondition("subDescription", parent).getAll());
				} catch (KAON2Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			cons = temp; 

		}

		return propset; 
	}

	public Set<IProperty> findPropertiesFrom(INamedConcept concept) throws DatasourceException {

		StatelessSession session = (StatelessSession)SessionFactory.getInstance().getCurrentSession();
		Set<IProperty> propSet = new HashSet<IProperty>();

		if (session.isReasoningOn() == false){
			Kaon2Ontology onto = (Kaon2Ontology) session.getOntology();
			if (s_log.isDebugEnabled()) s_log.debug("find all properties from concept: " + concept.getLabel());

			Set<IProperty> result1 = findPropertiesFromViaDomain(onto, concept);
			if(result1 != null) propSet.addAll(result1);

			Set<IProperty> result2 = findPropertiesFromViaDescriptions(onto, concept);
			if(result2 != null) propSet.addAll(result2);

		}

		return propSet;
	}

	private Set<IProperty> findPropertiesFromViaDomain(Kaon2Ontology onto, INamedConcept concept){

		Set<IProperty> propset = new HashSet<IProperty>();

		if (s_log.isDebugEnabled()) s_log.debug("find all properties from concept via domain axiom: " + concept.getLabel());
		try {

			concept = (INamedConcept)checkForDelegate(concept); 
			if(concept == null) return null;

			// get properties specified via domain axiom
			Set props = new HashSet();
			Set<ObjectPropertyDomain> result1 = onto.getDelegate().createAxiomRequest(ObjectPropertyDomain.class).
			setCondition("domain", concept.getDelegate()).getAll();

			Set<DataPropertyDomain> result2 = onto.getDelegate().createAxiomRequest(DataPropertyDomain.class).
			setCondition("domain", concept.getDelegate()).getAll();

			// also add data property to list 
			if (result1 != null) props.addAll(result1);
			if (result2 != null) props.addAll(result2);

			//no such axioms 
			if(props == null || props.size() == 0) return null;

			for (Object prop : props){

				IProperty iProp = null;
				if(prop instanceof ObjectPropertyDomain) 				
					iProp = (IProperty)Kaon2OMSModelConverter.convert(((ObjectPropertyDomain)prop).getObjectProperty(), onto);

				else if(prop instanceof DataPropertyDomain) 
					iProp = (IProperty)Kaon2OMSModelConverter.convert(((DataPropertyDomain)prop).getDataProperty(), onto);

				if (iProp != null) {
					propset.add(iProp);
					if (s_log.isDebugEnabled()) s_log.debug("property found: " + iProp.getUri());
				}
			}
		}
		catch (KAON2Exception e) {
			throw new DatasourceException(e);
		}			

		return propset;
	}

	private Set<IProperty> findPropertiesFromViaDescriptions(Kaon2Ontology onto, INamedConcept concept){

		Set<IProperty> propset = new HashSet<IProperty>();

		//	get properties via other axioms 
		Set<SubClassOf> cons = null;
		try {
			concept = (INamedConcept)checkForDelegate(concept); 
			if(concept == null) return null;

			cons = onto.getDelegate().createAxiomRequest(SubClassOf.class).
			setCondition("subDescription", concept.getDelegate()).getAll();
		} 

		catch (KAON2Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (cons == null) return propset;

		while (cons.size() > 0){

			Set<SubClassOf> temp = new HashSet<SubClassOf>();	
			for (SubClassOf con : cons){
				Description parent; 
				parent = con.getSuperDescription();

				IProperty prop; 
				List<DataPropertyExpression> props;

				if(parent instanceof ObjectHasValue){
					prop = (IProperty)Kaon2OMSModelConverter.convert(((ObjectHasValue)parent).getObjectProperty(), onto);
					propset.add(prop);
				}

				else if(parent instanceof ObjectSome){
					prop = (IProperty)Kaon2OMSModelConverter.convert(((ObjectSome)parent).getObjectProperty(), onto);
					propset.add(prop);
				}

				else if (parent instanceof ObjectAll){
					prop = (IProperty)Kaon2OMSModelConverter.convert(((ObjectAll)parent).getObjectProperty(), onto);
					propset.add(prop);
				}

				else if (parent instanceof ObjectCardinality){
					prop = (IProperty)Kaon2OMSModelConverter.convert(((ObjectCardinality)parent).getObjectProperty(), onto);
					propset.add(prop);
				}

				else if(parent instanceof DataHasValue){
					prop = (IProperty)Kaon2OMSModelConverter.convert(((DataHasValue)parent).getDataProperty(), onto);
					propset.add(prop);
				}

				else if(parent instanceof DataSome){
					props = ((DataSome)parent).getDataProperties();
					if(props != null && props.size() > 0){
						for (DataPropertyExpression p : props){
							propset.add((IProperty)Kaon2OMSModelConverter.convert(p, onto));							
						}
					}
				}

				else if (parent instanceof DataAll){

					props = ((DataAll)parent).getDataProperties();
					if(props != null && props.size() > 0){
						for (DataPropertyExpression p : props)
							propset.add((IProperty)Kaon2OMSModelConverter.convert(p, onto));							
					}

				}

				else if (parent instanceof DataCardinality){
					prop = (IProperty)Kaon2OMSModelConverter.convert(((DataCardinality)parent).getDataProperty(), onto);
					propset.add(prop);
				}


				else if (parent instanceof ObjectAnd || parent instanceof ObjectOr){
					Set<Description> descs;

					if(parent instanceof ObjectAnd) descs = ((ObjectAnd)parent).getDescriptions();
					else descs = ((ObjectOr)parent).getDescriptions();

					if(descs != null && descs.size() > 0){
						for (Description desc : descs){
							if(desc instanceof OWLClass){
								INamedConcept c = (INamedConcept)Kaon2OMSModelConverter.convert(desc, onto);											
								propset.addAll(findPropertiesFrom(c));
							}
						}
					}
				}

				else if (parent instanceof OWLClass){
					INamedConcept c = (INamedConcept)Kaon2OMSModelConverter.convertEntity((OWLClass)parent, onto);				
					propset.addAll(findPropertiesFrom(c));
				}


				//add parents of parent 
				try {
					temp.addAll(onto.getDelegate().createAxiomRequest(SubClassOf.class).
							setCondition("subDescription", parent).getAll());
				} catch (KAON2Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			cons = temp; 

		}

		return propset; 
	}

	public Set<IProperty> findPropertiesTo(INamedConcept concept) throws DatasourceException {
		StatelessSession session = (StatelessSession)SessionFactory.getInstance().getCurrentSession();
		Kaon2Ontology onto = (Kaon2Ontology) session.getOntology();
		Set<IProperty> propSet = null;

		Set<ObjectPropertyRange> props = new HashSet<ObjectPropertyRange>();
		if (s_log.isDebugEnabled()) s_log.debug("find all properties to concept: " + concept.getLabel());
		try {
			concept = (INamedConcept)checkForDelegate(concept); 
			if(concept == null) return null;

			props = onto.getDelegate().createAxiomRequest(ObjectPropertyRange.class).
			setCondition("range", concept.getDelegate()).getAll();

			//no such axioms 
			if(props == null || props.size() == 0) return null;

			propSet = new HashSet<IProperty>();
			for (ObjectPropertyRange prop  : props){
				propSet.add((IProperty)Kaon2OMSModelConverter.convert(prop.getObjectProperty(), onto));
				if (s_log.isDebugEnabled()) s_log.debug("property found: " + prop.getObjectProperty().toString());
			}
		} 

		catch (KAON2Exception e) {
			throw new DatasourceException(e);
		}

		return propSet;
	}

	public Set<IProperty> findProperties(INamedIndividual individual) throws DatasourceException {
		Emergency.checkPrecondition(individual instanceof INamedIndividual, "individual instanceof INamedIndividual");
		Set<IProperty> props = new HashSet<IProperty>();

		Set<IProperty>result1 = findPropertiesFrom(individual);
		if (s_log.isDebugEnabled()) s_log.debug("find all properties of individual: " + individual.getLabel());
		if (result1 != null) props.addAll(result1);

		Set<IProperty> result2 = findPropertiesTo(individual);
		if(result2 != null) props.addAll(result2);
		return props;
	}

	public Set<IProperty> findPropertiesFrom(INamedIndividual individual) throws DatasourceException {
		Emergency.checkPrecondition(individual instanceof INamedIndividual, "individual instanceof INamedIndividual");

		StatelessSession session = (StatelessSession)SessionFactory.getInstance().getCurrentSession();
		Kaon2Ontology onto = (Kaon2Ontology) session.getOntology();
		Set<IProperty> propSet = null;

		Set props = new HashSet();
		if (s_log.isDebugEnabled()) s_log.debug("find all properties from individual: " + individual.getLabel());
		try {

			individual = (INamedIndividual)checkForDelegate(individual); 
			if(individual == null) return null;

			Set<ObjectPropertyMember> result1 = onto.getDelegate().createAxiomRequest(ObjectPropertyMember.class).
			setCondition("sourceIndividual", individual.getDelegate()).getAll();

			Set<DataPropertyMember> result2 = onto.getDelegate().createAxiomRequest(DataPropertyMember.class).
			setCondition("sourceIndividual", individual.getDelegate()).getAll();

			// also add data property to list 
			if (result1 != null) props.addAll(result1);
			if (result2 != null) props.addAll(result2);

			//no such axioms 
			if(props == null || props.size() == 0) return null;

			propSet = new HashSet<IProperty>();
			for (Object prop : props){

				IProperty iProp = null;
				if(prop instanceof ObjectPropertyMember) 
					iProp = (IProperty)Kaon2OMSModelConverter.convert(((ObjectPropertyMember)prop).getObjectProperty(), onto);

				else if(prop instanceof DataPropertyMember) 
					iProp = (IProperty)Kaon2OMSModelConverter.convert(((DataPropertyMember)prop).getDataProperty(), onto);

				if (iProp != null) {
					propSet.add(iProp);
					if (s_log.isDebugEnabled()) s_log.debug("property found: " + iProp.getUri());
				}

			}
		} 

		catch (KAON2Exception e) {
			throw new DatasourceException(e);
		}

		return propSet;
	}

	public Set<IProperty> findPropertiesTo(INamedIndividual individual) throws DatasourceException {
		Emergency.checkPrecondition(individual instanceof INamedIndividual, "individual instanceof INamedIndividual");

		StatelessSession session = (StatelessSession)SessionFactory.getInstance().getCurrentSession();
		Kaon2Ontology onto = (Kaon2Ontology) session.getOntology();
		Set<IProperty> propSet = null;

		Set<ObjectPropertyMember> props = new HashSet<ObjectPropertyMember>();
		if (s_log.isDebugEnabled()) s_log.debug("find all properties to individual: " + individual.getLabel());
		try {

			individual = (INamedIndividual)checkForDelegate(individual); 
			if(individual == null) return null;

			props = onto.getDelegate().createAxiomRequest(ObjectPropertyMember.class).
			setCondition("targetIndividual", individual.getDelegate()).getAll();

			//no such axioms 
			if(props == null || props.size() == 0) return null;

			propSet = new HashSet<IProperty>();
			for (ObjectPropertyMember prop  : props){

				propSet.add((IProperty)Kaon2OMSModelConverter.convert(((ObjectPropertyMember)prop).getObjectProperty(), onto));
				if (s_log.isDebugEnabled()) s_log.debug("property found: " + prop.getObjectProperty().toString());

			}
		} 

		catch (KAON2Exception e) {
			throw new DatasourceException(e);
		}

		return propSet;
	}

	public IProperty findByUri(String uri) throws DatasourceException {
		StatelessSession session = (StatelessSession)SessionFactory.getInstance().getCurrentSession();
		Kaon2Ontology onto = (Kaon2Ontology) session.getOntology();

		IProperty iprop = null;
		if (s_log.isDebugEnabled()) s_log.debug("find individual by uri: " + uri);
		try {

			//construct a property to make the request
			ObjectProperty oProp = KAON2Manager.factory().objectProperty(uri);
			//TODO this may be not efficitent as retrieving the exsiting concept
			if (onto.getDelegate().containsEntity(oProp, true)) 				

				return (IProperty)Kaon2OMSModelConverter.convertEntity(oProp, onto);

			else {
				DataProperty dProp = KAON2Manager.factory().dataProperty(uri);
				//TODO this may be not efficitent as retrieving the exsiting concept
				if(onto.getDelegate().containsEntity(dProp, true)) 
					return (IProperty)Kaon2OMSModelConverter.convertEntity(dProp, onto);

			}
		}

		catch(KAON2Exception e){
			throw new DatasourceException(e);
		}

		return null;
	}

}