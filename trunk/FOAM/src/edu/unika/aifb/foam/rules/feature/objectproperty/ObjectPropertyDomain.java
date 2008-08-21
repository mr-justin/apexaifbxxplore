/*
 * Created on 10.12.2004
 *
 */
package edu.unika.aifb.foam.rules.feature.objectproperty;

import java.util.Set;

import org.semanticweb.kaon2.api.owl.elements.ObjectProperty;

import edu.unika.aifb.foam.input.MyOntology;
import edu.unika.aifb.foam.input.Structure;
import edu.unika.aifb.foam.rules.feature.Feature;

/**
 * @author meh
 *
 */
public class ObjectPropertyDomain implements Feature{

	private static final long serialVersionUID = 1L;

	public Object get(Object object, Structure structure) {
		try {
			MyOntology ontology = (MyOntology) structure;	
			ObjectProperty property = (ObjectProperty) object;
			Set descriptions = property.getDomainDescriptions(ontology.ontology);
			return descriptions;
		} catch (Exception e) {
			return null;
		}
	}
	
}
