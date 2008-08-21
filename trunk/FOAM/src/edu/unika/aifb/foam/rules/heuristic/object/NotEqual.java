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
public class NotEqual implements Heuristic {

	private static final long serialVersionUID = 1L;

	public double get(Object object1, Object object2) {
		boolean equal = object1.equals(object2);
		if (equal) {
			return 0;
		} else {
			return 1;
		}
	}

	public void setPreviousResult(ResultTable resultTable) {	
	}
	
}
