/*
 * Created on 26.11.2003
 *
 */
package edu.unika.aifb.foam.agenda;

import java.util.*;

import org.semanticweb.kaon2.api.owl.elements.DataProperty;
import org.semanticweb.kaon2.api.owl.elements.Description;
import org.semanticweb.kaon2.api.owl.elements.Individual;
import org.semanticweb.kaon2.api.owl.elements.OWLClass;
import org.semanticweb.kaon2.api.owl.elements.ObjectProperty;

import edu.unika.aifb.foam.input.MyOntology;
import edu.unika.aifb.foam.input.Structure;
import edu.unika.aifb.foam.result.ResultList;
import edu.unika.aifb.foam.util.UserInterface;

/**
 * If an adjacent node in the ontology graph has changed during
 * the last alignment iteration, the entity is scheduled for comparison
 * in the next run. This is a highly efficient agenda. 
 * 
 * @author Marc Ehrig
 */

public class ChangePropagationAgenda extends AgendaImpl {

	private ResultList oldList;
	private ResultList newList;	
//	private int MAXAGENDAELEMENTS = 300000;
	
	public void parameter(Object object) {
		ResultList[] lists = (ResultList[]) object;
		oldList = lists[0];
		newList = lists[1];
	}	
	
	public void create(Structure structure, boolean internaltooT) {
		MyOntology ontology = (MyOntology) structure;		
		internaltoo = internaltooT;
		list = new HashSet();
		Vector ent = new Vector();
		ent.addAll(newList.objectList());
		Iterator iter = ent.iterator();
		while (iter.hasNext()) {
			Object object1 = iter.next();
			for (int i = 0; i<3; i++) {
				Object object2 = newList.getObject(object1,i);
				Object object3 = oldList.getObject(object1,i);					
			if ((object2!=null)&&(object3!=null)&&(object2!=object3)) {  						//a change has occurred
			HashSet set1 = new HashSet();
			HashSet set2 = new HashSet();					
			if (object1 instanceof OWLClass) {
				try{
				OWLClass concept1 = (OWLClass) object1;
				OWLClass concept2 = (OWLClass) object2;						
				set1.addAll(concept1.getSuperDescriptions(ontology.ontology));
				set2.addAll(concept2.getSuperDescriptions(ontology.ontology));
				add(onlyEntities(set1),onlyEntities(set2));
				set1.clear();
				set2.clear();
				set1.addAll(concept1.getSubDescriptions(ontology.ontology));
				set2.addAll(concept2.getSubDescriptions(ontology.ontology));
				add(onlyEntities(set1),onlyEntities(set2));
				set1.clear();
				set2.clear();
				set1.addAll((Set) concept1.getDataPropertiesFrom(ontology.ontology));
				set2.addAll((Set) concept2.getDataPropertiesFrom(ontology.ontology));
/*				add(set1,set2);				
				set1.clear();
				set2.clear();*/
				set1.addAll((Set) concept1.getObjectPropertiesFrom(ontology.ontology));
				set2.addAll((Set) concept2.getObjectPropertiesFrom(ontology.ontology));
				add(set1,set2);				
				set1.clear();
				set2.clear();
				set1.addAll((Set) concept1.getObjectPropertiesTo(ontology.ontology));
				set2.addAll((Set) concept2.getObjectPropertiesTo(ontology.ontology));
				if ((set1.size()>100)&&(set2.size()>100)) {								//KAON2 bug?
//					System.out.println(object1.toString()+" "+object2.toString());
					set1.clear();
					set2.clear();
				}
				add(set1,set2);				
				set1.clear();
				set2.clear();
				set1.addAll((Set) concept1.getMemberIndividuals(ontology.ontology));
				set2.addAll((Set) concept2.getMemberIndividuals(ontology.ontology));
				add(set1,set2);
				} catch (Exception e) {
					UserInterface.print(e.getMessage());
				}
			}
			if ((object1 instanceof DataProperty)&&(object2 instanceof DataProperty)) {
				try{
				DataProperty property1 = (DataProperty) object1;
				DataProperty property2 = (DataProperty) object2;
				set1.addAll(property1.getDomainDescriptions(ontology.ontology));
				set2.addAll(property2.getDomainDescriptions(ontology.ontology));
				add(onlyEntities(set1),onlyEntities(set2));
				} catch (Exception e) {
					UserInterface.print(e.getMessage());
				}
			}
			if ((object1 instanceof DataProperty)&&(object2 instanceof ObjectProperty)) {
				try{
				DataProperty property1 = (DataProperty) object1;
				ObjectProperty property2 = (ObjectProperty) object2;
				set1.addAll(property1.getDomainDescriptions(ontology.ontology));
				set2.addAll(property2.getDomainDescriptions(ontology.ontology));
				add(onlyEntities(set1),onlyEntities(set2));
				} catch (Exception e) {
					UserInterface.print(e.getMessage());
				}
			}
			if ((object1 instanceof ObjectProperty)&&(object2 instanceof DataProperty)) {
				try{
				ObjectProperty property1 = (ObjectProperty) object1;
				DataProperty property2 = (DataProperty) object2;
				set1.addAll(property1.getDomainDescriptions(ontology.ontology));
				set2.addAll(property2.getDomainDescriptions(ontology.ontology));
				add(onlyEntities(set1),onlyEntities(set2));
				} catch (Exception e) {
					UserInterface.print(e.getMessage());
				}
			}
			if ((object1 instanceof ObjectProperty)&&(object2 instanceof ObjectProperty)) {
				try{
				ObjectProperty property1 = (ObjectProperty) object1;
				ObjectProperty property2 = (ObjectProperty) object2;
				set1.addAll(property1.getDomainDescriptions(ontology.ontology));
				set2.addAll(property2.getDomainDescriptions(ontology.ontology));
				add(onlyEntities(set1),onlyEntities(set2));
				set1.clear();
				set2.clear();
				set1.addAll(property1.getRangeDescriptions(ontology.ontology));
				set2.addAll(property2.getRangeDescriptions(ontology.ontology));
				add(onlyEntities(set1),onlyEntities(set2));
				set1.clear();
				set2.clear();
				} catch (Exception e) {
					UserInterface.print(e.getMessage());
				}
			}			
			if (object1 instanceof Individual) {
				try{
				Individual instance1 = (Individual) object1;
				Individual instance2 = (Individual) object2;
				set1.addAll(instance1.getDescriptionsMemberOf(ontology.ontology));
				set2.addAll(instance2.getDescriptionsMemberOf(ontology.ontology));
				add(onlyEntities(set1),onlyEntities(set2));
				set1.clear();
				set2.clear();
				} catch (Exception e) {
					UserInterface.print(e.getMessage());
				}
			}
			}
		}
	}
	}

	private Set onlyEntities(Set descriptions) {
		Set entities = new HashSet();
		Iterator iter = descriptions.iterator();
		while (iter.hasNext()) {
			Description next = (Description) iter.next();
			if ((next instanceof OWLClass)||(next instanceof DataProperty)||(next instanceof ObjectProperty)||(next instanceof Individual)) {
				entities.add(next);
			}
		}
		return entities;
	}
	
}
