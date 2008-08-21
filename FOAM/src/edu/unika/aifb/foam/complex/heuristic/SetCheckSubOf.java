package edu.unika.aifb.foam.complex.heuristic;

import java.util.Iterator;
import java.util.Set;

import edu.unika.aifb.foam.result.ResultTable;
import edu.unika.aifb.foam.rules.heuristic.Heuristic;
import edu.unika.aifb.foam.rules.heuristic.object.Similar;

/**
 * @author Pengyun Ren
 * 
 */
public class SetCheckSubOf implements Heuristic {

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
			Object object = (Object) object1;
			Set set = (Set) object2;
			double value = 0;
			double max = 0;
			Iterator iter = set.iterator();
			while (iter.hasNext()) {
				Object element = iter.next();
				value = goalRelation.get(element, object);
				
				if (value > max) {
					max = value;
				}
			}
			return max;
		}
		catch (Exception e) {
			return 0;
		}
	}

}
