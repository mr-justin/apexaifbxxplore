/*
 * Created on 26.05.2004
 *
 */
package edu.unika.aifb.foam.agenda;

import java.util.*;

import org.semanticweb.kaon2.api.Ontology;
import org.semanticweb.kaon2.api.owl.elements.DataProperty;
import org.semanticweb.kaon2.api.owl.elements.Individual;
import org.semanticweb.kaon2.api.owl.elements.OWLClass;
import org.semanticweb.kaon2.api.owl.elements.ObjectProperty;

import edu.unika.aifb.foam.input.MyOntology;
import edu.unika.aifb.foam.input.Structure;
import edu.unika.aifb.foam.util.UserInterface;

/**
 * The simplest and largest agenda. All entities are compared with all 
 * other entities, resulting in n x m comparisons.
 * 
 * @author Marc Ehrig
 */
public class CompleteAgenda extends AgendaImpl {
	
	public void create(Structure structure, boolean internaltooT) {
		internaltoo = internaltooT;
		try{
		MyOntology myOntology = (MyOntology) structure;
		Ontology ontology = myOntology.ontology;
		list = new HashSet();
		for (int i = 0; i<3; i++) {
			Set set1 = new HashSet();
			Set set2 = new HashSet();
			switch (i) {
				case 0: set1 = ontology.createEntityRequest(OWLClass.class).getAll();
						set2.addAll(set1); 
						break;
				case 1: set1 = ontology.createEntityRequest(ObjectProperty.class).getAll();
						set1.addAll(ontology.createEntityRequest(DataProperty.class).getAll());
						set2.addAll(set1); 										
						break;
				case 2:  set1 = ontology.createEntityRequest(Individual.class).getAll(); 
						set2.addAll(set1); 										
						break;
			}
			add(set1,set2);
		}
		} catch (Exception e) {
			UserInterface.print(e.getMessage());
		}
	}

}
