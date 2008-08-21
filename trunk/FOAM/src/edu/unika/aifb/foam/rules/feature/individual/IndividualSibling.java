/*
 * Created on 23.12.2004
 *
 */
package edu.unika.aifb.foam.rules.feature.individual;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.semanticweb.kaon2.api.owl.elements.OWLClass;

import edu.unika.aifb.foam.input.Structure;
import edu.unika.aifb.foam.rules.feature.Feature;
import edu.unika.aifb.foam.rules.feature.owlclass.OWLClassMemberIndividuals;

/**
 * @author Marc Ehrig
 *
 */
public class IndividualSibling implements Feature {

	private static final long serialVersionUID = 1L;
	private Feature indiMem = new IndividualMemberOf();
	private Feature classMemIndi = new OWLClassMemberIndividuals();
	
	public Object get(Object object, Structure structure) {
		try {
			Set parentClasses = (Set) indiMem.get(object,structure);
			Iterator iter = parentClasses.iterator();
			Set siblings = new HashSet();
			while (iter.hasNext()) {
				OWLClass next = (OWLClass) iter.next();
				siblings.addAll((Collection) classMemIndi.get(next,structure));
			}
			try{
			siblings.remove(object);
			} catch (Exception e) {}
			return siblings;
		} catch (Exception e) {
			return null;
		}
	}

}