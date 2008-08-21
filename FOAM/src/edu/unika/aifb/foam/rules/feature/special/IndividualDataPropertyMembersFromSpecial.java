/*
 * Created on 10.12.2004
 *
 */
package edu.unika.aifb.foam.rules.feature.special;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.semanticweb.kaon2.api.owl.axioms.DataPropertyMember;
import org.semanticweb.kaon2.api.owl.elements.DataProperty;
import org.semanticweb.kaon2.api.owl.elements.Individual;

import edu.unika.aifb.foam.input.MyOntology;
import edu.unika.aifb.foam.input.Structure;

/**
 * @author Marc Ehrig
 *
 */
public class IndividualDataPropertyMembersFromSpecial extends FeatureSpecialImpl {

	private static final long serialVersionUID = 1L;
	private DataProperty property;
	
	public IndividualDataPropertyMembersFromSpecial(Object propertyT) {
		property = (DataProperty) propertyT;		
	}
	
	public Object get(Object object, Structure structure) {
		try {
			MyOntology ontology = (MyOntology) structure;		
			Individual individual = (Individual) object;
			Set members = individual.getDataPropertyMembersFrom(ontology.ontology);
			Iterator iter = members.iterator();
			Set set = new HashSet();
			while (iter.hasNext()) {
				DataPropertyMember member = (DataPropertyMember) iter.next();
				if (member.getDataProperty().equals(property)) {
					set.add(member.getTargetValue());
				}
			}
			return set;
		} catch (Exception e) {
			return null;
		}
	}

}