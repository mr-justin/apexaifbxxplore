/*
 * Created on 22.06.2004
 *
 */
package edu.unika.aifb.foam.rules;

//import java.io.FileInputStream;
//import java.io.InputStream;
import java.util.HashSet;
import java.util.Iterator;

import org.semanticweb.kaon2.api.KAON2Manager;
import org.semanticweb.kaon2.api.owl.elements.DataProperty;
import org.semanticweb.kaon2.api.owl.elements.Individual;
import org.semanticweb.kaon2.api.owl.elements.OWLClass;
import org.semanticweb.kaon2.api.owl.elements.ObjectProperty;

//import edu.unika.aifb.foam.input.MyOntology;
import edu.unika.aifb.foam.input.Structure;
import edu.unika.aifb.foam.result.ResultTable;
import edu.unika.aifb.foam.rules.feature.special.DataPropertyDomainSpecial;
import edu.unika.aifb.foam.rules.feature.special.IndividualDataPropertyMembersFromSpecial;
import edu.unika.aifb.foam.rules.feature.special.IndividualMemberOfSpecial;
import edu.unika.aifb.foam.rules.feature.special.IndividualObjectPropertyMembersFromSpecial;
import edu.unika.aifb.foam.rules.feature.special.IndividualObjectPropertyMembersToSpecial;
import edu.unika.aifb.foam.rules.feature.special.OWLClassDataPropertiesFromSpecial;
import edu.unika.aifb.foam.rules.feature.special.OWLClassMemberIndividualsSpecial;
import edu.unika.aifb.foam.rules.feature.special.OWLClassObjectPropertiesFromSpecial;
import edu.unika.aifb.foam.rules.feature.special.OWLClassObjectPropertiesToSpecial;
import edu.unika.aifb.foam.rules.feature.special.OWLClassSubSpecial;
import edu.unika.aifb.foam.rules.feature.special.OWLClassSuperSpecial;
import edu.unika.aifb.foam.rules.feature.special.ObjectPropertyDomainSpecial;
import edu.unika.aifb.foam.rules.feature.special.ObjectPropertyRangeSpecial;
import edu.unika.aifb.foam.rules.heuristic.object.BothTrue;
import edu.unika.aifb.foam.rules.heuristic.object.Equal;
import edu.unika.aifb.foam.rules.heuristic.set.SetAvgMaxGoal;
import edu.unika.aifb.foam.util.CSVParse;


/**
 * The user can enter a file containing entities to consider in seperate
 * rules. To a different degree of extent new rules are created from 
 * this. They have to be somehow evaluated afterwards, as only a fraction
 * of them will actually be useful. This could be done manually or through 
 * machine learning.
 * 
 * @author Marc Ehrig
 */
public class AllFeaturesRule implements Rules {

	private static final long serialVersionUID = 1L;
	private final int MAXRULES = 10000;
	private int numberofrules = 0;	
	private IndividualRule rule[] = new IndividualRule[MAXRULES+20];	
	private ResultTable previousResult;
	
