/*
 * Created on 10.12.2004
 *
 */
package edu.unika.aifb.foam.rules.feature.individual;

import java.util.Set;

import org.semanticweb.kaon2.api.owl.elements.Individual;

import edu.unika.aifb.foam.input.MyOntology;
import edu.unika.aifb.foam.input.Structure;
import edu.unika.aifb.foam.rules.feature.Feature;

/**
 * @author Marc Ehrig
 *
 */
public class IndividualMemberOf implements Feature {

	private static final long serialVersionUID = 1L;

	public Object get(Object object, Structure structure) {
		try {
			MyOntology ontology = (MyOntology) structure;	
			Individual individual = (Individual) object;
			Set descriptions = individual.getDescriptionsMemberOf(ontology.ontology);
			return descriptions;
		} catch (Exception e) {
			return null;
		}
	}

}