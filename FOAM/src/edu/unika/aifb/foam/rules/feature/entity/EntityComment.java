/*
 * Created on 04.01.2005
 *
 */
package edu.unika.aifb.foam.rules.feature.entity;

import org.semanticweb.kaon2.api.owl.elements.OWLEntity;

import edu.unika.aifb.foam.input.MyOntology;
import edu.unika.aifb.foam.input.Structure;
import edu.unika.aifb.foam.rules.feature.Feature;

/**
 * @author Marc Ehrig
 *
 */
public class EntityComment implements Feature {

	private static final long serialVersionUID = 1L;

	public Object get(Object object, Structure structure) {
		try {
			MyOntology ontology = (MyOntology) structure;			
			OWLEntity entity = (OWLEntity) object;
			String label = (String) entity.getEntityAnnotationValue(ontology.ontology,ontology.owlxcomment);
			return label;
		} catch (Exception e) {
			return null;
		}
	}

}