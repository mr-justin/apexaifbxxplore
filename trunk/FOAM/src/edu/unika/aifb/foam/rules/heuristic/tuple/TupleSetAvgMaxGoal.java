/*
 * Created on 05.06.2004
 *
 */
package edu.unika.aifb.foam.rules.heuristic.tuple;

import java.util.Iterator;
import java.util.Set;

import edu.unika.aifb.foam.result.ResultTable;
import edu.unika.aifb.foam.rules.heuristic.Heuristic;

/**
 * @author Marc Ehrig
 *
 */
public class TupleSetAvgMaxGoal implements Heuristic {

	private static final long serialVersionUID = 1L;
	public String name = "tupleSetAvgGoal";
	public String group = "tupleSetComparators";
	public String attributeType1 = "tupleSet";
	public String attributeType2 = "tupleSet";

	Heuristic tupleGoal = new edu.unika.aifb.foam.rules.heuristic.tuple.TupleMinGoal();	

	public double get(Object object1, Object object2) {
		try {
		Set set1 = (Set) object1;
		Set set2 = (Set) object2;
		if ((set1.size()==0) || (set2.size()==0)) {
			return 0;
		}
		double avg = 0;
		Iterator iter1 = set1.iterator();
		while (iter1.hasNext()) {
			Object element1 = iter1.next();
			Iterator iter2 = set2.iterator();
			double found = 0;
			while (iter2.hasNext()) {
				Object element2 = iter2.next();
				double value = tupleGoal.get(element1,element2);
				if (value>found) {
					found = value;
				}
			}
			avg = avg + found/set1.size();
		}
		Iterator iter2 = set2.iterator();
		while (iter2.hasNext()) {
			Object element2 = iter2.next();
			iter1 = set1.iterator();
			double found = 0;
			while (iter1.hasNext()) {
				Object element1 = iter1.next();
				double value = tupleGoal.get(element1,element2);
				if (value>found) {
					found = value;
				}
			}
			avg = avg + found/set2.size();
		}
		return avg/2;
		} catch (Exception e) {
			return 0;
		}		
	}		
	
	public void setPreviousResult(ResultTable resultTable) {
		tupleGoal.setPreviousResult(resultTable);
	}


}
