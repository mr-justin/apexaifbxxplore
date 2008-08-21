/*
 * Created on 10.12.2004
 *
 */
package edu.unika.aifb.foam.rules.feature.owlclass;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.semanticweb.kaon2.api.owl.elements.DataHasValue;
import org.semanticweb.kaon2.api.owl.elements.Description;
import org.semanticweb.kaon2.api.owl.elements.OWLClass;

import edu.unika.aifb.foam.input.MyOntology;
import edu.unika.aifb.foam.input.Structure;
import edu.unika.aifb.foam.rules.feature.Feature;

/**
 * @author Marc Ehrig
 *
 */
public class OWLClassDataHasValue implements Feature {

	private static final long serialVersionUID = 1L;

	public Object get(Object object, Structure structure) {
		try {
			MyOntology ontology = (MyOntology) structure;	
			OWLClass concept = (OWLClass) object;
			Set descriptions = concept.getSuperDescriptions(ontology.ontology);
			Set specialDescriptions = new HashSet();
			Iterator iter = descriptions.iterator();
			while (iter.hasNext()) {
				Description next = (Description) iter.next();
				if (next instanceof DataHasValue) {
					specialDescriptions.add(next);
				}
			}
//			if (specialDescriptions.size()>0) {System.out.print("val");}
			return specialDescriptions;
		} catch (Exception e) {
			return null;
		}
	}

}