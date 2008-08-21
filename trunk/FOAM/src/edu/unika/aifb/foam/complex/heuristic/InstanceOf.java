package edu.unika.aifb.foam.complex.heuristic;

import edu.unika.aifb.foam.result.ResultTable;
import edu.unika.aifb.foam.result.ResultTableImpl;
import edu.unika.aifb.foam.rules.heuristic.Heuristic;

public class InstanceOf implements Heuristic{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public ResultTable goal = new ResultTableImpl();

	public void setPreviousResult(ResultTable goalT) {
		goal = goalT;
	}

	public double get(Object object1, Object object2) {
		double get = goal.get(object1, object2);
		return get;
		/*
		 * if (get==-1000) { return standardOutput.get(object1,object2); } else {
		 * return get; }
		 */
	}
}
