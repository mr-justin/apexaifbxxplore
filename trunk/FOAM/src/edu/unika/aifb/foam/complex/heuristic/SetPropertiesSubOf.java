package edu.unika.aifb.foam.complex.heuristic;

import java.util.Iterator;
import java.util.Set;

import edu.unika.aifb.foam.result.ResultTable;
import edu.unika.aifb.foam.rules.heuristic.Heuristic;
import edu.unika.aifb.foam.complex.heuristic.SubOf;

public class SetPropertiesSubOf implements Heuristic{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private SubOf goalRelation = new SubOf();

	public void setPreviousResult(ResultTable resultTable) {
		goalRelation.setPreviousResult(resultTable);
	}

	public double get(Object object1, Object object2) {
		try {
			Set set1 = (Set) object1;
			Set set2 = (Set) object2;
			double max = 0;
			Iterator iter1 = set1.iterator();
			while (iter1.hasNext()) {
				Object element1 = iter1.next();
				Iterator iter2 = set2.iterator();
				double localMax = 0;
				while (iter2.hasNext()) {
					Object element2 = iter2.next();
					double value = goalRelation.get(element1,element2);
					if ( value > localMax ) {						
						localMax = value;
					}
				}
				if (localMax > max) {
					max = localMax;
				}
			}
			return max;
		}
		catch (Exception e) {
			return 0;
		}
	}
}
