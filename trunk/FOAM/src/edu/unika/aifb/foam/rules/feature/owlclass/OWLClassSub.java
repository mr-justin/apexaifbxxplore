/*
 * Created on 10.12.2004
 *
 */
package edu.unika.aifb.foam.rules.feature.owlclass;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.semanticweb.kaon2.api.owl.elements.Description;
import org.semanticweb.kaon2.api.owl.elements.OWLClass;

import edu.unika.aifb.foam.input.MyOntology;
import edu.unika.aifb.foam.input.Structure;
import edu.unika.aifb.foam.rules.feature.Feature;

/**
 * @author Marc Ehrig
 *
 */
public class OWLClassSub implements Feature {

	private static final long serialVersionUID = 1L;

	public Object get(Object object, Structure structure) {
		try {
			MyOntology ontology = (MyOntology) structure;	
			OWLClass concept = (OWLClass) object;
			Set descriptions = concept.getSubDescriptions(ontology.ontology);
			Set entities = new HashSet();
			Iterator iter = descriptions.iterator();
			while (iter.hasNext()) {
				Description next = (Description) iter.next();
				if (next instanceof OWLClass) {
					entities.add(next);
				}
			}
			return entities;
		} catch (Exception e) {
			return null;
		}
	}

}
