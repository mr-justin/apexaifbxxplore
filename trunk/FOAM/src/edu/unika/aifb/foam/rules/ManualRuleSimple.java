/*
 * Created on 26.05.2004
 *
 */
package edu.unika.aifb.foam.rules;

import edu.unika.aifb.foam.input.Structure;
import edu.unika.aifb.foam.result.ResultTable;
import edu.unika.aifb.foam.result.ResultTableImpl;
import edu.unika.aifb.foam.rules.feature.dataproperty.DataPropertyDomain;
import edu.unika.aifb.foam.rules.feature.dataproperty.DataPropertyMembers;
import edu.unika.aifb.foam.rules.feature.dataproperty.DataPropertySub;
import edu.unika.aifb.foam.rules.feature.dataproperty.DataPropertySuper;
import edu.unika.aifb.foam.rules.feature.entity.EntityLabel;
import edu.unika.aifb.foam.rules.feature.entity.EntityUri;
import edu.unika.aifb.foam.rules.feature.individual.IndividualDataPropertyMembersFrom;
import edu.unika.aifb.foam.rules.feature.individual.IndividualMemberOf;
import edu.unika.aifb.foam.rules.feature.individual.IndividualObjectPropertyMembersFrom;
import edu.unika.aifb.foam.rules.feature.individual.IndividualObjectPropertyMembersTo;
import edu.unika.aifb.foam.rules.feature.objectproperty.ObjectPropertyDomain;
import edu.unika.aifb.foam.rules.feature.objectproperty.ObjectPropertyMembers;
import edu.unika.aifb.foam.rules.feature.objectproperty.ObjectPropertyRange;
import edu.unika.aifb.foam.rules.feature.objectproperty.ObjectPropertySub;
import edu.unika.aifb.foam.rules.feature.objectproperty.ObjectPropertySuper;
import edu.unika.aifb.foam.rules.feature.owlclass.OWLClassDataPropertiesFrom;
import edu.unika.aifb.foam.rules.feature.owlclass.OWLClassMemberIndividuals;
import edu.unika.aifb.foam.rules.feature.owlclass.OWLClassObjectPropertiesFrom;
import edu.unika.aifb.foam.rules.feature.owlclass.OWLClassObjectPropertiesTo;
import edu.unika.aifb.foam.rules.feature.owlclass.OWLClassSub;
import edu.unika.aifb.foam.rules.feature.owlclass.OWLClassSuper;
import edu.unika.aifb.foam.rules.heuristic.object.Equal;
import edu.unika.aifb.foam.rules.heuristic.set.SetAvgMaxGoal;
import edu.unika.aifb.foam.rules.heuristic.set.SetMaxMaxGoal;
import edu.unika.aifb.foam.rules.heuristic.simple.Syntactic;
import edu.unika.aifb.foam.rules.heuristic.tuple.TupleSetAvgMaxGoal;


/**
 * This rule set has been created by an expert ontology engineer. It
 * contains a big set of useful rules indicating an alignment or not, 
 * 25 rules exactly. The basis are RDFS ontology constructs.
 * The have to be combined through the ManualWeights
 * to gain correct results. 
 * How to directly use rules for ontology alignment has been presented 
 * at WM 05.
 * 
 * @author Marc Ehrig
 */
public class ManualRuleSimple implements Rules {

	private static final long serialVersionUID = 1L;
	private static final int NUMBEROFRULES = 23;
	private ResultTable previousResult = new ResultTableImpl();

	private IndividualRule rule[] = new IndividualRule[NUMBEROFRULES];
	
	public ManualRuleSimple() {
		rule[0] = new IndividualRule(0, new EntityLabel(), new EntityLabel(), new Syntactic(), null);
//		rule[0] = new IndividualRule(0, new EntityUriEnd(), new EntityUriEnd(), new Syntactic(), null);
		rule[1] = new IndividualRule(1, new EntityUri(), new EntityUri(), new Equal(), null);
		
		rule[2] = new IndividualRule(2, new OWLClassSuper(), new OWLClassSuper(), new SetAvgMaxGoal(), previousResult);
		rule[3] = new IndividualRule(3, new OWLClassSub(), new OWLClassSub(), new SetAvgMaxGoal(), previousResult);
		rule[4] = new IndividualRule(4, new OWLClassDataPropertiesFrom(), new OWLClassDataPropertiesFrom(), new SetAvgMaxGoal(), previousResult);
		rule[5] = new IndividualRule(5, new OWLClassObjectPropertiesFrom(), new OWLClassObjectPropertiesFrom(), new SetAvgMaxGoal(), previousResult);
		rule[6] = new IndividualRule(6, new OWLClassObjectPropertiesTo(), new OWLClassObjectPropertiesTo(), new SetAvgMaxGoal(), previousResult);
		rule[7] = new IndividualRule(7, new OWLClassMemberIndividuals(), new OWLClassMemberIndividuals(), new SetAvgMaxGoal(), previousResult);
		rule[8] = new IndividualRule(8, new OWLClassSuper(), new OWLClassSub(), new SetMaxMaxGoal(), previousResult);
		rule[9] = new IndividualRule(9, new OWLClassSub(), new OWLClassSuper(), new SetMaxMaxGoal(), previousResult);

		rule[10] = new IndividualRule(10, new DataPropertyDomain(), new DataPropertyDomain(), new SetAvgMaxGoal(), previousResult);
		rule[11] = new IndividualRule(11, new DataPropertySuper(), new DataPropertySuper(), new SetAvgMaxGoal(), previousResult);
		rule[12] = new IndividualRule(12, new DataPropertySub(), new DataPropertySub(), new SetAvgMaxGoal(), previousResult);
		rule[13] = new IndividualRule(13, new DataPropertyMembers(), new DataPropertyMembers(), new TupleSetAvgMaxGoal(), previousResult);

		rule[14] = new IndividualRule(14, new ObjectPropertyDomain(), new ObjectPropertyDomain(), new SetAvgMaxGoal(), previousResult);
		rule[15] = new IndividualRule(15, new ObjectPropertyRange(), new ObjectPropertyRange(), new SetAvgMaxGoal(), previousResult);
		rule[16] = new IndividualRule(16, new ObjectPropertySuper(), new ObjectPropertySuper(), new SetAvgMaxGoal(), previousResult);
		rule[17] = new IndividualRule(17, new ObjectPropertySub(), new ObjectPropertySub(), new SetAvgMaxGoal(), previousResult);
		rule[18] = new IndividualRule(18, new ObjectPropertyMembers(), new ObjectPropertyMembers(), new TupleSetAvgMaxGoal(), previousResult);
		
		rule[19] = new IndividualRule(19, new IndividualMemberOf(), new IndividualMemberOf(), new SetAvgMaxGoal(), previousResult);
		rule[20] = new IndividualRule(20, new IndividualDataPropertyMembersFrom(), new IndividualDataPropertyMembersFrom(), new TupleSetAvgMaxGoal(), previousResult);
		rule[21] = new IndividualRule(21, new IndividualObjectPropertyMembersFrom(), new IndividualObjectPropertyMembersFrom(), new TupleSetAvgMaxGoal(), previousResult);
		rule[22] = new IndividualRule(22, new IndividualObjectPropertyMembersTo(), new IndividualObjectPropertyMembersTo(), new TupleSetAvgMaxGoal(), previousResult);
		
	}

	public int total() {
		return NUMBEROFRULES;
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
		for (int i = 0; i<NUMBEROFRULES; i++) {
			rule[i].updatePreviousResult(previousResult);
		}
	}

}
