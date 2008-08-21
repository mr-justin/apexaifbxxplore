/*
 * Created on 10.12.2004
 *
 */
package edu.unika.aifb.foam.rules.feature.dataproperty;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.semanticweb.kaon2.api.owl.axioms.DataPropertyMember;
import org.semanticweb.kaon2.api.owl.elements.DataProperty;

import edu.unika.aifb.foam.input.MyOntology;
import edu.unika.aifb.foam.input.Structure;
import edu.unika.aifb.foam.rules.feature.Feature;
import edu.unika.aifb.foam.rules.heuristic.tuple.Tuple;

/**
 * @author meh
 *
 */
public class DataPropertyMembers implements Feature {

	private static final long serialVersionUID = 1L;

	public Object get(Object object, Structure structure) {
		try {
			MyOntology ontology = (MyOntology) structure;			
			DataProperty property = (DataProperty) object;
			Set members = property.getDataPropertyMembers(ontology.ontology);
			Set tupleSet = new HashSet();
			Iterator iter = members.iterator();
			while (iter.hasNext()) {
				DataPropertyMember data = (DataPropertyMember) iter.next();
				Tuple tuple = new Tuple(data.getSourceIndividual(),data.getTargetValue(), null);
				tupleSet.add(tuple);
			}
			return tupleSet;
		} catch (Exception e) {
			return null;
		}
	}
	
}
