/*
 * Created on 10.12.2004
 *
 */
package edu.unika.aifb.foam.rules.feature.special;

import java.util.Set;

import org.semanticweb.kaon2.api.owl.elements.DataProperty;
import org.semanticweb.kaon2.api.owl.elements.OWLClass;

import edu.unika.aifb.foam.input.MyOntology;
import edu.unika.aifb.foam.input.Structure;

/**
 * @author Marc Ehrig
 *
 */
public class OWLClassDataPropertiesFromSpecial extends FeatureSpecialImpl{

	private static final long serialVersionUID = 1L;
	private DataProperty property;
	
	public OWLClassDataPropertiesFromSpecial(Object propertyT) {
		property = (DataProperty) propertyT;		
	}
	
	public Object get(Object object, Structure structure) {
		try {
			MyOntology ontology = (MyOntology) structure;	
			OWLClass concept = (OWLClass) object;
			Set properties = concept.getDataPropertiesFrom(ontology.ontology);
			return new Boolean(properties.contains(property));
		} catch (Exception e) {
			return null;
		}
	}

}