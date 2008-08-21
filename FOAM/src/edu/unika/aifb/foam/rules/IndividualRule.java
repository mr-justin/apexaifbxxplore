/*
 * Created on 26.05.2004
 *
 */
package edu.unika.aifb.foam.rules;

import java.io.Serializable;

import edu.unika.aifb.foam.result.ResultTable;
import edu.unika.aifb.foam.rules.feature.Feature;
import edu.unika.aifb.foam.rules.heuristic.Heuristic;

/**
 * Definition of an individual rule.
 * 
 * @author Marc Ehrig
 */
public class IndividualRule implements Serializable {

	private static final long serialVersionUID = 1L;
	public int number;
	public Feature feature1;
	public Feature feature2;
	public Heuristic heuristic;
	
	public IndividualRule() {
	}
	
	public IndividualRule(int numberC, Feature feature1C, Feature feature2C, Heuristic heuristicC, ResultTable previousResultC) {
		number = numberC;
		feature1 = feature1C;
		feature2 = feature2C;
		heuristic = heuristicC;
		heuristic.setPreviousResult(previousResultC);
	}
	
	public void updatePreviousResult(ResultTable previousResultC) {
		heuristic.setPreviousResult(previousResultC);
	}
	
}
