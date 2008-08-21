/*
 * Created on 03.08.2004
 *
 */
package edu.unika.aifb.foam.rules;

import edu.unika.aifb.foam.input.Structure;
import edu.unika.aifb.foam.result.ResultTable;
import edu.unika.aifb.foam.rules.feature.entity.EntityLabel;
import edu.unika.aifb.foam.rules.feature.entity.EntityUriEnd;
import edu.unika.aifb.foam.rules.heuristic.simple.Syntactic;


/**
 * Very simple rule. Just checks whether the labels are syntactically similar.
 * 
 * @author Marc Ehrig
 */
public class LabelRule implements Rules {

	private static final long serialVersionUID = 1L;

	private static final int NUMBEROFRULES = 2;

	private IndividualRule rule[] = new IndividualRule[NUMBEROFRULES];
	
	public LabelRule() {
		rule[0] = new IndividualRule(0, new EntityLabel(), new EntityLabel(), new Syntactic(), null);
//		rule[0] = new IndividualRule(0, new EntityLabel(), new EntityLabel(), new Equal(), null);		
//		rule[0] = new IndividualRule(0, new EntityUriEnd(), new EntityUriEnd(), new Equal(), null);
		rule[1] = new IndividualRule(6, new EntityUriEnd(), new EntityUriEnd(), new Syntactic(), null);		
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
	}


}
