package edu.unika.aifb.foam.rules.feature.owlclass;

import java.util.Set;

import org.semanticweb.kaon2.api.owl.elements.OWLClass;

import edu.unika.aifb.foam.input.MyOntology;
import edu.unika.aifb.foam.input.Structure;
import edu.unika.aifb.foam.rules.feature.Feature;

public class OWLClassDataPropertiesFrom implements Feature {

	private static final long serialVersionUID = 1L;

	public Object get(Object object, Structure structure) {
		try {
			MyOntology ontology = (MyOntology) structure;	
			OWLClass concept = (OWLClass) object;
			Set properties = concept.getDataPropertiesFrom(ontology.ontology);
			return properties;
		} catch (Exception e) {
			return null;
		}
	}

}