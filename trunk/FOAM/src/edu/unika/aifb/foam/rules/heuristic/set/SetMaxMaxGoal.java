/*
 * Created on 28.05.2004
 *
 */
package edu.unika.aifb.foam.rules.heuristic.set;

import java.util.Iterator;
import java.util.Set;

import edu.unika.aifb.foam.result.ResultTable;
import edu.unika.aifb.foam.rules.heuristic.Heuristic;
import edu.unika.aifb.foam.rules.heuristic.object.Similar;

/**
 * @author Marc Ehrig
 *
 */
public class SetMaxMaxGoal implements Heuristic {

	private static final long serialVersionUID = 1L;
	public String name = "setMaxGoal";
	public String group = "setComparators";
	public String attributeType1 = "set";
	public String attributeType2 = "set";

	private Similar goal = new Similar();

	public double get(Object object1, Object object2) {
		try {
		Set set1 = (Set) object1;
		Set set2 = (Set) object2;
		if ((set1.size()==0) || (set2.size()==0)) {
			return 0;
		}
		double max = 0;
		Iterator iter1 = set1.iterator();
		while (iter1.hasNext()) {
			Object element1 = iter1.next();
			Iterator iter2 = set2.iterator();
			while (iter2.hasNext()) {
				Object element2 = iter2.next();
				double value = goal.get(element1,element2);
				if (value>max) {
					max = value;
				}
			}
		}
		return max;
		} catch (Exception e) {
			return 0;
		}		
	}
	
	public void setPreviousResult(ResultTable resultTable) {
		goal.setPreviousResult(resultTable);
	}	
}
