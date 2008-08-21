/*
 * Created on 10.12.2004
 *
 */
package edu.unika.aifb.foam.rules.feature.special;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.semanticweb.kaon2.api.owl.axioms.ObjectPropertyMember;
import org.semanticweb.kaon2.api.owl.elements.Individual;
import org.semanticweb.kaon2.api.owl.elements.ObjectProperty;

import edu.unika.aifb.foam.input.MyOntology;
import edu.unika.aifb.foam.input.Structure;

/**
 * @author Marc Ehrig
 *
 */
public class IndividualObjectPropertyMembersToSpecial extends FeatureSpecialImpl {

	private static final long serialVersionUID = 1L;
	private ObjectProperty property;
	
	public IndividualObjectPropertyMembersToSpecial(Object propertyT) {
		property = (ObjectProperty) propertyT;		
	}
	
	public Object get(Object object, Structure structure) {
		try {
			MyOntology ontology = (MyOntology) structure;		
			Individual individual = (Individual) object;
			Set members = individual.getObjectPropertyMembersTo(ontology.ontology);
			Iterator iter = members.iterator();
			Set set = new HashSet();
			while (iter.hasNext()) {
				ObjectPropertyMember member = (ObjectPropertyMember) iter.next();
				if (member.getObjectProperty().equals(property)) {
					set.add(member.getSourceIndividual());
				}
			}
			return set;
		} catch (Exception e) {
			return null;
		}
	}

}