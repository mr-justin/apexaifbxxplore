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
public class IndividualSame implements Feature {

	private static final long serialVersionUID = 1L;

	public Object get(Object object, Structure structure) {
		try {
			MyOntology ontology = (MyOntology) structure;	
			Individual individual = (Individual) object;
			Set entities = individual.getSameIndividuals(ontology.ontology);
//			if (entities.size()>0) {System.out.print("same");}																								
			return entities;
		} catch (Exception e) {
			return null;
		}
	}

}