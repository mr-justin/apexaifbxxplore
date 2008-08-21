/*
 * Created on 10.12.2004
 *
 */
package edu.unika.aifb.foam.rules.feature.owlclass;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.semanticweb.kaon2.api.owl.elements.OWLClass;
import org.semanticweb.kaon2.api.owl.elements.ObjectNot;
import org.semanticweb.kaon2.api.owl.elements.Description;

import edu.unika.aifb.foam.input.MyOntology;
import edu.unika.aifb.foam.input.Structure;
import edu.unika.aifb.foam.rules.feature.Feature;

/**
 * @author Marc Ehrig
 *
 */
public class OWLClassObjectNot implements Feature {

	private static final long serialVersionUID = 1L;

	public Object get(Object object, Structure structure) {
		try {
			MyOntology ontology = (MyOntology) structure;	
			OWLClass concept = (OWLClass) object;
			Set descriptions = concept.getEquivalentDescriptions(ontology.ontology);
			Set specialDescriptions = new HashSet();
			Iterator iter = descriptions.iterator();
			while (iter.hasNext()) {
				Description next = (Description) iter.next();
				if (next instanceof ObjectNot) {
					specialDescriptions.add(next);
				}
			}
//			if (specialDescriptions.size()>0) {System.out.print("not");}									
			return specialDescriptions;
		} catch (Exception e) {
			return null;
		}
	}

}