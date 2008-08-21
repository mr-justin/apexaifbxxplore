/*
 * Created on 06.11.2003
 *
 */
package edu.unika.aifb.foam.agenda;

import java.util.*;

import org.semanticweb.kaon2.api.Ontology;
import org.semanticweb.kaon2.api.owl.elements.DataProperty;
import org.semanticweb.kaon2.api.owl.elements.Individual;
import org.semanticweb.kaon2.api.owl.elements.OWLClass;
import org.semanticweb.kaon2.api.owl.elements.OWLEntity;
import org.semanticweb.kaon2.api.owl.elements.ObjectProperty;

import edu.unika.aifb.foam.input.MyOntology;
import edu.unika.aifb.foam.input.Structure;
import edu.unika.aifb.foam.util.UserInterface;

/**
 * This agenda has its entries assigned randomly. From the given ontologies as much 
 * as n % of the possible pairs are selected. Alternatively a fixed number of pairs
 * can be selected.
 * 
 * @author Marc Ehrig
 */
public class RandomAgenda extends AgendaImpl {

	private double percent = 0;	
	private int maxperentitytype = 100000;
	private int counter = 0;
	
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
				case 1:  set1 = ontology.createEntityRequest(ObjectProperty.class).getAll();
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

	public void add(Set set1, Set set2) {
		Iterator iter1 = set1.iterator();
		while ((iter1.hasNext())&&(counter<maxperentitytype)) {
			Object object1 = iter1.next();
			Iterator iter2 = set2.iterator();
			while (iter2.hasNext()) {
				Object object2 = iter2.next();	
				String substring1 = "";
				String substring2 = "";
				try {
				OWLEntity entity1 = (OWLEntity) object1;			//removes comparisons within the same namespace
				OWLEntity entity2 = (OWLEntity) object2;
				String string1 = entity1.getURI().toString();
				int index1 = string1.indexOf('#');
				substring1 = string1.substring(0,index1);
				String string2 = entity2.getURI().toString();
				int index2 = string2.indexOf('#');
				substring2 = string2.substring(0,index2);				
				} catch (Exception e) {}
				if (internaltoo||((substring1.equals(substring2)==false)&&(substring1.hashCode()<substring2.hashCode()))) {								AgendaElement element = new AgendaElement();
					element.object1 = object1;
					element.object2 = object2;
					element.action = "comp";
					double random = Math.random();
					if ((random<percent)&&(list.contains(element)==false)) {
						counter++;
						list.add(element);
					}
				} 
			}
		}
	}
	
	public void parameter(Object object) {	
		if (object instanceof Integer) {
			Integer number = (Integer) object;
			maxperentitytype = number.intValue();
			percent = 1.0;
		} else {
			maxperentitytype = 10000;
			Double number = (Double) object;
			percent = number.doubleValue();
		}
	}
}
