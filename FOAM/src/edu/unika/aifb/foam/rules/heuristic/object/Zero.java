/*
 * Created on 26.05.2004
 *
 */
package edu.unika.aifb.foam.rules.heuristic.object;

import edu.unika.aifb.foam.result.ResultTable;
import edu.unika.aifb.foam.rules.heuristic.Heuristic;

/**
 * @author Marc Ehrig
 *
 */
public class Zero implements Heuristic {

	private static final long serialVersionUID = 1L;

	public double get(Object object1, Object object2) {
		return -0.3;
	}

	public void setPreviousResult(ResultTable resultTable) {
	}

}
