/*
 * Created on 10.12.2004
 *
 */
package edu.unika.aifb.foam.rules.feature.special;

import java.util.Set;

import org.semanticweb.kaon2.api.owl.elements.OWLClass;

import edu.unika.aifb.foam.input.MyOntology;
import edu.unika.aifb.foam.input.Structure;

/**
 * @author Marc Ehrig
 *
 */
public class OWLClassSuperSpecial extends FeatureSpecialImpl {

	private static final long serialVersionUID = 1L;
	private OWLClass conceptS;
	
	public OWLClassSuperSpecial(Object conceptT) {
		conceptS = (OWLClass) conceptT;		
	}
	
	public Object get(Object object, Structure structure) {
		try {
			MyOntology ontology = (MyOntology) structure;		
			OWLClass concept = (OWLClass) object;
			Set descriptions = concept.getSuperDescriptions(ontology.ontology);
			return new Boolean(descriptions.contains(conceptS));
		} catch (Exception e) {
			return null;
		}
	}

}