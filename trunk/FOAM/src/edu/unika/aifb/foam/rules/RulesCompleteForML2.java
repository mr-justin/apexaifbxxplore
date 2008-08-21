/*
 * Created on 17.11.2005
 *
 */
package edu.unika.aifb.foam.rules;

import edu.unika.aifb.foam.input.Structure;
import edu.unika.aifb.foam.result.ResultTable;
import edu.unika.aifb.foam.result.ResultTableImpl;
import edu.unika.aifb.foam.rules.feature.dataproperty.DataPropertyDomain;
import edu.unika.aifb.foam.rules.feature.dataproperty.DataPropertyEquivalent;
import edu.unika.aifb.foam.rules.feature.dataproperty.DataPropertyFunctional;
import edu.unika.aifb.foam.rules.feature.dataproperty.DataPropertyMembers;
import edu.unika.aifb.foam.rules.feature.dataproperty.DataPropertyRange;
import edu.unika.aifb.foam.rules.feature.dataproperty.DataPropertySub;
import edu.unika.aifb.foam.rules.feature.dataproperty.DataPropertySuper;
import edu.unika.aifb.foam.rules.feature.entity.EntityComment;
import edu.unika.aifb.foam.rules.feature.entity.EntityLabel;
import edu.unika.aifb.foam.rules.feature.entity.EntityUri;
import edu.unika.aifb.foam.rules.feature.entity.EntityUriEnd;
import edu.unika.aifb.foam.rules.feature.general.Itself;
import edu.unika.aifb.foam.rules.feature.general.Null;
import edu.unika.aifb.foam.rules.feature.individual.IndividualDataPropertyMembersFrom;
import edu.unika.aifb.foam.rules.feature.individual.IndividualDifferent;
import edu.unika.aifb.foam.rules.feature.individual.IndividualMemberOf;
import edu.unika.aifb.foam.rules.feature.individual.IndividualMemberOfInfer;
import edu.unika.aifb.foam.rules.feature.individual.IndividualObjectPropertyMembersFrom;
import edu.unika.aifb.foam.rules.feature.individual.IndividualObjectPropertyMembersTo;
import edu.unika.aifb.foam.rules.feature.individual.IndividualSame;
import edu.unika.aifb.foam.rules.feature.individual.IndividualSibling;
import edu.unika.aifb.foam.rules.feature.objectproperty.ObjectPropertyDomain;
import edu.unika.aifb.foam.rules.feature.objectproperty.ObjectPropertyEquivalent;
import edu.unika.aifb.foam.rules.feature.objectproperty.ObjectPropertyFunctional;
import edu.unika.aifb.foam.rules.feature.objectproperty.ObjectPropertyInverseFunctional;
import edu.unika.aifb.foam.rules.feature.objectproperty.ObjectPropertyInverseProperties;
import edu.unika.aifb.foam.rules.feature.objectproperty.ObjectPropertyMembers;
import edu.unika.aifb.foam.rules.feature.objectproperty.ObjectPropertyRange;
import edu.unika.aifb.foam.rules.feature.objectproperty.ObjectPropertySub;
import edu.unika.aifb.foam.rules.feature.objectproperty.ObjectPropertySuper;
import edu.unika.aifb.foam.rules.feature.objectproperty.ObjectPropertySymmetric;
import edu.unika.aifb.foam.rules.feature.objectproperty.ObjectPropertyTransitive;
import edu.unika.aifb.foam.rules.feature.owlclass.OWLClassDataAll;
import edu.unika.aifb.foam.rules.feature.owlclass.OWLClassDataCardinality;
import edu.unika.aifb.foam.rules.feature.owlclass.OWLClassDataHasValue;
import edu.unika.aifb.foam.rules.feature.owlclass.OWLClassDataPropertiesFrom;
import edu.unika.aifb.foam.rules.feature.owlclass.OWLClassDataSome;
import edu.unika.aifb.foam.rules.feature.owlclass.OWLClassDisjointDescriptions;
import edu.unika.aifb.foam.rules.feature.owlclass.OWLClassEquivalentDescriptions;
import edu.unika.aifb.foam.rules.feature.owlclass.OWLClassMemberIndividuals;
import edu.unika.aifb.foam.rules.feature.owlclass.OWLClassObjectAll;
import edu.unika.aifb.foam.rules.feature.owlclass.OWLClassObjectAnd;
import edu.unika.aifb.foam.rules.feature.owlclass.OWLClassObjectCardinality;
import edu.unika.aifb.foam.rules.feature.owlclass.OWLClassObjectHasValue;
import edu.unika.aifb.foam.rules.feature.owlclass.OWLClassObjectNot;
import edu.unika.aifb.foam.rules.feature.owlclass.OWLClassObjectOneOf;
import edu.unika.aifb.foam.rules.feature.owlclass.OWLClassObjectOr;
import edu.unika.aifb.foam.rules.feature.owlclass.OWLClassObjectPropertiesFrom;
import edu.unika.aifb.foam.rules.feature.owlclass.OWLClassObjectPropertiesTo;
import edu.unika.aifb.foam.rules.feature.owlclass.OWLClassObjectSome;
import edu.unika.aifb.foam.rules.feature.owlclass.OWLClassSibling;
import edu.unika.aifb.foam.rules.feature.owlclass.OWLClassSub;
import edu.unika.aifb.foam.rules.feature.owlclass.OWLClassSubInfer;
import edu.unika.aifb.foam.rules.feature.owlclass.OWLClassSuper;
import edu.unika.aifb.foam.rules.feature.owlclass.OWLClassSuperInfer;
import edu.unika.aifb.foam.rules.heuristic.advanced.StringMetricStoilos;
import edu.unika.aifb.foam.rules.heuristic.advanced.Wordnetlookup;
import edu.unika.aifb.foam.rules.heuristic.object.EntityType;
import edu.unika.aifb.foam.rules.heuristic.object.Equal;
import edu.unika.aifb.foam.rules.heuristic.object.Zero;
import edu.unika.aifb.foam.rules.heuristic.set.SetAvgMaxAvgAvgGoal;
import edu.unika.aifb.foam.rules.heuristic.set.SetAvgMaxGoal;
import edu.unika.aifb.foam.rules.heuristic.set.SetMaxMaxGoal;
import edu.unika.aifb.foam.rules.heuristic.set.SetMultiVariatGoal;
import edu.unika.aifb.foam.rules.heuristic.simple.Syntactic;
import edu.unika.aifb.foam.rules.heuristic.simple.SyntacticStemmed;
import edu.unika.aifb.foam.rules.heuristic.tuple.TupleSetAvgMaxGoal;

