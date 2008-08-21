/*
 * Created on 07.10.2004
 *
 */
package edu.unika.aifb.foam.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.Vector;

import org.semanticweb.kaon2.api.owl.elements.DataProperty;
import org.semanticweb.kaon2.api.owl.elements.Individual;
import org.semanticweb.kaon2.api.owl.elements.OWLClass;
import org.semanticweb.kaon2.api.owl.elements.OWLEntity;
import org.semanticweb.kaon2.api.owl.elements.ObjectProperty;

import edu.unika.aifb.foam.input.ExplicitRelation;
import edu.unika.aifb.foam.input.MyOntology;
import edu.unika.aifb.foam.input.Structure;
import edu.unika.aifb.foam.result.ResultList;

/**
 * Adding the user in the loop of ontology alignment - during runtime.
 * This corresponds to an active learning approach. The most doubtful
 * alignments are presented to the user for validation.
 * This work is described our work for I-KNOW 05.
 * 
 * @author Marc Ehrig
 */
public class Semi {

	private double maxError = 0.45;
	private int inputnumber = 1;
	public double proposedCutoff = maxError;
	public int numberOfQuestions = 0;
	public int numberOfPositiveAnswers = 0;	
	
	/**
	 * Initializes the semi-automation. 
	 * @param error level of highest error
	 * @param inputnumberT number of questions presented to the user
	 */
	public Semi(double error, int inputnumberT) {
		maxError = error;
		proposedCutoff = maxError;
		inputnumber = inputnumberT;
	}
	
	private class Map {
		Object object1;
		Object object2;
		double valueForOrder;
	}

	public class MyComparator implements Comparator {
		public boolean equals(Object o1) {
			return false;
		}
		public int compare(Object o1, Object o2) {
			Map map1 = (Map) o1;
			Map map2 = (Map) o2;
			if (map1.object1.equals(map2.object1)&&(map1.object2.equals(map2.object2))&&(map1.valueForOrder==map2.valueForOrder)) return 0;
			if (map1.valueForOrder<=map2.valueForOrder) return -1;
			if (map1.valueForOrder>map2.valueForOrder) return 1;
			return 1;
		}		
	}
	
	private MyComparator myComparator = new MyComparator();	
	
	/**
	 * Entity pairs around the critical maxError are presented to the user and have to be validated.
	 * @param structure
	 * @param resultList
	 * @param explicit
	 */
	public void semi(Structure structure, ResultList resultList, ExplicitRelation explicit) {
		MyOntology ontology = (MyOntology) structure;		
		TreeSet tree = new TreeSet(myComparator);
		Iterator iter = resultList.objectList().iterator();
		while (iter.hasNext()) {
			Object object1 = iter.next();
			Object object2 = resultList.getObject(object1,0);
			OWLEntity entity1 = (OWLEntity) object1;
			OWLEntity entity2 = (OWLEntity) object2;
			String string1 = "";
			String string2 = "";
			try{
			string1 = entity1.getURI();
			string2 = entity2.getURI();
			} catch (Exception e) {
				UserInterface.print(e.getMessage());}
			if ((string1.hashCode()<=string2.hashCode())&&(string1.indexOf("#gen")==-1)&&(string2.indexOf("#gen")==-1)&&
					(explicit.checkFor(object1,object2)==false)&&(explicit.checkFor(object2,object1)==false)) {
				double value = resultList.getValue(object1,0);
				if ((value>0.001)&&(value<0.999)) {
					double valueForOrder = value-maxError;
					if (valueForOrder<0) {
						valueForOrder = valueForOrder*-1.0;
					}
					valueForOrder = valueForOrder+0.2-linking(ontology,entity1)*0.01-linking(ontology,entity2)*0.01;				
					if (valueForOrder<0) {
						valueForOrder = 0;
					}
					Map map = new Map();
					map.object1 = object1;
					map.object2 = object2;
					map.valueForOrder = valueForOrder;
					tree.add(map);
				}
			}
		}
		iter = tree.iterator();
		int count = 0;
		System.out.println();
		int numberOfQuestionsRound = 0;
		int numberOfPositiveAnswersRound = 0;
		while (iter.hasNext() && (count<inputnumber)) {
			Map map = (Map) iter.next();
			try {			
			OWLEntity entity1 = (OWLEntity) map.object1;
			String label1 = entity1.getURI();
			OWLEntity entity2 = (OWLEntity) map.object2;
			String label2 = entity2.getURI();
			String input = UserInterface.read(label1+" "+linking(ontology,entity1)+" "+label2+" "+linking(ontology,entity2)+" "+map.valueForOrder+" (y/n): ");
			numberOfQuestions++;		
			numberOfQuestionsRound++;
			if (input.equals("y")) {
				explicit.addExplicit(map.object1,map.object2,1.0);
				numberOfPositiveAnswers++;
				numberOfPositiveAnswersRound++;
				}
			 else if (input.equals("n")) {
				explicit.addExplicit(map.object1,map.object2,0.0);
			 }
			} catch (Exception e) {
				UserInterface.print(e.getMessage());
			}
			count++;
		}
		if (numberOfQuestionsRound!=0) {
		proposedCutoff = proposedCutoff-0.05*(2.0*numberOfPositiveAnswersRound/numberOfQuestionsRound-1.0);
		}
		if (proposedCutoff>0.98) {proposedCutoff = 0.98;}
		if (proposedCutoff<0.02) {proposedCutoff = 0.02;}
		maxError = proposedCutoff;
	}

