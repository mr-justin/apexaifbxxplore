/*
 * Created on 28.05.2004
 *
 */
package edu.unika.aifb.foam.rules.heuristic.tuple;

import edu.unika.aifb.foam.result.ResultTable;
import edu.unika.aifb.foam.rules.heuristic.Heuristic;

/**
 * @author Marc Ehrig
 *
 */
public class TupleMinEqual implements Heuristic {
	
	private static final long serialVersionUID = 1L;
	public String name = "tupleMinEqual";
	public String group = "tupleComparators";
	public String attributeType1 = "tuple";
	public String attributeType2 = "tuple";

	public double get(Object object1, Object object2) {
		try {
		Tuple tuple1 = (Tuple) object1;
		Tuple tuple2 = (Tuple) object2;
		if (tuple1.object1.equals(tuple2.object1) &&
			tuple1.object2.equals(tuple2.object2)) {
				return 1;
		} else {
			return 0;
		}
		} catch (Exception e) {
			return 0;
		}
	}

	public void setPreviousResult(ResultTable resultTable) {
	}
}

