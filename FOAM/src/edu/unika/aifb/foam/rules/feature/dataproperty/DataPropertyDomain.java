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
public class DataPropertyDomain implements Feature {

	private static final long serialVersionUID = 1L;

	public Object get(Object object, Structure structure) {
		try {
			MyOntology ontology = (MyOntology) structure;
			DataProperty property = (DataProperty) object;
//			if (property.getURI().equalsIgnoreCase("http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#yearValue")) {
//				System.out.println("!!");
//			}					
			Set domain = property.getDomainDescriptions(ontology.ontology);
//			if (domain.size()>0) {System.out.print("dom");}									
			return domain;
		} catch (Exception e) {
			return null;
		}
	}
	
}
