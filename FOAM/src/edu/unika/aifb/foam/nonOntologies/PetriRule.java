/*
 * Created on 16.07.2005
 *
 */
package edu.unika.aifb.foam.nonOntologies;

import org.semanticweb.kaon2.api.KAON2Manager;

import edu.unika.aifb.foam.input.Structure;
import edu.unika.aifb.foam.result.ResultTable;
import edu.unika.aifb.foam.result.ResultTableImpl;
import edu.unika.aifb.foam.rules.IndividualRule;
import edu.unika.aifb.foam.rules.Rules;
import edu.unika.aifb.foam.rules.feature.entity.EntityUriEnd;
import edu.unika.aifb.foam.rules.feature.individual.IndividualDataPropertyMembersFrom;
import edu.unika.aifb.foam.rules.feature.individual.IndividualMemberOf;
import edu.unika.aifb.foam.rules.feature.individual.IndividualObjectPropertyMembersFrom;
import edu.unika.aifb.foam.rules.feature.individual.IndividualObjectPropertyMembersTo;
import edu.unika.aifb.foam.rules.feature.special.IndividualMemberOfSpecial;
import edu.unika.aifb.foam.rules.feature.special.IndividualObjectPropertyMembersFromSpecial;
import edu.unika.aifb.foam.rules.feature.special.IndividualObjectPropertyMembersToFromSiblingSpecial;
import edu.unika.aifb.foam.rules.feature.special.IndividualObjectPropertyMembersToSpecial;
import edu.unika.aifb.foam.rules.heuristic.advanced.Wordnetlookup;
import edu.unika.aifb.foam.rules.heuristic.object.BothTrue;
import edu.unika.aifb.foam.rules.heuristic.object.Equal;
import edu.unika.aifb.foam.rules.heuristic.set.SetAvgMaxAvgAvgGoal;
import edu.unika.aifb.foam.rules.heuristic.simple.Syntactic;
import edu.unika.aifb.foam.rules.heuristic.simple.SyntacticNumber;
import edu.unika.aifb.foam.rules.heuristic.tuple.TupleSetAvgMaxGoal;

/**
 * Very simple rule. Just checks whether the labels are syntactically similar.
 * 
 * @author Marc Ehrig
 */
public class PetriRule implements Rules {

	private static final int NUMBEROFRULES = 18;
	private static final String PETRI = "http://www.aifb.uni-karlsruhe.de/Forschungsgruppen/BIK/wi2007/PNOntology.owl";
	
	private ResultTable previousResult = new ResultTableImpl();

	private IndividualRule rule[] = new IndividualRule[NUMBEROFRULES];
	
