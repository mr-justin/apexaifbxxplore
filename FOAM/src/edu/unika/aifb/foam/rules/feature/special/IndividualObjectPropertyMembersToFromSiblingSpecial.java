/*
 * Created on 16.07.2005
 *
 */
package edu.unika.aifb.foam.rules.feature.special;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.semanticweb.kaon2.api.owl.axioms.ObjectPropertyMember;
import org.semanticweb.kaon2.api.owl.elements.Individual;
import org.semanticweb.kaon2.api.owl.elements.ObjectProperty;

import edu.unika.aifb.foam.input.MyOntology;
import edu.unika.aifb.foam.input.Structure;
import edu.unika.aifb.foam.rules.feature.Feature;

/**
 * @author Marc Ehrig
 *
 */
public class IndividualObjectPropertyMembersToFromSiblingSpecial extends FeatureSpecialImpl {

	private static final long serialVersionUID = 1L;
	private ObjectProperty property;
	private Feature siblingFrom;
	
	public IndividualObjectPropertyMembersToFromSiblingSpecial(Object propertyT) {
		property = (ObjectProperty) propertyT;
		siblingFrom = new IndividualObjectPropertyMembersFromSpecial(propertyT);
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
			iter = set.iterator();
			Set siblings = new HashSet();
			while (iter.hasNext()) {
				Object next = iter.next();
				siblings.addAll((Collection) siblingFrom.get(next,structure));
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