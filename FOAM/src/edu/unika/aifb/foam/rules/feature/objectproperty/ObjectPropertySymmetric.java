/*
 * Created on 10.12.2004
 *
 */
package edu.unika.aifb.foam.rules.feature.objectproperty;

import org.semanticweb.kaon2.api.owl.elements.ObjectProperty;

import edu.unika.aifb.foam.input.MyOntology;
import edu.unika.aifb.foam.input.Structure;
import edu.unika.aifb.foam.rules.feature.Feature;

/**
 * @author meh
 *
 */
public class ObjectPropertySymmetric implements Feature{

	private static final long serialVersionUID = 1L;

	public Object get(Object object, Structure structure) {
		try {
			MyOntology ontology = (MyOntology) structure;	
			ObjectProperty property = (ObjectProperty) object;
			Boolean boolean1 = new Boolean(property.isSymmetric(ontology.ontology));
			return boolean1;
		} catch (Exception e) {
			return null;
		}
	}
	
}
