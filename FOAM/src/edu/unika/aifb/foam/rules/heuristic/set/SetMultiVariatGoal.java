/*
 * Created on 28.05.2004
 *
 */
package edu.unika.aifb.foam.rules.heuristic.set;

import java.util.Set;
import java.util.Vector;

import edu.unika.aifb.foam.result.ResultTable;
import edu.unika.aifb.foam.rules.heuristic.Heuristic;
import edu.unika.aifb.foam.rules.heuristic.object.Similar;

/**
 * @author Marc Ehrig
 *
 */
public class SetMultiVariatGoal implements Heuristic{

	private static final long serialVersionUID = 1L;
	public String name = "setMultiVariatGoal";
	public String group = "setComparators";
	public String attributeType1 = "set";
	public String attributeType2 = "set";

	private Similar goal = new Similar();	
	
	public double get(Object object1, Object object2) {
		try {		
		Set set1 = (Set) object1;
		Set set2 = (Set) object2;
		if ((set1.size()==0) || (set2.size()==0) || (set1.size()!=set2.size())) {
			return 0;
		}		
		double[] vector1 = new double[set1.size()+set2.size()];
		double[] vector2 = new double[set1.size()+set2.size()];
		for (int i = 0; i<(set1.size()+set2.size()); i++) {
			vector1[i] = 0;
			vector2[i] = 0;
		}
		Vector list = new Vector(set1);
		list.addAll(set2);

		for (int i = 0; i<set1.size(); i++) {
			for (int j = 0; j<(set1.size()+set2.size()); j++) {
//				vector1[j] = vector1[j]+goal.get(list.get(i),list.get(j))/set1.size();
//				vector2[j] = vector2[j]+goal.get(list.get(i),list.get(j))/set2.size();
				vector1[j] = vector1[j]+goal.get(list.get(i),list.get(j));
				vector2[j] = vector2[j]+goal.get(list.get(i),list.get(j));								
			}
		}
		double cosinesquare = 0;
		double vector1length = 0;
		double vector2length = 0;
		for (int i = 0; i<list.size(); i++) {
			cosinesquare = cosinesquare + (vector1[i]*vector2[i]);
			vector1length = vector1length + vector1[i]*vector1[i];
			vector2length = vector2length + vector2[i]*vector2[i];  
		}
		if ((vector1length==0) || (vector2length == 0)) {
			cosinesquare = 0;
		} else {
			cosinesquare = cosinesquare/(vector1length+vector2length);
		}
		return cosinesquare;
		} catch (Exception e) {
			return 0;
		}		
	}
	
	public void setPreviousResult(ResultTable resultTable) {
		goal.setPreviousResult(resultTable);
	}


}