	public AllFeaturesRule(String fileName, Structure standardStructure, int level, ResultTable previousResultT) {
		previousResult = previousResultT;
		String array[][] = {{""}};
		try {
		/*			InputStream inputStream = new FileInputStream(fileName);	
		CSVParser csvparser = new CSVParser(inputStream, "", "", "");
		csvparser.changeDelimiter(';');		
		String array[][] = csvparser.getAllValues();*/
		CSVParse csvParse = new CSVParse(fileName);
		array = csvParse.getAllValues();
//		inputStream.close();
		} catch (Exception e) {
//					UserInterface.print(e.getMessage());
		}		
//		MyOntology ontology = (MyOntology) standardStructure;
		HashSet conceptSet = new HashSet();
		HashSet datapropertySet = new HashSet();
		HashSet objectpropertySet = new HashSet();
		HashSet instanceSet = new HashSet();
		int i = 0;
		while (i<array.length) {
			if (array[i][0].equals("C")) {
				try {
				OWLClass concept = KAON2Manager.factory().owlClass(array[i][1]);				
				conceptSet.add(concept);
				} catch (Exception e) {}			
			}
			if (array[i][0].equals("D")) {
				try {
				DataProperty dataproperty = KAON2Manager.factory().dataProperty(array[i][1]);
				datapropertySet.add(dataproperty);
				} catch (Exception e) {}													
			}
			if (array[i][0].equals("O")) {
				try {
				ObjectProperty objectproperty = KAON2Manager.factory().objectProperty(array[i][1]);
				objectpropertySet.add(objectproperty);
				} catch (Exception e) {}													
			}			
			if (array[i][0].equals("I")) {
				try {
				Individual instance = KAON2Manager.factory().individual(array[i][1]);
				instanceSet.add(instance);
				} catch (Exception e) {}						
			}
			i++;
		}
		
		if (level == 1) {		//only simple relations to special entity are checked; relations are the same on both sides
		Iterator iter1 = conceptSet.iterator();
		while (iter1.hasNext()&&(numberofrules<MAXRULES)) {
			OWLClass concept = (OWLClass) iter1.next();
			rule[numberofrules] = new IndividualRule(numberofrules, new OWLClassSubSpecial(concept), new OWLClassSubSpecial(concept), new BothTrue(), null);
			rule[numberofrules+1] = new IndividualRule(numberofrules+1, new OWLClassSuperSpecial(concept), new OWLClassSuperSpecial(concept), new BothTrue(), null);
			rule[numberofrules+2] = new IndividualRule(numberofrules+2, new IndividualMemberOfSpecial(concept), new IndividualMemberOfSpecial(concept), new BothTrue(), null);
			numberofrules= numberofrules+3;						
		}
		iter1 = datapropertySet.iterator();
		while (iter1.hasNext()&&(numberofrules<MAXRULES)) {
			DataProperty property = (DataProperty) iter1.next();
			rule[numberofrules] = new IndividualRule(numberofrules, new IndividualDataPropertyMembersFromSpecial(property), new IndividualDataPropertyMembersFromSpecial(property), new SetAvgMaxGoal(), previousResult);
			numberofrules= numberofrules+1;
		}
		iter1 = objectpropertySet.iterator();
		while (iter1.hasNext()&&(numberofrules<MAXRULES)) {
			ObjectProperty property = (ObjectProperty) iter1.next();
			rule[numberofrules] = new IndividualRule(numberofrules, new IndividualObjectPropertyMembersFromSpecial(property), new IndividualObjectPropertyMembersFromSpecial(property), new SetAvgMaxGoal(), previousResult);
			rule[numberofrules+1] = new IndividualRule(numberofrules+1, new IndividualObjectPropertyMembersToSpecial(property), new IndividualObjectPropertyMembersToSpecial(property), new SetAvgMaxGoal(), previousResult);
			numberofrules= numberofrules+2;
		}		
		iter1 = instanceSet.iterator();
		while (iter1.hasNext()&&(numberofrules<MAXRULES)) {
			Individual instance = (Individual) iter1.next();
			rule[numberofrules] = new IndividualRule(numberofrules, new OWLClassMemberIndividualsSpecial(instance), new OWLClassMemberIndividualsSpecial(instance), new Equal(), null);
			numberofrules= numberofrules+1;					
		}
		}
		
		if (level == 2) {		//more complex relations to special entity are checked; relations are the same on both sides
			Iterator iter1 = conceptSet.iterator();
			while (iter1.hasNext()&&(numberofrules<MAXRULES)) {
				OWLClass concept = (OWLClass) iter1.next();
				rule[numberofrules] = new IndividualRule(numberofrules, new OWLClassSubSpecial(concept), new OWLClassSubSpecial(concept), new BothTrue(), null);
				rule[numberofrules+1] = new IndividualRule(numberofrules+1, new OWLClassSuperSpecial(concept), new OWLClassSuperSpecial(concept), new BothTrue(), null);
				rule[numberofrules+2] = new IndividualRule(numberofrules+2, new IndividualMemberOfSpecial(concept), new IndividualMemberOfSpecial(concept), new BothTrue(), null);
				rule[numberofrules+3] = new IndividualRule(numberofrules+3, new DataPropertyDomainSpecial(concept), new DataPropertyDomainSpecial(concept), new BothTrue(), null);
				rule[numberofrules+4] = new IndividualRule(numberofrules+4, new ObjectPropertyDomainSpecial(concept), new ObjectPropertyDomainSpecial(concept), new BothTrue(), null);
				rule[numberofrules+5] = new IndividualRule(numberofrules+5, new ObjectPropertyRangeSpecial(concept), new ObjectPropertyRangeSpecial(concept), new BothTrue(), null);
				numberofrules= numberofrules+6;
			}
			iter1 = datapropertySet.iterator();
			while (iter1.hasNext()&&(numberofrules<MAXRULES)) {
				DataProperty property = (DataProperty) iter1.next();
				rule[numberofrules] = new IndividualRule(numberofrules, new IndividualDataPropertyMembersFromSpecial(property), new IndividualDataPropertyMembersFromSpecial(property), new SetAvgMaxGoal(), previousResult);
				rule[numberofrules+1] = new IndividualRule(numberofrules+1, new OWLClassDataPropertiesFromSpecial(property), new OWLClassDataPropertiesFromSpecial(property), new BothTrue(), null);
				numberofrules= numberofrules+2;
			}
			iter1 = objectpropertySet.iterator();
			while (iter1.hasNext()&&(numberofrules<MAXRULES)) {
				ObjectProperty property = (ObjectProperty) iter1.next();
				rule[numberofrules] = new IndividualRule(numberofrules, new IndividualObjectPropertyMembersFromSpecial(property), new IndividualObjectPropertyMembersFromSpecial(property), new SetAvgMaxGoal(), previousResult);
				rule[numberofrules+1] = new IndividualRule(numberofrules+1, new IndividualObjectPropertyMembersToSpecial(property), new IndividualObjectPropertyMembersToSpecial(property), new SetAvgMaxGoal(), previousResult);
				rule[numberofrules+2] = new IndividualRule(numberofrules+2, new OWLClassObjectPropertiesFromSpecial(property), new OWLClassObjectPropertiesFromSpecial(property), new BothTrue(), null);
				rule[numberofrules+3] = new IndividualRule(numberofrules+3, new OWLClassObjectPropertiesToSpecial(property), new OWLClassObjectPropertiesToSpecial(property), new BothTrue(), null);
				numberofrules= numberofrules+4;
			}		
			iter1 = instanceSet.iterator();
			while (iter1.hasNext()&&(numberofrules<MAXRULES)) {
				Individual instance = (Individual) iter1.next();
				rule[numberofrules] = new IndividualRule(numberofrules, new OWLClassMemberIndividualsSpecial(instance), new OWLClassMemberIndividualsSpecial(instance), new Equal(), null);
				numberofrules= numberofrules+1;					
			}
			}		
			
/*		if (level == 3) {				
		Iterator iter1 = conceptSet.iterator();
		while (iter1.hasNext()&&(numberofrules<MAXRULES)) {
			Concept concept = (Concept) iter1.next();
			Iterator iter2 = conceptSet.iterator();
			while (iter2.hasNext()&&(numberofrules<MAXRULES)) {
				Concept concept2 = (Concept) iter2.next();
				rule[numberofrules] = new IndividualRule(numberofrules, new HasSubclassSpecial(concept), new HasSubclassSpecial(concept2), new BothTrue());
				rule[numberofrules+1] = new IndividualRule(numberofrules+1, new HasSuperclassSpecial(concept), new HasSuperclassSpecial(concept2), new BothTrue());
				rule[numberofrules+2] = new IndividualRule(numberofrules+2, new IsTypeOfSpecial(concept), new IsTypeOfSpecial(concept2), new BothTrue());
				numberofrules= numberofrules+3;						
			}
		}
		iter1 = propertySet.iterator();
		while (iter1.hasNext()&&(numberofrules<MAXRULES)) {
			Property property = (Property) iter1.next();
			Iterator iter2 = propertySet.iterator();
			while (iter2.hasNext()&&(numberofrules<MAXRULES)) {
				Property property2 = (Property) iter2.next();
				rule[numberofrules] = new IndividualRule(numberofrules, new InstanceHasPropertyInstanceSpecial(property2), new InstanceHasPropertyInstanceSpecial(property), new SetAvgGoal());
				rule[numberofrules+1] = new IndividualRule(numberofrules+1, new InstanceObjectHasPropertyInstanceSpecial(property2), new InstanceObjectHasPropertyInstanceSpecial(property2), new SetAvgGoal());
				numberofrules= numberofrules+2;
			}
		}
		iter1 = instanceSet.iterator();
		while (iter1.hasNext()&&(numberofrules<MAXRULES)) {
			Instance instance = (Instance) iter1.next();
			Iterator iter2 = instanceSet.iterator();
			while (iter2.hasNext()&&(numberofrules<MAXRULES)) {
				Instance instance2 = (Instance) iter2.next();
				rule[numberofrules] = new IndividualRule(numberofrules, new HasInstanceSpecial(instance), new HasInstanceSpecial(instance2), new Equal());
				numberofrules= numberofrules+1;					
			}
		}
		}		
				
		if (level == 4) {		
		Iterator iter1 = conceptSet.iterator();
		while (iter1.hasNext()&&(numberofrules<MAXRULES)) {
			Concept concept = (Concept) iter1.next();
			Iterator iter2 = conceptSet.iterator();
			while (iter2.hasNext()&&(numberofrules<MAXRULES)) {
				Concept concept2 = (Concept) iter2.next();
				rule[numberofrules] = new IndividualRule(numberofrules, new HasDomainSpecial(concept), new HasDomainSpecial(concept2), new BothTrue());
				rule[numberofrules+1] = new IndividualRule(numberofrules+1, new HasRangeSpecial(concept), new HasRangeSpecial(concept2), new BothTrue());
				rule[numberofrules+2] = new IndividualRule(numberofrules+2, new HasSubclassSpecial(concept), new HasSubclassSpecial(concept2), new BothTrue());
				rule[numberofrules+3] = new IndividualRule(numberofrules+3, new HasSuperclassSpecial(concept), new HasSuperclassSpecial(concept2), new BothTrue());
				rule[numberofrules+4] = new IndividualRule(numberofrules+4, new IsTypeOfSpecial(concept), new IsTypeOfSpecial(concept2), new BothTrue());
				numberofrules= numberofrules+5;						
			}
		}
		iter1 = propertySet.iterator();
		while (iter1.hasNext()&&(numberofrules<MAXRULES)) {
			Property property = (Property) iter1.next();
			Iterator iter2 = propertySet.iterator();
			while (iter2.hasNext()&&(numberofrules<MAXRULES)) {
				Property property2 = (Property) iter2.next();
				rule[numberofrules] = new IndividualRule(numberofrules, new InstanceHasPropertyInstanceSpecial(property2), new InstanceHasPropertyInstanceSpecial(property), new SetAvgGoal());
				rule[numberofrules+1] = new IndividualRule(numberofrules+1, new InstanceObjectHasPropertyInstanceSpecial(property2), new InstanceObjectHasPropertyInstanceSpecial(property2), new SetAvgGoal());
				rule[numberofrules+2] = new IndividualRule(numberofrules+2, new IsDomainOfSpecial(property), new IsDomainOfSpecial(property2), new BothTrue());
				rule[numberofrules+3] = new IndividualRule(numberofrules+3, new IsRangeOfSpecial(property), new IsRangeOfSpecial(property2), new BothTrue());
				numberofrules= numberofrules+4;
			}
		}
		iter1 = instanceSet.iterator();
		while (iter1.hasNext()&&(numberofrules<MAXRULES)) {
			Instance instance = (Instance) iter1.next();
			Iterator iter2 = instanceSet.iterator();
			while (iter2.hasNext()&&(numberofrules<MAXRULES)) {
				Instance instance2 = (Instance) iter2.next();
				rule[numberofrules] = new IndividualRule(numberofrules, new HasInstanceSpecial(instance), new HasInstanceSpecial(instance2), new Equal());
				numberofrules= numberofrules+1;					
			}
		}
		}*/
	}

	public int total() {
		return numberofrules; 
	}

	public double process(Object object1, Object object2, int ruleNumber, Structure structure) {
		Object appearance1 = rule[ruleNumber].feature1.get(object1, structure);
		Object appearance2 = rule[ruleNumber].feature2.get(object2, structure);
		if ((appearance1==null)&&(appearance2==null)) return -0.3;
		if (appearance1==null) return -0.1;
		if (appearance2==null) return -0.2;		
		double result = rule[ruleNumber].heuristic.get(appearance1,appearance2);			
		return result;
	}	

	public IndividualRule rule(int number) {
		return rule[number];
	}

	public void addIndividualRule(IndividualRule rule) {
	}

	public void setPreviousResult(ResultTable resultTable) {
		previousResult = resultTable;
		for (int i = 0; i<numberofrules; i++) {
			rule[i].updatePreviousResult(previousResult);
		}
	}

}
