/*
 * Created on 29.06.2004
 *
 */
package edu.unika.aifb.foam.rules;

import edu.unika.aifb.foam.input.Structure;
import edu.unika.aifb.foam.result.ResultTable;

/**
 * Creates an empty rule set. It has the full functionality of rules
 * though. Through adding individual rules this can become a full
 * fledged rule set.
 * 
 * @author Marc Ehrig
 */
public class EmptyRule implements Rules {

	private static final long serialVersionUID = 1L;
	private int numberOfRules = 0;
	private int counter = 0;
	private IndividualRule rule[];		
	private ResultTable previousResult;
	
	public EmptyRule(int numberOfRulesT) {
		numberOfRules = numberOfRulesT;
		rule = new IndividualRule[numberOfRules];
	}

	public int total() {
		return numberOfRules;
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

	public void addIndividualRule(IndividualRule ruleT) {
		rule[counter] = ruleT;
		counter++;
	}

	public void setPreviousResult(ResultTable resultTable) {
		previousResult = resultTable;
		for (int i = 0; i<numberOfRules; i++) {
			rule[i].updatePreviousResult(previousResult);
		}
	}

}
