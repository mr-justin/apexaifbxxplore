/*
 * Created on 28.05.2004
 *
 */
package edu.unika.aifb.foam.rules.heuristic.simple;

import edu.unika.aifb.foam.result.ResultTable;
import edu.unika.aifb.foam.rules.heuristic.Heuristic;

/**
 * @author Marc Ehrig
 *
 */
public class LargerSmaller implements Heuristic {

	private static final long serialVersionUID = 1L;
	public String name = "numberLargerSmaller";
	public String group = "numberComparators";
	public String attributeType1 = "number";
	public String attributeType2 = "number";

	public double get(Object object1, Object object2) {
		try {
		Double number1 = (Double) object1;
		Double number2 = (Double) object2;
		return number1.compareTo(number2);
		} catch (Exception e) {
			return 0;
		}
	}

	public void setPreviousResult(ResultTable resultTable) {
		// TODO Auto-generated method stub
		
	}

}
