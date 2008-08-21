/*
 * Created on 10.12.2004
 *
 */
package edu.unika.aifb.foam.rules.feature.special;

import java.util.Set;

import org.semanticweb.kaon2.api.owl.elements.OWLClass;
import org.semanticweb.kaon2.api.owl.elements.ObjectProperty;

import edu.unika.aifb.foam.input.MyOntology;
import edu.unika.aifb.foam.input.Structure;

/**
 * @author Marc Ehrig
 *
 */
public class OWLClassObjectPropertiesToSpecial extends FeatureSpecialImpl {

	private static final long serialVersionUID = 1L;
	private ObjectProperty property;
	
	public OWLClassObjectPropertiesToSpecial(Object propertyT) {
		property = (ObjectProperty) propertyT;		
	}
	
	public Object get(Object object, Structure structure) {
		try {
			MyOntology ontology = (MyOntology) structure;		
			OWLClass concept = (OWLClass) object;
			Set properties = concept.getObjectPropertiesTo(ontology.ontology);
			return new Boolean(properties.contains(property));
		} catch (Exception e) {
			return null;
		}
	}

}