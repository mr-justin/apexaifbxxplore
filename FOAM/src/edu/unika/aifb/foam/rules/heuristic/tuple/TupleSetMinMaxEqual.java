/*
 * Created on 28.05.2004
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
public class TupleSetMinMaxEqual implements Heuristic {

	private static final long serialVersionUID = 1L;
	public String name = "tupleSetMinEqual";
	public String group = "tupleSetComparators";
	public String attributeType1 = "tupleSet";
	public String attributeType2 = "tupleSet";

	Heuristic tupleEquality = new edu.unika.aifb.foam.rules.heuristic.tuple.TupleMinEqual();

	public double get(Object object1, Object object2) {
		try {
		Set set1 = (Set) object1;
		Set set2 = (Set) object2;
		if ((set1.size()==0) || (set2.size()==0) || (set1.size()!=set2.size())) {
			return 0;
		}
		Iterator iter1 = set1.iterator();
		while (iter1.hasNext()) {
			Object element1 = iter1.next();
			Iterator iter2 = set2.iterator();
			boolean found = false;
			while (iter2.hasNext()) {
				Object element2 = iter2.next();
				if (tupleEquality.get(element1,element2)==1) {
					found = true;
				}
			}
			if (found==false) {
				return 0;
			}
		}
		Iterator iter2 = set2.iterator();
		while (iter2.hasNext()) {
			Object element2 = iter2.next();
			iter1 = set1.iterator();
			boolean found = false;
			while (iter1.hasNext()) {
				Object element1 = iter1.next();
				if (tupleEquality.get(element1,element2)==1) {
					found = true;
				}
			}
			if (found==false) {
				return 0;
			}
		}		
		return 1;
		} catch (Exception e) {
			return 0;
		}		
	}

	public void setPreviousResult(ResultTable resultTable) {
	}

}