/**
 * @author Marc Ehrig
 *
 */
public class RulesCompleteForML2 implements Rules {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final int NUMBEROFRULES = 111;
	
	private ResultTable previousResult = new ResultTableImpl();

	private IndividualRule rule[] = new IndividualRule[NUMBEROFRULES];
	
	public RulesCompleteForML2() {
		for (int i = 0; i<NUMBEROFRULES; i++) {
			rule[i] = new IndividualRule(i, new Null(), new Null(), new Zero(), null);
		}
		
//		rule[0] = new IndividualRule(0, new EntityLabel(), new EntityLabel(), new Syntactic(), null);								//standard label comparison
		rule[1] = new IndividualRule(1, new EntityLabel(), new EntityLabel(), new SyntacticStemmed(), null);								//standard label comparison
		rule[2] = new IndividualRule(2, new EntityLabel(), new EntityLabel(), new Wordnetlookup(), null);							//standard label comparison
		rule[3] = new IndividualRule(3, new EntityLabel(), new EntityLabel(), new StringMetricStoilos(), null);									//standard label comparison
//		rule[2] = new IndividualRule(2, new EntityLabel(), new EntityLabel(), new GoogleHits(), null);								//advanced label comparison
//		rule[3] = new IndividualRule(3, new EntityLabel(), new EntityLabel(), new GoogleSnippets(), null);
		rule[4] = new IndividualRule(4, new EntityComment(), new EntityComment(), new Syntactic(), null);				
		rule[5] = new IndividualRule(5, new EntityUri(), new EntityUri(), new Equal(), null);										//standard uri comparison
		rule[6] = new IndividualRule(6, new EntityUriEnd(), new EntityUriEnd(), new Syntactic(), null);
		
		rule[10] = new IndividualRule(10, new OWLClassSuper(), new OWLClassSuper(), new SetMultiVariatGoal(), previousResult);			//rdfs, same relation
		rule[11] = new IndividualRule(11, new OWLClassSuper(), new OWLClassSuper(), new SetAvgMaxGoal(), previousResult);				
		rule[13] = new IndividualRule(13, new OWLClassSub(), new OWLClassSub(), new SetMultiVariatGoal(), previousResult);
		rule[14] = new IndividualRule(14, new OWLClassSub(), new OWLClassSub(), new SetAvgMaxGoal(), previousResult);
		rule[16] = new IndividualRule(16, new OWLClassDataPropertiesFrom(), new OWLClassDataPropertiesFrom(), new SetMultiVariatGoal(), previousResult);
		rule[17] = new IndividualRule(17, new OWLClassDataPropertiesFrom(), new OWLClassDataPropertiesFrom(), new SetAvgMaxGoal(), previousResult);
		rule[19] = new IndividualRule(19, new OWLClassObjectPropertiesFrom(), new OWLClassObjectPropertiesFrom(), new SetMultiVariatGoal(), previousResult);
		rule[20] = new IndividualRule(20, new OWLClassObjectPropertiesFrom(), new OWLClassObjectPropertiesFrom(), new SetAvgMaxGoal(), previousResult);
		rule[22] = new IndividualRule(22, new OWLClassObjectPropertiesTo(), new OWLClassObjectPropertiesTo(), new SetMultiVariatGoal(), previousResult);
		rule[23] = new IndividualRule(23, new OWLClassObjectPropertiesTo(), new OWLClassObjectPropertiesTo(), new SetAvgMaxGoal(), previousResult);
		rule[25] = new IndividualRule(25, new OWLClassMemberIndividuals(), new OWLClassMemberIndividuals(), new SetMultiVariatGoal(), previousResult);
		rule[26] = new IndividualRule(26, new OWLClassMemberIndividuals(), new OWLClassMemberIndividuals(), new SetAvgMaxGoal(), previousResult);
		rule[28] = new IndividualRule(28, new OWLClassSuper(), new OWLClassSub(), new SetMaxMaxGoal(), previousResult);						//rdfs, others
		rule[29] = new IndividualRule(29, new OWLClassSub(), new OWLClassSuper(), new SetMaxMaxGoal(), previousResult);
		rule[30] = new IndividualRule(30, new OWLClassSibling(), new OWLClassSibling(), new SetMultiVariatGoal(), previousResult);		
		rule[31] = new IndividualRule(31, new OWLClassSibling(), new OWLClassSibling(), new SetAvgMaxGoal(), previousResult);				
		rule[33] = new IndividualRule(33, new OWLClassSuperInfer(), new OWLClassSuperInfer(), new SetMultiVariatGoal(), previousResult);	
		rule[34] = new IndividualRule(34, new OWLClassSuperInfer(), new OWLClassSuperInfer(), new SetAvgMaxGoal(), previousResult);	
		rule[35] = new IndividualRule(35, new OWLClassSubInfer(), new OWLClassSubInfer(), new SetMultiVariatGoal(), previousResult);	
		rule[36] = new IndividualRule(36, new OWLClassSubInfer(), new OWLClassSubInfer(), new SetAvgMaxGoal(), previousResult);	
		rule[37] = new IndividualRule(37, new OWLClassDataAll(), new OWLClassDataAll(), new SetAvgMaxAvgAvgGoal(), previousResult);			//owl, same relation
		rule[38] = new IndividualRule(38, new OWLClassDataCardinality(), new OWLClassDataCardinality(), new SetAvgMaxAvgAvgGoal(), previousResult);
		rule[39] = new IndividualRule(39, new OWLClassDataHasValue(), new OWLClassDataHasValue(), new SetAvgMaxAvgAvgGoal(), previousResult);
		rule[40] = new IndividualRule(40, new OWLClassDataSome(), new OWLClassDataSome(), new SetAvgMaxAvgAvgGoal(), previousResult);
		rule[41] = new IndividualRule(41, new OWLClassDisjointDescriptions(), new OWLClassDisjointDescriptions(), new SetAvgMaxAvgAvgGoal(), previousResult);
		rule[42] = new IndividualRule(42, new OWLClassEquivalentDescriptions(), new OWLClassEquivalentDescriptions(), new SetMaxMaxGoal(), previousResult);
		rule[43] = new IndividualRule(43, new OWLClassObjectAll(), new OWLClassObjectAll(), new SetAvgMaxAvgAvgGoal(), previousResult);
		rule[44] = new IndividualRule(44, new OWLClassObjectAnd(), new OWLClassObjectAnd(), new SetAvgMaxAvgAvgGoal(), previousResult);
		rule[45] = new IndividualRule(45, new OWLClassObjectCardinality(), new OWLClassObjectCardinality(), new SetAvgMaxAvgAvgGoal(), previousResult);
		rule[46] = new IndividualRule(46, new OWLClassObjectHasValue(), new OWLClassObjectHasValue(), new SetAvgMaxAvgAvgGoal(), previousResult);
		rule[47] = new IndividualRule(47, new OWLClassObjectNot(), new OWLClassObjectNot(), new SetAvgMaxAvgAvgGoal(), previousResult);
		rule[48] = new IndividualRule(48, new OWLClassObjectOneOf(), new OWLClassObjectOneOf(), new SetAvgMaxAvgAvgGoal(), previousResult);
		rule[49] = new IndividualRule(49, new OWLClassObjectOr(), new OWLClassObjectOr(), new SetAvgMaxAvgAvgGoal(), previousResult);
		rule[50] = new IndividualRule(50, new OWLClassObjectSome(), new OWLClassObjectSome(), new SetAvgMaxAvgAvgGoal(), previousResult);
		rule[51] = new IndividualRule(51, new OWLClassDataAll(), new OWLClassDataSome(), new SetAvgMaxAvgAvgGoal(), previousResult);			//owl, others
		rule[52] = new IndividualRule(52, new OWLClassDataSome(), new OWLClassDataAll(), new SetAvgMaxAvgAvgGoal(), previousResult);
		rule[53] = new IndividualRule(53, new OWLClassDisjointDescriptions(), new OWLClassEquivalentDescriptions(), new SetMaxMaxGoal(), previousResult);
		rule[54] = new IndividualRule(54, new OWLClassEquivalentDescriptions(), new OWLClassDisjointDescriptions(), new SetMaxMaxGoal(), previousResult);
		rule[55] = new IndividualRule(55, new OWLClassObjectAll(), new OWLClassObjectSome(), new SetAvgMaxAvgAvgGoal(), previousResult);
		rule[56] = new IndividualRule(56, new OWLClassObjectSome(), new OWLClassObjectAll(), new SetAvgMaxAvgAvgGoal(), previousResult);
		
		rule[60] = new IndividualRule(60, new DataPropertyDomain(), new DataPropertyDomain(), new SetMultiVariatGoal(), previousResult);	//rdfs
		rule[61] = new IndividualRule(61, new DataPropertyDomain(), new DataPropertyDomain(), new SetAvgMaxGoal(), previousResult);			
		rule[63] = new IndividualRule(63, new DataPropertySuper(), new DataPropertySuper(), new SetAvgMaxAvgAvgGoal(), previousResult);
		rule[64] = new IndividualRule(64, new DataPropertySub(), new DataPropertySub(), new SetAvgMaxAvgAvgGoal(), previousResult);
		rule[65] = new IndividualRule(65, new DataPropertyMembers(), new DataPropertyMembers(), new TupleSetAvgMaxGoal(), previousResult);
		rule[66] = new IndividualRule(66, new DataPropertyEquivalent(), new DataPropertyEquivalent(), new SetMaxMaxGoal(), previousResult);	//owl
		rule[67] = new IndividualRule(67, new DataPropertyFunctional(), new DataPropertyFunctional(), new Equal(), null);
		rule[68] = new IndividualRule(68, new DataPropertyRange(), new DataPropertyRange(), new SetMultiVariatGoal(), previousResult);		
		rule[69] = new IndividualRule(69, new DataPropertyRange(), new DataPropertyRange(), new SetAvgMaxGoal(), previousResult);		
		
		rule[75] = new IndividualRule(75, new ObjectPropertyDomain(), new ObjectPropertyDomain(), new SetMultiVariatGoal(), previousResult);	//rdfs
		rule[76] = new IndividualRule(76, new ObjectPropertyDomain(), new ObjectPropertyDomain(), new SetAvgMaxGoal(), previousResult);	
		rule[78] = new IndividualRule(78, new ObjectPropertyRange(), new ObjectPropertyRange(), new SetMultiVariatGoal(), previousResult);
		rule[79] = new IndividualRule(79, new ObjectPropertyRange(), new ObjectPropertyRange(), new SetAvgMaxGoal(), previousResult);
		rule[81] = new IndividualRule(81, new ObjectPropertySuper(), new ObjectPropertySuper(), new SetAvgMaxAvgAvgGoal(), previousResult);
		rule[82] = new IndividualRule(82, new ObjectPropertySub(), new ObjectPropertySub(), new SetAvgMaxAvgAvgGoal(), previousResult);
		rule[83] = new IndividualRule(83, new ObjectPropertyMembers(), new ObjectPropertyMembers(), new TupleSetAvgMaxGoal(), previousResult);
		rule[84] = new IndividualRule(84, new ObjectPropertyEquivalent(), new ObjectPropertyEquivalent(), new SetMaxMaxGoal(), previousResult);	//owl, same relation
		rule[85] = new IndividualRule(85, new ObjectPropertyFunctional(), new ObjectPropertyFunctional(), new Equal(), null);
		rule[86] = new IndividualRule(86, new ObjectPropertyInverseFunctional(), new ObjectPropertyInverseFunctional(), new Equal(), null);
		rule[87] = new IndividualRule(87, new ObjectPropertyInverseProperties(), new ObjectPropertyInverseProperties(), new SetAvgMaxAvgAvgGoal(), previousResult);
		rule[88] = new IndividualRule(88, new ObjectPropertySymmetric(), new ObjectPropertySymmetric(), new Equal(), null);
		rule[89] = new IndividualRule(89, new ObjectPropertyTransitive(), new ObjectPropertyTransitive(), new Equal(), null);
		rule[90] = new IndividualRule(90, new ObjectPropertyInverseProperties(), new ObjectPropertyEquivalent(), new SetMaxMaxGoal(), previousResult);	//owl, others
		rule[91] = new IndividualRule(91, new ObjectPropertyEquivalent(), new ObjectPropertyInverseProperties(), new SetMaxMaxGoal(), previousResult);
		
		rule[95] = new IndividualRule(95, new IndividualMemberOf(), new IndividualMemberOf(), new SetMultiVariatGoal(), previousResult);		//rdfs
		rule[96] = new IndividualRule(96, new IndividualMemberOf(), new IndividualMemberOf(), new SetAvgMaxGoal(), previousResult);		
		rule[98] = new IndividualRule(98, new IndividualDataPropertyMembersFrom(), new IndividualDataPropertyMembersFrom(), new TupleSetAvgMaxGoal(), previousResult);
		rule[99] = new IndividualRule(99, new IndividualObjectPropertyMembersFrom(), new IndividualObjectPropertyMembersFrom(), new TupleSetAvgMaxGoal(), previousResult);
		rule[100] = new IndividualRule(100, new IndividualObjectPropertyMembersTo(), new IndividualObjectPropertyMembersTo(), new TupleSetAvgMaxGoal(), previousResult);
		rule[101] = new IndividualRule(101, new IndividualSibling(), new IndividualSibling(), new SetMultiVariatGoal(), previousResult);
		rule[102] = new IndividualRule(102, new IndividualSibling(), new IndividualSibling(), new SetAvgMaxGoal(), previousResult);
		rule[104] = new IndividualRule(104, new IndividualMemberOfInfer(), new IndividualMemberOfInfer(), new SetMultiVariatGoal(), previousResult);	
		rule[105] = new IndividualRule(105, new IndividualMemberOfInfer(), new IndividualMemberOfInfer(), new SetAvgMaxAvgAvgGoal(), previousResult);	
		rule[106] = new IndividualRule(106, new IndividualDifferent(), new IndividualDifferent(), new SetAvgMaxAvgAvgGoal(), previousResult);		//owl, same relation
		rule[107] = new IndividualRule(107, new IndividualSame(), new IndividualSame(), new SetMaxMaxGoal(), previousResult);
		rule[108] = new IndividualRule(108, new IndividualDifferent(), new IndividualSame(), new SetMaxMaxGoal(), previousResult);					//owl, others
		rule[109] = new IndividualRule(109, new IndividualSame(), new IndividualDifferent(), new SetMaxMaxGoal(), previousResult);
		
		rule[110] = new IndividualRule(110, new Itself(), new Itself(), new EntityType(), null);
		
	}

	public int total() {
		return NUMBEROFRULES;
	}

	public double process(Object object1, Object object2, int ruleNumber, Structure structure) {
		Object appearance1 = rule[ruleNumber].feature1.get(object1, structure);
		Object appearance2 = rule[ruleNumber].feature2.get(object2, structure);
		if ((appearance1==null)&&(appearance2==null)) return -0.3;			//both values do not exist
		if (appearance1==null) return -0.1;									//the first value doesn't exist
		if (appearance2==null) return -0.2;									//the second value doesn't exist
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
