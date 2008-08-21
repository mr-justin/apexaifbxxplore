/*
 * Created on 10.12.2004
 *
 */
package edu.unika.aifb.foam.rules.feature.objectproperty;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.semanticweb.kaon2.api.owl.axioms.ObjectPropertyMember;
import org.semanticweb.kaon2.api.owl.elements.ObjectProperty;

import edu.unika.aifb.foam.input.MyOntology;
import edu.unika.aifb.foam.input.Structure;
import edu.unika.aifb.foam.rules.feature.Feature;
import edu.unika.aifb.foam.rules.heuristic.tuple.Tuple;

/**
 * @author meh
 *
 */
public class ObjectPropertyMembers implements Feature{

	private static final long serialVersionUID = 1L;

	public Object get(Object object, Structure structure) {
		try {
			MyOntology ontology = (MyOntology) structure;					
			ObjectProperty property = (ObjectProperty) object;
			Set members = property.getObjectPropertyMembers(ontology.ontology);
			Iterator iter = members.iterator();
			Set tupleSet = new HashSet();
			while (iter.hasNext()) {
				ObjectPropertyMember member = (ObjectPropertyMember) iter.next();
				Tuple tuple = new Tuple(member.getSourceIndividual(),member.getTargetIndividual(), null);
				tupleSet.add(tuple);
			}
			return tupleSet;			
		} catch (Exception e) {
			return null;
		}
	}
	
}
