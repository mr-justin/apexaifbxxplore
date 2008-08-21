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
public class ObjectPropertyRangeSpecial extends FeatureSpecialImpl {

	private static final long serialVersionUID = 1L;
	private OWLClass concept;
	
	public ObjectPropertyRangeSpecial(Object conceptT) {
		concept = (OWLClass) conceptT;		
	}
	
	public Object get(Object object, Structure structure) {	
		try {
			MyOntology ontology = (MyOntology) structure;	
			ObjectProperty property = (ObjectProperty) object; 
			Set descriptions = property.getRangeDescriptions(ontology.ontology);
			return new Boolean(descriptions.contains(concept));
		} catch (Exception e) {
			return null;
		}
	}

}