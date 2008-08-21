/*
 * Created on 21.11.2003
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
import edu.unika.aifb.foam.rules.feature.entity.EntityLabel;
import edu.unika.aifb.foam.util.UserInterface;

/**
 * The system checks the labels of various entities. If the labels are similar enough,
 * the entities are included into the list. Similar enough is defined as having the first
 * three (or six) letters in common. 
 * 
 * @author Marc Ehrig
 */
public class ClosestLabelAgenda extends AgendaImpl {
	
	public void create(Structure structure, boolean internaltooT) {
		internaltoo = internaltooT;
		try{
		MyOntology myOntology = (MyOntology) structure;
		Ontology ontology = myOntology.ontology;
		EntityLabel entLabel = new EntityLabel();
		list = new HashSet();
		SortedMap entityList = new TreeMap();
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
			entityList.clear();
			Iterator iter1 = set1.iterator();
			while (iter1.hasNext()) {
				OWLEntity entity1 = (OWLEntity) iter1.next();
				String label1 = (String) entLabel.get(entity1,myOntology);
				if (label1 != null) {
					entityList.put(label1+entity1.getURI(),entity1);
				}
				String uri = entity1.getURI();
				int pos = uri.indexOf("#");
				if (pos!=-1) {
					String sub = uri.substring(pos+1,uri.length());
					entityList.put(sub+entity1.getURI(),entity1);
				} 				
			}		
			Iterator iter2 = set2.iterator();
			while (iter2.hasNext()) {
				OWLEntity entity2 = (OWLEntity) iter2.next();
				Set set4 = new HashSet();
				set4.add(entity2);
				String label = (String) entLabel.get(entity2,myOntology);
				if ((label != null)&&(label.length()>3)) {
					String labelpre = label.substring(0,3);
					char character = labelpre.charAt(2);
					character++;
					String labelpost = label.substring(0,2)+character;
					SortedMap range = entityList.subMap(labelpre,labelpost);
					Collection objectsOfRange = range.values();
					Set set3 = new HashSet();
					set3.addAll(objectsOfRange);
					add(set3,set4);
				}
				String uri = entity2.getURI();
				int pos = uri.indexOf("#");
				if (pos!=-1) {
					String sub = uri.substring(pos+1,uri.length());
					if (sub.length()>3) {
					String labelpre = sub.substring(0,3);
					char character = labelpre.charAt(2);
					character++;
					String labelpost = sub.substring(0,2)+character;
					SortedMap range = entityList.subMap(labelpre,labelpost);
					Collection objectsOfRange = range.values();
					Set set3 = new HashSet();
					set3.addAll(objectsOfRange);
					add(set3,set4);
					}
				} 				
			}
		}
		} catch (Exception e) {
			UserInterface.print(e.getMessage());
		}
	}

}
