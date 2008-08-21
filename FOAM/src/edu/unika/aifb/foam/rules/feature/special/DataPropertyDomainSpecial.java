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
public class DataPropertyDomainSpecial extends FeatureSpecialImpl {

	private static final long serialVersionUID = 1L;
	private OWLClass concept;
	
	public DataPropertyDomainSpecial(Object conceptT) {
		concept = (OWLClass) conceptT;		
		set(concept, "C");
	}
	
	public Object get(Object object, Structure structure) {
		try {
			MyOntology ontology = (MyOntology) structure;		
			DataProperty property = (DataProperty) object; 
			Set descriptions = property.getDomainDescriptions(ontology.ontology);
			return new Boolean(descriptions.contains(concept));
		} catch (Exception e) {
			return null;
		}
	}

}