/*
 * Created on 26.05.2004
 *
 */
package edu.unika.aifb.foam.rules.heuristic.object;

import org.semanticweb.kaon2.api.owl.elements.DataProperty;
import org.semanticweb.kaon2.api.owl.elements.DataRange;
import org.semanticweb.kaon2.api.owl.elements.Datatype;
import org.semanticweb.kaon2.api.owl.elements.Description;
import org.semanticweb.kaon2.api.owl.elements.Individual;
import org.semanticweb.kaon2.api.owl.elements.OWLClass;
import org.semanticweb.kaon2.api.owl.elements.ObjectProperty;

import edu.unika.aifb.foam.result.ResultTable;
import edu.unika.aifb.foam.result.ResultTableImpl;
import edu.unika.aifb.foam.rules.heuristic.Heuristic;
import edu.unika.aifb.foam.rules.heuristic.description.DescriptionCompGoal;
import edu.unika.aifb.foam.rules.heuristic.simple.Syntactic;

/**
 * @author Marc Ehrig
 *
 */
public class Similar implements Heuristic {

	private static final long serialVersionUID = 1L;
	public ResultTable goal = new ResultTableImpl();
	private Syntactic syntactic = new Syntactic();
	private DescriptionCompGoal descriptionSimilar = new DescriptionCompGoal();
	
	public void setPreviousResult(ResultTable goalT) {
		goal = goalT;
		descriptionSimilar.setPreviousResult(goalT);
	}

	public double get(Object object1, Object object2) {
		double get = goal.get(object1,object2);
		if (get!=-1000) {
			return get;
		} else {
			if (object1.equals(object2)) return 1;
			if ((object1 instanceof OWLClass)||(object1 instanceof DataProperty)||(object1 instanceof ObjectProperty)||(object1 instanceof Individual)||(object1 instanceof Datatype)||(object1 instanceof DataRange)||
					(object2 instanceof OWLClass)||(object2 instanceof DataProperty)||(object2 instanceof ObjectProperty)||(object2 instanceof Individual)||(object1 instanceof Datatype)||(object1 instanceof DataRange)) {
				return 0;
			}		
			if ((object1 instanceof String) && (object2 instanceof String)) {
				return syntactic.get(object1,object2);
			} 
			if ((object1 instanceof Integer) && (object2 instanceof Integer)) {
//				return relativeDistance.get(object1,object2);
			}
			if ((object1 instanceof Description) && (object2 instanceof Description)) {
				return descriptionSimilar.get(object1,object2);
			}		
		}
		return 0;
	}
}
