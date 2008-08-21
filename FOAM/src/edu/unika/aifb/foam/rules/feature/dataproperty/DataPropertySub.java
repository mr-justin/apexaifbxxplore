/*
 * Created on 10.12.2004
 *
 */
package edu.unika.aifb.foam.rules.feature.dataproperty;

import java.util.Set;

import org.semanticweb.kaon2.api.owl.elements.DataProperty;

import edu.unika.aifb.foam.input.MyOntology;
import edu.unika.aifb.foam.input.Structure;
import edu.unika.aifb.foam.rules.feature.Feature;

/**
 * @author meh
 *
 */
public class DataPropertySub implements Feature{

	private static final long serialVersionUID = 1L;

	public Object get(Object object, Structure structure) {
		try {
			MyOntology ontology = (MyOntology) structure;			
			DataProperty property = (DataProperty) object;
			Set entities = property.getSubDataProperties(ontology.ontology);
//			if (entities.size()>0) {System.out.print("sub");}												
			return entities;
		} catch (Exception e) {
			return null;
		}
	}
	
}
