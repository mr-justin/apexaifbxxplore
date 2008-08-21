/*
 * Created on 10.12.2004
 *
 */
package edu.unika.aifb.foam.rules.feature.entity;

import org.semanticweb.kaon2.api.StringWithLanguage;
import org.semanticweb.kaon2.api.owl.elements.OWLEntity;

import edu.unika.aifb.foam.input.MyOntology;
import edu.unika.aifb.foam.input.Structure;
import edu.unika.aifb.foam.rules.feature.Feature;

/**
 * @author Marc Ehrig
 *
 */
public class EntityLabel implements Feature {

	private static final long serialVersionUID = 1L;

	public Object get(Object object, Structure structure) {
		MyOntology ontology = (MyOntology) structure;			
		OWLEntity entity = (OWLEntity) object;
		try {
		String label = (String) entity.getEntityAnnotationValue(ontology.ontology,ontology.owlxlabel);
		return label;
		} catch (Exception e) {
		try{
		StringWithLanguage label2 = (StringWithLanguage) entity.getEntityAnnotationValue(ontology.ontology,ontology.owlxlabel);
		return label2.getString();
		} catch (Exception e2) {
		return null;
		}
		}
	}

}