	/**
	 * Entity pairs are chosen randomly for validation. This is the standard baseline to evaluate active learning
	 * approaches.
	 * @param structure
	 * @param resultList
	 * @param explicit
	 */
	public void semi2(Structure structure, ResultList resultList, ExplicitRelation explicit) {
		MyOntology ontology = (MyOntology) structure;				
		Vector vect = new Vector();
		Iterator iter = resultList.objectList().iterator();
		while (iter.hasNext()) {
			Object object1 = iter.next();
			Map map = new Map();
			map.object1 = object1;
			map.object2 = resultList.getObject(object1,0);
			map.valueForOrder = resultList.getValue(object1,0);
			vect.add(map);
		}
		int count = 0;
		System.out.println();
		while (count<inputnumber) {
			int index = (int) (Math.random()*vect.size());
			Map map = (Map) vect.get(index);
			try {			
			OWLEntity entity1 = (OWLEntity) map.object1;
			String label1 = entity1.getURI();
			OWLEntity entity2 = (OWLEntity) map.object2;
			String label2 = entity2.getURI();
			System.out.print(label1+" "+linking(ontology,entity1)+" "+label2+" "+linking(ontology,entity2)+" "+map.valueForOrder+" ");
			numberOfQuestions++;			
			InputStreamReader isr = new InputStreamReader(System.in);
			BufferedReader br = new BufferedReader(isr);
			String input = br.readLine();
			if (input.equals("y")) {
				explicit.addExplicit(map.object1,map.object2,1.0);
				numberOfPositiveAnswers++;				
				}
			 else if (input.equals("n")) {
				explicit.addExplicit(map.object1,map.object2,0.0);
			 }
			} catch (Exception e) {
				UserInterface.print(e.getMessage());
			}
			count++;
		}
	}
	
	/**
	 * Entity pairs around the critical maxError automatically provided with the correct answer.
	 * @param structure
	 * @param resultList
	 * @param explicit
	 */
	public void semi3(Structure structure, ResultList resultList, ExplicitRelation explicit, ExplicitRelation manualMappings) {
		MyOntology ontology = (MyOntology) structure;		
		TreeSet tree = new TreeSet(myComparator);
		Iterator iter = resultList.objectList().iterator();
		while (iter.hasNext()) {
			Object object1 = iter.next();
			Object object2 = resultList.getObject(object1,0);
			OWLEntity entity1 = (OWLEntity) object1;
			OWLEntity entity2 = (OWLEntity) object2;
			String string1 = "";
			String string2 = "";
			try{
			string1 = entity1.getURI();
			string2 = entity2.getURI();
			} catch (Exception e) {
				UserInterface.print(e.getMessage());}
			if ((string1.hashCode()<=string2.hashCode())&&(string1.indexOf("#gen")==-1)&&(string2.indexOf("#gen")==-1)&&
					(explicit.checkFor(object1,object2)==false)&&(explicit.checkFor(object2,object1)==false)) {
				double value = resultList.getValue(object1,0);
				if ((value>0.001)&&(value<0.999)) {
					double valueForOrder = value-maxError;
					if (valueForOrder<0) {
						valueForOrder = valueForOrder*-1.0;
					}
					valueForOrder = valueForOrder+0.2-linking(ontology,entity1)*0.01-linking(ontology,entity2)*0.01;				
					if (valueForOrder<0) {
						valueForOrder = 0;
					}
					Map map = new Map();
					map.object1 = object1;
					map.object2 = object2;
					map.valueForOrder = valueForOrder;
					tree.add(map);
				}
			}
		}
		iter = tree.iterator();
		int count = 0;
		int numberOfQuestionsRound = 0;
		int numberOfPositiveAnswersRound = 0;		
		while (iter.hasNext() && (count<inputnumber)) {			//automatically provide the answers
			Map map = (Map) iter.next();
			OWLEntity entity1 = (OWLEntity) map.object1;
			OWLEntity entity2 = (OWLEntity) map.object2;
			numberOfQuestions++;
			numberOfQuestionsRound++;
			if ((manualMappings.checkFor(entity1,entity2))||(manualMappings.checkFor(entity2,entity1))) {
				explicit.addExplicit(map.object1,map.object2,1.0);
				numberOfPositiveAnswers++;
				numberOfPositiveAnswersRound++;				
			} else {
				explicit.addExplicit(map.object1,map.object2,0.0);
			}
			count++;
		}
		if (numberOfQuestionsRound!=0) {
		proposedCutoff = proposedCutoff-0.05*(2.0*numberOfPositiveAnswersRound/numberOfQuestionsRound-1.0);
		}
		if (proposedCutoff>0.98) {proposedCutoff = 0.98;}
		if (proposedCutoff<0.02) {proposedCutoff = 0.02;}		
		maxError = proposedCutoff;		
	}
	
