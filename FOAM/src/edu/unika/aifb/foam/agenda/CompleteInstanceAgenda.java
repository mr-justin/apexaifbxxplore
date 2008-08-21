/*
 * Created on 16.07.2005
 *
 */
package edu.unika.aifb.foam.agenda;

import java.util.*;

import org.semanticweb.kaon2.api.Ontology;
import org.semanticweb.kaon2.api.owl.elements.Individual;

import edu.unika.aifb.foam.input.MyOntology;
import edu.unika.aifb.foam.input.Structure;
import edu.unika.aifb.foam.util.UserInterface;

/**
 * All instances are compared with all 
 * other instances, resulting in n x m comparisons.
 * 
 * @author Marc Ehrig
 */
public class CompleteInstanceAgenda extends AgendaImpl {
	
	public void create(Structure structure, boolean internaltooT) {
		internaltoo = internaltooT;
		try{
		MyOntology myOntology = (MyOntology) structure;
		Ontology ontology = myOntology.ontology;
		list = new HashSet();
		Set set1 = ontology.createEntityRequest(Individual.class).getAll();
		Set set2 = new HashSet();
		set2.addAll(set1); 										
		add(set1,set2);
		} catch (Exception e) {
			UserInterface.print(e.getMessage());
		}
	}

}
