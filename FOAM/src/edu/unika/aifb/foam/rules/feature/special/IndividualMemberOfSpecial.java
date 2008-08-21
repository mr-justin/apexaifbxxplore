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
public class IndividualMemberOfSpecial extends FeatureSpecialImpl {

	private static final long serialVersionUID = 1L;
	private OWLClass concept;
	
	public IndividualMemberOfSpecial(Object conceptT) {
		concept = (OWLClass) conceptT;		
	}
	
	public Object get(Object object, Structure structure) {
		try {
			MyOntology ontology = (MyOntology) structure;		
			Individual individual = (Individual) object;
			Set descriptions = individual.getDescriptionsMemberOf(ontology.ontology);
			return new Boolean(descriptions.contains(concept));
		} catch (Exception e) {
			return null;
		}
	}

}