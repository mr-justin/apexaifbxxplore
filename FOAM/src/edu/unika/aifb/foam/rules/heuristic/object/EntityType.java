/*
 * Created on 27.01.2005
 *
 */
package edu.unika.aifb.foam.rules.heuristic.object;

import org.semanticweb.kaon2.api.owl.elements.DataProperty;
import org.semanticweb.kaon2.api.owl.elements.Individual;
import org.semanticweb.kaon2.api.owl.elements.OWLClass;
import org.semanticweb.kaon2.api.owl.elements.ObjectProperty;

import edu.unika.aifb.foam.result.ResultTable;
import edu.unika.aifb.foam.rules.heuristic.Heuristic;

/**
 * @author Marc Ehrig
 *
 */
public class EntityType implements Heuristic {

	private static final long serialVersionUID = 1L;

	public double get(Object object1, Object object2) {
		if ((object1 instanceof OWLClass)&&(object2 instanceof OWLClass)) {
			return 1.0;
		} else if ((object1 instanceof DataProperty)&&(object2 instanceof DataProperty)) {
			return 0.9;
		} else if ((object1 instanceof ObjectProperty)&&(object2 instanceof ObjectProperty)) {
			return 0.8;
		} else if ((object1 instanceof Individual)&&(object2 instanceof Individual)) {
			return 0.7;
		} else if ((object1 instanceof DataProperty)&&(object2 instanceof ObjectProperty)) {
			return 0.3;
		} else if ((object1 instanceof ObjectProperty)&&(object2 instanceof DataProperty)) {
			return 0.3;
		}
		return 0.0;
	}

	public void setPreviousResult(ResultTable resultTable) {
	}

}
