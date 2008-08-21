/*
 * Created on 10.12.2004
 *
 */
package edu.unika.aifb.foam.rules.feature.individual;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.semanticweb.kaon2.api.owl.elements.Description;
import org.semanticweb.kaon2.api.owl.elements.Individual;
import org.semanticweb.kaon2.api.owl.elements.OWLClass;

import edu.unika.aifb.foam.input.MyOntology;
import edu.unika.aifb.foam.input.Structure;
import edu.unika.aifb.foam.rules.feature.Feature;

/**
 * @author Marc Ehrig
 *
 */
public class IndividualMemberOfInfer implements Feature {

	private static final long serialVersionUID = 1L;

	public Object get(Object object, Structure structure) {
		try {
			MyOntology ontology = (MyOntology) structure;	
			Individual individual = (Individual) object;
			Set descriptions = individual.getDescriptionsMemberOf(ontology.ontology);
			Set entities = new HashSet();
			Iterator iter = descriptions.iterator();
			while (iter.hasNext()) {
				Description next = (Description) iter.next();
				if (next instanceof OWLClass) {
					entities.add(next);
					Set descriptions2 = next.getSuperDescriptions(ontology.ontology);
					Iterator iter2 = descriptions2.iterator();
					while (iter2.hasNext()) {
						Description next2 = (Description) iter2.next();
						if (next2 instanceof OWLClass) {
							entities.add(next2);
							Set descriptions3 = next.getSuperDescriptions(ontology.ontology);
							Iterator iter3 = descriptions3.iterator();
							while (iter3.hasNext()) {
								Description next3 = (Description) iter3.next();
								if (next3 instanceof OWLClass) {
									entities.add(next3);
								}
							}
						}
					}
				}
			}
			return entities;
		} catch (Exception e) {
			return null;
		}
	}

}