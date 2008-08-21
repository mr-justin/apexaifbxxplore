/*
 * Created on 05.06.2004
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
public class SetAvgMaxGoal implements Heuristic {

	private static final long serialVersionUID = 1L;
	public String name = "setAvgGoal";
	public String group = "setComparators";
	public String attributeType1 = "set";
	public String attributeType2 = "set";
	
	private Similar goalRelation = new Similar();
	
	public double get(Object object1, Object object2) {
		try {		
		Set set1 = (Set) object1;
		Set set2 = (Set) object2;
		double avg = 0;
		Iterator iter1 = set1.iterator();
		while (iter1.hasNext()) {
			Object element1 = iter1.next();
			Iterator iter2 = set2.iterator();
			double found = 0;
			while (iter2.hasNext()) {
				Object element2 = iter2.next();
				double value = goalRelation.get(element1,element2);
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
				double value = goalRelation.get(element1,element2);
				if (value>found) {
					found = value;
				}
			}
			avg = avg + found/set2.size();	
		}
		double result = avg/2;
		if (result>1) {
			result = 1;
		}
		return result;
		} catch (Exception e) {
			return 0;
		}				
	}

	public void setPreviousResult(ResultTable resultTable) {
		goalRelation.setPreviousResult(resultTable);
	}
	
}
