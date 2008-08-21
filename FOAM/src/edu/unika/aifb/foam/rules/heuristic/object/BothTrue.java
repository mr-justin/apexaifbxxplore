/*
 * Created on 03.08.2004
 *
 */
package edu.unika.aifb.foam.rules.heuristic.object;

import edu.unika.aifb.foam.result.ResultTable;
import edu.unika.aifb.foam.rules.heuristic.Heuristic;

/**
 * @author Marc Ehrig
 *
 */
public class BothTrue implements Heuristic {

	private static final long serialVersionUID = 1L;

	public double get(Object object1, Object object2) {
		if ((object1 instanceof Boolean) && (object2 instanceof Boolean)) {
			Boolean boolean1 = (Boolean) object1;
			Boolean boolean2 = (Boolean) object2;			
			if ((boolean1.booleanValue() && boolean2.booleanValue())) {
				return 1;
			} 
		} 
		return 0;
	}

	public void setPreviousResult(ResultTable resultTable) {
	}
}
