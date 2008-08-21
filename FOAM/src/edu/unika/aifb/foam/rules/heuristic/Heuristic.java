/*
 * Created on 26.05.2004
 *
 */
package edu.unika.aifb.foam.rules.heuristic;

import java.io.Serializable;

import edu.unika.aifb.foam.result.ResultTable;

/**
 * The retrieved feature instantiations from the two ontologies
 * have to be compared. We do not rely on exact logics, but
 * inexact heuristics for this.
 * 
 * @author Marc Ehrig
 */
public interface Heuristic extends Serializable{

	public void setPreviousResult(ResultTable resultTable);
	public double get(Object object1, Object object2);

}
