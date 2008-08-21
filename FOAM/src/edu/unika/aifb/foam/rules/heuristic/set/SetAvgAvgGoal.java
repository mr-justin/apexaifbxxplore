/*
 * Created on 19.05.2005
 *
 */
package edu.unika.aifb.foam.rules.heuristic.set;

import java.util.Iterator;
import java.util.Set;

import edu.unika.aifb.foam.result.ResultTable;
import edu.unika.aifb.foam.rules.heuristic.Heuristic;
import edu.unika.aifb.foam.rules.heuristic.object.Similar;

/**
 * @author meh
 *
 */
public class SetAvgAvgGoal implements Heuristic {

	private static final long serialVersionUID = 1L;
	private Similar goalRelation = new Similar();
	
	public void setPreviousResult(ResultTable resultTable) {
		goalRelation.setPreviousResult(resultTable);
	}

	public double get(Object object1, Object object2) {
		try {		
			Set set1 = (Set) object1;
			Set set2 = (Set) object2;
			double avg = 0;
			Iterator iter1 = set1.iterator();
			while (iter1.hasNext()) {
				Object element1 = iter1.next();
				Iterator iter2 = set2.iterator();
				double average = 0;
				while (iter2.hasNext()) {
					Object element2 = iter2.next();
					double value = goalRelation.get(element1,element2);
/*					if (value>0) {
						System.out.print("!");
					}*/
					average = average + value/set2.size();
				}
				avg = avg + average/set1.size();			
			}
			Iterator iter2 = set2.iterator();
			while (iter2.hasNext()) {
				Object element2 = iter2.next();
				iter1 = set1.iterator();
				double average = 0;
				while (iter1.hasNext()) {
					Object element1 = iter1.next();
					double value = goalRelation.get(element1,element2);
					average = average + value/set1.size();
				}
				avg = avg + average/set2.size();	
			}
			return avg/2;
			} catch (Exception e) {
				return 0;
			}			
	}

}