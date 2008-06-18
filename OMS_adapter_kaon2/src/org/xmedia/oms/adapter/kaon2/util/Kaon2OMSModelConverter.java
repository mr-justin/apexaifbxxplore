package org.xmedia.oms.adapter.kaon2.util;

import org.semanticweb.kaon2.api.logic.Constant;
import org.semanticweb.kaon2.api.owl.axioms.DataPropertyMember;
import org.semanticweb.kaon2.api.owl.axioms.OWLAxiom;
import org.semanticweb.kaon2.api.owl.axioms.ObjectPropertyMember;
import org.semanticweb.kaon2.api.owl.elements.DataProperty;
import org.semanticweb.kaon2.api.owl.elements.DataPropertyExpression;
import org.semanticweb.kaon2.api.owl.elements.DataRange;
import org.semanticweb.kaon2.api.owl.elements.Description;
import org.semanticweb.kaon2.api.owl.elements.Individual;
import org.semanticweb.kaon2.api.owl.elements.OWLClass;
import org.semanticweb.kaon2.api.owl.elements.OWLEntity;
import org.semanticweb.kaon2.api.owl.elements.ObjectProperty;
import org.semanticweb.kaon2.api.owl.elements.ObjectPropertyExpression;
import org.xmedia.oms.model.api.IAxiom;
import org.xmedia.oms.model.api.IConcept;
import org.xmedia.oms.model.api.IDatatype;
import org.xmedia.oms.model.api.IEntity;
import org.xmedia.oms.model.api.ILiteral;
import org.xmedia.oms.model.api.INamedConcept;
import org.xmedia.oms.model.api.INamedIndividual;
import org.xmedia.oms.model.api.IOntology;
import org.xmedia.oms.model.api.IProperty;
import org.xmedia.oms.model.api.IPropertyMember;
import org.xmedia.oms.model.api.IResource;
import org.xmedia.oms.model.impl.Concept;
import org.xmedia.oms.model.impl.Datatype;
import org.xmedia.oms.model.impl.Literal;
import org.xmedia.oms.model.impl.NamedConcept;
import org.xmedia.oms.model.impl.NamedIndividual;
import org.xmedia.oms.model.impl.PropertyMember;
import org.xmedia.oms.model.owl.api.ICompositeProperty;
import org.xmedia.oms.model.owl.impl.CompositeProperty;

public class Kaon2OMSModelConverter {

	public static IEntity convertEntity(OWLEntity entity, IOntology onto){
		if (entity instanceof OWLClass){
			INamedConcept concept = new NamedConcept(entity.getURI(), (IOntology)onto);
			concept.setDelegate(entity);
			return concept; 
		}

		else if (entity instanceof Individual){
			INamedIndividual ind = new NamedIndividual(entity.getURI(), (IOntology)onto);
			ind.setDelegate(entity);
			return ind; 
		}

		else if (entity instanceof ObjectProperty){
			IProperty prop = new org.xmedia.oms.model.impl.ObjectProperty(entity.getURI(), onto);
			prop.setDelegate(entity);
			return prop; 
		}

		else if (entity instanceof DataProperty){
			IProperty prop = new org.xmedia.oms.model.impl.DataProperty(entity.getURI(), onto);
			prop.setDelegate(entity);
			return prop; 
		}

		else return null; 
	}

	public static IAxiom convertAxiom(OWLAxiom entity, IOntology onto){

		if(entity instanceof ObjectPropertyMember){

			if(((ObjectPropertyMember)entity).getObjectProperty() instanceof ObjectProperty) {
				IPropertyMember iProp = new PropertyMember(
						new org.xmedia.oms.model.impl.ObjectProperty( ((ObjectProperty)((ObjectPropertyMember)entity).getObjectProperty()).getURI(), onto), 
						new NamedIndividual(((ObjectPropertyMember)entity).getSourceIndividual().getURI(), onto), 
						new NamedIndividual(((ObjectPropertyMember)entity).getTargetIndividual().getURI(), onto), 
						onto, PropertyMember.OBJECT_PROPERTY_MEMBER);
				iProp.setDelegate(entity);
				return iProp;
			}
			else
				//PropertyExpression not supported yet
				return null;	
		}

		else if(entity instanceof DataPropertyMember) {
			if(((DataPropertyMember)entity).getDataProperty() instanceof DataProperty) {
				IPropertyMember iProp = new PropertyMember(
						//TODO fixe expression
						new org.xmedia.oms.model.impl.DataProperty(((DataProperty)((DataPropertyMember)entity).getDataProperty()).getURI(), onto),
						new NamedIndividual(((DataPropertyMember)entity).getSourceIndividual().getURI(), onto), 
						new Literal(((DataPropertyMember)entity).getTargetValue().getValue()),
						onto, PropertyMember.DATA_PROPERTY_MEMBER);
				iProp.setDelegate(entity);
				return iProp;
			}
			else
				return null;	
			
		}

		else return null;

	}

	public static IResource convert(Object entity, IOntology onto){

		//its an OWLEntity that are not converted to an IEntity but an IResource
		if (entity instanceof org.semanticweb.kaon2.api.owl.elements.Datatype){
			IDatatype type = new Datatype(((org.semanticweb.kaon2.api.owl.elements.Datatype)entity).getURI(), onto);
			type.setDelegate(entity);
			return type; 
		}
		
		else if (entity instanceof OWLEntity) return convertEntity((OWLEntity) entity, onto);
		else if (entity instanceof OWLAxiom) return convertAxiom((OWLAxiom) entity, onto);

		else if (entity instanceof DataRange){
			IDatatype type = new Datatype((IOntology)onto);
			type.setDelegate(entity);
			return type; 
		}

		else if (entity instanceof Description){
			IConcept concept = new Concept((IOntology)onto);
			concept.setDelegate(entity);
			return concept; 
		}

		else if (entity instanceof Constant){
			ILiteral literal = new Literal(entity, onto);
			literal.setDelegate(entity);
			return literal; 
		}

		else if (entity instanceof ObjectPropertyExpression){
			if(entity instanceof ObjectProperty)
				return (IResource)convertEntity((ObjectProperty)entity, onto);
			else {
				ICompositeProperty prop = new CompositeProperty((IOntology)onto);
				prop.setDelegate(entity);
				return prop; 
			}
		}

		else if (entity instanceof DataPropertyExpression){
			if(entity instanceof DataProperty)
				return (IResource)convertEntity((DataProperty)entity, onto);
			else {
				ICompositeProperty prop = new CompositeProperty((IOntology)onto);
				prop.setDelegate(entity);
				return prop; 
			}		
		}

		else return null;


	}

}
