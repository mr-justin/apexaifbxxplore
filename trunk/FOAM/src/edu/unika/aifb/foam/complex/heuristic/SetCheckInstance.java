package edu.unika.aifb.foam.complex.heuristic;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import edu.unika.aifb.foam.result.ResultTable;
import edu.unika.aifb.foam.rules.heuristic.Heuristic;
import edu.unika.aifb.foam.rules.heuristic.object.Similar;

public class SetCheckInstance implements Heuristic{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Similar goalRelation = new Similar();

	public void setPreviousResult(ResultTable resultTable) {
		goalRelation.setPreviousResult(resultTable);
	}

	public double get(Object object1, Object object2) {
		try {
			Set set1 = (Set) object1;
			Set set2 = (Set) object2;
			int set1Size = set1.size();
			int set2Size = set2.size();
			
			if (set1Size > set2Size ) {
				return 0;
			}
			ArrayList maxArrayList = new ArrayList();
			Iterator iter1 = set1.iterator();
			while (iter1.hasNext()) {
				Object element1 = iter1.next();
				Iterator iter2 = set2.iterator();
				double localMax = 0;
				while (iter2.hasNext()) {
					Object element2 = iter2.next();
					double value = goalRelation.get(element1, element2);
					if (value > localMax) {
						localMax = value;
					}
				}
				maxArrayList.add(new Double(localMax));
			}
			int counter = 0;
			for (int i = 0; i < maxArrayList.size(); i++) {
				Double dbl = (Double) maxArrayList.get(i);
				double d = dbl.doubleValue();
				if (d > 0.0) {
					counter++;
				}
			}
			if ((counter/set1Size) >= 0.7 ) {
				double sum = 0.0;
				for (int i = 0; i < maxArrayList.size(); i++) {
					Double dbl = (Double) maxArrayList.get(i);
					double d = dbl.doubleValue();
					if (d > 0.0) {
						sum = sum + d;
					}
				}
				double avg = sum / counter;
				if ( avg > 0.5) {
					return avg;
				}
				else {
					return 0;
				}
			}
			else {
				return 0;
			}
		}
		catch (Exception e) {
			return 0;
		}
	}
}
