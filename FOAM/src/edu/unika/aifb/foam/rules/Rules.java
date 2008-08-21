/*
 * Created on 26.05.2004
 *
 */
package edu.unika.aifb.foam.rules;

import java.io.Serializable;

import edu.unika.aifb.foam.input.Structure;
import edu.unika.aifb.foam.result.ResultTable;

/**
 * The core of the mapping process are rules supporting a mapping or not.
 * Individual rules are added to the complete rule set. And they can be
 * processed.
 * 
 * @author Marc Ehrig
 */
public interface Rules extends Serializable{

	public int total();	
	public double process(Object object1, Object object2, int ruleNumber, Structure structure);
	public IndividualRule rule(int number);
	public void addIndividualRule(IndividualRule rule);
	public void setPreviousResult(ResultTable resultTable);

}
