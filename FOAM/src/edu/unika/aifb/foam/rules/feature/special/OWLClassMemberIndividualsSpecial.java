/*
 * Created on 10.12.2004
 *
 */
package edu.unika.aifb.foam.rules.feature.special;

import java.util.Set;

import org.semanticweb.kaon2.api.owl.elements.Individual;
import org.semanticweb.kaon2.api.owl.elements.OWLClass;

import edu.unika.aifb.foam.input.MyOntology;
import edu.unika.aifb.foam.input.Structure;

/**
 * @author Marc Ehrig
 *
 */
public class OWLClassMemberIndividualsSpecial extends FeatureSpecialImpl {

	private static final long serialVersionUID = 1L;
	private Individual individual;
	
	public OWLClassMemberIndividualsSpecial(Object individualT) {
		individual = (Individual) individualT;		
	}
	
	public Object get(Object object, Structure structure) {
		try {
			MyOntology ontology = (MyOntology) structure;	
			OWLClass concept = (OWLClass) object;
			Set individuals = concept.getMemberIndividuals(ontology.ontology);
			return new Boolean(individuals.contains(individual));
		} catch (Exception e) {
			return null;
		}
	}

}