/*
 * Created on 28.05.2004
 *
 */
package edu.unika.aifb.foam.rules.heuristic.tuple;

import edu.unika.aifb.foam.result.ResultTable;
import edu.unika.aifb.foam.rules.heuristic.Heuristic;
import edu.unika.aifb.foam.rules.heuristic.object.Similar;

/**
 * @author Marc Ehrig
 *
 */
public class TupleMinGoal implements Heuristic{

	private static final long serialVersionUID = 1L;
	public String name = "tupleMinGoal";
	public String group = "tupleComparators";
	public String attributeType1 = "tuple";
	public String attributeType2 = "tuple";

	private Similar goal = new Similar();
	
	public double get(Object object1, Object object2) {
		try {		
		Tuple tuple1 = (Tuple) object1;
		Tuple tuple2 = (Tuple) object2;
		double firstcomp = 0;
		double secondcomp = 0;
		firstcomp = goal.get(tuple1.object1,tuple2.object1);
		secondcomp = goal.get(tuple1.object2,tuple2.object2);
		if (firstcomp<secondcomp) {
			return firstcomp;		
		} else {
			return secondcomp;
		}
		} catch (Exception e) {
			return 0;
		}		
	}
	
	public void setPreviousResult(ResultTable resultTable) {
		goal.setPreviousResult(resultTable);
	}

}
