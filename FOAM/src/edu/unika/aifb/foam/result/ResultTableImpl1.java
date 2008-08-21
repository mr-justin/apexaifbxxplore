/*
 * Created on 28.05.2004
 *
 */
package edu.unika.aifb.foam.result;

import java.util.Map;

/**
 * Generally
 * the system returns a value which is not related to the actual value. These
 * could be 1.0 or random. The idea is to have different similarities as a 
 * starting point. From first tests results didn't increase through this 
 * step.
 * 
 * @author Marc Ehrig
 */
public class ResultTableImpl1 implements ResultTable{
	
	public void set(Object object1, Object object2, double value) {
	}
	
	public double get(Object object1, Object object2) {
		if ((object1==null)||(object2==null)) {
			return -1000;
		}
//		return Math.random();
		return 1.0;	
	}
	
	public void clear() {
	}	
	
	public void copy(ResultTable other) {
	}

	public Map[] map() {
		return null;
	}

	public void copy(ResultList list, int howmany, double minthreshold) {
	}	
	
}
