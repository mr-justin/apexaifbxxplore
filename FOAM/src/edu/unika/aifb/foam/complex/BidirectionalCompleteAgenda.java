package edu.unika.aifb.foam.complex;

import java.util.HashSet;
import java.util.Set;
//import java.util.SortedMap;
//import java.util.TreeMap;

import org.semanticweb.kaon2.api.Ontology;
import org.semanticweb.kaon2.api.owl.elements.DataProperty;
import org.semanticweb.kaon2.api.owl.elements.Individual;
import org.semanticweb.kaon2.api.owl.elements.OWLClass;
import org.semanticweb.kaon2.api.owl.elements.ObjectProperty;

import edu.unika.aifb.foam.input.MyOntology;
import edu.unika.aifb.foam.input.Structure;
import edu.unika.aifb.foam.util.UserInterface;

/**
 * All entities are compared with all other entities bidirectionally, resulting
 * in (n + m)*(n + m) comparisons.
 * 
 * @author Pengyun Ren
 */
public class BidirectionalCompleteAgenda extends BidirectionalAgendaImpl {

//	private int position = 0;
//	private int total = 0;

	public void create(Structure structure, boolean internaltooT) {
		internaltoo = internaltooT;
		try {
			MyOntology myOntology = (MyOntology) structure;
			Ontology ontology = myOntology.ontology;
			list = new HashSet();
//			SortedMap entityList = new TreeMap();

			for (int i = 0; i < 3; i++) {
				Set set1 = new HashSet();
				Set set2 = new HashSet();
				switch (i) {
				case 0:
					set1 = ontology.createEntityRequest(OWLClass.class)
						.getAll();
					set2.addAll(set1);
					break;
				case 1:
					set1 = ontology.createEntityRequest(ObjectProperty.class)
						.getAll();
					set1.addAll(ontology.createEntityRequest(DataProperty.class)
						.getAll());
					set2.addAll(set1);
					break;
				case 2:
					set1 = ontology.createEntityRequest(Individual.class)
						.getAll();
					set2.addAll(set1);
					break;
				}

				add(set1, set2);
			}
		}
		catch (Exception e) {
			UserInterface.print(e.getMessage());
		}
	}

}
