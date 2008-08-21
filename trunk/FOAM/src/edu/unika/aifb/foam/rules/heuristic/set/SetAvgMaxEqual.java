/*
 * Created on 05.06.2004
 *
 */
package edu.unika.aifb.foam.rules.heuristic.set;

import java.util.Iterator;
import java.util.Set;

import edu.unika.aifb.foam.result.ResultTable;
import edu.unika.aifb.foam.rules.heuristic.Heuristic;

/**
 * @author Marc Ehrig
 *
 */
public class SetAvgMaxEqual implements Heuristic {

	private static final long serialVersionUID = 1L;
	public String name = "setAvgEqual";
	public String group = "setComparators";
	public String attributeType1 = "set";
	public String attributeType2 = "set";
	
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
				if (element1.equals(element2)) {
					found = 1;
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
				if (element1.equals(element2)) {
					found = 1;
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
	}

}
