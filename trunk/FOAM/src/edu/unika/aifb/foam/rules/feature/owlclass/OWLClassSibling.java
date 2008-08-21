/*
 * Created on 23.12.2004
 *
 */
package edu.unika.aifb.foam.rules.feature.owlclass;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import edu.unika.aifb.foam.input.Structure;
import edu.unika.aifb.foam.rules.feature.Feature;

/**
 * @author Marc Ehrig
 *
 */
public class OWLClassSibling implements Feature {

	private static final long serialVersionUID = 1L;
	private Feature classSuper = new OWLClassSuper();
	private Feature classSub = new OWLClassSub();
	
	public Object get(Object object, Structure structure) {
		try {
			Set superClasses = (Set) classSuper.get(object,structure);
			Iterator iter = superClasses.iterator();
			Set siblings = new HashSet();
			while (iter.hasNext()) {
				Object next = iter.next();
				siblings.addAll((Collection) classSub.get(next,structure));
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