	/**
	 * Returns the most doubtable elements. 
	 * @param structure
	 * @param resultList
	 * @param explicit
	 * @return
	 */
	public Vector questions(Structure structure, ResultList resultList, ExplicitRelation explicit) {
		MyOntology ontology = (MyOntology) structure;		
		TreeSet tree = new TreeSet(myComparator);
		Iterator iter = resultList.objectList().iterator();
		while (iter.hasNext()) {
			Object object1 = iter.next();
			Object object2 = resultList.getObject(object1,0);
			OWLEntity entity1 = (OWLEntity) object1;
			OWLEntity entity2 = (OWLEntity) object2;
			String string1 = "";
			String string2 = "";
			try{
			string1 = entity1.getURI();
			string2 = entity2.getURI();
			} catch (Exception e) {
				UserInterface.print(e.getMessage());}
			if ((string1.hashCode()<=string2.hashCode())&&(string1.indexOf("#gen")==-1)&&(string2.indexOf("#gen")==-1)&&
					(explicit.checkFor(object1,object2)==false)&&(explicit.checkFor(object2,object1)==false)) {
				double value = resultList.getValue(object1,0);
				if ((value>0.001)&&(value<0.999)) {
					double valueForOrder = value-maxError;
					if (valueForOrder<0) {
						valueForOrder = valueForOrder*-1.0;
					}
					valueForOrder = valueForOrder+0.2-linking(ontology,entity1)*0.01-linking(ontology,entity2)*0.01;				
					if (valueForOrder<0) {
						valueForOrder = 0;
					}
					Map map = new Map();
					map.object1 = object1;
					map.object2 = object2;
					map.valueForOrder = valueForOrder;
					tree.add(map);
				}
			}
		}
		iter = tree.iterator();
		int count = 0;
		Vector vector = new Vector();
		while (iter.hasNext() && (count<inputnumber*3)) {
			Map map = (Map) iter.next();
			String[] dataset = new String[3];
			try{
			OWLEntity entity1 = (OWLEntity) map.object1;
			OWLEntity entity2 = (OWLEntity) map.object2;
			dataset[0] = entity1.getURI();
			dataset[1] = entity2.getURI();
			} catch (Exception e) {
				UserInterface.errorPrint(e.getMessage());
			}
			dataset[2] = Double.toString(map.valueForOrder);
			vector.add(dataset);	
			count++;
		}
		return vector;
	}
	
	/**
	 * Determines the degree of interlinking.
	 * @param ontology
	 * @param entity
	 * @return
	 */
	private int linking(MyOntology ontology, OWLEntity entity) {
		int counter = 1;
		if (entity instanceof OWLClass) {
			OWLClass concept = (OWLClass) entity;
			try{
			counter = counter + concept.getMemberIndividuals(ontology.ontology).size();
			counter = counter + concept.getDataPropertiesFrom(ontology.ontology).size();
			counter = counter + concept.getObjectPropertiesFrom(ontology.ontology).size();
			counter = counter + concept.getObjectPropertiesTo(ontology.ontology).size();
			counter = counter + concept.getSubDescriptions(ontology.ontology).size();
			} catch (Exception e) {
				UserInterface.print(e.getMessage());
			}
		}
		if (entity instanceof DataProperty) {
			counter = counter +2;
		}
		if (entity instanceof ObjectProperty) {
			counter = counter +2;
		}		
		if (entity instanceof Individual) {
			Individual instance = (Individual) entity;
			try{
			counter++;
			counter = counter + instance.getDataPropertyMembersFrom(ontology.ontology).size();
			counter = counter + instance.getObjectPropertyMembersFrom(ontology.ontology).size();
			counter = counter + instance.getObjectPropertyMembersTo(ontology.ontology).size();
			} catch (Exception e) {
				UserInterface.print(e.getMessage());
			}
		}
		if (counter>10) {
			return 10;
		}
		return counter;
	}
	
}
