package edu.unika.aifb.foam.complex.heuristic;

import java.util.Iterator;
import java.util.Set;

import edu.unika.aifb.foam.result.ResultTable;
import edu.unika.aifb.foam.rules.heuristic.Heuristic;
import edu.unika.aifb.foam.complex.heuristic.InstanceOf;

public class SetInstanceSubOf implements Heuristic {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private InstanceOf goalRelation = new InstanceOf();

	public void setPreviousResult(ResultTable resultTable) {
		goalRelation.setPreviousResult(resultTable);
	}

	public double get(Object object1, Object object2) {
		try {
			Object object = (Object) object1;
			Set set = (Set) object2;
			Iterator iter = set.iterator();
			double max = 0;
			double localMax1 = 0;
			double localMax2 = 0;
			while (iter.hasNext()) {
				Object element = iter.next();
				double value = goalRelation.get(object, element);
				if (value > localMax1) {
					localMax1 = value;
				}
				value = goalRelation.get(element, object);
				if (value > localMax2) {
					localMax2 = value;
				}
			}
			if (localMax1 > localMax2) {
				max = localMax1;
			}
			else {
				max = localMax2;
			}
			return max;
		}
		catch (Exception e) {
			return 0;
		}
	}
}
