package edu.unika.aifb.foam.complex;

import edu.unika.aifb.foam.input.Structure;
import edu.unika.aifb.foam.result.ResultTable;
import edu.unika.aifb.foam.result.ResultTableImpl;
import edu.unika.aifb.foam.rules.IndividualRule;
import edu.unika.aifb.foam.rules.Rules;
import edu.unika.aifb.foam.rules.feature.entity.EntityLabel;
import edu.unika.aifb.foam.rules.feature.general.Itself;
import edu.unika.aifb.foam.rules.feature.owlclass.OWLClassDataPropertiesFrom;
import edu.unika.aifb.foam.rules.feature.owlclass.OWLClassEquivalentDescriptions;
import edu.unika.aifb.foam.rules.feature.owlclass.OWLClassMemberIndividuals;
import edu.unika.aifb.foam.rules.feature.owlclass.OWLClassObjectPropertiesFrom;
import edu.unika.aifb.foam.rules.feature.owlclass.OWLClassObjectPropertiesTo;
import edu.unika.aifb.foam.rules.feature.owlclass.OWLClassSub;
import edu.unika.aifb.foam.complex.heuristic.SetCheckInstance;
import edu.unika.aifb.foam.complex.heuristic.SetCheckSubOf;
import edu.unika.aifb.foam.complex.heuristic.SetEquivalentSubOf;
import edu.unika.aifb.foam.complex.heuristic.SetInstanceSubOf;
import edu.unika.aifb.foam.complex.heuristic.SetPropertiesSubOf;
import edu.unika.aifb.foam.complex.heuristic.SetUnionSubOf;
import edu.unika.aifb.foam.complex.heuristic.StringLastPartOf;

public class ComplexRules implements Rules{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final int NUMBEROFRULES = 9;
	private ResultTable previousResultSimilar;
	private ResultTable previousResultSubOf = new ResultTableImpl();
//	private ResultTable previousResultPartOf = new ResultTableImpl();
	private ResultTable previousResultInstanceOf = new ResultTableImpl();
	

	private IndividualRule rule[] = new IndividualRule[NUMBEROFRULES];
	
	
	public ComplexRules () {
		rule[0] = new IndividualRule(0, new EntityLabel(), new EntityLabel(), new StringLastPartOf(), null);
		rule[1] = new IndividualRule(1, new Itself(), new OWLClassSub(), new SetCheckSubOf(), previousResultSimilar);
		rule[2] = new IndividualRule(2, new Itself(), new OWLClassSub(), new SetUnionSubOf(), previousResultSubOf);
		rule[3] = new IndividualRule(3, new OWLClassMemberIndividuals(), new OWLClassMemberIndividuals(), new SetCheckInstance(), previousResultSimilar);
		
		rule[4] = new IndividualRule(4, new OWLClassDataPropertiesFrom(), new OWLClassDataPropertiesFrom(), new SetPropertiesSubOf(), previousResultSubOf);
		rule[5] = new IndividualRule(5, new OWLClassObjectPropertiesFrom(), new OWLClassObjectPropertiesFrom(), new SetPropertiesSubOf(), previousResultSubOf);
		rule[6] = new IndividualRule(6, new OWLClassObjectPropertiesTo(), new OWLClassObjectPropertiesTo(), new SetPropertiesSubOf(), previousResultSubOf);
		
		rule[7] = new IndividualRule(7, new Itself(), new OWLClassEquivalentDescriptions(), new SetEquivalentSubOf(), previousResultSubOf);
		rule[8] = new IndividualRule(8, new Itself(), new OWLClassSub(), new SetInstanceSubOf(), previousResultInstanceOf);


	}

	public double process(Object object1, Object object2, int ruleNumber, Structure structure) {
		Object appearance1 = rule[ruleNumber].feature1.get(object1, structure);
		Object appearance2 = rule[ruleNumber].feature2.get(object2, structure);
		double result = rule[ruleNumber].heuristic.get(appearance1,appearance2);			
		return result;
	}

	public void addIndividualRule(IndividualRule rule) {
	}

	public IndividualRule rule(int number) {
		return rule[number];
	}

	public void setPreviousResult(ResultTable resultTable) {
	}

	public void setPreviousResultSimilar(ResultTable resultTable) {
		previousResultSimilar = resultTable;
		for (int i = 0; i<9; i++) {
			if ((i==3)||(i==1)) {
			rule[i].updatePreviousResult(previousResultSimilar);
			}
		}
	}
	
	public void setPreviousResultSubsumption(ResultTable resultTable) {
		previousResultSubOf = resultTable;
		for (int i = 0; i<9; i++) {
			if ((i==2)||(i==4)||(i==5)||(i==6)||(i==7)) {
			rule[i].updatePreviousResult(previousResultSubOf);
			}
		}
	}
	
	public int total() {
		return NUMBEROFRULES;
	}
	

}
