/*
 * Created on 28.05.2004
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
public class SetMaxMaxEqual implements Heuristic{

	private static final long serialVersionUID = 1L;
	public String name = "setMaxEqual";
	public String group = "setComparators";
	public String attributeType1 = "set";
	public String attributeType2 = "set";

	public double get(Object object1, Object object2) {
		try {
		Set set1 = (Set) object1;
		Set set2 = (Set) object2;
		if ((set1.size()==0) || (set2.size()==0)) {
			return 0;
		}
		Iterator iter1 = set1.iterator();
		while (iter1.hasNext()) {
			Object element1 = iter1.next();
			if (set2.contains(element1)) {
				return 1;
			}
		}
		} catch (Exception e) {
		}
		return 0;
	}

	public void setPreviousResult(ResultTable resultTable) {	
	}

}