	public PetriRule() {
		rule[0] = new IndividualRule(0, new EntityUriEnd(), new EntityUriEnd(), new SyntacticNumber(),  null);		//name of entity

		rule[1] = new IndividualRule(1, new EntityUriEnd(), new EntityUriEnd(), new PetriWordnetlookup(), null);
		
		rule[2] = new IndividualRule(2, new IndividualMemberOfSpecial(KAON2Manager.factory().owlClass(PETRI+"#Place")), new IndividualMemberOfSpecial(KAON2Manager.factory().owlClass(PETRI+"#Place")), new BothTrue(), null);		//both place?
		//rule[2] = new IndividualRule(2, new IndividualObjectPropertyMembersFromSpecial(KAON2Manager.factory().objectProperty(PETRI+"#hasMarking")), new IndividualObjectPropertyMembersFromSpecial(KAON2Manager.factory().objectProperty(PETRI+"#hasMarking")), new SetAvgMaxAvgAvgGoal(), previousResult);
		//rule[3] = new IndividualRule(3, new IndividualObjectPropertyMembersFromSpecial(KAON2Manager.factory().objectProperty(PETRI+"#IndivdualDataItem")), new IndividualObjectPropertyMembersFromSpecial(KAON2Manager.factory().objectProperty(PETRI+"#IndividualDataItem")), new SetAvgMaxAvgAvgGoal(), previousResult); //Marking of the place
		rule[3] = new IndividualRule(3, new IndividualObjectPropertyMembersFromSpecial(KAON2Manager.factory().objectProperty(PETRI+"#hasAttribute")), new IndividualObjectPropertyMembersFromSpecial(KAON2Manager.factory().objectProperty(PETRI+"#hasAttribute")), new SetAvgMaxAvgAvgGoal(), previousResult);	
		rule[4] = new IndividualRule(4, new IndividualObjectPropertyMembersFromSpecial(KAON2Manager.factory().objectProperty(PETRI+"#Attribute")), new IndividualObjectPropertyMembersFromSpecial(KAON2Manager.factory().objectProperty(PETRI+"#Attribute")), new SetAvgMaxAvgAvgGoal(), previousResult);      //attributes from this place
		rule[5] = new IndividualRule(5, new IndividualObjectPropertyMembersFromSpecial(KAON2Manager.factory().objectProperty(PETRI+"#hasValue")), new IndividualObjectPropertyMembersFromSpecial(KAON2Manager.factory().objectProperty(PETRI+"#hasValue")), new SetAvgMaxAvgAvgGoal(), previousResult);
		rule[6] = new IndividualRule(6, new IndividualObjectPropertyMembersFromSpecial(KAON2Manager.factory().objectProperty(PETRI+"#Value")), new IndividualObjectPropertyMembersFromSpecial(KAON2Manager.factory().objectProperty(PETRI+"#Value")), new SetAvgMaxAvgAvgGoal(), previousResult);   // Values of this place
		rule[7] = new IndividualRule(7, new IndividualObjectPropertyMembersFromSpecial(KAON2Manager.factory().objectProperty(PETRI+"#transRef")), new IndividualObjectPropertyMembersFromSpecial(KAON2Manager.factory().objectProperty(PETRI+"#transRef")), new SetAvgMaxAvgAvgGoal(), previousResult);			//transitions following this place
		
		rule[8] = new IndividualRule(8, new IndividualMemberOfSpecial(KAON2Manager.factory().owlClass(PETRI+"#Transition")), new IndividualMemberOfSpecial(KAON2Manager.factory().owlClass(PETRI+"#Transition")), new BothTrue(), null);		//both transitions?
		rule[9] = new IndividualRule(9, new IndividualObjectPropertyMembersFromSpecial(KAON2Manager.factory().objectProperty(PETRI+"#placeRef")), new IndividualObjectPropertyMembersFromSpecial(KAON2Manager.factory().objectProperty(PETRI+"#placeRef")), new SetAvgMaxAvgAvgGoal(), previousResult);		//places following this transition
		//rule[9] = new IndividualRule(9, new IndividualObjectPropertyMembersToSpecial(KAON2Manager.factory().objectProperty(PETRI+"#hasLogicalConcept")), new IndividualObjectPropertyMembersToSpecial(KAON2Manager.factory().objectProperty(PETRI+"#hasLogicalConcept")), new SetAvgMaxAvgAvgGoal(), previousResult);			
		//rule[10] = new IndividualRule(10, new IndividualObjectPropertyMembersToSpecial(KAON2Manager.factory().objectProperty(PETRI+"#LogicalConcept")), new IndividualObjectPropertyMembersToSpecial(KAON2Manager.factory().objectProperty(PETRI+"#LogicalConcept")), new SetAvgMaxAvgAvgGoal(), previousResult);			//logicalConcept of this transition	
		//rule[11] = new IndividualRule(11, new IndividualObjectPropertyMembersToSpecial(KAON2Manager.factory().objectProperty(PETRI+"#hasAttribute")), new IndividualObjectPropertyMembersToSpecial(KAON2Manager.factory().objectProperty(PETRI+"#hasAttribute")), new SetAvgMaxAvgAvgGoal(), previousResult);	
		//rule[12] = new IndividualRule(12, new IndividualObjectPropertyMembersToSpecial(KAON2Manager.factory().objectProperty(PETRI+"#Attribute")), new IndividualObjectPropertyMembersToSpecial(KAON2Manager.factory().objectProperty(PETRI+"#Attribute")), new SetAvgMaxAvgAvgGoal(), previousResult);   //Attributes involved in this transition
		
		rule[10] = new IndividualRule(10, new IndividualMemberOfSpecial(KAON2Manager.factory().owlClass(PETRI+"#Attribute")), new IndividualMemberOfSpecial(KAON2Manager.factory().owlClass(PETRI+"#Attribute")), new BothTrue(), null);	//both attribute?
		rule[11] = new IndividualRule(11, new IndividualObjectPropertyMembersFromSpecial(KAON2Manager.factory().objectProperty(PETRI+"#hasValue")), new IndividualObjectPropertyMembersFromSpecial(KAON2Manager.factory().objectProperty(PETRI+"#hasValue")), new SetAvgMaxAvgAvgGoal(), previousResult);			//values of this attribute
		rule[12] = new IndividualRule(12, new IndividualObjectPropertyMembersFromSpecial(KAON2Manager.factory().objectProperty(PETRI+"#Value")), new IndividualObjectPropertyMembersFromSpecial(KAON2Manager.factory().objectProperty(PETRI+"#Value")), new SetAvgMaxAvgAvgGoal(), previousResult);
		rule[13] = new IndividualRule(13, new IndividualObjectPropertyMembersToSpecial(KAON2Manager.factory().objectProperty(PETRI+"#hasRef")), new IndividualObjectPropertyMembersToSpecial(KAON2Manager.factory().objectProperty(PETRI+"#hasRef")), new SetAvgMaxAvgAvgGoal(), previousResult);		//references of this values
		
		//rule[17] = new IndividualRule(17, new IndividualMemberOfSpecial(KAON2Manager.factory().owlClass(PETRI+"#Value")), new IndividualMemberOfSpecial(KAON2Manager.factory().owlClass(PETRI+"#Value")), new BothTrue(), null);		//both value?
		//rule[18] = new IndividualRule(18, new IndividualObjectPropertyMembersToSpecial(KAON2Manager.factory().objectProperty(PETRI+"#hasRef")), new IndividualObjectPropertyMembersToSpecial(KAON2Manager.factory().objectProperty(PETRI+"#hasRef")), new SetAvgMaxAvgAvgGoal(), previousResult);			//values in the same row
		
		//rule[19] = new IndividualRule(19, new IndividualMemberOfSpecial(KAON2Manager.factory().owlClass(PETRI+"#FromPlace")), new IndividualMemberOfSpecial(KAON2Manager.factory().owlClass(PETRI+"#FromPlace")), new BothTrue(), null);	//arc P to T
//		rule[20] = new IndividualRule(20, new IndividualObjectPropertyMembersFromSpecial(KAON2Manager.factory().objectProperty(PETRI+"#hasNode")), new IndividualObjectPropertyMembersFromSpecial(KAON2Manager.factory().objectProperty(PETRI+"#hasNode")), new SetAvgMaxAvgAvgGoal(), previousResult);			//input place of this connection
//		rule[21] = new IndividualRule(21, new IndividualObjectPropertyMembersToFromSiblingSpecial(KAON2Manager.factory().objectProperty(PETRI+"#hasInscription")), new IndividualObjectPropertyMembersToFromSiblingSpecial(KAON2Manager.factory().objectProperty(PETRI+"#hasInscription")), new SetAvgMaxAvgAvgGoal(), previousResult);		//inscription of this connection
//		rule[22] = new IndividualRule(22, new IndividualObjectPropertyMembersFromSpecial(KAON2Manager.factory().objectProperty(PETRI+"#hasAttribute")), new IndividualObjectPropertyMembersFromSpecial(KAON2Manager.factory().objectProperty(PETRI+"#hasAttribute")), new SetAvgMaxAvgAvgGoal(), previousResult);			//attribute inscription
//
//		rule[23] = new IndividualRule(23, new IndividualMemberOfSpecial(KAON2Manager.factory().owlClass(PETRI+"#ToPlace")), new IndividualMemberOfSpecial(KAON2Manager.factory().owlClass(PETRI+"#ToPlace")), new BothTrue(), null);	//arc T to P
//		rule[24] = new IndividualRule(24, new IndividualObjectPropertyMembersFromSpecial(KAON2Manager.factory().objectProperty(PETRI+"#hasNode")), new IndividualObjectPropertyMembersFromSpecial(KAON2Manager.factory().objectProperty(PETRI+"#hasNode")), new SetAvgMaxAvgAvgGoal(), previousResult);			//output place of this connection
//		rule[25] = new IndividualRule(25, new IndividualObjectPropertyMembersToFromSiblingSpecial(KAON2Manager.factory().objectProperty(PETRI+"#hasInscription")), new IndividualObjectPropertyMembersToFromSiblingSpecial(KAON2Manager.factory().objectProperty(PETRI+"#hasInscription")), new SetAvgMaxAvgAvgGoal(), previousResult);		//inscription of this connection
//		rule[26] = new IndividualRule(26, new IndividualObjectPropertyMembersFromSpecial(KAON2Manager.factory().objectProperty(PETRI+"#hasAttribute")), new IndividualObjectPropertyMembersFromSpecial(KAON2Manager.factory().objectProperty(PETRI+"#hasAttribute")), new SetAvgMaxAvgAvgGoal(), previousResult);			//attribute inscription

		rule[14] = new IndividualRule(14, new IndividualMemberOf(), new IndividualMemberOf(), new Equal(), previousResult);
		rule[15] = new IndividualRule(15, new IndividualDataPropertyMembersFrom(), new IndividualDataPropertyMembersFrom(), new TupleSetAvgMaxGoal(), previousResult);
		rule[16] = new IndividualRule(16, new IndividualObjectPropertyMembersFrom(), new IndividualObjectPropertyMembersFrom(), new TupleSetAvgMaxGoal(), previousResult);
		rule[17] = new IndividualRule(17, new IndividualObjectPropertyMembersTo(), new IndividualObjectPropertyMembersTo(), new TupleSetAvgMaxGoal(), previousResult);

	}

	public int total() {
		return NUMBEROFRULES;
	}

	public double process(Object object1, Object object2, int ruleNumber, Structure structure) {
		Object appearance1 = rule[ruleNumber].feature1.get(object1, structure);
		Object appearance2 = rule[ruleNumber].feature2.get(object2, structure);
		if ((appearance1==null)||(appearance2==null)) return 0;		
		double result = rule[ruleNumber].heuristic.get(appearance1,appearance2);	
		if (result>0) {
//			System.out.println(object1.toString()+" "+object2.toString()+" "+ruleNumber+" "+result);
		}
		return result;
	}

	public IndividualRule rule(int number) {
		return rule[number];
	}

	public void addIndividualRule(IndividualRule rule) {
	}

	public void setPreviousResult(ResultTable resultTable) {
		previousResult = resultTable;
		for (int i = 0; i<NUMBEROFRULES; i++) {
			rule[i].updatePreviousResult(previousResult);
		}
	}

}
